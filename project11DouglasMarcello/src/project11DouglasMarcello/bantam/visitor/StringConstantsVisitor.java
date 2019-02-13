package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.ConstStringExpr;
import project11DouglasMarcello.bantam.ast.Program;

import java.util.HashMap;
import java.util.Map;

public class StringConstantsVisitor extends Visitor{

    private Map<String, String> stringConstants;
    private int count;

    /**
     * Visits the AST and creates a Map with:
     * Key: String Constant
     * Value: A Unique ID for the String Constant of Form (StringConst_0, StringConst_1, StringConst_2, etc.)
     *
     * @param ast The AST to traverse
     * @return Map a Map of the string constants in the AST
     */
    public Map<String, String> getStringConstants(Program ast) {
        stringConstants = new HashMap<String, String>();
        count = 0;
        ast.accept(this); //starts visitation
        return stringConstants;
    }

    /**
     * Visit a string constant expression node
     *
     * increments the counter for each string constant encountered.
     *
     * @param node the string constant expression node
     * @return result of the visit
     */
    @Override
    public Object visit(ConstStringExpr node) {
        stringConstants.put( node.getConstant(), "StringConst_"+count );
        count++;
        return null;
    }
}
