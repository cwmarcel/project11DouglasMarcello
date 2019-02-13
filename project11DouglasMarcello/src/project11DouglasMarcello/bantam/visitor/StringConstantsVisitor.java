package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.ConstStringExpr;
import project11DouglasMarcello.bantam.ast.Program;

import java.util.HashMap;
import java.util.Map;

public class StringConstantsVisitor extends Visitor{

    private Map<String, String> stringConstants;
    private int count;

    public Map<String, String> getStringConstants(Program ast) {
        stringConstants = new HashMap<String, String>();
        count = 0;
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
