/*
 * File: FindMenuController.java
 * F18 CS361 Project 10
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 11/17/2018
 * This file contains the FindMenuController class, handling Find menu related actions.
 */

package proj10JiangQuanZhaoMarcello.controllers;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.fxmisc.richtext.CodeArea;
import proj10JiangQuanZhaoMarcello.java.*;

/**
 * FindMenuController class handles Find menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 */
public class FindMenuController {
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;
    /**
     *   A list containing the start indices of the text found by handleFind()
     */
    private ArrayList<Integer> occurrenceIndices;
    /**
     * The target string to be found in handleFind
     */
    private String targetText;
    /**
     * The index of the occurrence currently looked at.
     */
    private int curOccurrenceIndex;

    /**
     * Constructor for the class. Initializes the occurrenceIndices array list.
     */
    public FindMenuController() {
        this.occurrenceIndices = new ArrayList<>();
        this.curOccurrenceIndex = 0;
    }

    /**
     * Sets the tab pane.
     *
     * @param tabPane TabPane defined in Main.fxml
     */
    public void setTabPane(TabPane tabPane) { this.tabPane = tabPane; }

    /**
     * Handler for the "Find & Replace" menu item in the "Edit" menu.
     */
    public void handleFindReplace(){
        if(this.tabPane.getSelectionModel().getSelectedItem() != null ) {
            this.createFindReplaceDialog();
        }
    }

    /**
     * Creates the Find and Replace Dialog
     * Includes two textfields (find and replace)
     * Includes 4 main buttons: find next, find all, replace next, and replace all.
     * Each has its own method as a handler.
     */
    private void createFindReplaceDialog(){
        Dialog findReplaceDialog = new Dialog<>();
        findReplaceDialog.setTitle("Find & Replace");

        // Set the button types.
        ButtonType find = new ButtonType("Find Next");
        ButtonType findAll = new ButtonType("Find All");
        ButtonType replace = new ButtonType("Replace");
        ButtonType replaceAll = new ButtonType("Replace All");
        findReplaceDialog.getDialogPane().getButtonTypes().addAll(find, findAll, replace,replaceAll, ButtonType.CLOSE);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField findField = new TextField();
        TextField replaceField = new TextField();

        gridPane.add(new Label("Find:"),0,0);
        gridPane.add(findField, 0, 1);
        gridPane.add(new Label("Replace With:"), 0, 2);
        gridPane.add(replaceField, 0, 3);

        Button findButton = (Button) findReplaceDialog.getDialogPane().lookupButton(find);
        findButton.setDisable(true);

        Button findAllButton = (Button) findReplaceDialog.getDialogPane().lookupButton(findAll);
        findAllButton.setDisable(true);

        Button replaceButton = (Button) findReplaceDialog.getDialogPane().lookupButton(replace);
        replaceButton.setDisable(true);

        Button replaceAllButton = (Button) findReplaceDialog.getDialogPane().lookupButton(replaceAll);
        replaceAllButton.setDisable(true);

        // set binding
        findField.textProperty().addListener((observable, oldValue, newValue) -> {
            findButton.setDisable(newValue.trim().isEmpty());
            findAllButton.setDisable(newValue.trim().isEmpty());
            replaceField.setDisable(newValue.trim().isEmpty());
            replaceButton.setDisable(newValue.trim().isEmpty());
            replaceAllButton.setDisable(newValue.trim().isEmpty());
        });

        findReplaceDialog.getDialogPane().setContent(gridPane);

        // set event handlers
        final Button findBT = (Button) findReplaceDialog.getDialogPane().lookupButton(find);
        findBT.addEventFilter(ActionEvent.ACTION, event -> {
            this.handleFind(findField.getText());
            event.consume();
        });

        final Button findAllBT = (Button) findReplaceDialog.getDialogPane().lookupButton(findAll);
        findAllBT.addEventFilter(ActionEvent.ACTION, event -> {
            this.handleFindAll(findField.getText());
        });

        final Button replaceBT = (Button) findReplaceDialog.getDialogPane().lookupButton(replace);
        replaceBT.addEventFilter(ActionEvent.ACTION, event -> {
            this.handleReplace(findField.getText(), replaceField.getText());
            event.consume();
        });

        final Button replaceAllBT = (Button) findReplaceDialog.getDialogPane().lookupButton(replaceAll);
        replaceAllBT.addEventFilter(ActionEvent.ACTION, event -> {
            this.handleReplaceAll(findField.getText(), replaceField.getText());
        });

        findReplaceDialog.showAndWait();

        this.targetText = null;
    }

    /**
     * searches through the current CodeArea for all instances of findText and populates this.findIndices.
     * Allows the User to scroll through all of the find results if the findText does not change
     * between button clicks.
     * @param findText text to find in the CodeArea
     */
    private void handleFind(String findText){
        CodeArea codeArea = this.getActiveCodeArea();

        // check if this is the first time to find the given text
        if(!findText.equals(this.targetText)){
            this.resetFindReplace();
            this.targetText = findText;
            if (!this.findTargetText(codeArea, findText)) return;
            codeArea.selectRange(this.occurrenceIndices.get(0), this.occurrenceIndices.get(0) + findText.length());
        }

        // if the second or more time to find the given text
        else{
            // Check if there are any occurrences.
            if (this.occurrenceIndices.isEmpty()){
                this.createWarningDialog("Term '" + targetText + "' was not found in the current file.");
                return;
            }

            // Increment the current occurrence looking at.
            this.curOccurrenceIndex += 1;
            if (this.curOccurrenceIndex >= this.occurrenceIndices.size()){ this.curOccurrenceIndex = 0;}

            int currentIdx = this.occurrenceIndices.get(this.curOccurrenceIndex);
            codeArea.selectRange(currentIdx, currentIdx + findText.length());
        }
    }

    /**
     * Finds the target text in the code area and update the occurrenceIndices field.
     *
     * @param codeArea code area to be searched
     * @param targetText target text to be found
     * @return true if the target text is found; false is the target text is not found.
     */
    private boolean findTargetText(CodeArea codeArea, String targetText) {
        int index = codeArea.getText().indexOf(targetText);
        while (index != -1) {
            this.occurrenceIndices.add(index);
            index = codeArea.getText().indexOf(targetText, index + 1);
        }

        // if the search word is not found
        if (this.occurrenceIndices.size() == 0) {
            this.createWarningDialog("Term '" + targetText + "' was not found in the current file.");
            this.resetFindReplace();
            return false;
        }
        return true;
    }

    /**
     * Handles FindAll - finds all occurrances of a given search term
     */
    private void handleFindAll(String findText) {
        CodeArea activeCodeArea = this.getActiveCodeArea();
        this.findTargetText(activeCodeArea, findText);
        // changes the text to be italicized and colored
        for (Integer i: this.occurrenceIndices) {
            activeCodeArea.setStyleClass(i, i + findText.length(), "find");
        }
        this.resetFindReplace();
    }

    /**
     * Uses the currently selected instance of findText and replaces it with replaceText
     *
     * @param findText text to be found
     * @param replaceText text to replace the current findText
     */
    private void handleReplace(String findText, String replaceText){
        CodeArea codeArea = this.getActiveCodeArea();

        // check if this is the first time to find the given text
        if(!findText.equals(this.targetText)){
            this.resetFindReplace();
            this.targetText = findText;
            if (!this.findTargetText(codeArea, findText)) return;
        }

        //Check if there are any occurrence of the text to be found
        if (this.occurrenceIndices == null || this.occurrenceIndices.isEmpty()){
            this.createWarningDialog("Term '" + targetText + "' was not found in the current file.");
            return;
        }

        String codeAreaText = codeArea.getText();

        String newContent;
        String beforeFind;
        String afterFind;

        //Find the text indices of the text to replace
        int startReplace = this.occurrenceIndices.get(this.curOccurrenceIndex);
        int endReplace = startReplace + this.targetText.length();

        //Find the substring before and after the text to be replaced
        if (startReplace != 0) {
            beforeFind = codeAreaText.substring(0, startReplace);
            afterFind = codeAreaText.substring(endReplace);
        }
        else{
            beforeFind = "";
            afterFind = codeAreaText.substring(endReplace);
        }

        //Combine the substrings and the replacetext to get the new content in the codearea
        newContent = beforeFind + replaceText + afterFind;

        //Update the index of the occurrences
        int lengthDiff = replaceText.length() - this.targetText.length();
        for(int i = this.curOccurrenceIndex + 1; i < this.occurrenceIndices.size(); i++){
            this.occurrenceIndices.set(i, this.occurrenceIndices.get(i) + lengthDiff);
        }

        codeArea.replaceText(newContent);

        //High light the next found text if there are any
        if(!this.occurrenceIndices.isEmpty()) {
            int currentIdx;
            if (this.curOccurrenceIndex >= this.occurrenceIndices.size() - 1){
                currentIdx = this.occurrenceIndices.get(0);
            } else {
                currentIdx = this.occurrenceIndices.get(this.curOccurrenceIndex + 1);
            }
            if (this.occurrenceIndices.size() != 1) {
                codeArea.selectRange(currentIdx, currentIdx + this.targetText.length());
            }
        }

        //Remove the index of the occurrence that was replaced
        int removeIdx = this.curOccurrenceIndex;
        this.occurrenceIndices.remove(removeIdx);

        //Update the index of the found text currently highlighted
        if (this.curOccurrenceIndex == this.occurrenceIndices.size()){
            this.curOccurrenceIndex = 0;
        }

    }

    /**
     * Replaces all the text that is findText with replaceText
     *
     * @param findText text to be replaced
     * @param replaceText text to replace this.textToFind
     */
    private void handleReplaceAll(String findText, String replaceText){
        CodeArea codeArea = this.getActiveCodeArea();
        this.findTargetText(codeArea, findText);
        String newContent = codeArea.getText().replaceAll(findText, replaceText);
        codeArea.replaceText(newContent);
        this.resetFindReplace();
    }

    /**
     * Resets find & replace settings.
     */
    private void resetFindReplace() {
        this.occurrenceIndices.clear(); //all have been replaced.
        this.curOccurrenceIndex = 0;
        this.targetText = null;
    }

    /**
     * Helper method to get active styled java code area.
     *
     * @return active styled java code area.
     */
    private JavaCodeArea getActiveCodeArea() {
        return JavaTabPane.getCurrentCodeArea(this.tabPane);
    }

    /**
     * Create a dialog notifying the user warnings.
     *
     * @param msg the warning message to be displayed
     */
    private void createWarningDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Warning");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }
}
