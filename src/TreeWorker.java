import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectManagerImpl;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.awt.*;

class TreeWorker {

    static void runSelected(JTree jtree)
    {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jtree.getLastSelectedPathComponent();
        String suite = jtree.getSelectionPath().getPathComponent(1).toString();
        TestNode node = (TestNode)selectedNode.getUserObject();
        node.React(suite);
    }

    static void refreshData(JTree jtree)
    {
        Project project = ProjectManagerImpl.getInstance().getOpenProjects()[0];
        if(project.getBasePath() == null) {
            return;
        }
        File baseDir = new File(project.getBasePath());
        File ymlMain = new File(".");
        File testDir = DirWorker.findDirectory(baseDir, "tests", null);
        TestNode tRoot = new TestNode(testDir.getName(), MyIcons.FOLDER_ICON, testDir, null, null);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tRoot);
        File[] files = testDir.getParentFile().listFiles();
        for(File file : files != null ? files : new File[0]) {
            if(file.getName().equals("codeception.yml")) {
                ymlMain = file;
            }
        }
        root = DirWorker.getDirContent(testDir, root, testDir, ymlMain);
        jtree.setModel(new DefaultTreeModel(root));
    }
}
