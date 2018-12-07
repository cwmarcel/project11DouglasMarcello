/*
 * File: ToolBarController.java
 * F18 CS361 Project 10
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 11/17/2018
 * This file contains the ToolBarController class, handling Toolbar related actions.
 */

package proj10JiangQuanZhaoMarcello.controllers;

import java.io.*;
import java.util.Map;
import java.util.List;
import java.util.concurrent.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.concurrent.Task;
import javafx.concurrent.Service;
import javafx.application.Platform;
import org.fxmisc.richtext.StyleClassedTextArea;
import proj10JiangQuanZhaoMarcello.java.*;
import proj10JiangQuanZhaoMarcello.bantam.parser.Parser;
import proj10JiangQuanZhaoMarcello.bantam.ast.*;
import proj10JiangQuanZhaoMarcello.bantam.lexer.*;
import proj10JiangQuanZhaoMarcello.bantam.util.*;
import proj10JiangQuanZhaoMarcello.bantam.util.Error;
import proj10JiangQuanZhaoMarcello.bantam.treedrawer.Drawer;

/**
 * ToolbarController handles Toolbar related actions.
 *
 * @author Liwei Jiang
 * @author Chris Marcello
 * @author Tracy Quan
 * @author Danqing Zhao
 */
public class ToolBarController {
    /**
     * Console defined in Main.fxml
     */
    private StyleClassedTextArea console;
    /**
     * Mutex lock to control input and output threads' access to console
     */
    private Semaphore mutex;
    /**
     * The FileMenuController
     */
    private FileMenuController fileMenuController;
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab,File> tabFileMap;
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;
    /**
     * A ScanWorker object scan a Java file in a separate thread.
     */
    private ScanWorker scanWorker;
    /**
     * A Scanner object to scan a Java file into a list of tokens.
     */
    private Scanner scanner;
    /**
     * A ParseWorker object to parse a Java file in a separate thread.
     */
    private ParseWorker parseWorker;
    /**
     * A Parser object to parse a Java file into a program.
     */
    private Parser parser;
    /**
     * A String to store the list of tokens.
     */
    private String tokenStr;
    /**
     * A Program to store the parsed program.
     */
    private Program program;
    /**
     * A Drawer to draw the AST once the program has been parsed.
     */
    private Drawer drawer;

    /**
     * Initializes the ToolBarController controller.
     * Sets the Semaphore, the CompileWorker and the CompileRunWorker.
     * Disables the user to move the focus by mouse or keyboard in the console.
     */
    public void initialize() {
        this.mutex = new Semaphore(1);
        this.scanWorker = new ScanWorker();
        this.parseWorker = new ParseWorker();
        this.drawer = new Drawer();
        this.disableConsoleFocusMove();
    }

    /**
     * Disables the user to move the focus by mouse or keyboard in the console.
     */
    public void disableConsoleFocusMove() {
        console.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case LEFT:
                        keyEvent.consume();
                    case UP:
                        keyEvent.consume();
                    case DOWN:
                        keyEvent.consume();
                    case RIGHT:
                        keyEvent.consume();
                }
            }
        });

        console.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton() == MouseButton.PRIMARY) {
                    mouseEvent.consume();
                    if(!console.isFocused()) {
                        Platform.runLater(() -> console.requestFocus());
                    }
                }
            }
        });
    }

    /**
     * Sets the tabFileMap.
     *
     * @param tabFileMap HashMap mapping the tabs and the associated files
     */
    public void setTabFileMap(Map<Tab,File> tabFileMap) { this.tabFileMap = tabFileMap; }

    /**
     * Sets the console pane.
     *
     * @param console StyleClassedTextArea defined in Main.fxml
     */
    public void setConsole(StyleClassedTextArea console) { this.console = console; }

    /**
     * Sets the FileMenuController.
     *
     * @param fileMenuController FileMenuController created in main Controller.
     */
    public void setFileMenuController(FileMenuController fileMenuController) {
        this.fileMenuController = fileMenuController;
    }

    /**
     * Sets the TabPane
     * @param tabPane TabPane created in main Controller.
     */
    public void setTabPane(TabPane tabPane){ this.tabPane = tabPane; }

    /**
     * Gets the ScanWorker.
     *
     * @return ScanWorker object
     */
    public ScanWorker getScanWorker() { return this.scanWorker; }

    /**
     * Gets the ParseWorker.
     *
     * @return ParseWorker object
     */
    public ParseWorker getParseWorker() { return this.parseWorker; }

    /**
     * Helper method for displaying the list of tokens into a new tab.
     */
    private void outputToNewTab(String tokenStr) throws java.lang.InterruptedException {
        this.mutex.tryAcquire();
        Platform.runLater(() ->{
            this.fileMenuController.handleNewAction();
            JavaCodeArea newCodeArea = JavaTabPane.getCurrentCodeArea(this.tabPane);
            newCodeArea.appendText(tokenStr);
        });
        Thread.sleep(1);
        this.mutex.release();
    }

    /**
     * Helper method for displaying scanner the errors in the console.
     *
     * @param errorList a list of errors
     * @param runType the String describing the type of the running program (scanning or parsing)
     * @throws java.lang.InterruptedException
     */
    private void errorToConsole(List<Error> errorList, String runType) throws java.lang.InterruptedException {
        this.mutex.tryAcquire();
        Platform.runLater(() ->{
            for (Error err: errorList){
                this.console.appendText(err.toString()+"\n");
            }
            if (errorList.size()==0){
                this.console.appendText(runType + " was successful!");
                this.console.setStyleClass(0, this.console.getText().length(), "cons");
            }
            else if (errorList.size()==1){
                this.console.appendText(runType + " failed -- ");
                this.console.appendText("1 error was found.");
                this.console.setStyleClass(0, this.console.getText().length(), "err");
            }
            else{
                this.console.appendText(runType + " failed -- ");
                this.console.appendText(errorList.size()+" errors were found.");
                this.console.setStyleClass(0, this.console.getText().length(), "err");
            }
        });
        Thread.sleep(1);
        this.mutex.release();
    }

    /**
     * Helper method to clear the console.
     */
    public void clearConsole() { this.console.clear(); }

    /**
     * A ScanWorker subclass handling Java program scanning in a separated thread in the background.
     * ScanWorker extends the javafx Service class.
     */
    public class ScanWorker extends Service<Boolean> {
        /**
         * the file embedded in the selected tab.
         */
        private File file;

        /**
         * Sets the selected tab and the associating file.
         *
         * @param file the file to be scanned embedded in the selected tab.
         */
        private void setFile(File file) { this.file = file; }

        /**
         * Overrides the createTask method in Service class.
         * Scans the file embedded in the selected tab, if appropriate.
         *
         * @return true if scans the program successfully;
         *         false otherwise.
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
                    Boolean scanResult = scanParseJavaFile(file, false);
                    return scanResult;
                }
            };
        }
    }

    /**
     * A ParseWorker subclass handling Java program parsing in a separated thread in the background.
     * ParseWorker extends the javafx Service class.
     */
    public class ParseWorker extends Service<Boolean> {
        /**
         * the file embedded in the selected tab.
         */
        private File file;

        /**
         * Sets the selected tab and the associating file.
         *
         * @param file the file to be parsed embedded in the selected tab.
         */
        private void setFile(File file) { this.file = file; }

        /**
         * Overrides the createTask method in Service class.
         *parses the file embedded in the selected tab, if appropriate.
         *
         * @return true if parses the program successfully;
         *         false otherwise.
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
                    Boolean parseResult = scanParseJavaFile(file, true);
                    return parseResult;
                }
            };
        }
    }

    /**
     * Helper method for running Java scanning in a separate thread.
     *
     * @param file the file being scanned or scanned & parsed
     * @param scanAndParse a boolean value indicating whether parse after scanning
     *                     false if only scan; true if scan & parse
     * @return true if scanning or scanning & parsing succeeds;
     *         false if scanning or scanning & parsing fails
     */
    private boolean scanParseJavaFile(File file, boolean scanAndParse) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
            });
            String filename = file.getPath(); // get the filename(path) of the file
            ErrorHandler errorHandler = new ErrorHandler();
            if (scanAndParse) {
                this.parser = new Parser(errorHandler);
                this.program = this.parser.parse(filename);
                this.drawer.draw(filename, this.program);
                this.errorToConsole(errorHandler.getErrorList(), "Parsing");
            } else {
                this.scanner = new Scanner( filename, errorHandler);
                this.tokenStr = this.scanner.scanFile();
                this.outputToNewTab(this.tokenStr);
                this.errorToConsole(errorHandler.getErrorList(), "Scanning");
            }
            return true;
        } catch (Throwable e) {
            Platform.runLater(() -> {
                if (scanAndParse) {
                    this.fileMenuController.createErrorDialog("File Parsing", "Error parsing.\nPlease try again with another valid Java File.");
                } else {
                    this.fileMenuController.createErrorDialog("File Scanning", "Error scanning.\nPlease try again with another valid Java File.");
                }
            });
            return false;
        }
    }

    /**
     * Helper method to handle scanning or scanning & parsing a file.
     *
     * @param event Event object
     * @param scanAndParse a boolean value indicating whether parse after scanning
     *                     false if only scan; true if scan & parse
     */
    private void handleScanParseFile(Event event, boolean scanAndParse) {
        // get the file associated with the selected tab
        File file = this.tabFileMap.get(this.tabPane.getSelectionModel().getSelectedItem());

        int checkIfSaved;
        if (scanAndParse) {
            checkIfSaved = this.fileMenuController.checkSaved("parsed");
        } else {
            checkIfSaved = this.fileMenuController.checkSaved("scanned");
        }

        // if the user chooses cancel
        if (checkIfSaved==2){
            event.consume();
        }
        else{
            if(checkIfSaved==1) {
                // re-get the file associated with the selected tab because the file is saved and updated
                file = this.tabFileMap.get(this.tabPane.getSelectionModel().getSelectedItem());
            }
            if (scanAndParse) {
                this.parseWorker.setFile(file);
                this.parseWorker.restart();
            } else {
                this.scanWorker.setFile(file);
                this.scanWorker.restart();
            }
        }
    }

    /**
     * Handles the Scan button action.
     *
     * @param event Event object
     */
    public void handleScanButtonAction(Event event) {
        this.handleScanParseFile(event, false);
    }

    /**
     * Handles the Parse button action.
     *
     * @param event Event object
     */
    public void handleParseButtonAction(Event event) {
        this.handleScanParseFile(event, true);
    }
}