package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.*;

public class MainMainVisitor extends Visitor {
    private boolean hasMain;

    /**
     * Returns a boolean value, true if the program has a Main class with a
     * main method with return type void and no parameters, false otherwise.
     *
     * @param ast The AST to check
     * @return boolean has Main.main with return type void and no params.
     */
    public boolean hasMain(Program ast) {
        hasMain = false;
        ast.accept(this); //starts visitation
        return hasMain;
    }


    /**
     * Visit a class node
     * only visits a class's member list if it has the name Main
     *
     * @param node the class node
     * @return result of the visit
     */
    @Override
    public Object visit(Class_ node) {
        if (node.getName().equals("Main")) {
            node.getMemberList().accept(this);
            return null;
        }
        return null;
    }

    /**
     * Visit a method node
     * checks if the method has name main, return type void, and no parameters
     * doesn't visit below the method level because we don't need to.
     *
     * @param node the method node
     * @return result of the visit
     */
    //
    @Override
    public Object visit(Method node) {
        if (node.getReturnType().equals("void") &&
                node.getFormalList().getSize() == 0 &&
                node.getName().equals("main")) {
            hasMain = true;
        }
        return null;
    }
}
