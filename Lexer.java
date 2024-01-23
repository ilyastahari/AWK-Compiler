package assignment1;

import java.util.HashMap;
import java.util.LinkedList;

public class Lexer {

    private StringHandler stringHandler;
    private int linePosition;
    private int characterPosition;

    /**HashMap of <String, TokenType> , fully populated  */
    private static HashMap<String, Token.tokenType> knownWords = new HashMap<String, Token.tokenType>();
    private static HashMap<String, Token.tokenType> knownSymbol = new HashMap<String, Token.tokenType>();
    private static HashMap<String, Token.tokenType> knownDoubleSymbol = new HashMap<String, Token.tokenType>();


    public static void keyWords() {
        /**All the keyword tokenTypes being initialized*/
        knownWords.put("break", Token.tokenType.BREAK);
        knownWords.put("continue", Token.tokenType.CONTINUE);
        knownWords.put("delete", Token.tokenType.DELETE);
        knownWords.put("do", Token.tokenType.DO);
        knownWords.put("begin", Token.tokenType.BEGIN);
        knownWords.put("else", Token.tokenType.ELSE);
        knownWords.put("for", Token.tokenType.FOR);
        knownWords.put("end", Token.tokenType.END);
        knownWords.put("if", Token.tokenType.IF);
        knownWords.put("print", Token.tokenType.PRINT);
        knownWords.put("return", Token.tokenType.RETURN);
        knownWords.put("printf", Token.tokenType.PRINTF);
        knownWords.put("while", Token.tokenType.WHILE);
        knownWords.put("next", Token.tokenType.NEXT);
        knownWords.put("in", Token.tokenType.IN);
        knownWords.put("getline", Token.tokenType.GETLINE);
        knownWords.put("exit", Token.tokenType.EXIT);
        knownWords.put("nextfile", Token.tokenType.NEXTFILE);
        knownWords.put("function", Token.tokenType.FUNCTION);

        /**All the single symbol tokenTypes being initialized */
        knownSymbol.put("{", Token.tokenType.OPENCURLYBRACE);
        knownSymbol.put("}", Token.tokenType.CLOSECURLYBRACE);
        knownSymbol.put("[", Token.tokenType.OPENBRACKET);
        knownSymbol.put("]", Token.tokenType.CLOSEBRACKET);
        knownSymbol.put("(", Token.tokenType.OPENPARENTHESES);
        knownSymbol.put(")", Token.tokenType.CLOSEPARENTHESES);
        knownSymbol.put("$", Token.tokenType.DOLLARSIGN);
        knownSymbol.put("~", Token.tokenType.TILDE);
        knownSymbol.put("=", Token.tokenType.EQUALS);
        knownSymbol.put("<", Token.tokenType.LESSTHAN);
        knownSymbol.put(">", Token.tokenType.GREATERTHAN);
        knownSymbol.put("!", Token.tokenType.EXCLAMATION);
        knownSymbol.put("+", Token.tokenType.ADD);
        knownSymbol.put("^", Token.tokenType.POWEROF);
        knownSymbol.put("-", Token.tokenType.MINUS);
        knownSymbol.put("?", Token.tokenType.QUESTION);
        knownSymbol.put(":", Token.tokenType.COLON);
        knownSymbol.put("*", Token.tokenType.TIMES);
        knownSymbol.put("/", Token.tokenType.DIVIDE);
        knownSymbol.put("%", Token.tokenType.PERCENT);
        knownSymbol.put(";", Token.tokenType.SEMICOLON);
        knownSymbol.put("\n", Token.tokenType.NEWLINE);
        knownSymbol.put("|", Token.tokenType.VERTBAR);
        knownSymbol.put(",", Token.tokenType.COMMA);

        /**All the double symbol token types being initialized */
        knownDoubleSymbol.put(">=", Token.tokenType.GREATEROREQUAL);
        knownDoubleSymbol.put("++", Token.tokenType.INCEREMENT);
        knownDoubleSymbol.put("--", Token.tokenType.DECREMENT);
        knownDoubleSymbol.put("<=", Token.tokenType.LESSOREQUAL);
        knownDoubleSymbol.put("==", Token.tokenType.EQUALTO);
        knownDoubleSymbol.put("!=", Token.tokenType.NOTEQUALTO);
        knownDoubleSymbol.put("^=", Token.tokenType.EXCLUSIVETO);
        knownDoubleSymbol.put("%=", Token.tokenType.MODASSIGN);
        knownDoubleSymbol.put("*=", Token.tokenType.MULTASSIGN);
        knownDoubleSymbol.put("/=", Token.tokenType.DIVASSIGN);
        knownDoubleSymbol.put("+=", Token.tokenType.ADDASSIGN);
        knownDoubleSymbol.put("-=", Token.tokenType.MINUSASSIGN);
        knownDoubleSymbol.put("!~", Token.tokenType.DOESNOTCONTAIN);
        knownDoubleSymbol.put("&&", Token.tokenType.LOGICALAND);
        knownDoubleSymbol.put(">>", Token.tokenType.RIGHTSHIFT);
        knownDoubleSymbol.put("||", Token.tokenType.LOGICALOR);

    }

    /**constructor which creates a stringHandler and takes string as a param*/
    /**also keeps track of what line we're at and char position*/
    public Lexer(String awkfile){
        stringHandler = new StringHandler(awkfile);
        linePosition = 0;
        characterPosition = 0;
        keyWords();

    }


    /**the lex method which breaks data from StringHandler into linkedList of tokens*/
    public LinkedList<Token> Lex() throws Exception{
        LinkedList<Token> tokens = new LinkedList<>();
        while(!stringHandler.isDone()){
            char next = stringHandler.Peek(0);
            if( next == '\t' || next == ' '){
                characterPosition++;
                stringHandler.GetChar();
            }
            else if (next == '\n'){
                Token sep = new Token(Token.tokenType.SEPARATOR, linePosition, characterPosition);
                tokens.add(sep);
                linePosition++;
                characterPosition = 0;
                stringHandler.GetChar();

            }
            else if( next == '\r'){
                stringHandler.GetChar();
            }

            else if (Character.isLetter(next)){
                tokens.add(ProcessWord());
            }

            else if(Character.isDigit(next)){
                tokens.add(ProcessNumber());

            }

            else if(next == '#'){
                while(!stringHandler.isDone() && next != '\n'){
                    stringHandler.GetChar();
                    next = stringHandler.Peek(0);
                }
            }
            else if(next == '"'){
               tokens.add(HandleStringLiteral());
            }
            else if(next == '`'){
                tokens.add(HandlePattern());
            }


            else{
                Token symbol = ProcessSymbol();
                if (symbol != null){
                    tokens.add(symbol);
                    stringHandler.GetChar();
                } else{
                    throw new Exception();
                }
            }


        }

        return tokens;
    }


    /**method which returns a NUMBER token and only accepts 0-9 and one . per token */
    private Token ProcessNumber() throws Exception{
        boolean foundPoint = false;
        String currentString = "";
        while (!stringHandler.isDone() && (Character.isDigit(stringHandler.Peek(0)) || stringHandler.Peek(0) == '.')){
            char c = stringHandler.GetChar();
            currentString += c;
            if (c == '.' && foundPoint)
                throw new Exception();
            else if (c == '.')
                foundPoint = true;
        }
        Token number = new Token(Token.tokenType.NUMBER, currentString, linePosition, characterPosition);
        characterPosition += currentString.length();
        return number;

    }


    /**method which returns a WORD token and only accepts letters, digits and _ */
    /**Now modified to check hashMap for known words and makes a token specific to the word. */
    private Token ProcessWord() {
        String currentString = "";
        while (!stringHandler.isDone() && (Character.isLetterOrDigit(stringHandler.Peek(0)) || stringHandler.Peek(0) == '_')){
            char c = stringHandler.GetChar();
            currentString += c;
        }
        Token word;
        if (knownWords.containsKey(currentString.toLowerCase()))
            word = new Token(knownWords.get(currentString.toLowerCase()), linePosition, characterPosition);
        else
            word = new Token(Token.tokenType.WORD, currentString, linePosition, characterPosition);
        /**this line keeps track of how deep i am into a line and update length of WORD */
        characterPosition += currentString.length();
        return word;

    }

    /**Method which reads up through matching " and creates string literal token */
    private Token HandleStringLiteral(){
        String currentString = "";
        stringHandler.GetChar();
        char next = stringHandler.Peek(0);
        while(!stringHandler.isDone() && (next != '"' && stringHandler.Peek(-1) != '\\')){
            currentString+=next;
            stringHandler.GetChar();
            next = stringHandler.Peek(0);
        }
        stringHandler.GetChar();
        return new Token(Token.tokenType.STRINGLITERAL, currentString, linePosition, characterPosition);
    }



    /**Method which deals with backticks */
    private Token HandlePattern(){
        String currentString = "";
        stringHandler.GetChar();
        char next = stringHandler.Peek(0);
        while(!stringHandler.isDone() && (next != '`' && stringHandler.Peek(-1) != '\\')){
            currentString+=next;
            stringHandler.GetChar();
            next = stringHandler.Peek(0);
        }
        stringHandler.GetChar();
        return new Token(Token.tokenType.PATTERN, currentString, linePosition, characterPosition);
    }

    /**Method which uses PeekString two look into the DoubleSymbol hashmap, creates said token */
    private Token ProcessSymbol(){
        String symbol;
        try {
            symbol = stringHandler.PeekString(2);
            if (knownDoubleSymbol.containsKey(symbol)) {
                stringHandler.GetChar();
                return new Token(knownDoubleSymbol.get(symbol), linePosition, characterPosition);
            }
        } catch (StringIndexOutOfBoundsException e){

        }
        symbol = stringHandler.PeekString(1);
        if(knownSymbol.containsKey(symbol)){
            return new Token(knownSymbol.get(symbol), linePosition, characterPosition);
        }
        else
            return null;

    }








}
