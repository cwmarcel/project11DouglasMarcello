/*
 * File: EditMenuController.java
 * F18 CS361 Project 10
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 10/23/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj10JiangQuanZhaoMarcello.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert;
import org.fxmisc.richtext.CodeArea;
import proj10JiangQuanZhaoMarcello.java.JavaTabPane;

import java.lang.String;

/**
 * EditMenu controller handles Edit menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 * @author Michael Coyne
 */
public class EditMenuController {
    /**
     * TabPane defined in Main.fxml
     */
    @FXML private TabPane tabPane;
    /**
     * the active code area embedded in the selected tab
     */
    private CodeArea activeCodeArea;
    /**
     * the selected text in the active code area
     */
    private String selectedText;
    /**
     * each string in the array represents one line in the selected text of the active code area
     */
    private String[] selectedTextLines;
    /**
     * the index range of the selected text in the active code area
     */
    private int[] selectionRange;

    /**
     * Sets the tab pane.
     *
     * @param tabPane TabPane defined in Main.fxml
     */
    public void setTabPane(TabPane tabPane) { this.tabPane = tabPane; }

    /**
     * Helper method to get the active code area and update the active code area field.
     */
    private void updateActiveCodeArea() {
        this.activeCodeArea = JavaTabPane.getCurrentCodeArea(this.tabPane);
    }

    /**
     * Helper method to get the selected text in the active code area and update the selected text field.
     */
    private void updateSelectedText() {
        this.updateActiveCodeArea();
        this.selectedText = this.activeCodeArea.getSelectedText();;
    }

    /**
     * creates and displays an informational alert
     *
     * @param header the content of the alert
     */
    private void showAlert(String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.show();
    }

    /**
     * Handles the Edit menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleEditMenuAction(ActionEvent event) {
        this.updateActiveCodeArea();
        MenuItem clickedItem = (MenuItem)event.getTarget();
        switch(clickedItem.getId()) {
            case "undoMenuItem":
                this.activeCodeArea.undo();
                break;
            case "redoMenuItem":
                this.activeCodeArea.redo();
                break;
            case "cutMenuItem":
                this.activeCodeArea.cut();
                break;
            case "copyMenuItem":
                this.activeCodeArea.copy();
                break;
            case "pasteMenuItem":
                this.activeCodeArea.paste();
                break;
            case "selectMenuItem":
                this.activeCodeArea.selectAll();
                break;
            default:
        }
    }

    /**
     * A private helper method to help the private fields to load the selected lines information.
     */
    private void setupSelectedLines() {
        this.updateActiveCodeArea();
        this.updateSelectedText();
        this.selectedTextLines = this.selectedText.split("\n");
        this.selectionRange = this.getTextSelectionRange(this.activeCodeArea);
    }

    /**
     * A private helper method to delete original selected lines from the code area,
     * and enter the edited selected lines to the code area.
     */
    private void displayEditedSelectedLines(){
        this.activeCodeArea.replaceSelection("");
        this.activeCodeArea.insertText(this.activeCodeArea.getCaretPosition(), String.join("\n", this.selectedTextLines));
        this.activeCodeArea.selectRange(this.selectionRange[0], this.activeCodeArea.getCaretPosition());
    }

    /**
     * Helper method to check whether a chunk of text is already commented.
     *
     * @param lines an array of strings to be checked.
     *              Each string in the array represents one line in the chunk of text.
     * @return true if the text is already commented; false if the text is not already commented.
     */
    private boolean isCommented(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (!((lines[i].length() >= 2) && (lines[i].substring(0, 2).equals("//")))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to comment a chunk of text.
     *
     * @param lines an array of strings to be commented.
     *              Each string in the array represents one line in a chunk of text.
     */
    private void commentText(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            lines[i] = "// " + lines[i];
        }
    }

    /**
     * Helper method to un-comments a chunk of text.
     *
     * @param lines an array of strings to be un-commented.
     *              Each string in the array represents one line in a chunk of text.
     */
    private void uncommentText(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if ((lines[i].length() > 2) && (String.valueOf(lines[i].charAt(2)).equals(" "))) {
                lines[i] = lines[i].substring(3);
            }
            else {
                lines[i] = lines[i].substring(2);
            }
        }
    }

    /**
     * Helper method to check whether a chunk of text is already block commented.
     *
     * @param lines an array of strings to be checked.
     *              Each string in the array represents one line in the chunk of text.
     *
     * @return true if the text is already block commented; false if the text is not already block commented.
     */
    private boolean isBlockCommented(String[] lines) {
        if (lines.length != 0) {
            if (    (lines[0].length() >= 2) &&
                    (lines[0].substring(0, 2).equals("/*")) &&
                    (lines[lines.length - 1].length() >= 2) &&
                    (lines[lines.length - 1].substring(lines[lines.length - 1].length() - 2).equals("*/"))  ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to block comment a chunk of text.
     *
     * @param lines an array of strings to be block commented.
     *              Each string in the array represents one line in a chunk of text.
     */
    private void blockCommentText(String[] lines) {
        if (lines.length != 0) {
            lines[0] = "/*" + lines[0];
            lines[lines.length - 1] += "*/";
        }
    }

    /**
     * Helper method to block un-comment a chuck of text.
     *
     * @param lines an array of strings to be block un-commented.
     *              Each string in the array represents one line in a chunk of text.
     */
    private void blockUncommentText(String[] lines) {
        if (lines.length != 0) {
            if ((lines[0].length() > 2) && (String.valueOf(lines[0].charAt(2)).equals(" "))) {
                lines[0] = lines[0].substring(3);
            }
            else {
                lines[0] = lines[0].substring(2);
            }
            lines[lines.length - 1] = lines[lines.length - 1].substring(0, lines[lines.length - 1].length() - 2);
        }
    }

    /**
     * Helper method to get the index range of the text selection.
     *
     * @param codeArea CodeArea to extract the selected text.
     * @return the index range of the selected text.
     */
    private int[] getTextSelectionRange(CodeArea codeArea) {
        int[] selectionRange = new int[2];

        if (codeArea.getAnchor() <= codeArea.getCaretPosition()) {
            selectionRange[0] = codeArea.getAnchor();
            selectionRange[1] = codeArea.getCaretPosition();
        }
        else {
            selectionRange[0] = codeArea.getCaretPosition();
            selectionRange[1] = codeArea.getAnchor();
        }
        return selectionRange;
    }

    /**
     * Handles the comment action.
     * Comments out the selected lines in the top CodeArea if they are not already commented out.
     * Un-comments them if they are commented out.
     */
    public void handleToggleCommentAction() {
        this.setupSelectedLines();

        if (this.isCommented(this.selectedTextLines)) {
            this.uncommentText(this.selectedTextLines);
        }
        else {
            this.commentText(this.selectedTextLines);
        }

        this.displayEditedSelectedLines();
    }

    /**
     * Handles the toggle block comment action.
     * Comments out the selected lines in block in the top CodeArea if they are not already block commented out.
     * Un-comments them in block if they are block commented out.
     */
    public void handleToggleBlockCommentAction() {
        this.setupSelectedLines();

        if (this.isBlockCommented(this.selectedTextLines)) {
            this.blockUncommentText(this.selectedTextLines);
        }
        else {
            this.blockCommentText(this.selectedTextLines);
        }

        this.displayEditedSelectedLines();
    }

    /**
     * Helper method to indent a chunk of text.
     *
     * @param lines an array of strings to be indented.
     *              Each string in the array represents one line in a chunk of text.
     */
    public void indentText(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            lines[i] = "\t" + lines[i];
        }
    }

    /**
     * Helper method to un-indent a chunk of text.
     *
     * @param lines an array of strings to be un-indented.
     *              Each string in the array represents one line in a chunk of text.
     */
    private void unindentText(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            // full tab(s) present at the start of the line
            if (lines[i].startsWith("\t")) {
                lines[i] = lines[i].substring(1);
            }
            // space(s) present at the start of the line, not a full tab
            else {
                int spaceCounter = 0;
                for (int j = 0; j < lines[i].length() && spaceCounter < 8; j++) {
                    if ((String.valueOf(lines[i].charAt(j))).equals(" ")) {
                        spaceCounter ++;
                    }
                    else {
                        break;
                    }
                }
                lines[i] = lines[i].substring(spaceCounter);
            }
        }
    }

    /**
     * Handles the indent action.
     * Indents the text being selected.
     */
    public void handleIndentAction() {
        this.setupSelectedLines();
        this.indentText(this.selectedTextLines);
        this.displayEditedSelectedLines();
    }

    /**
     * Handles the un-indent action.
     * Un-indents the text being selected.
     */
    public void handleUnindentAction() {
        this.setupSelectedLines();
        this.unindentText(this.selectedTextLines);
        this.activeCodeArea.replaceSelection(String.join("\n", selectedTextLines));
        this.activeCodeArea.selectRange(selectionRange[0], activeCodeArea.getCaretPosition());
        if (this.activeCodeArea.getSelectedText().length() == 1) {
            this.activeCodeArea.deselect();
        }
    }
}
