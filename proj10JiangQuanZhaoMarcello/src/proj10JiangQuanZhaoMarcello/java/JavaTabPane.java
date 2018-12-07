/*
 * File: JavaTabPane.java
 * F18 CS361 Project 10
 * Names: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello
 * Date: 11/17/2018
 * This file contains the JavaTabPane class, which extends the TabPane class.
 */

package proj10JiangQuanZhaoMarcello.java;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * This class extends the TabPane class to handle methods associated with java tab pane.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 */
public class JavaTabPane extends TabPane {
    /**
     * Returns the JavaCodeArea of the currently selected tab.
     *
     * @param tabPane the tab pane to get currently selected tab from
     * @return the JavaCodeArea embedded in the currently selected tab
     */
    static public JavaCodeArea getCurrentCodeArea(TabPane tabPane){
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        JavaCodeArea currentCodeArea = (JavaCodeArea)((VirtualizedScrollPane)selectedTab.getContent()).getContent();
        return currentCodeArea;
    }

    /**
     * Returns the JavaCodeArea of the specified tab.
     *
     * @param tab a tab to get the CodeArea from
     * @return the JavaCodeArea embedded in the specified tab
     */
    static public JavaCodeArea getCodeArea(Tab tab){
        return (JavaCodeArea)((VirtualizedScrollPane)tab.getContent()).getContent();
    }
}
