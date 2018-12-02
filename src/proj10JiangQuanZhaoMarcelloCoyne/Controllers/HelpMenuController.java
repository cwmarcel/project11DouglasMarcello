/*
 * File: HelpMenuController.java
 * F18 CS361 Project 9
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello, Michael Coyne
 * Date: 11/17/2018
 * This file contains the HelpMenuController class, handling Help menu related actions.
 */

package proj10JiangQuanZhaoMarcelloCoyne.Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.lang.reflect.Method;

/**
 * HelpMenuController handles Help menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 * @author Michael Coyne
 */
public class HelpMenuController {
    /**
     * Handles the User Manual menu item's action.
     *
     * @throws Exception general exception
     */
    public void handleUserManualAction() throws Exception {
        Stage userManualWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/UserManual.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 610, 170);
        userManualWindow.setTitle("User Manual");
        userManualWindow.sizeToScene();
        userManualWindow.setResizable(false);
        userManualWindow.setScene(scene);
        userManualWindow.show();
    }

    /**
     * Handles the Java Documentation menu item's action.
     */
    public void handleJavaDocAction() {
        Stage docWindow = new Stage();
        docWindow.setTitle("Java Documentation");
        VBox docRoot = new VBox();

        WebView docWebView = new WebView();
        WebEngine engine = docWebView.getEngine();
        engine.load("https://docs.oracle.com/en/java/");

        docRoot.getChildren().add(docWebView);
        Scene docScene = new Scene(docRoot, 600,500);
        docWindow.setScene(docScene);

        docWindow.show();
    }

    /**
     * Helper method to open a URL in the default browser.
     * @param url String of URL to be opened
     */
    private static void openURL(String url) {
        try {
            browse(url);
        } catch (Exception e) { }
    }

    /**
     * Helper method to figure out the default browser.
     * @param url String of URL to be opened
     * @throws Exception general exception to be catched
     */
    private static void browse(String url) throws Exception {
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL",new Class[] { String.class });
            openURL.invoke(null, new Object[] { url });
        }
        else if (osName.startsWith("Windows")) {
            Runtime.getRuntime().exec(
                    "rundll32 url.dll,FileProtocolHandler " + url);
        }
        else {
            String[] browsers = { "firefox", "opera", "mozilla"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) {
                if (Runtime.getRuntime()
                        .exec(new String[] { "which", browsers[count] })
                        .waitFor() == 0)
                    browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    Runtime.getRuntime().exec(new String[] { browser, url });
            }
        }
    }

    /**
     * Handles the Java Help menu item's action.
     * Opens the Java help website in the default browser.
     */
    public void handleJavaHelpMenuItemAction(){ this.openURL("https://docs.oracle.com/javase/tutorial/"); }

    /**
     * Handles the URL menu item's action.
     * Opens the URL tutorial in the default browser.
     */
    public void handleUrlMenuItemAction(){
        this.openURL("https://github.com/phoenixding/idrem/tree/master/sourcecode/edu/cmu/cs/sb/chromviewer");
    }
}