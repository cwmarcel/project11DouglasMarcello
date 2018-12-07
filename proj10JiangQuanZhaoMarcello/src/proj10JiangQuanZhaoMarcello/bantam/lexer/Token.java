 /*
  * @(#)Token.java                        2.0 1999/08/11
  *
  * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
  * and School of Computer and Math Sciences, The Robert Gordon University,
  * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
  * All rights reserved.
  *
  * This software is provided free for educational use only. It may
  * not be used for commercial purposes without the prior written permission
  * of the authors.
  *
  * Modified by Dale Skrien, Fall 2018
  */

 package proj10JiangQuanZhaoMarcello.bantam.lexer;

 import java.util.Set;

 /**
  * The Token class.
  */
 public class Token {
     //instance variables
     public Kind kind; // an enum type of the token
     public String spelling; // the actual sequence of chars in the token
     public int position; // the line number where the token was found

     /**
      * Constructor of the Token class.
      *
      * @param kind the emum type of the token
      * @param spelling the spelling of the token as a String
      * @param position the position of the token as an int
      */
     Token(Kind kind, String spelling, int position) {
         this.spelling = spelling;
         this.position = position;

         // patch the kind in the case of boolean constants and keywords
         if (kind == Kind.IDENTIFIER && (spelling.equals("true") || spelling.equals("false"))) {
             this.kind = Kind.BOOLEAN;
         }
         else if (kind == Kind.IDENTIFIER && reservedWords.contains(spelling)) {
             this.kind = Enum.valueOf(Kind.class, spelling.toUpperCase());
         }
         else {
             this.kind = kind;
         }
     }

     /**
      * Return the information of the token as a String.
      *
      * @return the information of the Token as a String
      */
     public String toString() {
         return "Token: Kind = " + this.kind.name() + ", spelling = " +
                 this.spelling + ", " + "position = " + this.position + "\n";
     }

     /**
      * Gets the kind of the token.
      *
      * @return the enum kind of the token
      */
     public Kind getKind () {
         return this.kind;
     }

     /**
      * Get the spelling of the token in a String.
      *
      * @return the token as a String.
      */
     public String getSpelling() { return this.spelling; }

     /**
      * Definition of the enum type fo the Token.
      */
     public enum Kind {
         // literals, identifiers...
         INTCONST, STRCONST, BOOLEAN, IDENTIFIER,

         // operators...
         BINARYLOGIC, PLUSMINUS, MULDIV, COMPARE, UNARYINCR, UNARYDECR, ASSIGN,
         UNARYNOT,

         // punctuation...
         DOT, COLON, SEMICOLON, COMMA,

         // brackets...
         LPAREN, RPAREN, LBRACKET, RBRACKET, LCURLY, RCURLY,

         // special tokens...
         COMMENT, ERROR, EOF, //end of file token

         // reserved words
         BREAK, CAST, CLASS, VAR, ELSE, EXTENDS, FOR, IF, INSTANCEOF, NEW,
         RETURN, WHILE, THIS, SUPER
     }

     /**
      * Definition of a set of keywords.
      */
     private static Set<String> reservedWords = Set.of("break", "cast", "class", "var",
             "else", "extends", "for", "if", "instanceof", "new", "return", "while",
             "this", "super");

 }
