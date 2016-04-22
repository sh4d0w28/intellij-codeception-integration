import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class MyMouseAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
        JTree tree = (JTree)e.getSource();
        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
        if (path != null) {
            DefaultMutableTreeNode comp = (DefaultMutableTreeNode)path.getLastPathComponent();
            TestNode node = (TestNode)(comp.getUserObject());
            String suite = path.getPathComponent(1).toString();
            node.React(suite);
        }
    }
}
