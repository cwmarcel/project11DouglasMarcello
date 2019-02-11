package project11DouglasMarcello.bantam.visitor;

import project11DouglasMarcello.bantam.ast.Program;

import java.util.Map;

public class StringConstantsVisitor {

    public Map<String, String> stringConstants;

    public Map<String, String> getStringConstants(Program ast) {
        ast.accept(this); //starts visitation
        return stringConstants;
    }
}
