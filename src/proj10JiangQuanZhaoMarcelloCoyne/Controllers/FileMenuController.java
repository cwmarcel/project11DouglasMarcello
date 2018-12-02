/*
 * File: FileMenuController.java
 * F18 CS361 Project 9
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello, Michael Coyne
 * Date: 11/17/2018
 * This file contains the FileMenuController class, handling File menu related actions.
 */

package proj10JiangQuanZhaoMarcelloCoyne.Controllers;

import proj10JiangQuanZhaoMarcelloCoyne.Java.JavaCodeArea;
import proj10JiangQuanZhaoMarcelloCoyne.Java.JavaTabPane;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;

/**
 * FileMenuController handles File menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 * @author Michael Coyne
 */
public class FileMenuController {
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;
    /**
     * Dark Mode menu item defined in Main.fxml
     */
    private RadioMenuItem darkModeMenuItem;
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab,File> tabFileMap;
    /**
     * ContextMenuController handling context menu actions
     */
    private ContextMenuController contextMenuController;
    /**
     * DirectoryController handling Directory Tree actions
     */
    private DirectoryController directoryController;
    /**
     * StructureViewController handling the current file's treeStructure View
     */
    private StructureViewController structureViewController;

    /**
     * Sets the tabPane.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(TabPane tabPane) { this.tabPane = tabPane; }

    /**
     * Sets the tabFileMap.
     *
     * @param tabFileMap HashMap mapping the tabs and the associated files
     */
    public void setTabFileMap(Map<Tab,File> tabFileMap) { this.tabFileMap = tabFileMap; }

    /** 
     * Sets the darkModeMenuItem.
     *
     * @param darkModeMenuItem a boolean value indicating whether the dark mode is selected
     */
    public void setDarkModeMenuItem(RadioMenuItem darkModeMenuItem) { this.darkModeMenuItem = darkModeMenuItem; }

    /**
     * Sets the contextMenuController.
     *
     * @param contextMenuController ContextMenuController handling context menu actions
     */
    public void setContextMenuController(ContextMenuController contextMenuController) {
        this.contextMenuController = contextMenuController;
    }

    /**
     * Sets the directory controller.
     *
     * @param directoryController DirectoryController handling Directory Tree actions
     */
    public void setDirectoryController(DirectoryController directoryController) {
        this.directoryController = directoryController;
    }

    /**
     * Sets the structure view controller.
     *
     * @param structureViewController StructureViewController handling the current file's treeStructure View
     */
    public void setStructureViewController(StructureViewController structureViewController) {
        this.structureViewController = structureViewController;
    }

    /**
     * Helper method to get the text content of a specified file.
     *
     * @param file File to get the text content from
     * @return the text content of the specified file; null if an error occurs when reading the specified file.
     */
    private String getFileContent(File file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file.toURI())));
        } catch (Exception ex) {
            this.createErrorDialog("Reading File", "Cannot read " + file.getName() + ".");
            return null;
        }
    }

    /**
     * Helper method to save the input string to a specified file.
     *
     * @param content String that is saved to the specified file
     * @param file File that the input string is saved to
     * @return true is the specified file is successfully saved; false if an error occurs when saving the specified file.
     */
    public boolean saveFileContent(String content, File file){
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (IOException ex) {
            this.createErrorDialog("Saving File", "Cannot save to " + file.getName() + ".");
            return false;
        }
    }

    /**
     * Helper method to check if the content of the specified StyledCodeArea
     * matches the content of the specified File.
     *
     * @param javaCodeArea StyledJavaCodeArea to compare with the the specified File
     * @param file File to compare with the the specified StyledCodeArea
     * @return true if the content of the StyledCodeArea matches the content of the File; false if not
     */
    public boolean fileContainsMatch(JavaCodeArea javaCodeArea, File file) {
        String styledCodeAreaContent = javaCodeArea.getText();
        String fileContent = this.getFileContent(file);
        return styledCodeAreaContent.equals(fileContent);
    }

    /**
     * Helper method to handle closing tag action.
     * Checks if the text content within the specified tab window should be saved.
     *
     * @param tab Tab to be closed
     * @param ifSaveEmptyFile boolean false if not to save the empty file; true if to save the empty file
     * @return true if the tab needs saving; false if the tab does not need saving.
     */
    public boolean tabNeedsSaving(Tab tab, boolean ifSaveEmptyFile) {
        JavaCodeArea activeStyledCodeArea = JavaTabPane.getCodeArea(tab);
        // check whether the embedded text has been saved or not
        if (this.tabFileMap.get(tab) == null) {
            // if the newly created file is empty, don't save
            if (!ifSaveEmptyFile) {
                if (activeStyledCodeArea.getText().equals("")) {
                    return false;
                }
            }
            return true;
        }
        // check whether the saved file match the tab content or not
        else {
            return !this.fileContainsMatch(activeStyledCodeArea, this.tabFileMap.get(tab));
        }
    }

    /**
     * Helper method to handle closing tag action.
     * Removed the tab from the tab file mapping and from the TabPane.
     *
     * @param tab Tab to be closed
     */
    private void removeTab(Tab tab) {
        this.tabFileMap.remove(tab);
        this.tabPane.getTabs().remove(tab);
    }

    /**
     * Helper method to create a confirmation dialog window.
     *
     * @param title the title of the confirmation dialog
     * @param headerText the header text of the confirmation dialog
     * @param contentText the content text of the confirmation dialog
     * @return 0 if the user clicks No button; 1 if the user clicks the Yes button; 2 if the user clicks cancel button.
     */
    public int createConfirmationDialog(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonNo) { return 0; }
        else if (result.get() == buttonYes){ return 1; }
        else { return 2; }
    }

    /**
     * set the appearance mode of the code area
     *
     * @param newStyledJavaCodeArea new code area created
     */
    private void setupCodeAreaStyleMode(JavaCodeArea newStyledJavaCodeArea) {
        String newcss = getClass().getResource("../css/DarkModeStyledJavaCodeArea.css").toExternalForm();
        if (this.darkModeMenuItem.isSelected()) {
            newStyledJavaCodeArea.getStylesheets().add(newcss);
        } else {
            newStyledJavaCodeArea.getStylesheets().remove(newcss);
        }

        newStyledJavaCodeArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
            selectedTab.setStyle("-fx-text-base-color: green");
        });
    }

    /**
     * Helper method to create a new tab.
     *
     * @param contentString the contentString being added into the styled code area; empty string if
     *                      creating an empty window
     * @param filename the name of the file opened; "untitled" if creating an empty window
     * @param file File opened; null if creating an empty window
     */
    private void createTab(String contentString, String filename, File file) {
        JavaCodeArea newStyledJavaCodeArea = new JavaCodeArea();
        newStyledJavaCodeArea.setWrapText(true);
        newStyledJavaCodeArea.appendText(contentString);
        this.contextMenuController.setupStyledJavaCodeAreaContextMenuHandler(newStyledJavaCodeArea);
        this.setupCodeAreaStyleMode(newStyledJavaCodeArea);

        Tab newTab = new Tab();
        newTab.setText(filename);
        newTab.setContent(new VirtualizedScrollPane<>(newStyledJavaCodeArea));
        newTab.setOnCloseRequest(event -> this.handleCloseAction(event));

        this.tabPane.getTabs().add(newTab);
        this.contextMenuController.setupTabContextMenuHandler(newTab);
        this.tabPane.getSelectionModel().select(newTab);
        this.tabFileMap.put(newTab, file);
    }

    /**
     * Helper method to handle closing tag action.
     * If the text embedded in the tab window has not been saved yet,
     * or if a saved file has been changed, asks the user if to save
     * the file via a dialog window.
     *
     * @param tab Tab to be closed
     * @return true if the tab is closed successfully; false if the user clicks cancel.
     */
    private boolean closeTab(Tab tab) {
        // if the file has not been saved or has been changed
        // pop up a dialog window asking whether to save the file
        if (this.tabNeedsSaving(tab, false)) {
            int buttonClicked = this.createConfirmationDialog("Save Changes?",
                    "Do you want to save the changes you made?",
                    "Your changes will be lost if you don't save them.");

            // if user presses No button, close the tab without saving
            if (buttonClicked == 0) {
                this.removeTab(tab);
                this.structureViewController.resetStructureView();
                return true;
            }
            // if user presses Yes button, close the tab and save the tab content
            else if (buttonClicked == 1) {
                if (this.handleSaveAction()) {
                    this.removeTab(tab);
                    this.structureViewController.resetStructureView();
                    return true;
                }
                return false;
            }
            // if user presses cancel button
            else {
                return false;
            }
        }
        // if the file has not been changed, close the tab
        else {
            this.structureViewController.resetStructureView();
            this.removeTab(tab);
            return true;
        }
    }

    /**
     * Creates a error dialog displaying message of any error encountered.
     *
     * @param errorTitle String of the error title
     * @param errorString String of error message
     */
    public void createErrorDialog(String errorTitle, String errorString) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(errorTitle + " Error");
        alert.setHeaderText("Error for " + errorTitle);
        alert.setContentText(errorString);
        alert.showAndWait();
    }

    /**
     * Checks whether a file embedded in the specified tab should be saved before Scanning.
     * Pops up a dialog asking whether the user wants to save the file before compiling.
     * Saves the file if the user agrees so.
     *
     * @return 0 if user clicked NO button; 1 if user clicked OK button;
     *         2 is user clicked Cancel button; -1 is no saving is needed
     */
    public int checkSaveBeforeScan() {
        Tab currentTab = this.tabPane.getSelectionModel().getSelectedItem();
        // if the file has not been saved or has been changed
        if (this.tabNeedsSaving(currentTab, true)) {
            int buttonClicked = this.createConfirmationDialog("Save Changes?",
                    "Do you want to save the changes before compiling?",
                    "Your recent file changes would not be compiled if not saved.");
            // if user presses Yes button
            if (buttonClicked == 1) {
                this.handleSaveAction();
            }
            return buttonClicked;
        }
        return -1;
    }

    /**
     * Handles the New button action.
     * Opens a styled code area embedded in a new tab.
     * Sets the newly opened tab to the the topmost one.
     */
    public void handleNewAction() { this.createTab("", "untitled", null); }

    /**
     * Handles the open button action.
     * Opens a dialog in which the user can select a file to open.
     * If the user chooses a valid file, a new tab is created and the file is loaded into the styled code area.
     * If the user cancels, the dialog disappears without doing anything.
     */
    public void handleOpenAction() {
        FileChooser fileChooser = new FileChooser();
        File openFile = fileChooser.showOpenDialog(this.tabPane.getScene().getWindow());
        this.handleOpenFile(openFile);
        this.directoryController.createDirectoryTree();
    }

    /**
     * Handles opening the given file object
     * @param file the File to open in a new tab
     */
    public void handleOpenFile(File file) {
        if(file != null) {
            // if the selected file is already open, it cannot be opened twice
            // the tab containing this file becomes the current (topmost) one
            for (Map.Entry<Tab, File> entry : this.tabFileMap.entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getValue().equals(file)) {
                        this.tabPane.getSelectionModel().select(entry.getKey());
                        return;
                    }
                }
            }
            String contentString = this.getFileContent(file);
            if (contentString == null) return;
            this.createTab(contentString, file.getName(), file);
            this.structureViewController.updateStructureView();
        }
    }

    /**
     * Handles the save button action.
     * If a styled code area was not loaded from a file nor ever saved to a file,
     * behaves the same as the save as button.
     * If the current styled code area was loaded from a file or previously saved
     * to a file, then the styled code area is saved to that file.
     *
     * @return true if save as successfully; false if cancels or an error occurs when saving the file.
     */
    public boolean handleSaveAction(){
        // get the selected tab from the tab pane
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();

        // if the tab content was not loaded from a file nor ever saved to a file
        // save the content of the active styled code area to the selected file path
        if (this.tabFileMap.get(selectedTab) == null) {
             return this.handleSaveAsAction();
        }
        // if the current styled code area was loaded from a file or previously saved to a file,
        // then the styled code area is saved to that file
        else {
            JavaCodeArea activeStyledJavaCodeArea = JavaTabPane.getCodeArea(selectedTab);
            if(!this.saveFileContent(activeStyledJavaCodeArea.getText(), this.tabFileMap.get(selectedTab))) {
                return false;
            }
            selectedTab.setStyle("-fx-text-base-color: black");
            this.structureViewController.updateStructureView();
            return true;
        }
    }

    /**
     * Handles the Save As button action.
     * Shows a dialog in which the user is asked for the name of the file into
     * which the contents of the current styled code area are to be saved.
     * If the user enters any legal name for a file and presses the OK button in the dialog,
     * then creates a new text file by that name and write to that file all the current
     * contents of the styled code area so that those contents can later be reloaded.
     * If the user presses the Cancel button in the dialog, then the dialog closes and no saving occurs.
     *
     * @return true if save as successfully; false if cancels or an error occurs when saving the file.
     */
    public boolean handleSaveAsAction() {
        FileChooser fileChooser = new FileChooser();
        File saveFile = fileChooser.showSaveDialog(this.tabPane.getScene().getWindow());

        if (saveFile != null) {
            // get the selected tab from the tab pane
            Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
            JavaCodeArea activeStyledJavaCodeArea = JavaTabPane.getCurrentCodeArea(this.tabPane);
            if(!this.saveFileContent(activeStyledJavaCodeArea.getText(), saveFile)) {
                return false;
            }
            // set the title of the tab to the name of the saved file
            selectedTab.setText(saveFile.getName());
            selectedTab.setStyle("-fx-text-base-color: black");

            // map the tab and the associated file
            this.tabFileMap.put(selectedTab, saveFile);
            this.directoryController.createDirectoryTree();
            this.structureViewController.updateStructureView();
            return true;
        }
        return false;
    }

    /**
     * Handles the close button action.
     * If the current styled code area has already been saved to a file, then the current tab is closed.
     * If the current styled code area has been changed since it was last saved to a file, a dialog
     * appears asking whether you want to save the text before closing it.
     *
     * @param event Event object
     */
    public void handleCloseAction(Event event) {
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();

        // selectedTab is null if this method is evoked by closing a tab
        // in this case the selectedTab tab should be the tab that evokes this method
        if (selectedTab == null) {
            selectedTab = (Tab)event.getSource();
        }
        // if the user select to not close the tab, then we consume the event (not performing the closing action)
        if (!this.closeTab(selectedTab)) {
            event.consume();
        }
        this.structureViewController.updateStructureView();
    }

    /**
     * Handles the Exit button action.
     * Exits the program when the Exit button is clicked.
     *
     * @param event Event object
     */
    public void handleExitAction(Event event) {
        ArrayList<Tab> tabList = new ArrayList<Tab>(this.tabFileMap.keySet());
        for (Tab currentTab: tabList) {
            this.tabPane.getSelectionModel().select(currentTab);
            if (!this.closeTab(currentTab)){
                event.consume();
                return;
            }
        }
        System.exit(0);
    }

    /**
     * Handles the About button action.
     * Creates a dialog window that displays the authors' names.
     */
    public void handleAboutAction() {
        // create a information dialog window displaying the About text
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);

        // enable to close the window by clicking on the red cross on the top left corner of the window
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        // set the title and the content of the About window
        dialog.setTitle("About");
        dialog.setHeaderText("Authors");
        dialog.setContentText("---- Project 4 ---- \nLiwei Jiang\nDanqing Zhao\nWyett MacDonald\nZeb Keith-Hardy" +
                          "\n\n---- Project 5 ---- \nLiwei Jiang\nMartin Deutsch\nMelody Mao\nTatsuya Yakota" +
                          "\n\n---- Project 6 ---- \nLiwei Jiang\nTracy Quan\nChris Marcello" +
                          "\n\n---- Project 7 ---- \nLiwei Jiang\nTracy Quan\nChris Marcello\nDanqing Zhao\nMichael Coyne" +
                          "\n\n---- Project 9 ---- \nLiwei Jiang\nTracy Quan\nChris Marcello\nDanqing Zhao\nMichael Coyne");

        // enable to resize the About window
        dialog.setResizable(true);
        dialog.showAndWait();
    }
}