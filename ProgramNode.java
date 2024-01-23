package assignment1;

import java.util.LinkedList;

public class ProgramNode extends Node {
    private LinkedList<BlockNode> begin;
    private LinkedList<BlockNode> end;
    private LinkedList<BlockNode> other;
    private LinkedList<FunctionDefinitionNode> functions;


    public ProgramNode(){
        begin = new LinkedList<BlockNode>();
        end = new LinkedList<BlockNode>();
        other = new LinkedList<BlockNode>();
        functions = new LinkedList<FunctionDefinitionNode>();
    }

    public void addBeginNode(BlockNode node) {
        this.begin.add(node);
    }

    public void addEndNode(BlockNode node) {
        this.end.add(node);
    }

    public void addOtherNode(BlockNode node) {
        this.other.add(node);
    }

    public void addFunctionNode(FunctionDefinitionNode node) {
        this.functions.add(node);
    }

    public LinkedList<BlockNode> getBegin() {
        return begin;
    }

    public LinkedList<BlockNode> getEnd() {
        return end;
    }

    public LinkedList<BlockNode> getOther() {
        return other;
    }

    public LinkedList<FunctionDefinitionNode> getFunctions() {
        return functions;
    }

    @Override
    public String toString() {
        return "ProgramNode{" +
                "begin=" + begin.size() +
                ", end=" + end.size() +
                ", other=" + other.size() +
                ", functions=" + functions.size() +
                '}';
    }
}
