package assignment1;

import java.util.LinkedList;


public class WhileNode extends StatementNode {

    private Node condition;
    private LinkedList<StatementNode> statements;

    public WhileNode(Node condition, LinkedList<StatementNode> statements) {
        this.condition = condition;
        this.statements = statements;
    }

    public Node getCondition() {
        return condition;
    }

    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return condition.toString() + statements.toString();
    }
}