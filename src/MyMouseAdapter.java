import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectManagerImpl;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

class MyMouseAdapter extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e) {
        Project project = ProjectManagerImpl.getInstance().getOpenProjects()[0];
        if(project.getBasePath() == null) {
            return;
        }

        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            if (path != null) {
                DefaultMutableTreeNode comp = (DefaultMutableTreeNode) path.getLastPathComponent();
                TestNode node = (TestNode) (comp.getUserObject());
                if(node.getPayload() == null || (File)node.getPayload() == null) {
                    return;
                }
                VirtualFile fileToOpen = VfsUtil.findFileByIoFile((File)node.getPayload(), true);
                if(fileToOpen == null) {
                    return;
                }
                OpenFileDescriptor d = new OpenFileDescriptor(project, fileToOpen, 0);
                d.navigate(true);
            }
        }
    }
}
