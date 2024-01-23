package assignment1;

import java.util.LinkedList;
import java.util.Optional;

import assignment1.Token.tokenType;

public class Parser {
    private TokenManager tokenManager;


    /**
     * constructor that accepts linked list of token and creates a tokenManager
     */
    public Parser(LinkedList<Token> stream) {
        this.tokenManager = new TokenManager(stream);
    }

    /**
     * helper method that accepts any number of separators and returns true
     * if at least one is found
     */
    public Boolean AcceptSeperators() {
        Optional<Token> next = tokenManager.Peek(0);
        if (next.isPresent()) {
            tokenType nextType = next.get().gettype();
            if (nextType.equals(tokenType.SEMICOLON) || nextType.equals(tokenType.NEWLINE)
                    || nextType.equals(tokenType.SEPARATOR)) {
                next.get().settype(tokenType.SEPARATOR);
                return true;
            }
        }
        return false;
    }

    /**
     * parse method that returns a programNode. loops if there are more tokens
     */
    public ProgramNode Parse() throws Exception {
        ProgramNode program = new ProgramNode();
        while (tokenManager.MoreTokens()) {
            if (!ParseFunction(program) && !ParseAction(program)) {
                throw new Exception();
            }
        }
        return program;
    }

    /**
     * returns false if not a function. if true, creates functionDefinitionNode populates it
     * with name and param, and adds it to programNode's function list
     */
    public Boolean ParseFunction(ProgramNode node) throws Exception {

        Optional<Token> nextToken = tokenManager.Peek(0);
        if (nextToken.get().gettype().equals(tokenType.FUNCTION)) {
            tokenManager.MatchAndRemove(tokenType.FUNCTION);
            String funcName = tokenManager.MatchAndRemove(tokenType.WORD).map(Token::getvalue).orElse(null);
            tokenManager.MatchAndRemove(tokenType.OPENPARENTHESES);
            nextToken = tokenManager.Peek(0);
            LinkedList<String> parameters = new LinkedList<>();
            if (nextToken.isPresent()) {
                while (nextToken.isEmpty() || !nextToken.get().gettype().equals(tokenType.CLOSEPARENTHESES)) {
                    if (nextToken.get().gettype().equals(tokenType.COMMA)) {
                        tokenManager.MatchAndRemove(nextToken.get().gettype());
                        while (AcceptSeperators()) {
                            tokenManager.MatchAndRemove(nextToken.get().gettype());
                        }
                        nextToken = tokenManager.Peek(0);
                    }
                    parameters.add(nextToken.get().getvalue());
                    tokenManager.MatchAndRemove(nextToken.get().gettype());
                    nextToken = tokenManager.Peek(0);
                }
                tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
            }
            LinkedList<StatementNode> statements = ParseBlock().getStatements();

            FunctionDefinitionNode funcNode = new FunctionDefinitionNode(funcName, parameters, statements);
            node.addFunctionNode(funcNode);
            return true;
        }

        return false;
    }

    /**
     * if begin is found, calls parseBlock. if not, looks for end.
     * if neither, calls parseOperation then parseBlock.
     */
    public Boolean ParseAction(ProgramNode node) throws Exception {

        Optional<Token> nextToken = tokenManager.Peek(0);
        if (nextToken.get().gettype().equals(tokenType.BEGIN)) {
            tokenManager.MatchAndRemove(tokenType.BEGIN);
            node.addBeginNode(ParseBlock());
            while (AcceptSeperators()) {
                tokenManager.MatchAndRemove(tokenType.SEPARATOR);
            }
            return true;
        } else if (nextToken.get().gettype().equals(tokenType.END)) {
            tokenManager.MatchAndRemove(tokenType.END);
            node.addEndNode(ParseBlock());
            while (AcceptSeperators()) {
                tokenManager.MatchAndRemove(tokenType.SEPARATOR);
            }
            return true;
        } else {
            Optional<Node> condition = ParseOperation();
            BlockNode newBlock = ParseBlock();
            while (AcceptSeperators()) {
                tokenManager.MatchAndRemove(tokenType.SEPARATOR);
            }
            newBlock.setCondition(condition);
            node.addOtherNode(newBlock);
            return true;
        }
    }


    /**lowest of level of expressions, call to parseBottomLvl removed*/
    /**
     * uses either left or right associativity to build an expression/term/factor
     */
    public Optional<Node> ParseOperation() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(1);
        Optional<Token> prevToken = tokenManager.Peek(0);
        if(prevToken.get().gettype() == tokenType.DOLLARSIGN)
            currentToken = tokenManager.Peek(2);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.INCEREMENT) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.INCEREMENT);
                OperationNode operationNode = new OperationNode(parseBottomLevel.get(), Optional.empty(), OperationNode.OperationType.POSTINC);
                return Optional.of(operationNode);
            } else if (currentToken.get().gettype() == tokenType.DECREMENT) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.DECREMENT);
                OperationNode operationNode = new OperationNode(parseBottomLevel.get(), Optional.empty(), OperationNode.OperationType.POSTDEC);
                return Optional.of(operationNode);
            } else if (currentToken.get().gettype() == tokenType.TIMES || currentToken.get().gettype() == tokenType.DIVIDE ||
                    currentToken.get().gettype() == tokenType.ADD || currentToken.get().gettype() == tokenType.PERCENT ||
                    currentToken.get().gettype() == tokenType.MINUS || currentToken.get().gettype() == tokenType.POWEROF ||
                    currentToken.get().gettype() == tokenType.COMMA || (currentToken.get().gettype() == tokenType.WORD &&
                    (prevToken.get().gettype() != tokenType.OPENBRACKET))) {
                Node exp = Expression();
                currentToken = tokenManager.Peek(0);
                if (currentToken.isPresent()) {

                    if (currentToken.get().gettype() == tokenType.LESSTHAN) {
                        tokenManager.MatchAndRemove(tokenType.LESSTHAN);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.LT));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.LESSOREQUAL) {
                        tokenManager.MatchAndRemove(tokenType.LESSOREQUAL);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.LE));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.NOTEQUALTO) {
                        tokenManager.MatchAndRemove(tokenType.NOTEQUALTO);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.NE));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.EQUALTO) {
                        tokenManager.MatchAndRemove(tokenType.EQUALTO);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.EQ));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.GREATERTHAN) {
                        tokenManager.MatchAndRemove(tokenType.GREATERTHAN);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.GT));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.GREATEROREQUAL) {
                        tokenManager.MatchAndRemove(tokenType.GREATEROREQUAL);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.GE));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.TILDE) {
                        tokenManager.MatchAndRemove(tokenType.TILDE);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.MATCH));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.TILDE) {
                        tokenManager.MatchAndRemove(tokenType.TILDE);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.MATCH));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.DOESNOTCONTAIN) {
                        tokenManager.MatchAndRemove(tokenType.DOESNOTCONTAIN);
                        Optional<Node> right = ParseOperation();
                        Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.NOTMATCH));
                        return combined;
                    } else if (currentToken.get().gettype() == tokenType.QUESTION) {
                        tokenManager.MatchAndRemove(tokenType.QUESTION);
                        Optional<Node> trueCase = ParseOperation();
                        tokenManager.MatchAndRemove(tokenType.COLON);
                        Optional<Node> falseCase = ParseOperation();
                        TernaryNode ternaryNode = new TernaryNode(exp, trueCase.get(), falseCase.get());
                        return Optional.of(ternaryNode);
                    }

                    return Optional.of(exp);
                }
                return Optional.of(exp);

            } else if (currentToken.get().gettype() == tokenType.LESSTHAN) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.LESSTHAN);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.LT));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.LESSOREQUAL) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.LESSOREQUAL);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.LE));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.NOTEQUALTO) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.NOTEQUALTO);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.NE));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.EQUALTO) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.EQUALTO);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.EQ));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.GREATERTHAN) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.GREATERTHAN);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.GT));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.GREATEROREQUAL) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.GREATEROREQUAL);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.GE));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.TILDE) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.TILDE);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.MATCH));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.TILDE) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.TILDE);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.MATCH));
                return combined;
            } else if (currentToken.get().gettype() == tokenType.DOESNOTCONTAIN) {
                Node exp = ParseBottomLevel().get();
                tokenManager.MatchAndRemove(tokenType.DOESNOTCONTAIN);
                Optional<Node> right = ParseOperation();
                Optional<Node> combined = Optional.of(new OperationNode(exp, right, OperationNode.OperationType.NOTMATCH));
                return combined;
            } else if (prevToken.get().gettype() == tokenType.OPENBRACKET) {
                tokenManager.MatchAndRemove(tokenType.OPENBRACKET);
                Node exp = Expression();
                return Optional.of(exp);
            } else if (currentToken.get().gettype() == tokenType.LOGICALAND) {
                tokenManager.MatchAndRemove(tokenType.LOGICALAND);
                Node exp = Expression();
                return Optional.of(exp);
            } else if (currentToken.get().gettype() == tokenType.LOGICALOR) {
                tokenManager.MatchAndRemove(tokenType.LOGICALOR);
                Node exp = Expression();
                return Optional.of(exp);
            } else if (currentToken.get().gettype() == tokenType.QUESTION) {
                Optional<Node> condition = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.QUESTION);
                Optional<Node> trueCase = ParseOperation();
                tokenManager.MatchAndRemove(tokenType.COLON);
                Optional<Node> falseCase = ParseOperation();
                TernaryNode ternaryNode = new TernaryNode(condition.get(), trueCase.get(), falseCase.get());
                return Optional.of(ternaryNode);
            } else if (currentToken.get().gettype() == tokenType.EXCLUSIVETO) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.EXCLUSIVETO);
                OperationNode op = new OperationNode(parseBottomLevel.get(), ParseOperation(), OperationNode.OperationType.EXPONENT);
                AssignmentNode node = new AssignmentNode(parseBottomLevel.get(), op);
                return Optional.of(node);
            } else if (currentToken.get().gettype() == tokenType.MODASSIGN) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.MODASSIGN);
                OperationNode op = new OperationNode(parseBottomLevel.get(), ParseOperation(), OperationNode.OperationType.MODULO);
                AssignmentNode node = new AssignmentNode(parseBottomLevel.get(), op);
                return Optional.of(node);
            } else if (currentToken.get().gettype() == tokenType.MULTASSIGN) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.MULTASSIGN);
                OperationNode op = new OperationNode(parseBottomLevel.get(), ParseOperation(), OperationNode.OperationType.MULTIPLY);
                AssignmentNode node = new AssignmentNode(parseBottomLevel.get(), op);
                return Optional.of(node);
            } else if (currentToken.get().gettype() == tokenType.DIVASSIGN) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.DIVASSIGN);
                OperationNode op = new OperationNode(parseBottomLevel.get(), ParseOperation(), OperationNode.OperationType.DIVIDE);
                AssignmentNode node = new AssignmentNode(parseBottomLevel.get(), op);
                return Optional.of(node);
            } else if (currentToken.get().gettype() == tokenType.ADDASSIGN) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.ADDASSIGN);
                OperationNode op = new OperationNode(parseBottomLevel.get(), ParseOperation(), OperationNode.OperationType.ADD);
                AssignmentNode node = new AssignmentNode(parseBottomLevel.get(), op);
                return Optional.of(node);
            } else if (currentToken.get().gettype() == tokenType.MINUSASSIGN) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.MINUSASSIGN);
                OperationNode op = new OperationNode(parseBottomLevel.get(), ParseOperation(), OperationNode.OperationType.SUBTRACT);
                AssignmentNode node = new AssignmentNode(parseBottomLevel.get(), op);
                return Optional.of(node);
            } else if (currentToken.get().gettype() == tokenType.EQUALS) {
                Optional<Node> parseBottomLevel = ParseBottomLevel();
                tokenManager.MatchAndRemove(tokenType.EQUALS);
                AssignmentNode node = new AssignmentNode(parseBottomLevel.get(), ParseOperation().get());
                return Optional.of(node);
            }
        }
        return ParseBottomLevel();
    }


    /**
     * looks for a specific pattern involving a tokenType and returns the value and type
     */
    public Optional<Node> ParseLValue() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.DOLLARSIGN) {
                tokenManager.MatchAndRemove(tokenType.DOLLARSIGN);
                Optional<Node> parseBottomLevelValue = ParseBottomLevel();
                if (parseBottomLevelValue.isPresent()) {
                    return Optional.of(new OperationNode(parseBottomLevelValue.get(), Optional.empty(), OperationNode.OperationType.DOLLAR));
                } else {
                    return Optional.empty();
                }
            }
            if (currentToken.get().gettype() == tokenType.WORD) {
                String varName = currentToken.get().getvalue();
                tokenManager.MatchAndRemove(tokenType.WORD);
                currentToken = tokenManager.Peek(0);
                if (currentToken.isPresent()) {
                    if (currentToken.get().gettype() == tokenType.OPENBRACKET) {
                        tokenManager.MatchAndRemove(tokenType.OPENBRACKET);
                        Optional<Node> expIndex = ParseOperation();
                        tokenManager.MatchAndRemove(tokenType.CLOSEBRACKET);
                        if (expIndex.isPresent()) {
                            return Optional.of(new VariableReferenceNode(varName, expIndex));
                        } else {
                            return Optional.of(new VariableReferenceNode(varName, Optional.empty()));
                        }
                    } else {
                        return Optional.of(new VariableReferenceNode(varName, Optional.empty()));
                    }
                }
                return Optional.of(new VariableReferenceNode(varName, Optional.empty()));
            }
        }
        return Optional.empty();
    }

    /**
     * looks for a constant, matches and removes it, and returns its value
     */
    public Optional<Node> ParseBottomLevel() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.STRINGLITERAL) {
                String value = currentToken.get().getvalue();
                tokenManager.MatchAndRemove(tokenType.STRINGLITERAL);
                return Optional.of(new ConstantNode(value));
            }
            if (currentToken.get().gettype() == tokenType.NUMBER) {
                double value = Double.parseDouble(currentToken.get().getvalue());
                tokenManager.MatchAndRemove(tokenType.NUMBER);
                return Optional.of(new ConstantNode(String.valueOf(value)));
            }
            if (currentToken.get().gettype() == tokenType.PATTERN) {
                String value = currentToken.get().getvalue();
                tokenManager.MatchAndRemove(tokenType.PATTERN);
                return Optional.of(new PatternNode(value));
            }
            if (currentToken.get().gettype() == tokenType.OPENPARENTHESES) {
                tokenManager.MatchAndRemove(tokenType.OPENPARENTHESES);
                Optional<Node> parseOperationResult = ParseOperation();
                tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
                return parseOperationResult;
            }
            if (currentToken.get().gettype() == tokenType.EXCLAMATION) {
                tokenManager.MatchAndRemove(tokenType.EXCLAMATION);
                Optional<Node> parseOperationResult = ParseOperation();
                OperationNode node = new OperationNode(parseOperationResult.get(), Optional.empty(), OperationNode.OperationType.NOT);
                return Optional.of(node);
            }
            if (currentToken.get().gettype() == tokenType.MINUS) {
                tokenManager.MatchAndRemove(tokenType.MINUS);
                Optional<Node> parseOperationResult = ParseOperation();
                OperationNode node = new OperationNode(parseOperationResult.get(), Optional.empty(), OperationNode.OperationType.UNARYNEG);
                return Optional.of(node);
            }
            if (currentToken.get().gettype() == tokenType.ADD) {
                tokenManager.MatchAndRemove(tokenType.ADD);
                Optional<Node> parseOperationResult = ParseOperation();
                OperationNode node = new OperationNode(parseOperationResult.get(), Optional.empty(), OperationNode.OperationType.UNARYPOS);
                return Optional.of(node);
            }
            if (currentToken.get().gettype() == tokenType.INCEREMENT) {
                tokenManager.MatchAndRemove(tokenType.INCEREMENT);
                Optional<Node> parseOperationResult = ParseOperation();
                OperationNode node = new OperationNode(parseOperationResult.get(), Optional.empty(), OperationNode.OperationType.PREINC);
                return Optional.of(node);
            }
            if (currentToken.get().gettype() == tokenType.DECREMENT) {
                tokenManager.MatchAndRemove(tokenType.DECREMENT);
                Optional<Node> parseOperationResult = ParseOperation();
                OperationNode node = new OperationNode(parseOperationResult.get(), Optional.empty(), OperationNode.OperationType.PREDEC);
                return Optional.of(node);
            }
            Optional<StatementNode> node = parseFunctionCall();

            if(node.isPresent()){
                return Optional.of(node.get());
            }

            return ParseLValue();


        }
        return Optional.empty();
    }

    /**these Node methods allow for left associative parsing
     * and mantain precedence by calling methods that parse
     * higher presedence functions first */

    /**
     * factor specifically parses power of which comes first
     * then it returns and parses multiplication
     */
    public Node Factor() {
        Node left = Primary();
        Optional<Token> op = tokenManager.MatchAndRemove(tokenType.POWEROF);
        if (op.isPresent()) {
            Node right = Factor(); // Right-associative
            return new OperationNode(left, Optional.of(right), OperationNode.OperationType.EXPONENT);
        }
        return left;
    }

    public Node Term() {
        Node left = Factor();
        while (true) {
            Optional<Token> op = tokenManager.MatchAndRemove(Token.tokenType.TIMES);
            if (op.isEmpty()) {
                op = tokenManager.MatchAndRemove(Token.tokenType.DIVIDE);
                if (op.isEmpty()) {
                    op = tokenManager.MatchAndRemove(Token.tokenType.PERCENT);
                    if (op.isEmpty()) {
                        return left;
                    }
                }
            }
            Node right = Factor();
            left = new OperationNode(left, Optional.of(right), getOperationType(op.get()));
        }
    }

    public Node Expression() {
        Node left = Term();
        while (true) {
            Optional<Token> op = tokenManager.MatchAndRemove(Token.tokenType.ADD);
            if (op.isEmpty()) {
                op = tokenManager.MatchAndRemove(Token.tokenType.MINUS);
                if (op.isEmpty()) {
                    op = tokenManager.MatchAndRemove(tokenType.WORD);
                }
                if (op.isEmpty()) {
                    op = tokenManager.MatchAndRemove(tokenType.COMMA);
                }
                if (op.isEmpty()) {
                    op = tokenManager.MatchAndRemove(tokenType.COMMA);
                }
                if (op.isEmpty()) {
                    op = tokenManager.MatchAndRemove(tokenType.CLOSEBRACKET);
                    if (op.isPresent())
                        return new OperationNode(left, Optional.empty(), getOperationType(op.get()));
                }
                if (op.isEmpty()) {
                    op = tokenManager.MatchAndRemove(tokenType.LOGICALAND);
                }
                if (op.isEmpty()) {
                    op = tokenManager.MatchAndRemove(tokenType.LOGICALOR);
                }
                if (op.isEmpty()) {
                    return left;
                }

            }
            Node right = Term();
            left = new OperationNode(left, Optional.of(right), getOperationType(op.get()));
        }
    }

    /**
     * Helper method to map TokenType to OperationType
     */
    private OperationNode.OperationType getOperationType(Token token) {
        switch (token.gettype()) {
            case ADD:
                return OperationNode.OperationType.ADD;
            case MINUS:
                return OperationNode.OperationType.SUBTRACT;
            case TIMES:
                return OperationNode.OperationType.MULTIPLY;
            case DIVIDE:
                return OperationNode.OperationType.DIVIDE;
            case PERCENT:
                return OperationNode.OperationType.MODULO;
            case POWEROF:
                return OperationNode.OperationType.EXPONENT;
            case WORD:
                return OperationNode.OperationType.CONCATENATION;
            case LOGICALAND:
                return OperationNode.OperationType.AND;
            case CLOSEBRACKET:
            case COMMA:
                return OperationNode.OperationType.IN;
            case LOGICALOR:
                return OperationNode.OperationType.OR;
            default:
                throw new RuntimeException("Invalid operation token");
        }
    }

    public Node Primary() {
        Optional<Token> num = tokenManager.MatchAndRemove(Token.tokenType.NUMBER);
        Optional<Token> var = tokenManager.MatchAndRemove(Token.tokenType.WORD);

        if (num.isPresent()) {
            return new ConstantNode(num.get().getvalue());
        } else if (var.isPresent()) {
            return new VariableReferenceNode(var.get().getvalue(), Optional.empty());
        } else if (tokenManager.MatchAndRemove(Token.tokenType.OPENPARENTHESES).isPresent()) {
            Node exp = Expression();
            if (tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES).isEmpty()) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            return exp;
        }

        throw new RuntimeException("Invalid factor");
    }


    /**handles either the case of a multi-line or single line block*/
    public BlockNode ParseBlock() throws Exception {
        while (AcceptSeperators()) {
            tokenManager.MatchAndRemove(tokenType.SEPARATOR);
        }
        BlockNode node = new BlockNode();
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.OPENCURLYBRACE) {
                tokenManager.MatchAndRemove(tokenType.OPENCURLYBRACE);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
                Optional<StatementNode> statement = ParseStatement();
                while (statement.isPresent()) {
                    node.addStatementNode(statement.get());
                    while (AcceptSeperators()) {
                        tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                    }
                    statement = ParseStatement();
                }
                Optional<Token> closeBrace = tokenManager.MatchAndRemove(tokenType.CLOSECURLYBRACE);
                if (closeBrace.isEmpty()) {
                    throw new Exception("Multi-line block must end with curly brace");
                }
            } else {
                Optional<StatementNode> statement = ParseStatement();
                if (statement.isPresent())
                    node.addStatementNode(statement.get());
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
            }
        }

        return node;
    }

    /**works with multi-line; called until statement not returned; statements added to parseBlock*/
    public Optional<StatementNode> ParseStatement() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {

            Optional<StatementNode> statementNode = parseContinue();
            if (statementNode.isPresent()) {
                return statementNode;
            }
            statementNode = parseBreak();
            if (statementNode.isPresent()) {
                return statementNode;
            }
            statementNode = parseIf();
            if (statementNode.isPresent()) {
                return statementNode;
            }
            statementNode = parseFor();
            if (statementNode.isPresent()) {
                return statementNode;
            }
            statementNode = parseDelete();
            if (statementNode.isPresent()) {
                return statementNode;
            }

            statementNode = parseWhile();
            if (statementNode.isPresent()) {
                return statementNode;
            }

            statementNode = parseDoWhile();
            if (statementNode.isPresent()) {
                return statementNode;
            }

            statementNode = parseReturn();
            if (statementNode.isPresent()) {
                return statementNode;
            }

            Optional<Node> op = ParseOperation();
            if (op.isPresent()) {
                try {
                    StatementNode node = (StatementNode) op.get();
                    return Optional.of(node);
                } catch (Exception e) {
                    throw new Exception("Invalid operation inside block");
                }
            }
        }
        return Optional.empty();
    }

    /**node for the 'continue' statement type; returns continueNode*/
    public Optional<StatementNode> parseContinue() {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.CONTINUE) {
                tokenManager.MatchAndRemove(tokenType.CONTINUE);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
                return Optional.of(new ContinueNode());
            }
        }
        return Optional.empty();
    }

    /**node for the 'break' statement type; returns breakNode*/
    public Optional<StatementNode> parseBreak() {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.BREAK) {
                tokenManager.MatchAndRemove(tokenType.BREAK);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
                return Optional.of(new BreakNode());
            }
        }
        return Optional.empty();
    }

    /**node for if statements; handles else if/else statements*/
    public Optional<StatementNode> parseIf() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            Token.tokenType type = currentToken.get().gettype();
            if (type == tokenType.ELSE) {
                tokenManager.MatchAndRemove(tokenType.ELSE);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
                currentToken = tokenManager.Peek(0);
                if (currentToken.isEmpty()) {
                    throw new Exception("Incomplete else statement");
                }
                type = currentToken.get().gettype();
                if (type != tokenType.IF) {
                    BlockNode block = ParseBlock();
                    LinkedList<StatementNode> statements = block.getStatements();
                    return Optional.of(new IfNode(Optional.empty(), statements, Optional.empty()));
                }

            }

            if (type == tokenType.IF) {
                tokenManager.MatchAndRemove(tokenType.IF);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
                Optional<Token> next = tokenManager.MatchAndRemove(tokenType.OPENPARENTHESES);
                if (next.isEmpty())
                    throw new Exception("Open parenthesis must follow if token");
                Optional<Node> condition = ParseOperation();
                if (condition.isEmpty())
                    throw new Exception("If needs condition");
                OperationNode cond = (OperationNode) condition.get();
                next = tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
                if (next.isEmpty())
                    throw new Exception("Close parenthesis must at the end of if condition");
                BlockNode block = ParseBlock();
                LinkedList<StatementNode> statements = block.getStatements();
                Optional<StatementNode> nextIf = parseIf();

                return Optional.of(new IfNode(Optional.of(cond), statements, nextIf));
            }
        }
        return Optional.empty();
    }


    /**decides between the two 'for' formats (either 'in' or conditional*/
    public Optional<StatementNode> parseFor() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            Optional<Token> next = tokenManager.MatchAndRemove(tokenType.FOR);
            if (next.isEmpty()) {
                return Optional.empty();
            }
            while (AcceptSeperators()) {
                tokenManager.MatchAndRemove(tokenType.SEPARATOR);
            }
            next = tokenManager.MatchAndRemove(tokenType.OPENPARENTHESES);
            if (next.isEmpty())
                throw new Exception("Open parenthesis must follow for token");


            currentToken = tokenManager.Peek(0);
            int i = 0;
            while (currentToken.isPresent() && currentToken.get().gettype() != tokenType.CLOSEPARENTHESES && currentToken.get().gettype() != tokenType.IN) {
                i++;
                currentToken = tokenManager.Peek(i);
            }
            if (currentToken.isEmpty()) {
                throw new Exception("for loop incorrect syntax");
            }
            if (currentToken.get().gettype() == tokenType.IN) {
                Optional<Node> var = ParseOperation();
                tokenManager.MatchAndRemove(tokenType.IN);
                Optional<Node> condition = ParseOperation();
                if (var.isEmpty() || condition.isEmpty()) {
                    throw new Exception("for each loop incorrect syntax");
                }
                next = tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
                if (next.isEmpty())
                    throw new Exception("Close parenthesis must at the end of for each condition");
                BlockNode block = ParseBlock();
                return Optional.of(new ForEachNode(block.getStatements(), var.get(), condition.get()));

            } else {
                tokenManager.MatchAndRemove(tokenType.WORD);
                Optional<Node> var = ParseOperation();
                tokenManager.MatchAndRemove(tokenType.SEMICOLON);
                Optional<Node> condition = ParseOperation();
                tokenManager.MatchAndRemove(tokenType.SEMICOLON);
                Optional<Node> increment = ParseOperation();
                if (var.isEmpty() || condition.isEmpty() || increment.isEmpty()) {
                    throw new Exception("for loop incorrect syntax");
                }

                next = tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
                if (next.isEmpty())
                    throw new Exception("Close parenthesis must at the end of for condition");

                BlockNode block = ParseBlock();
                return Optional.of(new ForNode(var.get(), condition.get(), increment.get(), block.getStatements()));

            }

        }
        return Optional.empty();

    }

    /**takes a param which is either a name or array reference with a comma separated list*/
    public Optional<StatementNode> parseDelete() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.DELETE) {
                tokenManager.MatchAndRemove(tokenType.DELETE);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
                Optional<Node> toBeDeleted = ParseLValue();
                if (toBeDeleted.isEmpty()) {
                    throw new Exception("Delete value not found");
                }
                return Optional.of(new DeleteNode(toBeDeleted.get()));
            }
        }
        return Optional.empty();
    }


    /**node for while loops; throws exceptions if invalid*/
    public Optional<StatementNode> parseWhile() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent() && currentToken.get().gettype() == tokenType.WHILE) {
            tokenManager.MatchAndRemove(tokenType.WHILE);

            while (AcceptSeperators()) {
                tokenManager.MatchAndRemove(tokenType.SEPARATOR);
            }

            Optional<Token> next = tokenManager.MatchAndRemove(tokenType.OPENPARENTHESES);

            if (next.isEmpty())
                throw new Exception("Open parenthesis must follow while token");


            Optional<Node> condition = ParseOperation();

            if (condition.isEmpty()) {
                throw new Exception("while loop incorrect syntax");
            }

            next = tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
            if (next.isEmpty())
                throw new Exception("Close parenthesis must at the end of while condition");

            BlockNode block = ParseBlock();
            return Optional.of(new WhileNode(condition.get(), block.getStatements()));

        }


        return Optional.empty();

    }

    /**node for doWhile loops; very similar to while node*/
    public Optional<StatementNode> parseDoWhile() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent() && currentToken.get().gettype() == tokenType.DO) {
            tokenManager.MatchAndRemove(tokenType.DO);

            BlockNode block = ParseBlock();

            while (AcceptSeperators()) {
                tokenManager.MatchAndRemove(tokenType.SEPARATOR);
            }

            Optional<Token> next = tokenManager.MatchAndRemove(tokenType.WHILE);


            if (next.isEmpty())
                throw new Exception("While token not found in do while");

            while (AcceptSeperators()) {
                tokenManager.MatchAndRemove(tokenType.SEPARATOR);
            }

            next = tokenManager.MatchAndRemove(tokenType.OPENPARENTHESES);

            if (next.isEmpty())
                throw new Exception("Open parenthesis must follow while token");


            Optional<Node> condition = ParseOperation();

            if (condition.isEmpty()) {
                throw new Exception("while loop incorrect syntax");
            }

            next = tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
            if (next.isEmpty())
                throw new Exception("Close parenthesis must at the end of while condition");


            tokenManager.MatchAndRemove(tokenType.SEMICOLON);

            return Optional.of(new DoWhileNode(condition.get(), block.getStatements()));

        }


        return Optional.empty();

    }

    /**node for return statement; take a single param and looks for 'return' */
    public Optional<StatementNode> parseReturn() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.RETURN) {
                tokenManager.MatchAndRemove(tokenType.RETURN);
                while (AcceptSeperators()) {
                    tokenManager.MatchAndRemove(tokenType.SEPARATOR);
                }
                Optional<Node> value = ParseOperation();
                if (value.isEmpty()) {
                    throw new Exception("Return value not found");
                }
                return Optional.of(new ReturnNode(value.get()));
            }
        }
        return Optional.empty();
    }


    /**requires name and parenthesis;
     * takes name of function to call and returns a statement if succeeds*/
    public Optional<StatementNode> parseFunctionCall() throws Exception {
        Optional<Token> currentToken = tokenManager.Peek(0);
        if (currentToken.isPresent()) {
            if (currentToken.get().gettype() == tokenType.GETLINE){
                String name = currentToken.get().gettype().toString();
                LinkedList<Node> parameters = new LinkedList<>();
                tokenManager.MatchAndRemove(currentToken.get().gettype());
                AcceptSeperators();
                Optional<Token> next;
                do {
                    tokenManager.MatchAndRemove(tokenType.COMMA);
                    next = tokenManager.Peek(1);
                    if(next.isPresent() && next.get().gettype()==tokenType.COMMA){
                        parameters.add(ParseBottomLevel().get());
                    }else{
                        parameters.add(ParseOperation().get());
                    }
                    AcceptSeperators();
                    currentToken = tokenManager.Peek(0);
                } while (currentToken.isPresent() && currentToken.get().gettype() == tokenType.COMMA);
                return Optional.of(new FunctionCallNode(name, parameters));

            } else if (currentToken.get().gettype() == tokenType.PRINT){
                String name = currentToken.get().gettype().toString();
                LinkedList<Node> parameters = new LinkedList<>();
                tokenManager.MatchAndRemove(currentToken.get().gettype());
                AcceptSeperators();
                Optional<Token> next;
                do {
                    tokenManager.MatchAndRemove(tokenType.COMMA);
                    next = tokenManager.Peek(1);
                    if(next.isPresent() && next.get().gettype()==tokenType.COMMA){
                        parameters.add(ParseBottomLevel().get());
                    }else{
                        parameters.add(ParseOperation().get());
                    }
                    AcceptSeperators();
                    currentToken = tokenManager.Peek(0);
                } while (currentToken.isPresent() && currentToken.get().gettype() == tokenType.COMMA);
                return Optional.of(new FunctionCallNode(name, parameters));

            }
            else if (currentToken.get().gettype() == tokenType.PRINTF){
                String name = currentToken.get().gettype().toString();
                LinkedList<Node> parameters = new LinkedList<>();
                tokenManager.MatchAndRemove(currentToken.get().gettype());
                AcceptSeperators();
                Optional<Token> next;
                do {
                    tokenManager.MatchAndRemove(tokenType.COMMA);
                    next = tokenManager.Peek(1);
                    if(next.isPresent() && next.get().gettype()==tokenType.COMMA){
                        parameters.add(ParseBottomLevel().get());
                    }else{
                        parameters.add(ParseOperation().get());
                    }
                    AcceptSeperators();
                    currentToken = tokenManager.Peek(0);
                } while (currentToken.isPresent() && currentToken.get().gettype() == tokenType.COMMA);
                return Optional.of(new FunctionCallNode(name, parameters));

            }
            else if (currentToken.get().gettype() == tokenType.EXIT){
                String name = currentToken.get().gettype().toString();
                LinkedList<Node> parameters = new LinkedList<>();
                tokenManager.MatchAndRemove(currentToken.get().gettype());
                AcceptSeperators();
                Optional<Token> next;
                do {
                    tokenManager.MatchAndRemove(tokenType.COMMA);
                    next = tokenManager.Peek(1);
                    if(next.isPresent() && next.get().gettype()==tokenType.COMMA){
                        parameters.add(ParseBottomLevel().get());
                    }else{
                        parameters.add(ParseOperation().get());
                    }
                    AcceptSeperators();
                    currentToken = tokenManager.Peek(0);
                } while (currentToken.isPresent() && currentToken.get().gettype() == tokenType.COMMA);
                return Optional.of(new FunctionCallNode(name, parameters));

            }
            else if (currentToken.get().gettype() == tokenType.NEXTFILE){
                String name = currentToken.get().gettype().toString();
                LinkedList<Node> parameters = new LinkedList<>();
                tokenManager.MatchAndRemove(currentToken.get().gettype());
                AcceptSeperators();
                Optional<Token> next;
                do {
                    tokenManager.MatchAndRemove(tokenType.COMMA);
                    next = tokenManager.Peek(1);
                    if(next.isPresent() && next.get().gettype()==tokenType.COMMA){
                        parameters.add(ParseBottomLevel().get());
                    }else{
                        parameters.add(ParseOperation().get());
                    }
                    AcceptSeperators();
                    currentToken = tokenManager.Peek(0);
                } while (currentToken.isPresent() && currentToken.get().gettype() == tokenType.COMMA);
                return Optional.of(new FunctionCallNode(name, parameters));

            }
            else if (currentToken.get().gettype() == tokenType.NEXT){
                String name = currentToken.get().gettype().toString();
                LinkedList<Node> parameters = new LinkedList<>();
                tokenManager.MatchAndRemove(currentToken.get().gettype());
                AcceptSeperators();
                Optional<Token> next;
                do {
                    tokenManager.MatchAndRemove(tokenType.COMMA);
                    next = tokenManager.Peek(1);
                    if(next.isPresent() && next.get().gettype()==tokenType.COMMA){
                        parameters.add(ParseBottomLevel().get());
                    }else{
                        parameters.add(ParseOperation().get());
                    }
                    AcceptSeperators();
                    currentToken = tokenManager.Peek(0);
                } while (currentToken.isPresent() && currentToken.get().gettype() == tokenType.COMMA);
                return Optional.of(new FunctionCallNode(name, parameters));

            }

            else if (currentToken.get().gettype() == tokenType.WORD) {
                Optional<Token> next = tokenManager.Peek(1);
                if(next.isEmpty() || next.get().gettype()!=tokenType.OPENPARENTHESES){
                    return Optional.empty();
                }
                String name = currentToken.get().getvalue();
                tokenManager.MatchAndRemove(tokenType.WORD);
                tokenManager.MatchAndRemove(tokenType.OPENPARENTHESES);
                LinkedList<Node> parameters = new LinkedList<>();
                currentToken = tokenManager.Peek(0);
                while (currentToken.isEmpty() || !currentToken.get().gettype().equals(tokenType.CLOSEPARENTHESES)) {
                    Optional<Token> nextToken  = tokenManager.Peek(1);
                    if(nextToken.isPresent() && (nextToken.get().gettype()==tokenType.COMMA || nextToken.get().gettype()==tokenType.CLOSEPARENTHESES)){
                          parameters.add(ParseBottomLevel().get());
                    }else{
                        parameters.add(ParseOperation().get());
                    }
                    tokenManager.MatchAndRemove(tokenType.COMMA);
                    currentToken = tokenManager.Peek(0);
                }
                tokenManager.MatchAndRemove(tokenType.CLOSEPARENTHESES);
                return Optional.of(new FunctionCallNode(name, parameters));
            }
        }
        return Optional.empty();
    }
}