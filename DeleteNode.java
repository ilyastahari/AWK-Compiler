package assignment1;


public class DeleteNode extends StatementNode {

    public Node getToBeDeleted() {
        return toBeDeleted;
    }

    private Node toBeDeleted;


    public DeleteNode(Node toBeDeleted) {
        this.toBeDeleted = toBeDeleted;

    }
}
