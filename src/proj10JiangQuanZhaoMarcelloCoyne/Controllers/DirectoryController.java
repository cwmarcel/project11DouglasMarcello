/*
 * File: DirectoryController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 *        Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello, Michael Coyne
 * Date: 11/17/2018
 * This file contains the DirectoryController class, handling the file directory portion of the GUI.
 */

package proj10JiangQuanZhaoMarcelloCoyne.Controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

/**
 * This controller handles directory related actions.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 * @author Michael Coyne
 */ 
public class DirectoryController {
    /**
     * the tree view representing the directory
     */
    private TreeView directoryTree;
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab, File> tabFileMap;
    /**
     * A HashMap mapping the TreeItems and associated files
     */
    private Map<TreeItem<String>, File> treeItemFileMap;
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;
    /**
     * FileMenuController defined in main controller
     */
    private FileMenuController fileMenuController;
    /**
     * A DirectoryTreeWorker object constructing a directory tree in a separate thread.
     */
    private DirectoryTreeWorker directoryTreeWorker;

    /**
     * Constructor of the DirectoryController class.
     */
    public DirectoryController() { this.directoryTreeWorker = new DirectoryTreeWorker(); }

    /**
     * Sets the directory tree from Main.fxml.
     * Adds listener to listen for clicks in the directory tree.
     *
     * @param directoryTree the directory tree
     */
    public void setDirectoryTree(TreeView directoryTree) {
        this.directoryTree = directoryTree;
        this.treeItemFileMap = new HashMap<>();
        this.directoryTree.setOnMouseClicked(event -> this.handleDirectoryItemClicked(event));
    }

    /**
     * Sets the tabFileMap.
     *
     * @param tabFileMap HashMap mapping the tabs and the associated files
     */
    public void setTabFileMap(Map<Tab, File> tabFileMap) { this.tabFileMap = tabFileMap; }

    /**
     * Sets the tabPane and adds listener to tab selection to switch directories based on open file.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> this.createDirectoryTree());
    }

    /**
     * Sets the FileMenuController.
     *
     * @param fileMenuController FileMenuController created in main Controller.
     */
    public void setFileMenuController(FileMenuController fileMenuController) {
        this.fileMenuController = fileMenuController;
    }

    /**
     * Returns the directory tree for the given file.
     *
     * @param file the file
     * @return the root TreeItem of the tree
     */
    private TreeItem<String> getNode(File file) {
        // create root, which is returned at the end
        TreeItem<String> root = new TreeItem<>(file.getName());
        treeItemFileMap.put(root, file);

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                // recursively traverse file directory
                root.getChildren().add(getNode(f));
            } else {
                if (f.getName().endsWith(".java")) {
                    TreeItem<String> leaf = new TreeItem<>(f.getName());
                    root.getChildren().add(leaf);
                    treeItemFileMap.put(leaf, f);
                }
            }
        }
        return root;
    }

    /**
     * A DirectoryTreeWorker subclass handling constructing a directory tree in a separate thread in the background.
     * DirectoryTreeWorker extends the javafx Service class.
     */
    public class DirectoryTreeWorker extends Service<Boolean> {
        /**
         * Overrides the createTask method in Service class.
         * Constructs a directory tree.
         *
         * @return true
         */
        @Override protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                /**
                 * Called when we execute the start() method of a CompileRunWorker object
                 * Scans the file.
                 *
                 * @return true if the program compiles successfully;
                 *         false otherwise.
                 */
                @Override protected Boolean call() {
                    createDirectoryTreeInThread();
                    return true;
                }
            };
        }
    }

    /**
     * Helper method to create a directory tree in a separate thread.
     */
    private void createDirectoryTreeInThread() {
        File curFile = this.tabFileMap.get(this.tabPane.getSelectionModel().getSelectedItem());
        // create the directory tree
        if (curFile != null) {
            Platform.runLater(() -> {
                this.directoryTree.setRoot(this.getNode(curFile.getParentFile()));
                this.directoryTree.getRoot().setExpanded(true);
            });
        }
    }

    /**
     * Adds the directory tree for the current file to the IDE.
     */
    public void createDirectoryTree() {
        this.directoryTreeWorker.restart();
    }

    /**
     * Event handler to open a file selected from the directory
     *
     * @param event a MouseEvent object
     */
    private void handleDirectoryItemClicked(MouseEvent event) {
        TreeItem selectedItem = (TreeItem) this.directoryTree.getSelectionModel().getSelectedItem();
        // only open java file if double clicked
        // ignore double click on the root nodes
        if (event.getClickCount() == 2 && !event.isConsumed() && selectedItem.getChildren().size() == 0) {
            event.consume();
            String fileName = (String) selectedItem.getValue();
            if (fileName.endsWith(".java") || fileName.endsWith(".btm")) {
                this.fileMenuController.handleOpenFile(this.treeItemFileMap.get(selectedItem));
            }
        }
    }
}