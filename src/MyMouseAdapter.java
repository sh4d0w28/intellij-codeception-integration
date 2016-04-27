import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class MyMouseAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
        JTree tree = (JTree)e.getSource();
        TreeWorker.runSelected(tree);
    }
}
