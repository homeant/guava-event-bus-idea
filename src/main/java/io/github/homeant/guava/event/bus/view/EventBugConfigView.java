package io.github.homeant.guava.event.bus.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;

public class EventBugConfigView {
    private JPanel rootPanel;

    private final Project project;

    public EventBugConfigView(Project project){
        this.project = project;
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, true);
        MyModel tableModel = new MyModel();
        JBTable table = new JBTable(tableModel);
        panel.setContent(table);
        rootPanel.add(panel);
    }

    public void save(){

    }

    public void reset(){

    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
    static class MyModel extends ListTableModel<String>{

    }

}
