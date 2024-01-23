package assignment1;

import java.util.LinkedList;


public class ForEachNode extends StatementNode{

    private LinkedList<StatementNode> statements;
    private Node var;
    private Node array;

    public ForEachNode(LinkedList<StatementNode> statements, Node var, Node array) {
        this.statements = statements;
        this.var = var;
        this.array = array;
    }

    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    public Node getVar() {
        return var;
    }

    public Node getArray() {
        return array;
    }
}

