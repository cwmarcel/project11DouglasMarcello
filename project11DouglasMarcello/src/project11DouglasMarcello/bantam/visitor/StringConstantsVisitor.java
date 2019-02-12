package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.ConstStringExpr;
import project11DouglasMarcello.bantam.ast.Program;

import java.util.Map;

public class StringConstantsVisitor extends Visitor{

    private Map<String, String> stringConstants;
    private int count = 0;

    public Map<String, String> getStringConstants(Program ast) {
        ast.accept(this); //starts visitation
        return stringConstants;
    }

    @Override
    //add the string constants
    public Object visit(ConstStringExpr node) {
        stringConstants.put( node.getConstant(), "StringConst_"+count );
        count++;
        return null;
    }
}
