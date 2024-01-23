package assignment1;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode {

    private Function<HashMap<String, InterpreterDataType>, String> execute;
    private boolean isVariadic;

    /**my lambda function*/
    public BuiltInFunctionDefinitionNode(String name,
                                         LinkedList<String> parameters,
                                         Function<HashMap<String, InterpreterDataType>, String> execute,
                                         boolean isVariadic) {
        super(name, parameters, null);
        this.execute = execute;
        this.isVariadic = isVariadic;
    }

    public boolean isVariadic() {
        return isVariadic;
    }

    public String execute(HashMap<String, InterpreterDataType> parameters) {
        return this.execute.apply(parameters);
    }

}
