package assignment1;


public class Token {

    /**the different token types in the form of enums */
    public enum tokenType {
        NUMBER,
        WORD,
        SEPARATOR,
        WHILE,
        IF,
        DO,
        FOR,
        BREAK,
        CONTINUE,
        ELSE,
        RETURN,
        BEGIN,
        END,
        PRINT,
        PRINTF,
        NEXT,
        IN,
        DELETE,
        GETLINE,
        EXIT,
        NEXTFILE,
        FUNCTION,
        STRINGLITERAL,
        PATTERN,
        OPENCURLYBRACE,
        CLOSECURLYBRACE,
        OPENBRACKET,
        CLOSEBRACKET,
        OPENPARENTHESES,
        CLOSEPARENTHESES,
        DOLLARSIGN,
        TILDE,
        EQUALS,
        LESSTHAN,
        GREATERTHAN,
        EXCLAMATION,
        ADD,
        POWEROF,
        MINUS,
        QUESTION,
        COLON,
        TIMES,
        DIVIDE,
        PERCENT,
        SEMICOLON,
        NEWLINE,
        VERTBAR,
        COMMA,
        GREATEROREQUAL,
        INCEREMENT,
        DECREMENT,
        LESSOREQUAL,
        EQUALTO,
        NOTEQUALTO,
        EXCLUSIVETO,
        MODASSIGN,
        MULTASSIGN,
        DIVASSIGN,
        ADDASSIGN,
        MINUSASSIGN,
        DOESNOTCONTAIN,
        LOGICALAND,
        RIGHTSHIFT,
        LOGICALOR,
    }

    /**string which holds value of the token */
    private String value;

    private tokenType t;
    private int lineNumber;
    private int position;


    /**constructor for tokens that does not store value */
    public Token(tokenType type, int lineNum, int pos) {
        this.t = type;
        this.lineNumber = lineNum;
        this.position = pos;

    }

    /**constructor for tokens that store value */
    public Token(tokenType type, String val, int lineNum, int pos) {

        this.t = type;
        this.value = val;
        this.lineNumber = lineNum;
        this.position = pos;
    }

    /**setters and getters for all relavent token data */
    public String getvalue() {
        return this.value;
    }

    public void setvalue(String value) {
        this.value = value;
    }

    public tokenType gettype() {
        return this.t;
    }

    public void settype(tokenType type) {
        this.t = type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**method to output the token type as well as the string/int */
    @Override
    public String toString() {
        return this.t + "(" + this.value + ") ";
    }
}
