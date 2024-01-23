package assignment1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Interpreter {

    private HashMap<String, InterpreterDataType> globalVariables;
    private HashMap<String, FunctionDefinitionNode> members;
    private LineHandler lineHandler;

    /**
     * constructor for the interpreter
     */
    public Interpreter(ProgramNode programNode, String input) throws IOException {
        this.globalVariables = new HashMap<>();
        this.members = new HashMap<>();
        if (input != null) {
            List<String> lines = Files.readAllLines(Paths.get(input));
            lineHandler = new LineHandler(lines, globalVariables);
        } else {
            lineHandler = new LineHandler(List.of(), globalVariables);
        }

        globalVariables.put("FILENAME", new InterpreterDataType(input));
        globalVariables.put("FS", new InterpreterDataType(" "));
        globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
        globalVariables.put("OFS", new InterpreterDataType(" "));
        globalVariables.put("ORS", new InterpreterDataType("\n"));

        for (FunctionDefinitionNode func : programNode.getFunctions()) {
            members.put(func.getName(), func);
        }


        /**multiple instances of BuiltInFunctionDefinitionNode, one for each built-in function*/
        BuiltInFunctionDefinitionNode printNode = new BuiltInFunctionDefinitionNode(
                "print",
                new LinkedList<>(),
                parameters -> {
                    InterpreterArrayDataType arrayData = (InterpreterArrayDataType) parameters.get("0");
                    String result = arrayData.getVariables().values().stream()
                            .map(InterpreterDataType::toString)
                            .collect(Collectors.joining(" "));
                    System.out.print(result);
                    return result;
                },
                true
        );


        members.put(printNode.getName(), printNode);

        BuiltInFunctionDefinitionNode printFNode = new BuiltInFunctionDefinitionNode(
                "printf",
                new LinkedList<>(), parameters -> {
            InterpreterArrayDataType arrayData = (InterpreterArrayDataType) parameters.get("0");
            if (!arrayData.getVariables().isEmpty()) {
                String formatString = arrayData.getVariables().get("0").toString();
                Object[] args = arrayData.getVariables().values().stream()
                        .skip(1) // skip the format string
                        .map(InterpreterDataType::toString)
                        .toArray();
                System.out.printf(formatString, args);
            }
            return "";
        },
                true
        );

        members.put(printFNode.getName(), printFNode);


        BuiltInFunctionDefinitionNode getLineNode = new BuiltInFunctionDefinitionNode(
                "getline",
                new LinkedList<>(),
                parameters -> {
                    boolean hasLine = lineHandler.SplitAndAssign();
                    if (hasLine) {
                        InterpreterDataType nfValue = globalVariables.get("NF");  // Assuming globalVariables is accessible
                        return nfValue.toString();
                    } else {
                        return "-1";
                    }
                },
                false
        );

        members.put(getLineNode.getName(), getLineNode);

        BuiltInFunctionDefinitionNode nextNode = new BuiltInFunctionDefinitionNode(
                "next",
                new LinkedList<>(),
                parameters -> {
                    lineHandler.SplitAndAssign();
                    return "";  // No return value for 'next'
                },
                false  // This function is not variadic
        );

        members.put(nextNode.getName(), nextNode);

        BuiltInFunctionDefinitionNode gsubNode = new BuiltInFunctionDefinitionNode(
                "gsub",
                new LinkedList<>(),
                parameters -> {
                    String regexp = parameters.get("0").toString();
                    String replacement = parameters.get("1").toString();
                    String target = parameters.containsKey("2") ? parameters.get("2").toString() : globalVariables.get("$0").toString();

                    Pattern pattern = Pattern.compile(regexp);
                    Matcher matcher = pattern.matcher(target);
                    String replaced = matcher.replaceAll(replacement);

                    int count = 0;
                    while (matcher.find()) count++;

                    if (parameters.containsKey("2")) {
                        parameters.get("2").setValue(replaced);
                    } else {
                        globalVariables.get("$0").setValue(replaced);
                    }

                    return String.valueOf(count);
                },
                false
        );
        members.put(gsubNode.getName(), gsubNode);

        BuiltInFunctionDefinitionNode matchNode = new BuiltInFunctionDefinitionNode(
                "match",
                new LinkedList<>(),
                parameters -> {
                    String str = parameters.get("0").toString();
                    String regexp = parameters.get("1").toString();

                    Pattern pattern = Pattern.compile(regexp);
                    Matcher matcher = pattern.matcher(str);

                    if (matcher.find()) {
                        return String.valueOf(matcher.start() + 1);
                    }
                    return "0";
                },
                false
        );
        members.put(matchNode.getName(), matchNode);

        BuiltInFunctionDefinitionNode subNode = new BuiltInFunctionDefinitionNode(
                "sub",
                new LinkedList<>(),
                parameters -> {
                    String regexp = parameters.get("0").toString();
                    String replacement = parameters.get("1").toString();
                    String target = parameters.containsKey("2") ? parameters.get("2").toString() : globalVariables.get("$0").toString();

                    Pattern pattern = Pattern.compile(regexp);
                    Matcher matcher = pattern.matcher(target);
                    String replaced = matcher.replaceFirst(replacement);

                    if (parameters.containsKey("2")) {
                        parameters.get("2").setValue(replaced);
                    } else {
                        globalVariables.get("$0").setValue(replaced);
                    }

                    return matcher.find() ? "1" : "0";
                },
                false
        );
        members.put(subNode.getName(), subNode);

        BuiltInFunctionDefinitionNode indexNode = new BuiltInFunctionDefinitionNode(
                "index",
                new LinkedList<>(),
                parameters -> {
                    String string = parameters.get("0").toString();
                    String substring = parameters.get("1").toString();
                    return String.valueOf(string.indexOf(substring) + 1);  // +1 because AWK's string indexing starts from 1
                },
                false
        );
        members.put(indexNode.getName(), indexNode);

        BuiltInFunctionDefinitionNode lengthNode = new BuiltInFunctionDefinitionNode(
                "length",
                new LinkedList<>(),
                parameters -> String.valueOf(parameters.get("0").toString().length()),
                false
        );
        members.put(lengthNode.getName(), lengthNode);

        BuiltInFunctionDefinitionNode splitNode = new BuiltInFunctionDefinitionNode(
                "split",
                new LinkedList<>(),
                parameters -> {
                    String string = parameters.get("0").toString();
                    String separator = parameters.get("2").toString();
                    String[] parts = string.split(separator);
                    InterpreterArrayDataType array = (InterpreterArrayDataType) parameters.get("1");
                    for (int i = 0; i < parts.length; i++) {
                        array.getVariables().put(String.valueOf(i), new InterpreterDataType(parts[i]));
                    }
                    return String.valueOf(parts.length);
                },
                false
        );
        members.put(splitNode.getName(), splitNode);

        BuiltInFunctionDefinitionNode substrNode = new BuiltInFunctionDefinitionNode(
                "substr",
                new LinkedList<>(),
                parameters -> {
                    String string = parameters.get("0").toString();
                    int start = Integer.parseInt(parameters.get("1").toString()) - 1;  // -1 because AWK's string indexing starts from 1
                    int length = parameters.containsKey("2") ? Integer.parseInt(parameters.get("2").toString()) : string.length() - start;
                    return string.substring(start, start + length);
                },
                false
        );
        members.put(substrNode.getName(), substrNode);

        BuiltInFunctionDefinitionNode tolowerNode = new BuiltInFunctionDefinitionNode(
                "tolower",
                new LinkedList<>(),
                parameters -> parameters.get("0").toString().toLowerCase(),
                false
        );
        members.put(tolowerNode.getName(), tolowerNode);

        BuiltInFunctionDefinitionNode toupperNode = new BuiltInFunctionDefinitionNode(
                "toupper",
                new LinkedList<>(),
                parameters -> parameters.get("0").toString().toUpperCase(),
                false
        );

        members.put(toupperNode.getName(), toupperNode);


    }


    public HashMap<String, FunctionDefinitionNode> getMembers() {
        return members;
    }

    /**calls getIDT on right side and sets target value to the result. returns result*/
    public InterpreterDataType getIDT(Node node, HashMap<String, InterpreterDataType> localVariables) throws Exception {
        if (node instanceof AssignmentNode assignmentNode) {
            Node target = ((AssignmentNode) node).getTarget();

            if (target instanceof VariableReferenceNode || target instanceof OperationNode && ((OperationNode) target).getOperation().equals(OperationNode.OperationType.DOLLAR)) {
                InterpreterDataType rightSide = getIDT(assignmentNode.getExpression(), localVariables);
                if(target instanceof VariableReferenceNode var)
                    localVariables.put(var.getVarName(), rightSide);
                else
                    localVariables.put(((OperationNode) target).getOperation().name(), rightSide);
                return rightSide;
            }
            throw new Exception("Assignment Node unexpected format");

            /**returns new IDT with value set to the constant node value*/
        } else if (node instanceof ConstantNode constantNode) {
            return new InterpreterDataType(constantNode.getValue());

            /**makes runFunctionCall method which takes function call node and locals and returns ""*/
        } else if (node instanceof FunctionCallNode functionCallNode) {
            return new InterpreterDataType(runFunctionCall(localVariables, functionCallNode));

            /**throws exception when someone is trying to pass pattern to function or assignment*/
        } else if (node instanceof PatternNode) {
            throw new Exception("Pattern Node not allowed as input");

            /**evaluates boolean condition then evaluate and return either true or false case*/
        } else if (node instanceof TernaryNode ternaryNode) {
            InterpreterDataType expression = getIDT(ternaryNode.getExpression(), localVariables);
            if (expression.getValue().equalsIgnoreCase("1")) {
                return getIDT(ternaryNode.getTrueCase(), localVariables);
            } else {
                return getIDT(ternaryNode.getFalseCase(), localVariables);
            }
            /**if not array reference, looks up variable in glocals/locals and returns it
             * if an array reference, resolves index and looks it up in variable hash map
             * if not an IADT, throws exception*/
        } else if (node instanceof VariableReferenceNode variableReferenceNode) {
            String variableName = variableReferenceNode.getVarName();
            Optional<Node> expressionIndex = variableReferenceNode.getExpression();
            InterpreterDataType var = localVariables.get(variableName);
            if (var == null && globalVariables.containsKey(variableName)) {
                var = globalVariables.get(variableName);
            }
            if (expressionIndex.isPresent()) {
                if (var instanceof InterpreterArrayDataType array) {
                    InterpreterDataType indexIDT = getIDT(expressionIndex.get(), localVariables);
                    if (array.getVariables().containsKey(indexIDT.getValue())) {
                        return array.getVariables().get(indexIDT.getValue());
                    }
                } else {
                    throw new Exception("Variable is not an IADT");
                }
            }
            return var;

            /**evaluates left and right using getIDT, performs operation
             * for math-based ops, converts to float, does op, converts to string to store in IDT*/
        } else if (node instanceof OperationNode operationNode) {
            InterpreterDataType leftOperation = getIDT(operationNode.getLeft(), localVariables);
            if (operationNode.getRight().isPresent()) {

                if (operationNode.getOperation() == OperationNode.OperationType.MATCH) {
                    if (operationNode.getRight().get() instanceof PatternNode patternNode) {
                        Pattern pattern = Pattern.compile(patternNode.getValue());
                        Matcher matcher = pattern.matcher(leftOperation.getValue());
                        boolean matchCheck = matcher.find();
                        return new InterpreterDataType(booleanToString(matchCheck));
                    }
                } else if (operationNode.getOperation() == OperationNode.OperationType.NOTMATCH) {
                    if (operationNode.getRight().get() instanceof PatternNode patternNode) {
                        Pattern pattern = Pattern.compile(patternNode.getValue());
                        Matcher matcher = pattern.matcher(leftOperation.getValue());
                        boolean matchCheck = matcher.find();
                        return new InterpreterDataType(booleanToString(!matchCheck));
                    }
                } else if (operationNode.getOperation() == OperationNode.OperationType.IN) {
                    if (operationNode.getRight().get() instanceof VariableReferenceNode rightVariable) {
                        String arrayName = rightVariable.getVarName();
                        if (localVariables.containsKey(arrayName)) {
                            InterpreterDataType array = localVariables.get(arrayName);
                            if (array instanceof InterpreterArrayDataType arrayData) {
                                return new InterpreterDataType(booleanToString(arrayData.getVariables().containsKey(leftOperation.getValue())));
                            }
                            throw new Exception("Array not found");
                        } else if (globalVariables.containsKey(arrayName)) {
                            InterpreterDataType array = localVariables.get(arrayName);
                            if (array instanceof InterpreterArrayDataType arrayData) {
                                return new InterpreterDataType(booleanToString(arrayData.getVariables().containsKey(leftOperation.getValue())));
                            }
                            throw new Exception("Array not found");
                        } else {
                            throw new Exception("Array not found");
                        }
                    } else {
                        throw new Exception("The right side not a variable reference node");
                    }
                }

                InterpreterDataType rightOperation = getIDT(operationNode.getRight().get(), localVariables);
                try {
                    float leftFloat = Float.parseFloat(leftOperation.getValue());
                    float rightFloat = Float.parseFloat(rightOperation.getValue());
                    if (operationNode.getOperation() == OperationNode.OperationType.ADD) {
                        return new InterpreterDataType(String.valueOf(leftFloat + rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.SUBTRACT) {
                        return new InterpreterDataType(String.valueOf(leftFloat - rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.MULTIPLY) {
                        return new InterpreterDataType(String.valueOf(leftFloat * rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.DIVIDE) {
                        if (rightFloat == 0)
                            throw new ArithmeticException("Cannot divide by 0");
                        return new InterpreterDataType(String.valueOf(leftFloat / rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.MODULO) {
                        if (rightFloat == 0)
                            throw new ArithmeticException("Cannot divide by 0");
                        return new InterpreterDataType(String.valueOf(leftFloat % rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.EXPONENT) {
                        return new InterpreterDataType(String.valueOf(Math.pow(leftFloat, rightFloat)));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.EQ) {
                        return new InterpreterDataType(booleanToString(leftFloat == rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.NE) {
                        return new InterpreterDataType(booleanToString(leftFloat != rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.LT) {
                        return new InterpreterDataType(booleanToString(leftFloat < rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.LE) {
                        return new InterpreterDataType(booleanToString(leftFloat <= rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.GT) {
                        return new InterpreterDataType(booleanToString(leftFloat > rightFloat));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.GE) {
                        return new InterpreterDataType(booleanToString(leftFloat >= rightFloat));
                    }
                } catch (NumberFormatException e) {
                    if (operationNode.getOperation() == OperationNode.OperationType.EQ) {
                        return new InterpreterDataType(booleanToString(leftOperation.getValue().equals(rightOperation.getValue())));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.NE) {
                        return new InterpreterDataType(booleanToString(!leftOperation.getValue().equals(rightOperation.getValue())));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.LT) {
                        return new InterpreterDataType(booleanToString(leftOperation.getValue().compareTo(rightOperation.getValue()) < 0));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.LE) {
                        return new InterpreterDataType(booleanToString(leftOperation.getValue().compareTo(rightOperation.getValue()) <= 0));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.GT) {
                        return new InterpreterDataType(booleanToString(leftOperation.getValue().compareTo(rightOperation.getValue()) > 0));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.GE) {
                        return new InterpreterDataType(booleanToString(leftOperation.getValue().compareTo(rightOperation.getValue()) >= 0));
                    } else if (operationNode.getOperation() == OperationNode.OperationType.CONCATENATION) {
                        return new InterpreterDataType(leftOperation.getValue() + rightOperation.getValue());
                    }
                }
                if (operationNode.getOperation() == OperationNode.OperationType.AND) {
                    boolean checkedLeft;
                    boolean checkedRight;
                    try {
                        float leftValue = Float.parseFloat(leftOperation.getValue());
                        checkedLeft = leftValue != 0.0;
                    } catch (NumberFormatException exception) {
                        checkedLeft = false;
                    }
                    try {
                        float rightValue = Float.parseFloat(rightOperation.getValue());
                        checkedRight = rightValue != 0.0;
                    } catch (NumberFormatException exception) {
                        checkedRight = false;
                    }
                    return new InterpreterDataType(booleanToString(checkedLeft && checkedRight));
                } else if (operationNode.getOperation() == OperationNode.OperationType.OR) {
                    boolean checkedLeft;
                    boolean checkedRight;
                    try {
                        float leftValue = Float.parseFloat(leftOperation.getValue());
                        checkedLeft = leftValue != 0.0;
                    } catch (NumberFormatException exception) {
                        checkedLeft = false;
                    }
                    try {
                        float rightValue = Float.parseFloat(rightOperation.getValue());
                        checkedRight = rightValue != 0.0;
                    } catch (NumberFormatException exception) {
                        checkedRight = false;
                    }
                    return new InterpreterDataType(booleanToString(checkedLeft || checkedRight));
                }
            }
            if (operationNode.getOperation() == OperationNode.OperationType.NOT) {
                boolean checkedLeft;
                try {
                    float leftValue = Float.parseFloat(leftOperation.getValue());
                    checkedLeft = leftValue != 0.0;
                } catch (NumberFormatException exception) {
                    checkedLeft = false;
                }
                return new InterpreterDataType(String.valueOf(!checkedLeft));
            } else if (operationNode.getOperation() == OperationNode.OperationType.DOLLAR) {
                float left = Float.parseFloat(leftOperation.getValue());
                int convert = (int) left;
                String variable = "$" + convert;
                return new InterpreterDataType(globalVariables.get(variable).getValue());
            } else if (operationNode.getOperation() == OperationNode.OperationType.POSTINC) {
                float leftFloat = Float.parseFloat(new InterpreterDataType(leftOperation.getValue()).toString());
                leftFloat++;
                if(operationNode.getLeft() instanceof  VariableReferenceNode var)
                    globalVariables.put(var.getVarName(), new InterpreterDataType(String.valueOf(leftFloat)));
                return new InterpreterDataType(String.valueOf(leftFloat));
            } else if (operationNode.getOperation() == OperationNode.OperationType.POSTDEC) {
                float leftFloat = Float.parseFloat(leftOperation.getValue());
                leftFloat--;
                if(operationNode.getLeft() instanceof  VariableReferenceNode var)
                    globalVariables.put(var.getVarName(), new InterpreterDataType(String.valueOf(leftFloat)));
                return new InterpreterDataType(String.valueOf(leftFloat));
            } else if (operationNode.getOperation() == OperationNode.OperationType.PREINC) {
                float leftFloat = Float.parseFloat(leftOperation.getValue());
                ++leftFloat;
                if(operationNode.getLeft() instanceof  VariableReferenceNode var)
                    globalVariables.put(var.getVarName(), new InterpreterDataType(String.valueOf(leftFloat)));
                return new InterpreterDataType(String.valueOf(leftFloat));
            } else if (operationNode.getOperation() == OperationNode.OperationType.PREDEC) {
                float leftFloat = Float.parseFloat(leftOperation.getValue());
                --leftFloat;
                if(operationNode.getLeft() instanceof  VariableReferenceNode var)
                    globalVariables.put(var.getVarName(), new InterpreterDataType(String.valueOf(leftFloat)));
                return new InterpreterDataType(String.valueOf(leftFloat));
            }


        }


            return null;
        }

        /**
         * take a List<String> as a parameter to the constructor and stores it in a member.
         */
        public class LineHandler {
            private List<String> lines;
            private int currentLine;
            private int NR;
            private int FNR;
            private HashMap<String, InterpreterDataType> globalVariables;

            public LineHandler(List<String> lines, HashMap<String, InterpreterDataType> globalVariables) {
                this.lines = lines;
                this.globalVariables = globalVariables;
                this.currentLine = 0;
                this.NR = 0;
                this.FNR = 0;
            }

            private String getFieldSeparator() {
                InterpreterDataType fsValue = globalVariables.get("FS");
                return (fsValue != null) ? fsValue.toString() : " ";
            }

            /**
             * gets the next line and splits it by looking at the global variables
             */
            public boolean SplitAndAssign() {
                if (currentLine >= lines.size()) {
                    return false;
                }
                String currentLineStr = lines.get(currentLine);
                globalVariables.put("$0", new InterpreterDataType(currentLineStr));

                String[] fields = currentLineStr.split(getFieldSeparator());
                for (int i = 0; i < fields.length; i++) {
                    String variableName = "$" + (i + 1);
                    globalVariables.put(variableName, new InterpreterDataType(fields[i]));
                }

                globalVariables.put("NF", new InterpreterDataType(String.valueOf(fields.length)));
                globalVariables.put("NR", new InterpreterDataType(String.valueOf(++NR)));
                globalVariables.put("FNR", new InterpreterDataType(String.valueOf(++FNR)));

                currentLine++;
                return true;
            }
        }


    ReturnType ProcessStatement(HashMap<String, InterpreterDataType> locals, StatementNode stmt) throws Exception {
        if (stmt instanceof BreakNode)
            return new ReturnType(ReturnType.ResultType.BREAK);
        else if (stmt instanceof ContinueNode)
            return new ReturnType(ReturnType.ResultType.CONTINUE);
        /**get array from variables, if indices is set, delete from array, else delete all*/
        else if (stmt instanceof DeleteNode deleteNode){
            if (deleteNode.getToBeDeleted() instanceof VariableReferenceNode variableReferenceNode){
                String arrayName = variableReferenceNode.getVarName();
                Optional<Node> expressionIndex = variableReferenceNode.getExpression();
                InterpreterDataType deleteArray = locals.get(arrayName);
                if (deleteArray == null && globalVariables.containsKey(arrayName)) {
                    deleteArray = globalVariables.get(arrayName);
                }
                if (!(deleteArray instanceof InterpreterArrayDataType)){
                    throw new Exception("The node trying to be deleted is not found");
                }
                if(expressionIndex.isPresent()){
                    InterpreterDataType indexIDT = getIDT(expressionIndex.get(), locals);
                    if(indexIDT instanceof InterpreterArrayDataType){
                        for(InterpreterDataType index: ((InterpreterArrayDataType) indexIDT).getVariables().values()){
                            ((InterpreterArrayDataType) deleteArray).getVariables().remove(index.getValue());
                        }
                    } else {
                        ((InterpreterArrayDataType) deleteArray).getVariables().remove(indexIDT.getValue());
                    }
                } else{
                    ((InterpreterArrayDataType) deleteArray).getVariables().clear();
                }
            } else {
                throw new Exception("The node trying to be deleted is not a variable");
            }
            /**calls interpretListOfStatements, uses getIDT to evaluate condition, checks
             * interpretListOfStatements, if break, break out of loop, on return, return from processStatement*/
        } else if (stmt instanceof DoWhileNode doWhileNode) {
            do {
                ReturnType listOfStatements = InterpretListOfStatements(doWhileNode.getStatements(), locals);
                if (listOfStatements.getResult() == ReturnType.ResultType.BREAK) {
                    break;
                }
                else if (listOfStatements.getResult() == ReturnType.ResultType.RETURN) {
                    return listOfStatements;
                }

            } while (getIDT(doWhileNode.getCondition(), locals).getValue().equals("1"));
            /**if an initial, call processStatement. call interpretListOfStatements on forNode*/
        } else if (stmt instanceof ForNode forNode) {
            if (forNode.getFirstCondition() != null) {
                ProcessStatement(locals, (StatementNode) forNode.getFirstCondition());
            }

            while (getIDT(forNode.getSecondCondition(), locals).getValue().equals("1")) {
                ReturnType listOfStatements = InterpretListOfStatements(forNode.getStatements(), locals);
                if (listOfStatements.getResult() == ReturnType.ResultType.BREAK) {
                    break;
                }
                else if (listOfStatements.getResult() == ReturnType.ResultType.RETURN) {
                    return listOfStatements;
                }
                ProcessStatement(locals, (StatementNode) forNode.getThirdCondition());
            }
            /**finds array, loops over every key in hashmap, sets variable to key, calls interpretListOfStatements*/
        } else if (stmt instanceof ForEachNode forEachNode) {
                if(forEachNode.getVar() instanceof VariableReferenceNode var && forEachNode.getArray() instanceof VariableReferenceNode array) {
                    InterpreterDataType forArray = locals.get(array.getVarName());
                    if (forArray == null && globalVariables.containsKey(array.getVarName())) {
                        forArray = globalVariables.get(array.getVarName());
                    }
                    if (!(forArray instanceof InterpreterArrayDataType)){
                        throw new Exception("The forEach can only be performed on Array");
                    }

                    for (String key : ((InterpreterArrayDataType) forArray).getVariables().keySet()) {
                        locals.put(var.getVarName(), new InterpreterDataType(key));
                        ReturnType listOfStatements = InterpretListOfStatements(forEachNode.getStatements(), locals);
                        if (listOfStatements.getResult() == ReturnType.ResultType.BREAK) {
                            break;
                        } else if (listOfStatements.getResult() == ReturnType.ResultType.RETURN) {
                            return  listOfStatements;}

                    }
                } else {
                    throw new Exception("The variable reference for array not found when attempting forEach");
                }
            /**walks linked list looking for ifNode where condition is empty or evaluates to be true
             * when found, calls interpretListOfStatements on ifNode Statements. if not 'none' returns*/
        } else if (stmt instanceof IfNode ifNode) {
            while(ifNode != null && ifNode.getCondition().isPresent() && getIDT(ifNode.getCondition().get(), locals).getValue().equals("0")){
                if(ifNode.getNext().isPresent())
                    ifNode = (IfNode) ifNode.getNext().get();
                else
                    ifNode = null;
            }
            if(ifNode != null){
                ReturnType listOfStatements = InterpretListOfStatements(ifNode.getStatements(), locals);
                if (listOfStatements.getResult() != ReturnType.ResultType.NONE) {
                    return listOfStatements;
                }
            }
            /**if value, evaluates*/
        } else if (stmt instanceof ReturnNode returnNode) {
            if (returnNode.getValue() != null) {
                InterpreterDataType returnValue = getIDT(returnNode.getValue(), locals);
                return new ReturnType(ReturnType.ResultType.RETURN, returnValue.getValue());
            } else {
                return new ReturnType(ReturnType.ResultType.RETURN);
            }
            /**similar to do-while, but with while*/
        } else if (stmt instanceof WhileNode whileNode) {
            while (getIDT(whileNode.getCondition(), locals).getValue().equals("1")) {
                ReturnType listOfStatements = InterpretListOfStatements(whileNode.getStatements(), locals);
                if (listOfStatements.getResult() == ReturnType.ResultType.BREAK) {
                    break;
                }
                else if (listOfStatements.getResult() == ReturnType.ResultType.RETURN) {
                    return listOfStatements;
                }

            }
        } else {
            /***/
            InterpreterDataType value = getIDT(stmt, locals);
            if(value == null){
                throw new Exception("getIDT did not return valid value");
            }

        }

        return new ReturnType(ReturnType.ResultType.NONE);
    }



    /**loops over statements, calling processStatement for each one
     * checks return of each processStatement, if not none return passing up same type*/
    ReturnType InterpretListOfStatements(LinkedList<StatementNode> statements, HashMap<String, InterpreterDataType> locals) throws Exception {
        for(StatementNode statementsLoop : statements){
            ReturnType processedValue = ProcessStatement(locals, statementsLoop);
            if(processedValue.getResult() != ReturnType.ResultType.NONE){
                return processedValue;
            }
        }
        return new ReturnType(ReturnType.ResultType.NONE);
    }

    /**runs BEGIN blocks, calls splitAndAssign, calls interpretBlock for every record*/
    public void InterpretProgram(ProgramNode programNode) throws Exception {

        for (BlockNode begin : programNode.getBegin()) {
           InterpretBlock(begin);
        }
        while (lineHandler.SplitAndAssign()) {
            for (BlockNode other : programNode.getOther()) {
                InterpretBlock(other);
            }
        }
        for (BlockNode end : programNode.getEnd()) {
            InterpretBlock(end);
        }

    }


    /**if block has a condition, tests to see if true, if no condition or if true, call processStatement*/
    public List<ReturnType> InterpretBlock(BlockNode blockNode) throws Exception {
        List<ReturnType> returnValues = new ArrayList<>();
        if(blockNode.getCondition().isPresent()){
            if(getIDT(blockNode.getCondition().get(), globalVariables).getValue().equals("1")) {
                for (StatementNode statement : blockNode.getStatements()) {
                   returnValues.add(ProcessStatement(globalVariables, statement));
                }
            }
        } else {
            for (StatementNode statement : blockNode.getStatements()) {
                returnValues.add(ProcessStatement(globalVariables, statement));
            }
        }
        return  returnValues;
    }

    /**maps using local variable hashmap, we follow the same mapping process for all but last variable
     * for last variable, makes an array and puts all valuesfrom functionCallNOde into array*/
    private String runFunctionCall(HashMap<String, InterpreterDataType> local, FunctionCallNode functionCallNode) throws Exception {

        FunctionDefinitionNode function = members.get(functionCallNode.getName().toLowerCase());
        if (function == null)
            throw new Exception("Function not found");

        if (function instanceof BuiltInFunctionDefinitionNode func && func.isVariadic()){

            if (func.getParameters().size() > functionCallNode.getParameters().size()) {
                throw new Exception("Function not found or incorrect parameter size");
            }
            HashMap<String, InterpreterDataType> map = new HashMap<>();
            int i;
            for (i = 0; i < func.getParameters().size()-1; i++)
                map.put(func.getParameters().get(i), getIDT(functionCallNode.getParameters().get(i), local));


            InterpreterArrayDataType remaining = new InterpreterArrayDataType();
            for (int j = 0; i < functionCallNode.getParameters().size(); i++){
                remaining.getVariables().put(String.valueOf(j), getIDT(functionCallNode.getParameters().get(i), local));
                j++;
            }

            if (!func.getParameters().isEmpty())
                map.put(func.getParameters().getLast(), remaining);
            else
                map.put("0", remaining);

            return func.execute(map);

        } else {
            if (function.getParameters().size() != functionCallNode.getParameters().size()) {
                throw new Exception("Function not found or incorrect parameter size");
            }
            HashMap<String, InterpreterDataType> map = new HashMap<>();
            int i;
            for (i = 0; i < function.getParameters().size(); i++)
                map.put(function.getParameters().get(i), getIDT(functionCallNode.getParameters().get(i), local));
            if (function instanceof BuiltInFunctionDefinitionNode func)
                return func.execute(map);
	        else
                return InterpretListOfStatements(function.getStatements(), map).getValue();

        }


    }



    public static String booleanToString(boolean value) {
        return value ? "1" : "0";
    }
}
