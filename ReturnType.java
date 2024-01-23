package assignment1;

public class ReturnType {
    /**returnType class with constructor for enum and string*/
    enum ResultType{
        NORMAL, BREAK, CONTINUE, RETURN, NONE
    }
    private ResultType result;
    private String value;

    public ReturnType(ResultType result, String value) {
        this.result = result;
        this.value = value;
    }

    public ReturnType(ResultType result) {
        this.result = result;
    }


    public String getValue() {
        return value;
    }

    public ResultType getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ReturnType{" +
                "result=" + result +
                ", value='" + value + '\'' +
                '}';
    }
}
