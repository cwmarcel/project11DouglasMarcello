package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.*;

import java.util.HashMap;
import java.util.Map;

public class NumLocalVarsVisitor extends Visitor {

    private Map<String, Integer> localVars;
    private String className;
    private String key;
    private int count;

    /**
     * Returns a Map of the number of local variables and parameters present in
     * the AST, on a method level.
     * Key - Class.Method
     * Value - Number of local variables and parameters present (integer)
     *
     *@param ast the AST node
     *@return Map the map of local variables
     */
    public Map<String, Integer> getNumLocalVars(Program ast) {
        className = "";
        key = "";
        count = 0;
        localVars = new HashMap<String, Integer>();
        ast.accept(this); //starts visitation
        return localVars;
    }


    /**
     * Visit a class node
     * sets the current class name each time the visitor enters a new class
     *
     * @param node the class node
     * @return result of the visit
     */
    @Override
    public Object visit(Class_ node) {
        className = node.getName();
        return super.visit(node);
    }


    /**
     * Visit a method node
     *
     * sets the current key name each time we enter a new method
     * resets the counter to 0 and then adds the number of parameters
     *
     * @param node the method node
     * @return result of the visit
     */
    @Override
    public Object visit(Method node) {
        key = className + "." + node.getName();
        count = node.getFormalList().getSize();
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        localVars.put(key, count);
        return null;
    }


    /**
     * Visit a declaration statement node
     * adds to the local variable counter each time a new Declaration Statement is made
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    @Override
    public Object visit(DeclStmt node) {
        count += 1;
        return super.visit(node);
    }
}
