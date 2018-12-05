/*
 * Authors: Haoyu Song and Dale Skrien
 * Date: Spring and Summer, 2018
 *
 * In the grammar below, the variables are enclosed in angle brackets.
 * The notation "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * All other symbols in the rules are terminals.
 * EMPTY indicates a rule with an empty right hand side.
 * All other terminal symbols that are in all caps correspond to keywords.
 */
package proj10JiangQuanZhaoMarcelloCoyne.bantam.parser;

import proj10JiangQuanZhaoMarcelloCoyne.Scanner.*;
import proj10JiangQuanZhaoMarcelloCoyne.Scanner.Error;
import proj10JiangQuanZhaoMarcelloCoyne.bantam.ast.*;
import proj10JiangQuanZhaoMarcelloCoyne.bantam.treedrawer.Drawer;
import proj10JiangQuanZhaoMarcelloCoyne.bantam.visitor.Visitor;

import javax.management.MBeanAttributeInfo;
import java.util.List;

import static proj10JiangQuanZhaoMarcelloCoyne.Scanner.Token.Kind.*;

/**
 * This class constructs an AST from a legal Bantam Java program.  If the
 * program is illegal, then one or more error messages are displayed.
 */
public class Parser
{
    // instance variables
    private Scanner scanner;
    private String fileName;
    private Token currentToken; // the lookahead token
    private ErrorHandler errorHandler;

    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }


    /**
     * parse the given file and return the root node of the AST
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) {
        this.fileName = filename;
        this.scanner = new Scanner(filename, this.errorHandler);
        this.currentToken = scanner.scan();
        return parseProgram();
    }


    /*
     * <Program> ::= <Class> | <Class> <Program>
     */
    private Program parseProgram() {
        System.out.println("parseProgram");
        int position = currentToken.position;
        ClassList classList = new ClassList(position);

        while (currentToken.kind != EOF) {
                Class_ aClass = parseClass();
                classList.addElement(aClass);
        }

        return new Program(position, classList);
    }


    /*
	 * <Class> ::= CLASS <Identifier> <ExtendsClause> { <MemberList> }
     * <ExtendsClause> ::= EXTENDS <Identifier> | EMPTY
     * <MemberList> ::= EMPTY | <Member> <MemberList>
     */
    private Class_ parseClass() {
        System.out.println("parseClass");
        int position = this.currentToken.position;

        this.currentToken = this.scanner.scan();    // <Identifier>
        String name = parseIdentifier();
        String parentName;

        if (this.currentToken.kind == EXTENDS){     // if there is <ExtendsClause>
            this.currentToken = this.scanner.scan();
            parentName = parseIdentifier();
        }
        else {                                      // if <ExtendsClause> is empty, currentToken: "{"
            parentName = null;
        }
        this.currentToken = this.scanner.scan();    // <MemberList>
        MemberList memberList = new MemberList(this.currentToken.position);
        if (!this.currentToken.spelling.equals("}")){
            while (!this.currentToken.spelling.equals("}")){
                System.out.println("parseClassCheck: "+currentToken.spelling);
                Member aMember = parseMember();
                memberList.addElement(aMember);
            }
        }

        this.currentToken = this.scanner.scan();

        return new Class_(position, this.fileName, name, parentName, memberList);
    }


    /* Fields and Methods
     * <Member> ::= <Field> | <Method>
     * <Method> ::= <Type> <Identifier> ( <Parameters> ) <Block>
     * <Field> ::= <Type> <Identifier> <InitialValue> ;
     * <InitialValue> ::= EMPTY | = <Expression>
     */
     private Member parseMember() {
         System.out.println("parseMember---");
         int position = this.currentToken.position;

         String type = parseType();
         System.out.println("type "+type);
         String identifier = parseIdentifier();
         System.out.println("id "+identifier);
         // if <Method>
         if (this.currentToken.spelling.equals("(")){
             System.out.println("parseMember 1 : " + currentToken.spelling);
             this.currentToken = this.scanner.scan();
             FormalList parameter = parseParameters();
             System.out.println("parseMember 2 : " + currentToken.spelling);
             if (!this.currentToken.spelling.equals(")")){
                 // TODO-ILLEGAL
             }
             this.currentToken = this.scanner.scan();   // <Block>
             Stmt stmt = parseBlock();
             StmtList stmtList = new StmtList(this.currentToken.position);
             stmtList.addElement(stmt);
             return new Method(position, type, identifier, parameter, stmtList);
         }
         // if <Field>
         else{
             System.out.println("parseField");
             System.out.println("Field: " + currentToken.spelling);
             // if there is an initial value
             if (this.currentToken.spelling.equals("=")){
                 this.currentToken = this.scanner.scan();       // <InitialValue>
                 System.out.println("Parse Member 1: " + currentToken.spelling);
                 Expr init = parseExpression();
                 System.out.println("2: " + currentToken.spelling);
                 if (!this.currentToken.equals(";")){
                     // TODO- ERROR HANDLER
                 }
                 int pos = this.currentToken.position;
                 this.currentToken = this.scanner.scan();
                 return new Field(pos, type, identifier, init);
             }
             else if (this.currentToken.spelling.equals(";")){
                 int pos = this.currentToken.position;
                 this.currentToken = this.scanner.scan();
                 return new Field(pos, type, identifier, null);
             }
             else{
                 // TODO - ERROR HANDLER
             }
         }

         this.currentToken = this.scanner.scan();

         return null;
     }


    //-----------------------------------

    /* Statements
     *  <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <DeclStmt>
     *              | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
     */
     private Stmt parseStatement() {
         System.out.println("parseStatement---");
         System.out.println("parseStatement:" + currentToken.spelling);
            Stmt stmt;

            switch (currentToken.kind) {
                case IF:
                    stmt = parseIf();
                    break;
                case LCURLY:
                    stmt = parseBlock();
                    break;
                case VAR:
                    stmt = parseDeclStmt();
                    break;
                case RETURN:
                    stmt = parseReturn();
                    break;
                case FOR:
                    stmt = parseFor();
                    break;
                case WHILE:
                    stmt = parseWhile();
                    break;
                case BREAK:
                    stmt = parseBreak();
                    break;
                default:
                    System.out.println("ExprStmt");
                    stmt = parseExpressionStmt();
            }

            return stmt;
    }


    /*
     * <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
     */
    private Stmt parseWhile() {
        System.out.println("parseWhile---");
        Stmt stmt;
        int position = this.currentToken.position;
        System.out.println("parseWhile1: " + currentToken.spelling);
        this.currentToken = scanner.scan();     // left paren
        System.out.println("parseWhile2: " + currentToken.spelling);
        this.currentToken = scanner.scan();     // <Expression>
        System.out.println("parseWhile3: " + currentToken.spelling);
        Expr preExpr = parseExpression();
        System.out.println("parseWhile4: " + currentToken.spelling);
        this.currentToken = scanner.scan();     // <Stmt>
        System.out.println("parseWhile5: " + currentToken.spelling);
        Stmt bodyStmt = parseStatement();

        stmt = new WhileStmt(position, preExpr, bodyStmt);
        return stmt;

    }


    /*
     * <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
     */
    private Stmt parseReturn() {
        Stmt stmt;
        int position = this.currentToken.position;

        this.currentToken = this.scanner.scan();    // <Expression> or ;

        // if return expression is empty, set expression to null
        if (this.currentToken.spelling.equals(";")){
            stmt = new ReturnStmt(position, null);
        }
        else{
            Expr returnExpr = parseExpression();
            stmt = new ReturnStmt(position, returnExpr);
        }

        this.currentToken = this.scanner.scan();

        return stmt;
    }


    /*
     * BreakStmt> ::= BREAK ;
     */
    private Stmt parseBreak() {
        int position = this.currentToken.position;
        this.currentToken = this.scanner.scan();
        return new BreakStmt(position);
    }


    /*
     * <ExpressionStmt> ::= <Expression> ;
     */
    private ExprStmt parseExpressionStmt() {
        System.out.println("parseExpressionStmt---");
        int position = this.currentToken.position;
        System.out.println("parseExpressionStmt 1: " + currentToken.spelling);
        Expr expr = parseExpression();
        if (this.currentToken.equals(";")){
            // TODO COMMA EXPECTED

        }
        System.out.println("parseExpressionStmt 2: " + currentToken.spelling);
        this.currentToken = this.scanner.scan();
        return new ExprStmt(position, expr);

    }


    /*
     * <DeclStmt> ::= VAR <Identifier> = <Expression> ;
     * every local variable must be initialized
     */
    private Stmt parseDeclStmt() {
        int position = this.currentToken.position;
        this.currentToken = this.scanner.scan();        // <Identifier>
        String name = this.currentToken.spelling;
        this.currentToken = scanner.scan();     // "="
        this.currentToken = scanner.scan();     // <Expression>
        Expr expr = parseExpression();

        this.currentToken = this.scanner.scan();
        return new DeclStmt(position, name, expr);

    }


    /*
     * <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
     * <Start>     ::= EMPTY | <Expression>
     * <Terminate> ::= EMPTY | <Expression>
     * <Increment> ::= EMPTY | <Expression>
     */
    private Stmt parseFor() {
        System.out.println("parseFor---");
        int position = this.currentToken.position;

        this.currentToken = this.scanner.scan();        // "("
        System.out.println("parseFor1: " + currentToken.spelling + " ?(");
        this.currentToken = this.scanner.scan();        // <Start> or ";"
        System.out.println("parseFor2: " + currentToken.spelling + " ? var");

        Expr initExpr;
        if (this.currentToken.spelling.equals(";")){
            initExpr = null;
        }
        else {
            initExpr = parseExpression();
        }

        this.currentToken = this.scanner.scan();        // <Terminate> or ";"
        Expr predExpr;
        if (this.currentToken.spelling.equals(";")){
            predExpr = null;
        }
        else{
            predExpr = parseExpression();
        }

        this.currentToken = this.scanner.scan();        // <Increment> or ")"
        Expr updateExpr;
        if (this.currentToken.spelling.equals(")")){
            updateExpr = null;
        }
        else{
            updateExpr = parseExpression();
        }

        this.currentToken = this.scanner.scan();        //<STMT>
        Stmt bodyStmt = parseStatement();


        return new ForStmt(position, initExpr, predExpr, updateExpr, bodyStmt);
    }


    /*
     * <BlockStmt> ::= { <Body> }
     * <Body> ::= EMPTY | <Stmt> <Body>
     */
    private Stmt parseBlock() {
        System.out.println("parseBlock---");
        int position = this.currentToken.position;      // current toke: "{"
        System.out.println("parseBlock 1:" + currentToken.spelling);
        this.currentToken = this.scanner.scan();        //<Body>
        System.out.println("parseBlock 2:" + currentToken.spelling);

        StmtList stmtList = new StmtList(position);
        if (!this.currentToken.spelling.equals("}")){
            System.out.println("YAS");
            while (!this.currentToken.spelling.equals("}")){

                Stmt stmt = parseStatement();
                System.out.println("parseBlock 3:" + currentToken.spelling);
                stmtList.addElement(stmt);
            }
        }

        this.currentToken = this.scanner.scan();
        System.out.println("parseBlock 4:" + currentToken.spelling);
        return  new BlockStmt(position, stmtList);
    }



    /*
     * <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
     */
    private Stmt parseIf() {
        System.out.println("parseIf---");
        int position = this.currentToken.position;
        System.out.println("parseIf1: " + currentToken.spelling);
        this.currentToken = this.scanner.scan();    // "("
        System.out.println("parseIf2: " + currentToken.spelling);
        this.currentToken = this.scanner.scan();    // <Expr>
        System.out.println("parseIf3: " + currentToken.spelling);
        Expr predExpr = parseExpression();
        System.out.println("parseIf4: " + currentToken.spelling);
        this.currentToken = this.scanner.scan();    // <Stmt>
        System.out.println("parseIf5: " + currentToken.spelling + " shouldbe {");
        Stmt thenStmt = parseStatement();
        Stmt elseStmt = null;
        if (this.currentToken.kind == ELSE){
            this.currentToken = this.scanner.scan();    // <Stmt>
            elseStmt = parseStatement();
        }


        return new IfStmt(position, predExpr, thenStmt, elseStmt);

    }


    //-----------------------------------------
    // Expressions
    //Here we introduce the precedence to operations

    /*
	 * <Expression> ::= <LogicalOrExpr> <OptionalAssignment>
     * <OptionalAssignment> ::= EMPTY | = <Expression>
     */
	private Expr parseExpression() {
	    System.out.println("parseExpression---");
        int position = this.currentToken.position;

        String name = this.currentToken.spelling;
        Expr left = parseOrExpr();
        System.out.println("parseExpr: "+ currentToken.spelling);
        while (this.currentToken.spelling.equals("=")){
            System.out.println("parseExpr1: " + currentToken.spelling);
            this.currentToken = this.scanner.scan();    // <LogicalOrExpr>
            System.out.println("parseExpr2: " + currentToken.spelling);
            Expr right = parseOrExpr();
            left = new AssignExpr(position, null, name, right);
        }

        return left;
	}


    /*
	 * <LogicalOR> ::= <logicalAND> <LogicalORRest>
     * <LogicalORRest> ::= EMPTY |  || <LogicalAND> <LogicalORRest>
     */
	private Expr parseOrExpr() {
        System.out.println("parseOrExpr---");
        int position = currentToken.position;

        Expr left = parseAndExpr();
        System.out.println("Or "+ currentToken.spelling);
        while (this.currentToken.spelling.equals("||")) {
            this.currentToken = scanner.scan();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
	}


    /*
	 * <LogicalAND> ::= <ComparisonExpr> <LogicalANDRest>
     * <LogicalANDRest> ::= EMPTY |  && <ComparisonExpr> <LogicalANDRest>
     */
	private Expr parseAndExpr() {
        System.out.println("parseAndExpr---");
        int position = this.currentToken.position;   // <ComparisonExpr>
        Expr left = parseEqualityExpr();                  // TODO ERROR
        System.out.println("And " + currentToken.spelling);
        while (this.currentToken.spelling.equals("&&")) {
            this.currentToken = this.scanner.scan();
            Expr right = parseExpression();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;
    }


    /*
	 * <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
     *                     <RelationalExpr>
     * <equalOrNotEqual> ::=  == | !=
     */
	private Expr parseEqualityExpr() {
        System.out.println("parseEqualityExpr---");
        int position = this.currentToken.position;     // <RelationalExpr>

        Expr left = parseRelationalExpr();
        Expr right;
        System.out.println("Equality: " + currentToken.spelling);
        if (this.currentToken.spelling.equals("==")){
            this.currentToken = this.scanner.scan();
            right = parseExpression();
            left = new BinaryCompEqExpr(position, left, right);
        }
        else if (this.currentToken.spelling.equals("!=")){
            this.currentToken = this.scanner.scan();
            right = parseExpression();
            left = new BinaryCompNeExpr(position, left, right);
        }

        return left;
    }


    /*
	 * <RelationalExpr> ::=<AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
     * <ComparisonOp> ::=  < | > | <= | >= | INSTANCEOF
     */
	private Expr parseRelationalExpr() {
        System.out.println("parseRelationalExpr---");
        int position = this.currentToken.position;      // <AddExpr>

        Expr left = parseAddExpr();
        System.out.println("Relational: " + currentToken.spelling);
        Expr right;
        if (this.currentToken.spelling.equals("<")){
            this.currentToken = this.scanner.scan();
            right = parseAddExpr();
            left = new BinaryCompLtExpr(position, left, right);
        }
        else if (this.currentToken.spelling.equals(">")){
            this.currentToken = this.scanner.scan();
            right = parseAddExpr();
            left = new BinaryCompGtExpr(position, left, right);
        }
        else if (this.currentToken.spelling.equals("<=")){
            this.currentToken = this.scanner.scan();
            right = parseAddExpr();
            left = new BinaryCompLeqExpr(position, left, right);
        }
        else if (this.currentToken.spelling.equals(">=")){
            this.currentToken = this.scanner.scan();
            right = parseAddExpr();
            left = new BinaryCompGeqExpr(position, left, right);
        }
        else if (this.currentToken.kind == INSTANCEOF){
            this.currentToken = this.scanner.scan();
            String type = this.currentToken.spelling;
            left = new InstanceofExpr(position, left, type);
        }

        return left;
    }


    /*
	 * <AddExpr>::＝ <MultExpr> <MoreMultExpr>
     * <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
     */
	private Expr parseAddExpr() {
	    System.out.println("parseAddExpr---");
        int position = this.currentToken.position;      // <MultExpr>

        Expr left = parseMultExpr();
        Expr right;
        while (this.currentToken.spelling.equals("+") || this.currentToken.spelling.equals("-") ){
            if (this.currentToken.spelling.equals("+")){
                this.currentToken = this.scanner.scan();
                right = parseMultExpr();
                left = new BinaryArithPlusExpr(position, left, right);
            }
            if (this.currentToken.spelling.equals("-")){
                this.currentToken = this.scanner.scan();
                right = parseMultExpr();
                left = new BinaryArithMinusExpr(position, left, right);
            }
        }

        return left;
    }


    /*
	 * <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
     * <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
     *               / <NewCastOrUnary> <MoreNCU> |
     *               % <NewCastOrUnary> <MoreNCU> |
     *               EMPTY
     */
	private Expr parseMultExpr() {
        System.out.println("parseMultExpr---");
	    int position = this.currentToken.position;      // <NewCastOrUnary>

        Expr left = parseNewCastOrUnary();
        Expr right;

        while (this.currentToken.spelling.equals("*") || this.currentToken.spelling.equals("/")
                                                        || this.currentToken.spelling.equals("%")){
            if (this.currentToken.spelling.equals("*")){
                this.currentToken = this.scanner.scan();
                right = parseNewCastOrUnary();
                left = new BinaryArithTimesExpr(position, left, right);
            }
            else if (this.currentToken.spelling.equals("/")){
                this.currentToken = this.scanner.scan();
                right = parseNewCastOrUnary();
                left = new BinaryArithDivideExpr(position, left, right);
            }
            else if (this.currentToken.spelling.equals("%")){
                this.currentToken = this.scanner.scan();
                right = parseNewCastOrUnary();
                left = new BinaryArithModulusExpr(position, left, right);
            }
        }

        if (!this.currentToken.spelling.equals("*") && !this.currentToken.spelling.equals("/") && !this.currentToken.spelling.equals("%")){
            // TODO- ILLEGAL TOKEN
        }

        return left;

    }

    /*
	 * <NewCastOrUnary> ::= < NewExpression> | <CastExpression> | <UnaryPrefix>
     */
	private Expr parseNewCastOrUnary() {
	    //int position = this.currentToken.position;
        System.out.println("parseNewCastExpr---");
        Expr expr;

        switch (currentToken.kind) {
            case NEW:
                expr = parseNew();
                break;
            case CAST:
                expr = parseCast();
                break;
            default:
                expr = parseUnaryPrefix();
                break;
                // TODO KIND CHECK UNARY PREFIX?
        }

        return expr;
    }


    /*
	 * <NewExpression> ::= NEW <Identifier> ( ) | NEW <Identifier> [ <Expression> ]
     */
	private Expr parseNew() {
	    int position = this.currentToken.position;      // NEW
        this.currentToken = this.scanner.scan();        // <identifier>

        String type = parseIdentifier();

        // if there is <Expression>
        if (this.currentToken.spelling.equals("[")){
            this.currentToken = this.scanner.scan();        // <Expression>
            Expr expr = parseExpression();
            this.currentToken = this.scanner.scan();
        }
        else{
            this.currentToken = this.scanner.scan();    // )
            this.currentToken = this.scanner.scan();    // new
        }

        return new NewExpr(position,type);
    }


    /*
	 * <CastExpression> ::= CAST ( <Type> , <Expression> )
     */
	private Expr parseCast() {
	    System.out.println("parseCast----");
	    int position = this.currentToken.position;      // CAST
        this.currentToken = this.scanner.scan();        // "("
        if ( !this.currentToken.spelling.equals("(") ){
            // TODO: ILLEGAL
        }

        this.currentToken = this.scanner.scan();        // <Type>
        if ( this.currentToken.kind != STRCONST ){
            // TODO: ILLEGAL
        }
        String type = this.currentToken.spelling;

        this.currentToken = this.scanner.scan();        // ","
        if ( !this.currentToken.spelling.equals(",") ){
            // TODO: ILLEGAL
        }

        this.currentToken = this.scanner.scan();        // <Expression>
        Expr expr = parseExpression();

        if (!this.currentToken.spelling.equals(")")){
            // TODO: ILLEGAL
        }
        this.currentToken = this.scanner.scan();

        return new CastExpr(position, type, expr);

	}


    /*
	 * <UnaryPrefix> ::= <PrefixOp> <UnaryPrefix> | <UnaryPostfix>
     * <PrefixOp> ::= - | ! | ++ | --
     */
	private Expr parseUnaryPrefix() {
        System.out.println("parseUnaryPrefix---");
        Expr expr = null;

        // if this.currentToken is <PrefixOp>
        if (this.currentToken.spelling.equals("-") || this.currentToken.spelling.equals("!")
                || this.currentToken.spelling.equals("++") || this.currentToken.spelling.equals("--")) {
            while (this.currentToken.spelling.equals("-") || this.currentToken.spelling.equals("!")
                    || this.currentToken.spelling.equals("++") || this.currentToken.spelling.equals("--")) {

                if (this.currentToken.spelling.equals("-")) {
                    this.currentToken = this.scanner.scan();
                    expr = new UnaryNegExpr(this.currentToken.position, parseUnaryPrefix());
                } else if (this.currentToken.spelling.equals("!")) {
                    this.currentToken = this.scanner.scan();
                    expr = new UnaryNotExpr(this.currentToken.position, parseUnaryPrefix());
                } else if (this.currentToken.spelling.equals("++")) {
                    this.currentToken = this.scanner.scan();
                    expr = new UnaryIncrExpr(this.currentToken.position, parseUnaryPrefix(), false);
                } else if (this.currentToken.spelling.equals("--")) {
                    this.currentToken = this.scanner.scan();
                    expr = new UnaryDecrExpr(this.currentToken.position, parseUnaryPrefix(), false);
                }
            }
        }
        else {
            expr = parseUnaryPostfix();
        }

        return expr;
    }


    /*
	 * <UnaryPostfix> ::= <Primary> <PostfixOp>
     * <PostfixOp> ::= ++ | -- | EMPTY
     */
	private Expr parseUnaryPostfix() {
        System.out.println("parseUnaryPostfix---");
	    int position = this.currentToken.position;      // <Primary>
        System.out.println("parsePostfix1: " + currentToken.spelling);
        Expr expr = parsePrimary();
        System.out.println("parsePostfix2: " + currentToken.spelling);

        if (this.currentToken.spelling.equals("++")){
            this.currentToken = this.scanner.scan();        // TODO NOT SURE
            return new UnaryIncrExpr(position, expr, true);
        }
        else if (this.currentToken.spelling.equals("--")){
            this.currentToken = this.scanner.scan();
            return new UnaryDecrExpr(position, expr, true);
        }
        else{
            return expr;        // TODO: NOT SURE
        }
    }


    /*
	 * <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> | <VarExpr> | <DispatchExpr>
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     * <DispatchExprPrefix> ::= <Primary> . | EMPTY
     */
    private Expr parsePrimary() {
        int position = currentToken.position;
        Expr expr;
        if(currentToken.kind ==  INTCONST) {
            expr = parseIntConst();
        } else if (currentToken.kind == STRCONST) {
            expr = parseStringConst();
        } else if (currentToken.kind == BOOLEAN) {
            expr = parseBoolean();
        }else{
            Expr ref = null;
            if(currentToken.spelling.equals("this")||currentToken.spelling.equals("super")){
                ref = new VarExpr(position, null, currentToken.spelling);
                this.currentToken=scanner.scan(); //"this." or "super."
                if(!currentToken.spelling.equals(".")){ //"this" or "super"
                    expr = ref;
                    return expr;
                }
                this.currentToken = scanner.scan();
            }

            String name = parseIdentifier(); //parse name (variable or method)
            if (!currentToken.spelling.equals("(") && !currentToken.spelling.equals(("."))) {
                if (!currentToken.spelling.equals("[")) {//not array member. like this.a
                    expr = new VarExpr(position, ref, name);
                    return expr;
                } else {//array member like this.a[2]
                    this.currentToken = scanner.scan();
                    Expr index = parseExpression();
                    expr = new ArrayExpr(position, ref, name, index);
                    this.currentToken = scanner.scan();
                    if(!this.currentToken.spelling.equals("]")){
                        this.errorHandler.register(Error.Kind.PARSE_ERROR, null, position,
                                "Non-Primary Found where Primary Expected");
                    }
                    this.currentToken = scanner.scan();
                    return expr;
                }
            } else {//dispatch
                System.out.println("parsePrimary-Dispatch1 : " + currentToken.spelling +" ?(");
                if (currentToken.spelling.equals(("."))) { //like this.a.method1()
                    ref = new VarExpr(position, ref, name); //reference = this.a
                    this.currentToken = this.scanner.scan();//this.a.method|()
                    name = parseIdentifier(); // name = method1
                    if(!currentToken.spelling.equals("(")){//like this.a.b, not allowed
                        this.errorHandler.register(Error.Kind.PARSE_ERROR, this.fileName, position, "Method not found");
                    }
                    this.currentToken = this.scanner.scan();
                } else if (currentToken.spelling.equals("(")) { //like this.method() or method()
                    this.currentToken = this.scanner.scan();

                }
                System.out.println("parsePrimary-Dispatch2 : " + currentToken.spelling +" print stmt ");
                ExprList paraList = parseArguments();
                System.out.println("parsePrimary-afterParseArg: " + currentToken.spelling);
                if (!this.currentToken.spelling.equals((")"))) {
                    this.errorHandler.register(Error.Kind.PARSE_ERROR, null, position,
                            "Non-Primary Found where Primary Expected");
                }
                expr = new DispatchExpr(position, ref, name, paraList);
                this.currentToken = this.scanner.scan();
                if(this.currentToken.spelling.equals(".")){
                    while(this.currentToken.spelling.equals(".")){
                        ref = new DispatchExpr(position, ref, name, paraList);
                        name = parseIdentifier();
                        paraList = parseArguments();
                        if (!this.currentToken.spelling.equals((")"))) {
                            this.errorHandler.register(Error.Kind.PARSE_ERROR, null, position,
                                    "Non-Primary Found where Primary Expected");
                        }
                        expr = new DispatchExpr(position, ref, name, paraList);
                        this.currentToken = this.scanner.scan();
                    }
                }
            }
        }

        return expr;
    }

    /*
     * <Arguments> ::= EMPTY | <Expression> <MoreArgs>
     * <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
     */
    private ExprList parseArguments(){
        int pos = currentToken.position;
        ExprList args = new ExprList(pos);

        //checks for the empty arguments case
        if ( this.currentToken.spelling.equals(")") ) {
            return args;
        }

        //if not empty, parse the first argument
        Expr arg = parseExpression();
        args.addElement(arg);

        //continue parsing arguments
        while (this.currentToken.spelling.equals(",")) {
            this.currentToken = scanner.scan();
            arg = parseExpression();
            args.addElement(arg);
        }
        return args;
    }
    /*
     * <Parameters>  ::= EMPTY | <Formal> <MoreFormals>
     * <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
     */
    private FormalList parseParameters() {
        int pos = currentToken.position;

        FormalList params = new FormalList(pos);
        //checks for the empty parameters case
        System.out.println("--parsePara: " + currentToken.spelling);
        if ( this.currentToken.spelling.equals(")") ) {
            return params;
        }
        //if not empty, parse the first parameter and add it
        Formal param = parseFormal();
        params.addElement(param);

            //continue parsing parameters and adding them to the list
        while (this.currentToken.spelling.equals(",")) {
            this.currentToken = scanner.scan();
            param = parseFormal();
            params.addElement(param);
        }
        return params;
    }
    /*
    * <Formal> ::= <Type> <Identifier>
    */
    private Formal parseFormal() {
        int pos = currentToken.position;
        String type = parseType();
        String id = parseIdentifier();
        //String identifier = scanner.scan().getSpelling();
        return (new Formal(pos, type, id));
    }
    /*
     * <Type> ::= <Identifier> <Brackets>
     * <Brackets> ::= EMPTY | [ ]
     */
    //TODO - BRACKETS - how do I do them
    private String parseType() {
        return parseIdentifier();
    }
    //----------------------------------------
    //Terminals
    // I'M NOT SURE ABOUT THESE AT ALL
    // JUST BEST GUESS
    //definitely not done or right
    private String parseOperator() {
        String spelling = currentToken.getSpelling();
        Boolean error = false;
        switch (spelling) {
            case "&&":
                if (currentToken.kind != BINARYLOGIC) { error = true; }
                break;
            case "||":
                if (currentToken.kind != BINARYLOGIC) { error = true; }
                break;
            case "+":
                if (currentToken.kind != PLUSMINUS) { error = true; }
                break;
            case "-":
                if (currentToken.kind != PLUSMINUS) { error = true; }
                break;
            case "*":
                if (currentToken.kind != MULDIV) { error = true; }
                break;
            case "/":
                if (currentToken.kind != MULDIV) { error = true; }
                break;
            case "==":
                if (currentToken.kind != COMPARE) { error = true; }
                break;
            case "!=":
                if (currentToken.kind != COMPARE) { error = true; }
                break;
            case "<":
                if (currentToken.kind != COMPARE) { error = true; }
                break;
            case "<=":
                if (currentToken.kind != COMPARE) { error = true; }
                break;
            case ">":
                if (currentToken.kind != COMPARE) { error = true; }
                break;
            case ">=":
                if (currentToken.kind != COMPARE) { error = true; }
                break;
            case "++":
                if (currentToken.kind != UNARYINCR) { error = true; }
                break;
            case "--":
                if (currentToken.kind != UNARYDECR) { error = true; }
                break;
        }
        if (error) {
            this.errorHandler.register(Error.Kind.PARSE_ERROR, this.fileName, currentToken.position,
                    "Non-Identifier Found where Identifier expected.");
        }
        this.currentToken = scanner.scan();
        return spelling;
    }
    private String parseIdentifier() {
        String spelling = currentToken.getSpelling();
        if (currentToken.kind != IDENTIFIER) {
            this.errorHandler.register(Error.Kind.PARSE_ERROR, this.fileName, currentToken.position,
                                        "Non-Identifier Found where Identifier expected.");
        }
        this.currentToken = scanner.scan();
        return spelling;
    }
    private ConstStringExpr parseStringConst() {
        String spelling = currentToken.getSpelling();
        int position = currentToken.position;
        if (currentToken.kind != STRCONST) {
            this.errorHandler.register(Error.Kind.PARSE_ERROR, this.fileName, position,
                    "Non-String Found where String Literal Expected");
        }
        this.currentToken = scanner.scan();
        return new ConstStringExpr(position, spelling);
    }
    private ConstIntExpr parseIntConst() {
        String spelling = currentToken.getSpelling();
        int position = currentToken.position;
        if (currentToken.kind != INTCONST) {
            this.errorHandler.register(Error.Kind.PARSE_ERROR, this.fileName, position,
                    "Non-Integer Found where Integer Literal Expected");
        }
        this.currentToken = scanner.scan();
        return new ConstIntExpr(position, spelling);
    }
    private ConstBooleanExpr parseBoolean() {
        String spelling = currentToken.getSpelling();
        int position = currentToken.position;
        if (currentToken.kind != BOOLEAN) {
            this.errorHandler.register(Error.Kind.PARSE_ERROR, this.fileName, position,
                    "Non-Boolean Found where Boolean Expected");
        }
        this.currentToken = scanner.scan();
        return new ConstBooleanExpr(position, spelling);
    }

    public static void main(String[] args) {
        // command line arguments we used for testing purpose
        // test/test1.java test/test2.java test/test3.java test/test4.java test/badtest.java
        for (int i=0; i < args.length; i++) {

            String filename = "C:\\Users\\Danqing Zhao\\Desktop\\CS361\\proj10\\proj10JiangQuanZhaoMarcelloCoyne\\src\\proj10JiangQuanZhaoMarcelloCoyne\\bantam\\test.btm";
            System.out.println("\n------------------ " + filename + " ------------------" + "\n");
            try{
                ErrorHandler handler = new ErrorHandler();
                Parser parser = new Parser(handler);
                Program program = parser.parse(filename);
                Drawer drawer = new Drawer();
                drawer.draw(filename, program);

                List<Error> errorList = handler.getErrorList();
                for (Error err : errorList) {
                    System.out.println(err.toString());
                }

                if (errorList.size() == 0) {
                    System.out.println("Parsing was successful!");
                } else if (errorList.size() == 1) {
                    System.out.println("\n1 illegal token was found.");
                } else {
                    System.out.println("\n" + errorList.size() + " illegal tokens were found.");
                }
            }catch (Exception e) {
                    System.out.println(e);
                    System.out.println("ERROR: Parsing " + filename + " failed!");
                }
            }


    }
}


