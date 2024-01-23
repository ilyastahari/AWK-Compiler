package assignment1;

public class AssignmentNode extends StatementNode{
    private Node target;
    private Node expression;
    /**constructor for AssignmentNode */
    public AssignmentNode(Node tar, Node expr){
        this.target = tar;
        this.expression = expr;
    }

    public Node getTarget() {
        return target;
    }

    public Node getExpression() {
        return expression;
    }

    public String toString(){
        return "AssignmentNode{"+ target.toString() + ", " +  expression.toString() + "}";
    }
}
