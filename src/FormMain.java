import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FormMain implements ToolWindowFactory {

    private JButton btnRefresh;
    private JScrollPane jspTree;
    private JTree jtMain;
    private JPanel jpMenu;
    private JPanel jpTree;
    private JPanel jpMain;
    private JButton btnRunSelected;
    private JButton runButton;

    private void initTree()
    {
        TreeWorker.refreshData(jtMain);
        jtMain.setCellRenderer(new MyTreeCellRenderer());
    }

    private void initButton()
    {
        btnRefresh.addActionListener(e -> {
            TreeWorker.refreshData(jtMain);
        });
        btnRunSelected.addActionListener(e -> {
            TreeWorker.runSelected(jtMain);
        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.initTree();
        this.initButton();
        JComponent component = toolWindow.getComponent();
        component.getParent().add(jpMain);
    }
}
