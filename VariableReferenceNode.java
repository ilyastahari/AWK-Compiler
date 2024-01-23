package assignment1;

import java.util.Optional;

/**holds the name of the variable, and an optional node that is the expression for the index*/
public class VariableReferenceNode extends Node {

    private String varName;
    private Optional<Node> expression;

    public VariableReferenceNode(String varName, Optional<Node> expression) {
        this.varName = varName;
        this.expression = expression;
    }

    public String getVarName() {
        return varName;
    }

    public Optional<Node> getExpression() {
        return expression;
    }
}
