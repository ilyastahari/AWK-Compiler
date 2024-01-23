package assignment1;

import java.util.HashMap;

/**we use this as our variable storage class as the interpreter runs.*/
public class InterpreterArrayDataType extends InterpreterDataType {

private HashMap <String, InterpreterDataType> variables;

    /**constructor which creates the hashmap*/
    public InterpreterArrayDataType() {
        this.variables = new HashMap<>();
    }

    public HashMap<String, InterpreterDataType> getVariables() {
        return variables;
    }
}