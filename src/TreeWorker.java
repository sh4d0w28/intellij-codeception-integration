import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectManagerImpl;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.BufferedReader;
import java.io.File;
import java.awt.*;
import java.io.FileReader;
import java.util.regex.Pattern;

class TreeWorker {

    static void runSelected(JTree jtree, JComponent sender)
    {
        sender.setEnabled(false);
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jtree.getLastSelectedPathComponent();
        DefaultMutableTreeNode suitenode = (DefaultMutableTreeNode)jtree.getSelectionPath().getPathComponent(1);
        TestNode suiteTestNode = (TestNode)suitenode.getUserObject();
        TestNode node = (TestNode)selectedNode.getUserObject();
        Thread reactThread = new Thread(() -> {
            try {
                node.React(suiteTestNode.getTitle(), selectedNode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sender.setEnabled(true);
        });
        reactThread.setDaemon(true);
        reactThread.start();
    }

    static void refreshData(JTree jtree)
    {
        Project project = ProjectManagerImpl.getInstance().getOpenProjects()[0];
        if(project.getBasePath() == null) {
            return;
        }
        File projectDir = new File(project.getBasePath());
        File codeceptYmlFile = DirWorker.findFile(projectDir, "codeception.yml", null);

        String testsFolder = "";

        try(BufferedReader br = new BufferedReader(new FileReader(codeceptYmlFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.contains("tests:")) {
                    testsFolder = line.replace("tests:", "").trim();
                    if(testsFolder.startsWith("/")) {
                        testsFolder = testsFolder.replaceFirst("/", "");
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            return;
        }
        if(testsFolder.isEmpty()) {
            return;
        }
        File testDir = new File(codeceptYmlFile.getParentFile().getAbsolutePath() + "/" + testsFolder);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TestNode("ROOT", null, null, null, null));
        root = DirWorker.getDirContent(testDir, root, testDir, codeceptYmlFile);
        jtree.setModel(new DefaultTreeModel(root));
    }
}
