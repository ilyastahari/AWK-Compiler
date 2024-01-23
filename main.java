package assignment1;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.Assert.*;


public class main {

    /**
     * this takes the command line parameter of filename
     */
    public static void main(String[] args) throws Exception {
        String awkFile = getAllBytes("code.awk");

        Lexer awkReader = new Lexer(awkFile);
        LinkedList<Token> tokens = awkReader.Lex();

        Parser parse = new Parser(tokens);
        ProgramNode node = parse.Parse();

        Interpreter interpreter = new Interpreter(node, "input.txt");
        interpreter.InterpretProgram(node);
    }


    /**method which calls GetAllBytes and passes result to lexer */
    /**
     * prints out resultant tokens
     */
    private static String getAllBytes(String filename) throws IOException {
        Path myPath = Paths.get(filename);
        return new String(Files.readAllBytes(myPath));
    }

    /**
     * JUnit tests which tests possible outcomes of our inputs
     */
    @Test
    public void test_single_word() throws Exception {
        Lexer forTest = new Lexer("This is a test");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals("test", tokens.get(3).getvalue());
        assertEquals(Token.tokenType.WORD, tokens.get(3).gettype());
    }

    @Test
    public void test_single_number() throws Exception {
        Lexer forTest = new Lexer("Number 3.14 correct");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals("3.14", tokens.get(1).getvalue());
        assertEquals(Token.tokenType.NUMBER, tokens.get(1).gettype());
    }

    @Test
    public void test_multi_word() throws Exception {
        Lexer forTest = new Lexer("Number 3.14 correct" + '\n' +
                "4555 cat" + '\n' + "blue cold");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals("cat", tokens.get(5).getvalue());
        assertEquals(Token.tokenType.WORD, tokens.get(5).gettype());
        assertEquals(Token.tokenType.SEPARATOR, tokens.get(3).gettype());
    }

    @Test
    public void test_multi_number() throws Exception {
        Lexer forTest = new Lexer("Number 3.14 correct" + '\n' +
                "4555 cat" + '\n' + "blue cold");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals("4555", tokens.get(4).getvalue());
        assertEquals(Token.tokenType.NUMBER, tokens.get(4).gettype());
        assertEquals(Token.tokenType.SEPARATOR, tokens.get(3).gettype());
    }

    @Test
    public void test_single_keywords() throws Exception {
        Lexer forTest = new Lexer("for while hello do");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals(Token.tokenType.FOR, tokens.get(0).gettype());
        assertEquals(Token.tokenType.WHILE, tokens.get(1).gettype());
        assertEquals(Token.tokenType.WORD, tokens.get(2).gettype());
    }

    @Test
    public void test_single_stringliteral() throws Exception {
        Lexer forTest = new Lexer("for \"while \" hello do");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals(Token.tokenType.STRINGLITERAL, tokens.get(1).gettype());
        System.out.println(tokens.get(1));
    }

    @Test
    public void test_keyword() throws Exception {
        Lexer forTest = new Lexer("BEGIN and END");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals(Token.tokenType.BEGIN, tokens.get(0).gettype());
        System.out.println(tokens.get(0));
    }

    @Test
    public void test_symbol() throws Exception {
        Lexer forTest = new Lexer("hello , world !");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals(Token.tokenType.COMMA, tokens.get(1).gettype());
        System.out.println(tokens.get(1));
    }

    @Test
    public void test_double_symbol() throws Exception {
        Lexer forTest = new Lexer("0 != 1");
        LinkedList<Token> tokens = forTest.Lex();
        assertEquals(Token.tokenType.NOTEQUALTO, tokens.get(1).gettype());
        System.out.println(tokens.get(1));
    }

    @Test
    public void test_acceptSeparators() throws Exception {
        Lexer forTest = new Lexer("\n \n \n");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        assertEquals(true, parser.AcceptSeperators());

    }

    @Test
    public void test_Parse() throws Exception {
        Lexer forTest = new Lexer("function factorial(f);");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getFunctions().size());

    }

    @Test
    public void test_ParseFunction() throws Exception {
        Lexer forTest = new Lexer("function factorial(f, x, s, g);");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode node = new ProgramNode();
        parser.ParseFunction(node);
        FunctionDefinitionNode func = node.getFunctions().get(0);
        assertEquals("factorial", func.getName());
        assertEquals(4, func.getParameters().size());


    }

    @Test
    public void test_ParseAction() throws Exception {
        Lexer forTest = new Lexer("BEGIN \n BEGIN END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(2, program.getBegin().size());
        assertEquals(1, program.getEnd().size());

    }

    @Test
    public void test_preInc() throws Exception {
        Lexer forTest = new Lexer("BEGIN ++a END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_preIncDollar() throws Exception {
        Lexer forTest = new Lexer("BEGIN ++$b END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_preIncExp() throws Exception {
        Lexer forTest = new Lexer("BEGIN (++d) END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_minusFive() throws Exception {
        Lexer forTest = new Lexer("BEGIN -5 END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_abcExp() throws Exception {
        Lexer forTest = new Lexer("BEGIN`[abc]`END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_eExp() throws Exception {
        Lexer forTest = new Lexer("BEGIN e[++b] END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_dollarSeven() throws Exception {
        Lexer forTest = new Lexer("BEGIN $7 END");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_equals() throws Exception {
        Lexer forTest = new Lexer("a = b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_plusEquals() throws Exception {
        Lexer forTest = new Lexer("a += b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_multEquals() throws Exception {
        Lexer forTest = new Lexer("a *= b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_questionMark() throws Exception {
        Lexer forTest = new Lexer("a ? b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_ANDOR() throws Exception {
        Lexer forTest = new Lexer("a && b, a || b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_notEqual() throws Exception {
        Lexer forTest = new Lexer("a != b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_separator() throws Exception {
        Lexer forTest = new Lexer("a b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_Ops() throws Exception {
        Lexer forTest = new Lexer("a + b, a - b, a * b, a / b, a % b");
        LinkedList<Token> tokens = forTest.Lex();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.Parse();
        assertEquals(1, program.getOther().size());
    }

    @Test
    public void test_BuiltInPrint() throws Exception {
        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, "code.awk");

        InterpreterArrayDataType parametersForPrint = new InterpreterArrayDataType();
        parametersForPrint.getVariables().put("0", new InterpreterDataType("Hello"));
        parametersForPrint.getVariables().put("1", new InterpreterDataType(","));
        parametersForPrint.getVariables().put("2", new InterpreterDataType("World!"));
        HashMap<String, InterpreterDataType> printParams = new HashMap<>();
        printParams.put("0", parametersForPrint);

        BuiltInFunctionDefinitionNode builtInFunctionDefinitionNode = (BuiltInFunctionDefinitionNode) interpreter.getMembers().get("print");
        String result = builtInFunctionDefinitionNode.execute(printParams);

        Assert.assertEquals("Hello , World!", result);
    }



    @Test
    public void test_BuiltInPrintf() throws Exception {
        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, "code.awk");
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        InterpreterArrayDataType parametersForPrintf = new InterpreterArrayDataType();
        parametersForPrintf.getVariables().put("0", new InterpreterDataType("%s %s ---- %s"));
        parametersForPrintf.getVariables().put("1", new InterpreterDataType("Hello"));
        parametersForPrintf.getVariables().put("2", new InterpreterDataType("Big"));
        parametersForPrintf.getVariables().put("3", new InterpreterDataType("World!"));
        HashMap<String, InterpreterDataType> printParams = new HashMap<>();
        printParams.put("0", parametersForPrintf);

        BuiltInFunctionDefinitionNode builtInFunctionDefinitionNode = (BuiltInFunctionDefinitionNode) interpreter.getMembers().get("printf");
        builtInFunctionDefinitionNode.execute(printParams);
        String printedContent = outContent.toString();


        Assert.assertEquals("Hello Big ---- World!", printedContent);
        System.setOut(originalOut);
    }


    @Test
    public void testAssignmentNodeAdd() throws Exception {
        AssignmentNode node = new AssignmentNode(new VariableReferenceNode("x", Optional.empty()),
                new OperationNode(new VariableReferenceNode("x", Optional.empty()), Optional.of(new ConstantNode("4")), OperationNode.OperationType.ADD));

        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, null);
        HashMap<String, InterpreterDataType> hashMap = new HashMap<>();
        hashMap.put("x", new InterpreterDataType("4"));
        InterpreterDataType getData = interpreter.getIDT(node, hashMap);

        Assert.assertEquals("8.0", getData.toString());
    }

    @Test
    public void testAssignmentNodeSubtract() throws Exception {
        AssignmentNode node = new AssignmentNode(new VariableReferenceNode("x", Optional.empty()),
                new OperationNode(new VariableReferenceNode("x", Optional.empty()), Optional.of(new ConstantNode("4")), OperationNode.OperationType.SUBTRACT));

        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, null);
        HashMap<String, InterpreterDataType> hashMap = new HashMap<>();
        hashMap.put("x", new InterpreterDataType("4"));
        InterpreterDataType getData = interpreter.getIDT(node, hashMap);

        Assert.assertEquals("0.0", getData.toString());
    }

    @Test
    public void testAssignmentNodeMultiply() throws Exception {
        AssignmentNode node = new AssignmentNode(new VariableReferenceNode("x", Optional.empty()),
                new OperationNode(new VariableReferenceNode("x", Optional.empty()), Optional.of(new ConstantNode("4")), OperationNode.OperationType.MULTIPLY));

        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, null);
        HashMap<String, InterpreterDataType> hashMap = new HashMap<>();
        hashMap.put("x", new InterpreterDataType("4"));
        InterpreterDataType getData = interpreter.getIDT(node, hashMap);

        Assert.assertEquals("16.0", getData.toString());
    }

    @Test
    public void testAssignmentNodeDivide() throws Exception {
        AssignmentNode node = new AssignmentNode(new VariableReferenceNode("x", Optional.empty()),
                new OperationNode(new VariableReferenceNode("x", Optional.empty()), Optional.of(new ConstantNode("4")), OperationNode.OperationType.DIVIDE));

        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, null);
        HashMap<String, InterpreterDataType> hashMap = new HashMap<>();
        hashMap.put("x", new InterpreterDataType("4"));
        InterpreterDataType getData = interpreter.getIDT(node, hashMap);
        Assert.assertEquals("1.0", getData.toString());
    }

    @Test
    public void testAssignmentNodeModulo() throws Exception {
        AssignmentNode node = new AssignmentNode(new VariableReferenceNode("x", Optional.empty()),
                new OperationNode(new VariableReferenceNode("x", Optional.empty()), Optional.of(new ConstantNode("4")), OperationNode.OperationType.MODULO));

        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, null);
        HashMap<String, InterpreterDataType> hashMap = new HashMap<>();
        hashMap.put("x", new InterpreterDataType("4"));
        InterpreterDataType getData = interpreter.getIDT(node, hashMap);

        Assert.assertEquals("0.0", getData.toString());
    }

    @Test
    public void testAssignmentNodeExponent() throws Exception {
        AssignmentNode node = new AssignmentNode(new VariableReferenceNode("x", Optional.empty()),
                new OperationNode(new VariableReferenceNode("x", Optional.empty()), Optional.of(new ConstantNode("4")), OperationNode.OperationType.EXPONENT));

        ProgramNode programNode = new ProgramNode();
        Interpreter interpreter = new Interpreter(programNode, null);
        HashMap<String, InterpreterDataType> hashMap = new HashMap<>();
        hashMap.put("x", new InterpreterDataType("4"));
        InterpreterDataType getData = interpreter.getIDT(node, hashMap);

        Assert.assertEquals("256.0", getData.toString());
    }


    @Test
    public void testAssignment() throws Exception {
        String awkFile = getAllBytes("code.awk");
        Lexer awkReader = new Lexer(awkFile);
        LinkedList<Token> tokens = awkReader.Lex();

        Parser parse = new Parser(tokens);
        ProgramNode node = parse.Parse();
        Interpreter interpreter = new Interpreter(node, "input.txt");
        interpreter.InterpretProgram(node);
    }
}