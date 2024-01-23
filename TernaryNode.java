package assignment1;

public class TernaryNode extends Node{
    private Node expression;
    private Node trueCase;
    private Node falseCase;

    public TernaryNode(Node expr, Node tru, Node fal){
        this.expression = expr;
        this.trueCase = tru;
        this.falseCase = fal;
    }
    @Override
    public String toString(){
        return "TernaryNode{" + this.expression.toString() +", " + this.trueCase.toString()
                +" , "  + this.falseCase.toString() + "}";
    }

    public Node getExpression() {
        return expression;
    }

    public Node getTrueCase() {
        return trueCase;
    }

    public Node getFalseCase() {
        return falseCase;
    }
}
