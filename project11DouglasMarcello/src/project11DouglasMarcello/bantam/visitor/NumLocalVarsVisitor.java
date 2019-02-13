package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.*;

import java.util.HashMap;
import java.util.Map;

public class NumLocalVarsVisitor extends Visitor {

    private Map<String, Integer> localVars;
    private String className;
    private String key;
    private int count;

    public Map<String, Integer> getNumLocalVars(Program ast) {
        className = "";
        key = "";
        count = 0;
        localVars = new HashMap<String, Integer>();
        ast.accept(this); //starts visitation
        return localVars;
    }

    @Override
    //sets the current class name each time we enter a new class
    public Object visit(Class_ node) {
        className = node.getName();
        return super.visit(node);
    }

    @Override
    //sets the current key name each time we enter a new method
    //resets the counter to 0 and then adds the number of parameters
    public Object visit(Method node) {
        key = className + "." + node.getName();
        count = node.getFormalList().getSize();
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        localVars.put(key, count);
        return null;
    }

    @Override
    //adds to the local variable counter each time a new Declaration Statement is made
    public Object visit(DeclStmt node) {
        count += 1;
        return super.visit(node);
    }
}
