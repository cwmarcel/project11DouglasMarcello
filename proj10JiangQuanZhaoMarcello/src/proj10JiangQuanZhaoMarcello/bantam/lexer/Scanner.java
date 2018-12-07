/*
 * File: Scanner.java
 * F18 CS361 Project 10
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 11/17/2018
 * This file contains the Scanner class, taking in a file,
 * splitting it into proper tokens, reporting bugs when necessary.
 */

package proj10JiangQuanZhaoMarcello.bantam.lexer;

import proj10JiangQuanZhaoMarcello.bantam.util.Error;
import proj10JiangQuanZhaoMarcello.bantam.util.ErrorHandler;
import proj10JiangQuanZhaoMarcello.bantam.util.CompilationException;
import proj10JiangQuanZhaoMarcello.bantam.lexer.Token.Kind;
import java.util.List;

/**
 * The Scanner class taking in a file, splitting it into proper tokens,
 * reporting bugs when necessary.
 *
 * @author liweijiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 */
public class Scanner {
    /**
     * the SourceFile object that reads a file letter by letter
     */
    private SourceFile sourceFile;
    /**
     * the ErrorHandler object that stores a list of errors
     */
    private ErrorHandler errorHandler;
    /**
     * the current character being scanned
     */
    private char currentChar;
    /**
     * a field storing the character that the sourceFile object currently pointing to,
     * needed when we want to look ahead by 1 character for special characters with length 2
     */
    private char sourceFileCurrentChar;
    /**
     * the string constant field needed for generating a string constant token
     */
    private String stringConstant;
    /**
     * an integer storing the first line number of the current token.
     */
    private int position;

    /**
     * A constructor of the Scanner class.
     *
     * @param filename a filename as a String
     * @param handler an ErrorHandler object
     */
    public Scanner(String filename, ErrorHandler handler) {
        this.errorHandler = handler;
        this.sourceFile = new SourceFile(filename);
        this.currentChar = ' ';
    }

    /**
     * Helper method to register an error to the error handler.
     *
     * @param kind the kind of the error
     * @param message the error message
     */
    private void registerError(Error.Kind kind, String message) {
        this.errorHandler.register(kind, this.sourceFile.getFilename(), this.position, message);
    }

    /**
     * Helper method to assign the current character to currentChar,
     * to get the next character and assign it to currentChar.
     *
     * @return the next token obtained from the sourceFile object
     */
    private char getNextChar() {
        this.sourceFileCurrentChar = this.sourceFile.getNextChar();
        return this.sourceFileCurrentChar;
    }

    /**
     * Helper method to create a new Token.
     * Gets the next character as specified.
     *
     * @param kind the Kind of the Token
     * @param spelling the spelling of the Token as a String
     * @param getNextChar a boolean value indicating whether to get the next character after creating the new Token
     * @return the new Token object created
     */
    private Token createNewToken(Token.Kind kind, String spelling, boolean getNextChar) {
        Token newToken = new Token(kind, spelling, this.position);
        // get the next character as specified
        if (getNextChar) {
            this.currentChar = this.getNextChar();
        }
        // rewind to the previous character
        else {
            this.currentChar = this.sourceFileCurrentChar;
        }
        return newToken;
    }

    /**
     * Helper method to create an ERROR Token and register this error to the error handler.
     *
     * @param message error message
     * @param spelling the spelling of the error token
     * @param GetNextChar a boolean value indicating whether to get the next character after creating the ERROR Token
     * @return the ERROR Token constructed
     */
    private Token createAndRegisterErrorToken(String message, String spelling, boolean GetNextChar) {
        this.registerError(Error.Kind.LEX_ERROR, message);
        return this.createNewToken(Kind.ERROR, message + ": " + spelling, GetNextChar);
    }

    /**
     * Helper method to construct a Token for Special Characters that has another special character with the same first letter.
     * For example, // (line comment) and / (divide), >= (greater or equal to) and > (greater than)
     *
     * @param firstChar the first character of the special character(s)
     * @param secondChar the second character of the special character(s)
     * @param kind_long the kind of the special character(s) with length two, with the first character equals to firstChar
     *              and the second character equals to secondChar
     * @param kind_short the kind of the special character(s) with length one, with the first character equals to firstChar
     * @return the Token constructed
     */
    private Token constructSpecialCharWithSameFirstLetterToken(char firstChar, char secondChar, Token.Kind kind_long, Token.Kind kind_short) {
        if (this.getNextChar() == secondChar) {
            return this.createNewToken(kind_long, Character.toString(firstChar) + Character.toString(secondChar), true);
        }
        else {
            return this.createNewToken(kind_short, Character.toString(firstChar), false);
        }
    }

    /**
     * Helper method to construct an integer constant Token.
     * Creates an INTCONST Token if the constructed integer constant token is legal.
     * Creates an Error Token (and registers the error) if the constructed integer constant token is too large (> 2^32 - 1).
     *
     * @return the Token constructed
     */
    private Token constructIntConstantToken() {
        String integerConstant = Character.toString(this.currentChar);
        this.currentChar = this.getNextChar();
        while (Character.isDigit(this.currentChar)) {
            integerConstant += this.currentChar;
            this.currentChar = this.getNextChar();
        }
        try {
            Integer.parseInt(integerConstant);
        } catch (Exception e) {
            return createAndRegisterErrorToken("Integer Constant Too Large", integerConstant, false);
        }
        return this.createNewToken(Kind.INTCONST, integerConstant, false);
    }

    /**
     * Helper method to construct an IDENTIFIER token.
     * An identifier is any non-keyword that starts with an uppercase or lowercase letter
     * and is followed by a sequence of letters (upper or lowercase), digits, and underscore '_'.
     *
     * @return the Token constructed
     */
    private Token constructIdentifierToken() {
        String identifier = Character.toString(this.currentChar);
        this.currentChar = this.getNextChar();
        while (Character.isLetterOrDigit(this.currentChar) || this.currentChar == '_') {
            identifier += this.currentChar;
            this.currentChar = this.getNextChar();
        }
        return this.createNewToken(Kind.IDENTIFIER, identifier, false);
    }

    /**
     * Helper method to construct a line comment token.
     * A line comment Token contains // and everything after it within the same line
     *
     * @return the COMMENT Token constructed
     */
    private Token constructLineCommentToken() {
        String lineComment = "//";
        this.currentChar = this.getNextChar();

        while (this.currentChar != SourceFile.eol && this.currentChar != SourceFile.eof) {
            lineComment += this.currentChar;
            this.currentChar = this.getNextChar();
        }
        return this.createNewToken(Kind.COMMENT, lineComment, true);
    }

    /**
     * Helper method to construct a block comment token.
     * Creates a COMMENT Token if the block comment is properly terminated.
     * Creates an ERROR Token (and registers the error) if the block comment is not properly terminated.
     *
     * @return the COMMENT Token constructed
     */
    private Token constructBlockCommentToken() {
        String blockComment = "/*";
        this.currentChar = this.getNextChar();
        while (!(this.currentChar == '*' && this.getNextChar() == '/')) {
            blockComment += this.currentChar;
            if (this.getNextChar() == SourceFile.eof) {
                return createAndRegisterErrorToken("Unterminated Block Comment", blockComment, true);
            }
            this.currentChar = this.sourceFileCurrentChar;
        }
        blockComment += "*/";
        return this.createNewToken(Kind.COMMENT, blockComment, true);
    }

    /**
     * Helper method to construct a string constant Token.
     * Creates a STRCONST Token if it is within 5000 characters, only contains the following special symbols:
     * \n (newline), \t (tab), \" (double quote), \\ (backslash), and \f (form feed), and if it is properly terminated.
     * Creates an unterminated string ERROR Token if the string is not properly terminated.
     * Creates a contains illegal escape characters ERROR Token if the string contains illegal escape characters.
     * Creates a string too long ERROR Token if the string exceeds 5000 characters.
     *
     * @return the STRCONST Token constructed
     */
    private Token constructStringConstantToken() {
        this.stringConstant = Character.toString(this.currentChar);
        boolean containIllegalEscapeChar = false;
        this.currentChar = this.getNextChar();

        while (!(this.currentChar == '\"' && !this.isEscaped())) {
            if (this.currentChar == SourceFile.eol) {
                return createAndRegisterErrorToken("Unterminated String Constant", this.stringConstant, true);
            }
            else if (this.currentChar == SourceFile.eof) {
                return createAndRegisterErrorToken("Unterminated String Constant", this.stringConstant, false);
            }
            else if (this.currentChar == '\\' ) {
                if (!this.isLegalEscapeChars()) {
                    containIllegalEscapeChar = true;
                }
            }
            else {
                this.stringConstant += this.currentChar;
                this.currentChar = this.getNextChar();
            }
        }
        this.stringConstant += "\"";

        if (containIllegalEscapeChar) {
            return createAndRegisterErrorToken("String Contains Illegal Escape Characters", this.stringConstant, true);
        }
        if (this.stringConstant.length() > 5002) {
            return createAndRegisterErrorToken("String Exceeds 5000 Characters", this.stringConstant, true);
        }
        return this.createNewToken(Kind.STRCONST, this.stringConstant, true);
    }

    /**
     * Helper method to check whether a sequence of characters starting with / are legal escape characters.
     * And appends the characters being checked to the string constant along the way of examining them.
     *
     * @return  true if they are legal escape characters;
     *          false if they are not legal escape characters
     */
    private boolean isLegalEscapeChars() {
        int countBackslash = 0;
        while (this.currentChar == '\\') {
            countBackslash++;
            this.stringConstant += this.currentChar;
            this.currentChar = this.getNextChar();
        }
        if (countBackslash%2 == 0) return true;
        return  (this.currentChar == 'n') ||
                (this.currentChar == 't') ||
                (this.currentChar == '\"') ||
                (this.currentChar == 'f');
    }

    /**
     * Helper method to determine whether a given character has been escaped in the given source string
     *
     * @return a boolean indicating whether or not the character has been escaped
     */
    private boolean isEscaped() {
        int countBackslash = 0;
        int tmpIndex = this.stringConstant.length() - 1;

        // count the number of consecutive backslashes before the given character
        while (tmpIndex >= 0 && this.stringConstant.charAt(tmpIndex) == '\\') {
            countBackslash++;
            tmpIndex--;
        }
        // if the number of backslashes is odd, then the character is escaped
        if (countBackslash%2 == 1) return true;
        return false;
    }

    /**
     * Iterates through the file and returns the next Token each time being called.
     * When it reaches the end of the file, any calls to scan() result in a Token of kind EOF.
     *
     * @return the next Token
     */
    public Token scan() {
        try {

            // ignore spaces, tabs, or newlines
            while (this.currentChar == ' ' || this.currentChar == '\t' || this.currentChar == SourceFile.eol) {
                this.currentChar = this.getNextChar();
            }

            // store the first line number of the current token.
            this.position = this.sourceFile.getCurrentLineNumber();

            // -------------------- EOF
            if (this.currentChar == SourceFile.eof) {
                return this.createNewToken(Token.Kind.EOF, "End of File", false);
            }

            // -------------------- Line Comment
            else if (this.currentChar == '/' && this.getNextChar() == '/') {
                return this.constructLineCommentToken();
            }

            // -------------------- Block Comment
            else if (this.currentChar == '/' && this.sourceFileCurrentChar == '*') {
                return this.constructBlockCommentToken();
            }

            // -------------------- Identifiers
            else if (Character.isLetter(this.currentChar)) {
                return this.constructIdentifierToken();
            }

            // -------------------- Integer constants
            else if (Character.isDigit(this.currentChar)) {
                return this.constructIntConstantToken();
            }

            // ----------------- String constants
            else if(this.currentChar == '\"') {
                return this.constructStringConstantToken();
            }

            // -------------------- Special Characters
            // &&
            else if (this.currentChar == '&' && this.getNextChar() == '&') {
                return this.createNewToken(Token.Kind.BINARYLOGIC, "&&", true);
            }
            // ||
            else if (this.currentChar == '|' && this.getNextChar() == '|') {
                return this.createNewToken(Token.Kind.BINARYLOGIC, "||", true);
            }
            // -- / -
            else if (this.currentChar == '-') {
                return this.constructSpecialCharWithSameFirstLetterToken('-', '-', Token.Kind.UNARYDECR, Token.Kind.PLUSMINUS);
            }
            // ++ / +
            else if (this.currentChar == '+') {
                return this.constructSpecialCharWithSameFirstLetterToken('+', '+', Token.Kind.UNARYINCR, Token.Kind.PLUSMINUS);
            }
            // != / !
            else if (this.currentChar == '!') {
                return this.constructSpecialCharWithSameFirstLetterToken('!', '=', Token.Kind.COMPARE, Token.Kind.UNARYNOT);
            }
            // == / =
            else if (this.currentChar == '=') {
                return this.constructSpecialCharWithSameFirstLetterToken('=', '=', Token.Kind.COMPARE, Token.Kind.ASSIGN);
            }
            // <= / <
            else if (this.currentChar == '<') {
                return this.constructSpecialCharWithSameFirstLetterToken('<', '=', Token.Kind.COMPARE, Token.Kind.COMPARE);
            }
            // >= / >
            else if (this.currentChar == '>') {
                return this.constructSpecialCharWithSameFirstLetterToken('>', '=', Token.Kind.COMPARE, Token.Kind.COMPARE);
            }
            // *
            else if (this.currentChar == '*') {
                return this.createNewToken(Token.Kind.MULDIV, "*", true);
            }
            // /
            else if (this.currentChar == '/') {
                return this.createNewToken(Token.Kind.MULDIV, "/", false);
            }
            // %
            else if (this.currentChar == '%') {
                return this.createNewToken(Token.Kind.MULDIV, "%", true);
            }
            // {
            else if (this.currentChar == '{') {
                return this.createNewToken(Token.Kind.LCURLY, "{", true);
            }
            // }
            else if (this.currentChar == '}') {
                return this.createNewToken(Token.Kind.RCURLY, "}", true);
            }
            // [
            else if (this.currentChar == '[') {
                return this.createNewToken(Token.Kind.LBRACKET, "[", true);
            }
            // ]
            else if (this.currentChar == ']') {
                return this.createNewToken(Token.Kind.RBRACKET, "]", true);
            }
            // (
            else if (this.currentChar == '(') {
                return this.createNewToken(Token.Kind.LPAREN, "(", true);
            }
            // )
            else if (this.currentChar == ')') {
                return this.createNewToken(Token.Kind.RPAREN, ")", true);
            }
            // .
            else if (this.currentChar == '.') {
                return this.createNewToken(Token.Kind.DOT, ".", true);
            }
            // ,
            else if (this.currentChar == ',') {
                return this.createNewToken(Token.Kind.COMMA, ",", true);
            }
            // ;
            else if (this.currentChar == ';') {
                return this.createNewToken(Token.Kind.SEMICOLON, ";", true);
            }
            // :
            else if (this.currentChar == ':') {
                return this.createNewToken(Token.Kind.COLON, ":", true);
            }
            // Illegal Special Characters
            else {
                return createAndRegisterErrorToken("Illegal Special Character", Character.toString(this.currentChar), true);
            }
        }
        // encounter any compilation error
        catch (CompilationException error) {
            System.out.println("Compilation Error!");
            return null;
        }
    }

    /**
     * Scans the file and returns a String containing all tokens of the given file, including the error tokens.
     *
     * @return a String containing all tokens of the given file, each on a separate line, including the error tokens.
     */
    public String scanFile() {
        Token curToken = null; // the current token
        String tokenResult = ""; // a String a String containing all tokens of the given file

        // scan the file from the beginning to the end of the file
        while (curToken == null || (curToken.getKind() != Token.Kind.EOF)) {
            curToken = this.scan();
            tokenResult += curToken.toString();
        }
        return tokenResult;
    }

    /**
     * Main function for testing the Scanner class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // command line arguments we used for testing purpose
        // test/test1.java test/test2.java test/test3.java test/test4.java test/badtest.java
        for (int i=0; i < args.length; i++) {
            String filename = args[i];
            System.out.println("\n------------------ " + filename + " ------------------" + "\n");
            try {
                ErrorHandler handler = new ErrorHandler();
                Scanner scanner = new Scanner(filename, handler);
                System.out.println(scanner.scanFile());
                List<Error> errorList = handler.getErrorList();
                for (Error err: errorList){
                    System.out.println(err.toString());
                }
                if (errorList.size()==0){
                    System.out.println("Scanning was successful!");
                }
                else if (errorList.size()==1){
                    System.out.println("\n1 illegal token was found.");
                }
                else{
                    System.out.println("\n" + errorList.size() + " illegal tokens were found.");
                }
            }
            catch (Exception e) {
                System.out.println("ERROR: Scanning " + filename + " failed!");
            }
        }
    }
}
