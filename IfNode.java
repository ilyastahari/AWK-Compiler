package assignment1;

import java.util.LinkedList;
import java.util.Optional;

public class IfNode extends StatementNode {

        private Optional<OperationNode> condition;
        private LinkedList<StatementNode> statements;
        private Optional<StatementNode> next;

        public IfNode(Optional<OperationNode> operation, LinkedList<StatementNode> statements, Optional<StatementNode> next) {
            this.condition = operation;
            this.statements = statements;
            this.next = next;

        }

    public Optional<OperationNode> getCondition() {
        return condition;
    }

    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    public Optional<StatementNode> getNext() {
        return next;
    }
}
