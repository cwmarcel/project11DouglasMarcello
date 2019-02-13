package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.*;

public class MainMainVisitor extends Visitor {
    private boolean hasMain;

    public boolean hasMain(Program ast) {
        hasMain = false;
        ast.accept(this); //starts visitation
        return hasMain;
    }

    //only visits a class's member list if it has the name Main
    //should it return after we find Main?
    @Override
    public Object visit(Class_ node) {
        if (node.getName().equals("Main")) {
            node.getMemberList().accept(this);
            return null;
        }
        return null;
    }

    //checks if the method has name main, return type void, and no parameters
    //doesn't visit below the method level because we don't need to.
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
