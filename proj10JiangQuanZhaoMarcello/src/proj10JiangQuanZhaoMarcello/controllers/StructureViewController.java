/*
 * File: StructureViewController.java
 * CS361 Project 10
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 *        Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 11/17/2018
 * This file contains the StructureViewController class, handling file structure view related actions.
 */

package proj10JiangQuanZhaoMarcello.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import proj10JiangQuanZhaoMarcello.resource.Java8Lexer;
import proj10JiangQuanZhaoMarcello.resource.Java8BaseListener;
import proj10JiangQuanZhaoMarcello.resource.Java8Parser;
import proj10JiangQuanZhaoMarcello.java.JavaCodeArea;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * Controller that manages the generation and display of the structure of the
 * java code in the file currently being viewed.
 *
 * @author Evan Savillo
 * @author Yi Feng
 * @author Zena Abulhab
 * @author Melody Mao
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 */
public class StructureViewController {
    /**
     * treeItemLineNumMap that maps tree item and line number to navigate to
     */
    private Map<TreeItem, Integer> treeItemLineNumMap;
    /**
     * ParseTreeWalker
     */
    private final ParseTreeWalker walker;
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab,File> tabFileMap;
    /**
     * treeView for displaying the file structure
     */
    private TreeView<String> fileStructTree;
    /**
     * Checkbox that toggles fileStructTree
     */
    private CheckBox displayFileStructCheckBox;
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;
    /**
     * A StructureViewWorker object constructing a structure view of a file in a separate thread.
     */
    private StructureViewWorker structureViewWorker;

    /**
     * Constructor for StructureViewController class
     */
    public StructureViewController() {
        this.walker = new ParseTreeWalker();
        this.treeItemLineNumMap = new HashMap<>();
    }

    /**
     * Sets the tabFileMap.
     *
     * @param tabFileMap HashMap mapping the tabs and the associated files
     */
    public void setTabFileMap(Map<Tab,File> tabFileMap) { this.tabFileMap = tabFileMap; }

    /**
     * Sets the display structure view checkbox.
     *
     * @param displayFileStructCheckBox  Checkbox that toggles fileStructTree
     */
    public void setDisplayFileStructCheckBox(CheckBox displayFileStructCheckBox) {
        this.displayFileStructCheckBox = displayFileStructCheckBox;
    }

    /**
     * Sets the tabPane.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(TabPane tabPane) { this.tabPane = tabPane; }

    /**
     * Takes in the fxml item treeView from main Controller.
     *
     * @param fileStructTree TreeView item representing structure display
     */
    public void setTreeView(TreeView fileStructTree) { this.fileStructTree = fileStructTree; }

    /**
     * Initializes the file structure view.
     * Adds event listeners to handle tree pane split and to update the file structure tree view.
     */
    public void initialize(SplitPane treeSplitPane) {
        SplitPane.Divider divider = treeSplitPane.getDividers().get(0);
        this.structureViewWorker = new StructureViewWorker();

        // toggles the side panel
        this.displayFileStructCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                divider.setPosition(0.0);
            else
                divider.setPosition(0.5);
            this.updateStructureView();
        });

        // updates the file structure view whenever the tab selection changes
        // e.g., open tab, remove tab, select another tab
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            this.updateStructureView();
        });
    }

    /**
     * Helper method to return the file object in the current tab.
     *
     * @return the File object of the item selected in the tab pane
     */
    private File getCurrentFile() {
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            return this.tabFileMap.get(selectedTab);
        } else {
            return null;
        }
    }

    /**
     * Helper method to return the code area currently being viewed in the current tab
     *
     * @return the JavaCodeArea object for the open tab
     */
    private JavaCodeArea getCurrentCodeArea() {
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            return (JavaCodeArea) ((VirtualizedScrollPane) selectedTab.getContent()).getContent();
        } else {
            return null;
        }
    }

    /**
     * A StructureViewWorker subclass handling constructing a structure view in a separate thread in the background.
     * StructureViewWorker extends the javafx Service class.
     */
    public class StructureViewWorker extends Service<Boolean> {
        /**
         * Overrides the createTask method in Service class.
         * Constructs a structure view of a file.
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
                    updateStructureViewInThread();
                    return true;
                }
            };
        }
    }

    /**
     * Helper method to update the structure view in a separated thread.
     */
    private void updateStructureViewInThread() {
        JavaCodeArea currentCodeArea = this.getCurrentCodeArea();
        File currentFile = this.getCurrentFile();

        if (currentCodeArea != null) {
            if (currentFile != null) {
                String fileName = currentFile.getName();
                if (fileName.endsWith(".java" )) {
                    this.generateStructureTree(currentCodeArea.getText());
                }
            } else {
                this.resetStructureView();
            }
        }
    }

    /**
     * Parses and generates the structure view for the currently open code area.
     */
    public void updateStructureView() { this.structureViewWorker.restart(); }

    /**
     * Parses a file thereby storing contents as TreeItems in our special tree.
     * @param fileContents the file to be parsed
     */
    public void generateStructureTree(String fileContents) {
        TreeItem<String> newRoot = new TreeItem<>(fileContents);

        // build lexer, parser, and parse tree for the given file
        Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(fileContents));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        // walk through parse tree with listening for code structure elements
        CodeStructureListener codeStructureListener = new CodeStructureListener(newRoot, this.treeItemLineNumMap);
        this.walker.walk(codeStructureListener, tree);

        this.setRootNode(newRoot);
    }

    /**
     * Handles the file structure item click actions.
     * Jumps to the line where the selected class/method/field is declared.
     */
    public void handleFileStructTreeItemClicked() {
        TreeItem selectedTreeItem = this.fileStructTree.getSelectionModel().getSelectedItem();
        JavaCodeArea currentCodeArea = this.getCurrentCodeArea();
        if (selectedTreeItem != null) {
            int lineNum = this.getTreeItemLineNum(selectedTreeItem);
            if (currentCodeArea != null) {
                currentCodeArea.showParagraphAtTop(lineNum - 1);
            }
        }
    }

    /**
     * Sets the currently displaying File TreeItem<String> View.
     *
     * @param root root node corresponding to currently displaying file
     */
    private void setRootNode(TreeItem<String> root) {
        Platform.runLater(() -> {
            this.fileStructTree.setRoot(root);
            this.fileStructTree.setShowRoot(false);
        });
    }

    /**
     * Clears the currently open structure view of all nodes
     * Sets the currently displaying file to nothing.
     */
    public void resetStructureView() { this.setRootNode(null); }

    /**
     * Returns the line number currently associated with the specified tree item
     *
     * @param treeItem Which TreeItem to get the line number of
     * @return the line number corresponding with that tree item
     */
    public Integer getTreeItemLineNum(TreeItem treeItem) { return this.treeItemLineNumMap.get(treeItem); }

    /**
     * Private helper class that listens for code structure declarations
     * (classes, fields, methods) during a parse tree walk and builds a
     * TreeView subtree representing the code structure.
     */
    private class CodeStructureListener extends Java8BaseListener {
        Image classPic;
        Image methodPic;
        Image fieldPic;
        private TreeItem<String> currentNode;
        private Map<TreeItem, Integer> treeItemIntegerMap;

        /**
         * Creates a new CodeStructureListener that builds a subtree from the given root TreeItem
         *
         * @param root root TreeItem to build subtree from
         */
        public CodeStructureListener(TreeItem<String> root, Map<TreeItem, Integer> treeItemIntegerMap) {
            this.currentNode = root;
            this.treeItemIntegerMap = treeItemIntegerMap;

            try {
                this.classPic = new Image(new FileInputStream(System.getProperty("user.dir") + "/include/c.png"));
                this.methodPic = new Image(new FileInputStream(System.getProperty("user.dir") + "/include/m.png"));
                this.fieldPic = new Image(new FileInputStream(System.getProperty("user.dir") + "/include/f.png"));
            }
            catch (IOException e) {
                System.out.println("Error Loading Images");
            }
        }

        /**
         * Starts a new subtree for the class declaration entered
         */
        @Override public void enterNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {
            // get class name
            TerminalNode node = ctx.Identifier();
            String className = node.getText();

            // add class to TreeView under the current class tree
            // set up the icon
            // store the line number of its declaration
            TreeItem<String> newNode = new TreeItem<>(className);
            newNode.setGraphic(new ImageView(this.classPic));
            newNode.setExpanded(true);
            this.currentNode.getChildren().add(newNode);
            //move current node into new subtree
            this.currentNode = newNode;
            this.treeItemIntegerMap.put(newNode, ctx.getStart().getLine());
        }

        /**
         * Ands the new subtree for the class declaration exited, returns traversal to parent node
         */
        @Override public void exitNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {
            this.currentNode = this.currentNode.getParent(); //move current node back to parent
        }

        /**
         * Adds a child node for the field entered under the TreeItem for the current class
         */
        @Override public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
            // get field name
            TerminalNode node = ctx.variableDeclaratorList().variableDeclarator(0).variableDeclaratorId().Identifier();
            String fieldName = node.getText();

            // add field to TreeView under the current class tree
            // set up the icon
            // store the line number of its declaration
            TreeItem<String> newNode = new TreeItem<>(fieldName);
            newNode.setGraphic(new ImageView(this.fieldPic));
            this.currentNode.getChildren().add(newNode);
            this.treeItemIntegerMap.put(newNode, ctx.getStart().getLine());
        }

        /**
         * Adds a child node for the method entered under the TreeItem for the current class
         */
        @Override public void enterMethodHeader(Java8Parser.MethodHeaderContext ctx) {
            // get method name
            TerminalNode nameNode = ctx.methodDeclarator().Identifier();
            String methodName = nameNode.getText();

            // add method to TreeView under the current class tree
            // set up the icon
            // store the line number of its declaration
            TreeItem<String> newNode = new TreeItem<>(methodName);
            newNode.setGraphic(new ImageView(this.methodPic));
            this.currentNode.getChildren().add(newNode);
            this.treeItemIntegerMap.put(newNode, ctx.getStart().getLine());
        }
    }
}