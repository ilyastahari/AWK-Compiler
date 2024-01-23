package assignment1;

import java.util.LinkedList;

public class FunctionDefinitionNode extends Node{

    private String name;
    private LinkedList<String> parameters;
    private LinkedList<StatementNode> statements;

    /**constructor for functionDefinitionNode */
    public FunctionDefinitionNode(String nam, LinkedList<String> par, LinkedList<StatementNode> stat){
        this.name = nam;
        this.parameters = par;
        this.statements = stat;
    }

    public String getName() {
        return name;
    }

    public LinkedList<String> getParameters() {
        return parameters;
    }


    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "FunctionDefinitionNode{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters.size() +
                ", statements=" + statements.size() +
                '}';
    }
}
