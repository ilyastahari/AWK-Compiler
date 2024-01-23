package assignment1;

import java.util.LinkedList;

public class ReturnNode extends StatementNode{

    private Node value;


    public ReturnNode(Node value) {
        this.value = value;
    }

    public Node getValue() {
        return value;
    }
}
