/*
 * File: SettingMenuController.java
 * F18 CS361 Project 9
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello, Michael Coyne
 * Date: 10/30/2018
 * This file contains the SettingMenuController class, handling Setting menu related actions.
 */

package proj10JiangQuanZhaoMarcelloCoyne.Controllers;

import proj10JiangQuanZhaoMarcelloCoyne.Java.JavaCodeArea;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.fxmisc.flowless.VirtualizedScrollPane;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * SettingMenuController handles Setting menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Chris Marcello
 * @author Michael Coyne
 * @author Danqing Zhao
 */
public class SettingMenuController {
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;
    /**
     * A string to store the user's selection of keyword color.
     */
    private String keywordColorSelected;
    /**
     * A string to store the user's selection of parenthesis/bracket color.
     */
    private String parenColorSelected;
    /**
     * A string to store the user's selection of string color.
     */
    private String strColorSelected;
    /**
     * A string to store the user's selection of integer color.
     */
    private String intColorSelected;

    /**
     * Sets the tabPane.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(TabPane tabPane){
        this.tabPane = tabPane;
    }

    /**
     * Handles Keyword Color menu item action.
     * Pops up a window displaying the color preference for the keywords.
     * By selecting a color from the drop-down menu, the color of the keywords will change accordingly.
     */
    public void handleKeywordColorAction(){
        Stage keywordColorWin = new Stage();
        keywordColorWin.setTitle("Keyword Color");

        VBox keywordColorRoot = new VBox();
        keywordColorRoot.setAlignment(Pos.CENTER);
        keywordColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.PURPLE);

        ChoiceBox keywordColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Purple","Black","Blue","Green","Pink","Yellow", "Red"));
        keywordColorCB.setValue("Purple");

        JavaCodeArea styledCodeArea = getCurrentCodeArea();

        Text message = new Text("Keyword Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.PURPLE);

        keywordColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                colorRectAndText(rect, message, keywordColorCB);
                keywordColorSelected = keywordColorCB.getSelectionModel().getSelectedItem().toString().toLowerCase();
                String newStyleClassname = "keyword-"+keywordColorSelected;
                styledCodeArea.setKeywordColorClass(newStyleClassname);
                styledCodeArea.highlightText();

            }
        });

        keywordColorRoot.getChildren().addAll(message,rect, keywordColorCB);
        Scene keywordColorScene = new Scene(keywordColorRoot, 200,200);
        keywordColorWin.setScene(keywordColorScene);
        keywordColorWin.show();
    }

    /**
     * Handles Parentheses/Brackets Color menu item action.
     */
    public void handleParenColorAction(){
        Stage parenColorWin = new Stage();
        parenColorWin.setTitle("Parentheses/Brackets Color");

        VBox parenColorRoot = new VBox();
        parenColorRoot.setAlignment(Pos.CENTER);
        parenColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.TEAL);

        ChoiceBox parenColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Green","Black","Blue","Purple","Pink","Yellow", "Red"));
        parenColorCB.setValue("Green");

        Text message = new Text("Parentheses/Brackets Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.TEAL);

        JavaCodeArea styledCodeArea = getCurrentCodeArea();

        parenColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                colorRectAndText(rect, message, parenColorCB);
                parenColorSelected = parenColorCB.getSelectionModel().getSelectedItem().toString().toLowerCase();
                String newStyleClassname = "paren-"+parenColorSelected;
                styledCodeArea.setParenColorClass(newStyleClassname);
                styledCodeArea.highlightText();
            }
        });

        parenColorRoot.getChildren().addAll(message,rect, parenColorCB);
        Scene keywordColorScene = new Scene(parenColorRoot, 230,200);
        parenColorWin.setScene(keywordColorScene);
        parenColorWin.show();
    }

    /**
     * Handles String Color menu item action.
     */
    public void handleStrColorAction(){
        Stage strColorWin = new Stage();
        strColorWin.setTitle("String Color");

        VBox strColorRoot = new VBox();
        strColorRoot.setAlignment(Pos.CENTER);
        strColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.ROYALBLUE);

        ChoiceBox strColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Blue","Black","Green","Purple","Pink","Yellow", "Red"));
        strColorCB.setValue("Blue");

        Text message = new Text("String Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.ROYALBLUE);

        JavaCodeArea styledCodeArea = getCurrentCodeArea();

        strColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                colorRectAndText(rect, message, strColorCB);
                strColorSelected = strColorCB.getSelectionModel().getSelectedItem().toString().toLowerCase();
                String newStyleClassname = "str-"+strColorSelected;
                styledCodeArea.setStringColorClass(newStyleClassname);
                styledCodeArea.highlightText();
            }
        });

        strColorRoot.getChildren().addAll(message,rect, strColorCB);
        Scene keywordColorScene = new Scene(strColorRoot, 230,200);
        strColorWin.setScene(keywordColorScene);
        strColorWin.show();
    }

    /**
     * Handles int Color menu item action.
     */
    public void handleIntColorAction(){
        Stage intColorWin = new Stage();
        intColorWin.setTitle("int Color");

        VBox intColorRoot = new VBox();
        intColorRoot.setAlignment(Pos.CENTER);
        intColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.FIREBRICK);

        ChoiceBox intColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Red","Black","Blue","Purple","Pink","Yellow", "Green"));
        intColorCB.setValue("Red");

        Text message = new Text("Integer(int) Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.FIREBRICK);

        JavaCodeArea styledCodeArea = getCurrentCodeArea();

        intColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                colorRectAndText(rect, message, intColorCB);
                intColorSelected = intColorCB.getSelectionModel().getSelectedItem().toString().toLowerCase();
                String newStyleClassname = "int-"+intColorSelected;
                styledCodeArea.setIntColorClass(newStyleClassname);
                styledCodeArea.highlightText();
            }
        });

        intColorRoot.getChildren().addAll(message,rect, intColorCB);
        Scene keywordColorScene = new Scene(intColorRoot, 230,200);
        intColorWin.setScene(keywordColorScene);
        intColorWin.show();
    }

    /**
     * Helper method to return the StyledJavaCodeArea within the current tab
     * @return StyledJavaCodeArea
     */
    private JavaCodeArea getCurrentCodeArea(){
        Tab currentTab = this.tabPane.getSelectionModel().getSelectedItem();
        VirtualizedScrollPane<JavaCodeArea> codeArea = (VirtualizedScrollPane<JavaCodeArea>)currentTab.getContent();
        JavaCodeArea styledCodeArea = codeArea.getContent();
        return styledCodeArea;
    }

    /**
     * Helper method to color the rectangle and text in the preference dialog.
     *
     * @param rect
     * @param text
     * @param choiceBox
     */
    private void colorRectAndText(Rectangle rect, Text text, ChoiceBox choiceBox){
        String selectedColor = choiceBox.getSelectionModel().getSelectedItem().toString();
        if (selectedColor=="Purple"){
            rect.setFill(Color.PURPLE);
            text.setFill(Color.PURPLE);
        }
        else if (selectedColor=="Blue"){
            rect.setFill(Color.ROYALBLUE);
            text.setFill(Color.ROYALBLUE);
        }
        else if (selectedColor=="Black"){
            rect.setFill(Color.BLACK);
            text.setFill(Color.BLACK);
        }
        else if (selectedColor=="Red"){
            rect.setFill(Color.FIREBRICK);
            text.setFill(Color.FIREBRICK);
        }
        else if (selectedColor=="Yellow"){
            rect.setFill(Color.ORANGE);
            text.setFill(Color.ORANGE);
        }
        else if (selectedColor=="Pink"){
            rect.setFill(Color.ORCHID);
            text.setFill(Color.ORCHID);
        }
        else if (selectedColor=="Green"){
            rect.setFill(Color.TEAL);
            text.setFill(Color.TEAL);
        }
    }

    /**
     * Handles DarkMode menu item action.
     */
    public void handleDarkMode(){
        for (Tab tab: this.tabPane.getTabs()){
            tab.getContent();
            VirtualizedScrollPane<JavaCodeArea> codeArea = (VirtualizedScrollPane<JavaCodeArea>)tab.getContent();
            JavaCodeArea styledCodeArea = codeArea.getContent();
            String newcss = getClass().getResource("../css/DarkModeStyledJavaCodeArea.css").toExternalForm();
            styledCodeArea.getStylesheets().add(newcss);
        }
    }

    /**
     * Handles LightMode menu item action.
     */
    public void handleLightMode(){
        for (Tab tab: this.tabPane.getTabs()){
            tab.getContent();
            VirtualizedScrollPane<JavaCodeArea> codeArea = (VirtualizedScrollPane<JavaCodeArea>)tab.getContent();
            JavaCodeArea styledCodeArea = codeArea.getContent();
            String newcss = getClass().getResource("../css/DarkModeStyledJavaCodeArea.css").toExternalForm();
            styledCodeArea.getStylesheets().remove(newcss);
        }
    }
}
