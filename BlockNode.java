package assignment1;

import java.util.LinkedList;
import java.util.Optional;

public class BlockNode extends Node{

    private LinkedList<StatementNode> statements;
    private Optional<Node>  condition;


    public BlockNode(){
        this.statements = new LinkedList<>();
        this.condition = Optional.empty();
    }


    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    public Optional<Node> getCondition() {
        return condition;
    }

    public void setCondition(Optional<Node> blockCondition) {
       this.condition = blockCondition;
    }

    public void addStatementNode( StatementNode node) {
        statements.add(node);
    }


    @Override
    public String toString() {
        return "BlockNode{" +
                "statements=" + statements.size() +
                ", condition=" + condition.toString() +
                '}';
    }
}
