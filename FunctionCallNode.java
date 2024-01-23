package assignment1;

import java.util.LinkedList;

public class FunctionCallNode extends StatementNode {

    private String name;
    private LinkedList<Node> parameters;



    public FunctionCallNode(String nam, LinkedList<Node> par) {
        this.name = nam;
        this.parameters = par;

    }

    public String getName() {
        return name;
    }

    public LinkedList<Node> getParameters() {
        return parameters;
    }
}