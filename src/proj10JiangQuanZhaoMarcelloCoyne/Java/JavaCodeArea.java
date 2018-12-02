/*
 * File: JavaCodeArea.java
 * F18 CS361 Project 9
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello, Michael Coyne
 * Date: 10/30/2018
 * This file contains the JavaCodeArea class, which extends the CodeArea class
 * to handle syntax highlighting.
 */

package proj10JiangQuanZhaoMarcelloCoyne.Java;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.Subscription;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class extends the CodeArea class from RichTextFx to handle
 * syntax highlighting.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 * @author Michael Coyne
 */
public class JavaCodeArea extends CodeArea {
    /**
     * a list of key words to be highlighted
     */
    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "var"
    };

    /**
     * Regular expressions of characters to be highlighted
     */
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String INTEGER_PATTERN = "(?<![\\w])(?<![\\d.])[0-9]+(?![\\d.])(?![\\w])";

    /**
     * Patterns to be highlighted
     */
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<INTEGER>" + INTEGER_PATTERN + ")"
    );

    /**
     * Private field to store the style class for KEYWORD pattern.
     */
    private static String keywordColorClass = "keyword";

    /**
     * Private field to store the style class for PAREN, BRACE, BRACKET pattern.
     */
    private static String parenColorClass = "paren";

    /**
     * Private field to store the style class for STRING pattern.
     */
    private static String stringColorClass = "string";

    /**
     * Private field to store the style class for INTEGER pattern.
     */
    private static String intColorClass = "integer";

    /**
     * Constructor of JavaCodeArea class
     */
    public JavaCodeArea() {
        this.setOnKeyPressed(event -> {
            this.highlightText();
            this.handleTextChange();
        });
        this.setOnMouseClicked(event -> {
            this.highlightText();
            this.handleTextChange();
        });
        this.setAutoParenCompletion();
    }

    /**
     * Sets and adds listeners to the auto-completion of the parenthesis.
     */
    private void setAutoParenCompletion() {
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));
        JavaCodeArea thisCodeArea = this;
        this.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if ( newValue.length() > oldValue.length() ) {
                    int caretPosition = thisCodeArea.getCaretPosition();
                    String inputChar = Character.toString(newValue.charAt(caretPosition - 1));
                    if (inputChar.equalsIgnoreCase("(")) {
                        thisCodeArea.insertText(caretPosition, ")");
                    } else if (inputChar.equalsIgnoreCase("{")) {
                        thisCodeArea.insertText(caretPosition, "\n}");
                    } else if (inputChar.equalsIgnoreCase("[")) {
                        thisCodeArea.insertText(caretPosition, "]");
                    }
                }
            }
        });
    }

    /**
     * Setter method for keywordColorClass field.
     *
     * @param keywordColorClass
     */
    public static void setKeywordColorClass(String keywordColorClass) {
        JavaCodeArea.keywordColorClass = keywordColorClass;
    }

    /**
     * Setter method for parenColorClass field.
     *
     * @param parenColorClass
     */
    public static void setParenColorClass(String parenColorClass) { JavaCodeArea.parenColorClass = parenColorClass; }

    /**
     * Setter method for stringColorClass field.
     *
     * @param stringColorClass
     */
    public static void setStringColorClass(String stringColorClass) {
        JavaCodeArea.stringColorClass = stringColorClass;
    }

    /**
     * Setter method for intColorClass field.
     *
     * @param intColorClass
     */
    public static void setIntColorClass(String intColorClass) { JavaCodeArea.intColorClass = intColorClass; }

    /**
     * Appends a text to the JavaCodeArea and highlights the text
     *
     * @param s String to append to the JavaCodeArea
     */
    public void appendText(String s) {
        super.appendText(s);
        this.highlightText();
    }

    /**
     * Helper function to highlight the text within the JavaCodeArea.
     */
    public void highlightText() { this.setStyleSpans(0, this.computeHighlighting(this.getText())); }

    /**
     * Handles the text change action.
     * Changes the tab title to green when the selected JavaCodeArea has been changed and not been saved.
     * Listens to the text changes and highlights the keywords real-time.
     */
    private void handleTextChange() {
        Subscription cleanupWhenNoLongerNeedIt = this

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                // when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> this.highlightText());
    }

    /**
     * Computes the highlighting of substrings of text to return the style of each substring.
     *
     * @param text string to compute highlighting of
     * @return StyleSpans Collection Object
     */
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? keywordColorClass :
                            matcher.group("PAREN") != null ? parenColorClass :
                                    matcher.group("BRACE") != null ? parenColorClass :
                                            matcher.group("BRACKET") != null ? parenColorClass :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? stringColorClass :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            matcher.group("INTEGER") != null ? intColorClass :
                                                                                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}