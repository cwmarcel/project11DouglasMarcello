package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.ASTNode;
import project11DouglasMarcello.bantam.ast.ClassList;
import project11DouglasMarcello.bantam.ast.Program;

import java.util.Map;

public class NumLocalVarsVisitor extends Visitor {
    public Map<String, String> localVars;

    public Map<String, String> getNumLocalVars(Program ast) {
        ast.accept(this); //starts visitation
        return localVars;
    }

    /**
     * Visit a list node of classes
     *
     * @param node the class list node
     * @return result of the visit
     */
    @Override
    public Object visit(ClassList node) {
        for (ASTNode aNode : node)
            aNode.accept(this);
        return null;
    }
}
