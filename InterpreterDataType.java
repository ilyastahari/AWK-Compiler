package assignment1;

/**we use this as our variable storage class as the interpreter runs.*/
public class InterpreterDataType {
    private String value;

    public InterpreterDataType(String value) {
        this.value = value;
    }

    public InterpreterDataType() {
    }

    @Override
    public String toString() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
