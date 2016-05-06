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

    private static final NotificationGroup GROUP_DISPLAY_BALOON_INFO = new NotificationGroup("My notification group", NotificationDisplayType.BALLOON, true);
    private static final NotificationGroup GROUP_DISPLAY_CONSOLE_INFO = new NotificationGroup("My notification group", NotificationDisplayType.NONE, true);

    TestNode(String oTitle, Icon oIcon, Object oPayload, @Nullable File oBaseDir, @Nullable File oCodeceptYml)
    {
        title = oTitle;
        icon = oIcon;
        payload = oPayload;
        baseDir = oBaseDir;
        codeceptYml = oCodeceptYml;
    }

    public Object getPayload() { return payload; }

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
                testnode.prefix = "[...]";
            }
            for (int i=0; i<childs; i++) {
                DefaultMutableTreeNode childnode = (DefaultMutableTreeNode)node.getChildAt(i);
                TestNode testnode = (TestNode)childnode.getUserObject();
                if(testnode.icon != MyIcons.ACTION_ICON) {
                    testnode.React(suite, childnode);
                }
            }
            prefix = "";
            return payload;
        }

        try {
            boolean itWasOk = true;
            String currentTest = "";
            String line, previous = null;
            boolean waitForResult = false;
            StringBuilder out = new StringBuilder();
            ArrayList<String> passed = new ArrayList<>();
            ArrayList<String> failed = new ArrayList<>();
            String path = codeceptYml.getParentFile().getPath();
            ProcessBuilder pb = null;
            List commands = new ArrayList();

            commands.add("vendor/bin/codecept");
            commands.add("run");
            commands.add("--no-ansi");
            commands.add("-d");
            commands.add("--no-interaction");
            commands.add("--no-colors");
            commands.add(suite);

            String mode = "action";
            if (icon == MyIcons.ACTION_ICON) {
                commands.add(payload.toString() + ":" + this.title);
            } else {
                mode = "pack";
                commands.add(payload.toString());
            }

            String outcommand = "";
            for (Object s : commands)
            {
                outcommand += s.toString() + " ";
            }
            out.append(outcommand);
            notification = GROUP_DISPLAY_CONSOLE_INFO.createNotification(out.toString(), NotificationType.INFORMATION);
            Notifications.Bus.notify(notification, projects[0]);

            out = new StringBuilder();

            pb = new ProcessBuilder(commands);
            pb.directory(codeceptYml.getParentFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while((line = br.readLine()) != null) {
                if (!line.equals(previous)) {
                    previous = line;
                    if(line.contains("Exception")) {
                        itWasOk = false;
                    }
                    if(line.startsWith("Test: ")) {
                        currentTest = line.replace("Test: ", "");
                        waitForResult = true;
                    }
                    if(line.startsWith(" PASSED") && waitForResult) {
                        waitForResult = false;
                        passed.add(path + "/" + currentTest);
                        out.append(currentTest + line + "\n");
                    } else if (line.startsWith(" FAIL") && waitForResult) {
                        waitForResult = false;
                        failed.add(path + "/" + currentTest);
                        out.append(currentTest + line + "\n");
                    }
                }
            }

            if(!out.toString().isEmpty()) {
                notification = GROUP_DISPLAY_CONSOLE_INFO.createNotification(out.toString(), NotificationType.INFORMATION);
                Notifications.Bus.notify(notification, projects[0]);
            }

            out = new StringBuilder();

            process.waitFor();

            if(mode.equals("action") && itWasOk) {
                prefix = "[OK]";
            } else if(itWasOk) {
                int childs = node.getChildCount();
                for (int i=0; i<childs; i++) {
                    DefaultMutableTreeNode childnode = (DefaultMutableTreeNode)node.getChildAt(i);
                    TestNode testnode = (TestNode)childnode.getUserObject();
                    if(passed.contains(testnode.payload + ":test" + testnode.title)) {
                        testnode.prefix = "[OK]";
                        prefix = "[OK]";
                    } else if(failed.contains(testnode.payload + ":test" + testnode.title)) {
                        testnode.prefix = "[ERR]";
                        prefix = "[ERR]";
                    } else {
                        testnode.prefix = "[ERR]";
                        prefix = "[ERR]";
                    }
                }
            } else  {
                prefix = "[ERR]";
            }
            notification = GROUP_DISPLAY_CONSOLE_INFO.createNotification(prefix, NotificationType.INFORMATION);
            Notifications.Bus.notify(notification, projects[0]);
        } catch (Exception e) {
            prefix = "[ERR]";
            Notification notification = GROUP_DISPLAY_CONSOLE_INFO.createNotification(e.toString(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, projects[0]);
        }
        return payload;
    }
}
