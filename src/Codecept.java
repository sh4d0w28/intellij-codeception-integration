import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.io.File;

public class Codecept implements ToolWindowFactory {

    private JTree jTree1;

    private File findDirectory(File parentDirectory, String folderNameToFind, @Nullable File result) {
        if (result != null) {
            return result;
        }
        File[] files = parentDirectory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                continue;
            }
            if (file.getName().equals(folderNameToFind)) {
                result = file;
                break;
            }
            if(file.isDirectory()) {
                result = findDirectory(file, folderNameToFind, result);
            }
        }
        return result;
    }

    private void initTree(@NotNull Project project) {
        if(project.getBasePath() == null) {
            return;
        }
        File baseDir = new File(project.getBasePath());
        File ymlMain = new File(".");
        File testDir = this.findDirectory(baseDir, "tests", null);
        TestNode tRoot = new TestNode(testDir.getName(), MyIcons.FOLDER_ICON, testDir, null, null);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tRoot);

        File[] files = testDir.getParentFile().listFiles();

        for(File file : files) {
            if(file.getName().equals("codeception.yml")) {
                ymlMain = file;
            }
        }

        root = DirWorker.getDirContent(testDir, root, testDir, ymlMain);
        jTree1 = new JTree(root);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JComponent component = toolWindow.getComponent();
        this.initTree(project);
        jTree1.setCellRenderer(new MyTreeCellRenderer());
        jTree1.addMouseListener(new MyMouseAdapter());
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

            }
        });
        JBScrollPane pane = new JBScrollPane(jTree1);
        component.getParent().add(pane);
        component.getParent().add(pane);
    }
}