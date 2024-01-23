package assignment1;

import java.util.LinkedList;

public class ForNode extends StatementNode{

    private Node firstCondition;
    private Node secondCondition;
    private Node thirdCondition;
    private LinkedList<StatementNode> statements;


    public ForNode(Node firstCondition, Node secondCondition, Node thirdCondition, LinkedList<StatementNode> statements) {
        this.firstCondition = firstCondition;
        this.secondCondition = secondCondition;
        this.thirdCondition = thirdCondition;
        this.statements = statements;
    }

    public Node getFirstCondition() {
        return firstCondition;
    }

    public Node getSecondCondition() {
        return secondCondition;
    }

    public Node getThirdCondition() {
        return thirdCondition;
    }

    public LinkedList<StatementNode> getStatements() {
        return statements;
    }
}
