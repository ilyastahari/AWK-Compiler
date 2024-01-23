package assignment1;

import java.util.Optional;

/**contains a left, right and list of possible operations */
public class OperationNode extends StatementNode{

    enum OperationType{
        EQ, NE, LT, LE, GT, GE, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR, PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG, IN,
        EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, CONCATENATION
    }

    private Node left;
    private Optional<Node> right;
    private OperationType operation;

    public OperationNode(Node left, Optional<Node> right, OperationType operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    public OperationType getOperation() {
        return operation;
    }

    public Node getLeft() {
        return left;
    }

    public Optional<Node> getRight() {
        return right;
    }
}
