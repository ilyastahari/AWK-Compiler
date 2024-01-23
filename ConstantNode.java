package assignment1;

public class ConstantNode extends Node {

    private String value;

    public ConstantNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
