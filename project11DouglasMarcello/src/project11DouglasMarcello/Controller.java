/*
 * File: Controller.java
 * F18 CS361 Project 10
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 12/03/2018
 * This file contains the Main controller class, handling actions evoked by the Main window.
 */

package project11DouglasMarcello;

import project11DouglasMarcello.controllers.ToolBarController;
import project11DouglasMarcello.controllers.FileMenuController;
import project11DouglasMarcello.controllers.EditMenuController;
import project11DouglasMarcello.controllers.ContextMenuController;
import project11DouglasMarcello.controllers.HelpMenuController;
import project11DouglasMarcello.controllers.SettingMenuController;
import project11DouglasMarcello.controllers.FindMenuController;
import project11DouglasMarcello.controllers.DirectoryController;
import project11DouglasMarcello.controllers.StructureViewController;


import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.File;
import java.util.*;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.beans.property.BooleanProperty;

/**
 * Main controller handles actions evoked by the Main window.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 */
public class Controller {
    /**
     * ToolbarController handling toolbar actions
     */
    private ToolBarController toolbarController;
    /**
     * FileMenuController handling File menu actions
     */
    private FileMenuController fileMenuController;
    /**
     * EditMenuController handling Edit menu actions
     */
    private EditMenuController editMenuController;
    /**
     * ContextMenuController handling context menu actions
     */
    private ContextMenuController contextMenuController;
    /**
     * HelpMenuController handling Help menu actions
     */
    private HelpMenuController helpMenuController;
    /**
     * SettingMenuController handling Setting menu actions
     */
    private SettingMenuController settingMenuController;
    /**
     * FindMenuController handling Find menu actions
     */
    private FindMenuController findMenuController;
    /**
     * DirectoryController handling Directory Tree actions
     */
    private DirectoryController directoryController;
    /**
     * StructureViewController handling File Structure Tree actions
     */
    private StructureViewController structureViewController;
    /**
     * Scan button defined in Main.fxml
     */
    @FXML private Button scanButton;
    /**
     * Parse button defined in Main.fxml
     */
    @FXML private Button parseButton;
    /**
     * DeleteTab button defined in Main.fxml
     */
    @FXML private Button deleteTabButton;
    /**
     * Tree of current directory defined in Main.fxml
     */
    @FXML private TreeView<String> directoryTree;
    /**
     * Tree of current file structure defined in Main.fxml
     */
    @FXML private TreeView fileStructTree;
    /**
     * Split pane which contains File treeStructure View on left and the rest on right
     */
    @FXML private SplitPane treeSplitPane;
    /**
     * Checkbox that toggles fileStructTree defined in Main.fxml
     */
    @FXML private CheckBox displayFileStructCheckBox;
    /**
     * the console pane defined in Main.fxml
     */
    @FXML private StyleClassedTextArea console;
    /**
     * ColorPreferenceMenu menu defined in Main.fxml
     */
    @FXML private Menu colorPreferenceMenu;
    /**
     * Find menu defined in Main.fxml
     */
    @FXML private Menu findMenu;
    /**
     * Edit menu defined in Main.fxml
     */
    @FXML private Menu editMenu;
    /**
     * Close menu item of the File menu defined in Main.fxml
     */
    @FXML private MenuItem closeMenuItem;
    /**
     * Save menu item of the File menu defined in Main.fxml
     */
    @FXML private MenuItem saveMenuItem;
    /**
     * Save As menu item of the File menu defined in Main.fxml
     */
    @FXML private MenuItem saveAsMenuItem;
    /**
     * Dark Mode menu item defined in Main.fxml
     */
    @FXML private RadioMenuItem darkModeMenuItem;
    /**
     * TabPane defined in Main.fxml
     */
    @FXML private TabPane tabPane;
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab,File> tabFileMap = new HashMap<Tab,File>();
    /**
     * a CompileWorker that scans a java file in a separate thread
     */
    private ToolBarController.ScanWorker scanWorker;
    /**
     * a CompileWorker that parses a java file in a separate thread
     */
    private ToolBarController.ParseWorker parseWorker;
    /**
     * a ReadOnlyBooleanProperty indicating if scanning
     */
    ReadOnlyBooleanProperty ifScanning;
    /**
     * a ReadOnlyBooleanProperty indicating if parsing
     */
    ReadOnlyBooleanProperty ifParsing;
    /**
     * a BooleanBinding indicating if the tab pane is empty
     */
    BooleanBinding ifTabPaneEmpty;

    /**
     * Creates a reference to the ToolbarController and passes in window items and other sub Controllers when necessary.
     */
    private void setupToolbarController() {
        this.toolbarController = new ToolBarController();
        this.toolbarController.setConsole(this.console);
        this.toolbarController.setFileMenuController(this.fileMenuController);
        this.toolbarController.initialize();
        this.scanWorker = this.toolbarController.getScanWorker();
        this.parseWorker = this.toolbarController.getParseWorker();
        this.toolbarController.setTabPane(this.tabPane);
        this.toolbarController.setTabFileMap(this.tabFileMap);
    }

    /**
     * Creates a reference to the FileMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupFileMenuController() {
        this.fileMenuController = new FileMenuController();
        this.fileMenuController.setTabFileMap(this.tabFileMap);
        this.fileMenuController.setTabPane(this.tabPane);
        this.fileMenuController.setDarkModeMenuItem(this.darkModeMenuItem);
    }

    /**
     * Creates a reference to the EditMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupEditMenuController() {
        this.editMenuController = new EditMenuController();
        this.editMenuController.setTabPane(this.tabPane);
    }

    /**
     * Creates a reference to the ContextMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupContextMenuController() {
        this.contextMenuController = new ContextMenuController();
        this.contextMenuController.setFileMenuController(this.fileMenuController);
        this.contextMenuController.setEditMenuController(this.editMenuController);
        this.contextMenuController.setToolBarController(this.toolbarController);
        this.fileMenuController.setContextMenuController(this.contextMenuController);

        this.console.setOnContextMenuRequested(event ->
                this.contextMenuController.setupConsoleContextMenuHandler(this.console,
                        this.ifScanning, this.ifParsing, this.ifTabPaneEmpty)
        );
    }

    /**
     * Creates a reference to the HelpMenuController.
     */
    private void setupHelpMenuController() { this.helpMenuController = new HelpMenuController(); }

    /**
     * Creates a reference to the SettingMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupSettingMenuController() {
        this.settingMenuController = new SettingMenuController();
        this.settingMenuController.setTabPane(this.tabPane);
    }

    /**
     * Creates a reference to the FindMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupFindMenuController() {
        this.findMenuController = new FindMenuController();
        this.findMenuController.setTabPane(this.tabPane);
    }

    /**
     * Creates a reference to the DirectoryController and passes in window items and other sub Controllers when necessary.
     */
    private void setupDirectoryController() {
        this.directoryController = new DirectoryController();
        this.directoryController.setTabFileMap(this.tabFileMap);
        this.directoryController.setTabPane(this.tabPane);
        this.directoryController.setFileMenuController(this.fileMenuController);
        this.directoryController.setDirectoryTree(this.directoryTree);
        this.fileMenuController.setDirectoryController(this.directoryController);
    }

    /**
     * Creates a reference to the StructureViewController and passes in window items and other sub Controllers when necessary.
     */
    private void setupStructureViewController() {
        this.structureViewController = new StructureViewController();
        this.structureViewController.setTreeView(this.fileStructTree);
        this.structureViewController.setTabFileMap(this.tabFileMap);
        this.structureViewController.setTabPane(this.tabPane);
        this.structureViewController.setDisplayFileStructCheckBox(this.displayFileStructCheckBox);
        this.structureViewController.initialize(this.treeSplitPane);
        this.fileMenuController.setStructureViewController(this.structureViewController);
    }

    /**
     * Initializes the subControllers
     */
    private void setupSubController() {
        this.setupEditMenuController();
        this.setupFileMenuController();
        this.setupToolbarController();
        this.setupContextMenuController();
        this.setupHelpMenuController();
        this.setupSettingMenuController();
        this.setupFindMenuController();
        this.setupDirectoryController();
        this.setupStructureViewController();
    }

    /**
     * Binds the Close, Save, Save As menu items of the File menu, the Edit menu,
     * the Delete Tab button, the Find menu with the condition whether the tab pane is empty.
     * Binds Scan button with the condition
     * whether a program is running and whether the tab is empty.
     */
    private void setupBinding() {
        this.ifTabPaneEmpty = Bindings.isEmpty(tabPane.getTabs());
        this.ifScanning = this.scanWorker.runningProperty();
        this.ifParsing = this.parseWorker.runningProperty();
        BooleanProperty ifNightModeSelected = this.darkModeMenuItem.selectedProperty();

        this.closeMenuItem.disableProperty().bind(this.ifTabPaneEmpty);
        this.saveMenuItem.disableProperty().bind(this.ifTabPaneEmpty);
        this.saveAsMenuItem.disableProperty().bind(this.ifTabPaneEmpty);
        this.editMenu.disableProperty().bind(this.ifTabPaneEmpty);
        this.deleteTabButton.disableProperty().bind(this.ifTabPaneEmpty);
        this.findMenu.disableProperty().bind(this.ifTabPaneEmpty);

        this.scanButton.disableProperty().bind(this.ifScanning.or(this.ifTabPaneEmpty));
        this.parseButton.disableProperty().bind(this.ifParsing.or(this.ifTabPaneEmpty));

        this.colorPreferenceMenu.disableProperty().bind(ifNightModeSelected);
    }

    /**
     * This function is called after the FXML fields are populated.
     * Sets up references to the sub Controllers.
     * Sets up bindings.
     * Sets focus to console on startup.
     */
    @FXML public void initialize() {
        this.setupSubController();
        this.setupBinding();
        this.console.requestFocus();
    }

    /**
     * Jump to the line where the selected class/method/field is declared.
     */
    @FXML private void handleFileStructTreeItemClicked() { this.structureViewController.handleFileStructTreeItemClicked(); }

    /**
     * Calls the method that handles the Scan button action from the toolbarController.
     *
     * @param event Event object
     */
    @FXML private void handleScanButtonAction(Event event) {
        this.toolbarController.handleScanButtonAction(event);
    }

    /**
     * Calls the method that handles the Parse button action from the toolbarController.
     *
     * @param event Event object
     */
    @FXML private void handleParseButtonAction(Event event) {
        this.toolbarController.handleParseButtonAction(event);
    }

    /**
     * Calls the method that handles About menu item action from the fileMenuController.
     */
    @FXML private void handleAboutAction() { this.fileMenuController.handleAboutAction(); }

    /**
     * Calls the method that handles the New menu item action from the fileMenuController.
     */
    @FXML private void handleNewAction() { this.fileMenuController.handleNewAction(); }

    /**
     * Calls the method that handles the Open menu item action from the fileMenuController.
     */
    @FXML private void handleOpenAction() { this.fileMenuController.handleOpenAction(); }

    /**
     * Calls the method that handles the Close menu item action from the fileMenuController.
     *
     * @param event Event object
     */
    @FXML private void handleCloseAction(Event event) { this.fileMenuController.handleCloseAction(event); }

    /**
     * Calls the method that handles the Save As menu item action from the fileMenuController.
     */
    @FXML private void handleSaveAsAction() { this.fileMenuController.handleSaveAsAction(); }

    /**
     * Calls the method that handles the Save menu item action from the fileMenuController.
     */
    @FXML private void handleSaveAction() { this.fileMenuController.handleSaveAction(); }

    /**
     * Calls the method that handles the Exit menu item action from the fileMenuController.
     *
     * @param event Event object
     */
    @FXML public void handleExitAction(Event event) { this.fileMenuController.handleExitAction(event); }

    /**
     * Calls the method that handles the Edit menu action from the editMenuController.
     *
     *  @param event ActionEvent object
     */
    @FXML private void handleEditMenuAction(ActionEvent event) { this.editMenuController.handleEditMenuAction(event); }

    /**
     * Calls the method that handles the toggle comment action from the editMenuController.
     */
    @FXML public void handleToggleCommentAction() { this.editMenuController.handleToggleCommentAction(); }

    /**
     * Calls the method that handles the toggle block comment action from the editMenuController.
     */
    @FXML public void handleToggleBlockCommentAction() { this.editMenuController.handleToggleBlockCommentAction(); }

    /**
     * Calls the method that handles the indent action from the editMenuController.
     */
    @FXML public void handleIndentAction() { this.editMenuController.handleIndentAction(); }

    /**
     * Calls the method that handles the un-indent action from the editMenuController.
     */
    @FXML  public void handleUnindentAction() { this.editMenuController.handleUnindentAction(); }

    /**
     * Calls the method that handles the Guide menu item from the helpMenuController.
     */
    @FXML public void handleUserManualAction() throws Exception { this.helpMenuController.handleUserManualAction(); }

    /**
     * Calls the method that handles the Documentation menu item from the helpMenuController.
     */
    @FXML public void handleJavaDocAction() { this.helpMenuController.handleJavaDocAction(); }

    /**
     * Calls the method that handles the Keyword color menu item from the settingMenuController.
     */
    @FXML public void handleKeywordColorAction() { this.settingMenuController.handleKeywordColorAction(); }

    /**
     * Calls the method that handles the Parentheses/Brackets color menu item from the settingMenuController.
     */
    @FXML public void handleParenColorAction() { this.settingMenuController.handleParenColorAction(); }

    /**
     * Calls the method that handles the String color menu item from the settingMenuController.
     */
    @FXML public void handleStrColorAction() { this.settingMenuController.handleStrColorAction(); }

    /**
     * Calls the method that handles the Int color menu item from the settingMenuController.
     */
    @FXML public void handleIntColorAction() { this.settingMenuController.handleIntColorAction(); }

    /**
     * Calls the method that handles the dark mode menu item from the settingMenuController.
     */
    @FXML public void handleDarkMode() { this.settingMenuController.handleDarkMode(); }

    /**
     * Calls the method that handles the light mode menu item from the settingMenuController.
     */
    @FXML public void handleLightMode() { this.settingMenuController.handleLightMode(); }

    /**
     * Calls the method that handles find and replace from the findMenuController.
     */
    @FXML public void handleFindReplaceAction() { this.findMenuController.handleFindReplace(); }

    /**
     * Calls the method that handles java help menu from the helpMenuController.
     */
    @FXML private void handleJavaHelpMenuItemAction() { this.helpMenuController.handleJavaHelpMenuItemAction(); }

    /**
     * Calls the method that handles url help menu from the helpMenuController.
     */
    @FXML private void handleUrlMenuItemAction() { this.helpMenuController.handleUrlMenuItemAction(); }
}