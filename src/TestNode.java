import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestNode {

    private String title;
    private String prefix = "";
    Icon icon;
    private Object payload;
    private File baseDir;
    private File codeceptYml;

    private Notification notification;

    private static final NotificationGroup GROUP_DISPLAY_ID_INFO = new NotificationGroup("My notification group", NotificationDisplayType.BALLOON, true);

    TestNode(String oTitle, Icon oIcon, Object oPayload, @Nullable File oBaseDir, @Nullable File oCodeceptYml)
    {
        title = oTitle;
        icon = oIcon;
        payload = oPayload;
        baseDir = oBaseDir;
        codeceptYml = oCodeceptYml;
    }

    public String getTitle()
    {
        return title;
    }

    @Override
    public String toString()
    {
        return prefix + " " + title;
    }

    Object React(String suite, DefaultMutableTreeNode node)
    {
        prefix = "[...]";

        Project[] projects = ProjectManager.getInstance().getOpenProjects();

        if ((codeceptYml == null || payload == null)) {
            int childs = node.getChildCount();
            for (int i=0; i<childs; i++) {
                DefaultMutableTreeNode childnode = (DefaultMutableTreeNode)node.getChildAt(i);
                TestNode testnode = (TestNode)childnode.getUserObject();
                testnode.React(suite, childnode);
            }
            prefix = "";
        }
        try {
            ProcessBuilder pb = null;
            List commands = new ArrayList();
            commands.add("vendor/bin/codecept");
            commands.add("run");
            commands.add("--no-colors");
            commands.add(suite);
            if (icon == MyIcons.ACTION_ICON) {
                commands.add(payload.toString());
            } else {
                commands.add(payload.toString() + ":" + this.title);
            }
            pb = new ProcessBuilder(commands);
            pb.directory(codeceptYml.getParentFile());codeceptYml.getParentFile().getAbsolutePath();
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder out = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line, previous = null;
            boolean itWasOk = true;
            Pattern pattern = Pattern.compile("([I|E|✔]) ([A-Za-z0-9]+): (\\w+)");
            while((line = br.readLine()) != null) {
                if (!line.equals(previous)) {
                    System.out.println(line);
                    previous = line;
                    if(line.contains("Exception")) {
                        itWasOk = false;
                    }
                    out.append(line).append('\n');
                    System.out.println(line);
                }
            }

            if(process.waitFor() == 0 && itWasOk) {
                prefix = "[OK]";
                System.out.println("Success");
            } else {
                prefix = "[ERR]";
            }
            notification = GROUP_DISPLAY_ID_INFO.createNotification(out.toString(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, projects[0]);

        } catch (Exception e) {
            prefix = "[ERR]";
            Notification notification = GROUP_DISPLAY_ID_INFO.createNotification(e.toString(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, projects[0]);
        }
        return payload;
    }
}
