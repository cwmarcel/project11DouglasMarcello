/*
 * File: Main.java
 * F18 CS361 Project 10
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 12/03/2018
 */

package proj10JiangQuanZhaoMarcello;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * This class creates a stage, as specified in Main.fxml, that contains a
 * set of tabs, embedded in a tab pane, with each tab window containing a
 * code area; a menu bar containing File and Edit menu; and a toolbar of
 * a button for scanning and tokenize a file; and a program console that
 * takes in standard input, displays standard output and program message.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 */
public class Main extends Application {
    /**
     * Creates a stage as specified in Main.fxml, that contains a set of tabs,
     * embedded in a tab pane, with each tab window containing a code area; a menu
     * bar containing File and Edit menu; and a toolbar of a button for scanning and
     * tokenize a file; and a program console that takes in standard input, displays
     * standard output and program message.
     *
     * @param stage The stage that contains the window content
     */
    @Override public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Main.fxml"));
        Parent root = loader.load();
        
        // initialize a scene and add features specified in the css file to the scene
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("css/Main.css").toExternalForm());
        // configure the stage
        stage.setTitle("proj10JiangQuanZhaoMarcello's Project 10");
        stage.sizeToScene();
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> ((proj10JiangQuanZhaoMarcello.Controller)loader.getController()).handleExitAction(event));
        stage.show();
    }

    /**
     * main function of Main class
     *
     * @param args command line arguments
     */
    public static void main(String[] args) { launch(args); }
}