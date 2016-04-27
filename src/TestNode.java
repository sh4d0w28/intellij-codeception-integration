import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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

    @Override
    public String toString()
    {
        return prefix + " " + title;
    }

    Object React(String suite)
    {
        prefix = "[...]";

        Project[] projects = ProjectManager.getInstance().getOpenProjects();

        if((codeceptYml == null || payload == null) && icon == MyIcons.FOLDER_ICON)
        {
            return null;
        }
        try {
            List<String> commands = new ArrayList<>();
            commands.add("../vendor/bin/codecept");
            commands.add("run");
            commands.add("--no-colors");
            commands.add("-c" + codeceptYml.getAbsolutePath());
            commands.add(suite);
            if (icon == MyIcons.ACTION_ICON) {
                commands.add(payload.toString() + ":" + this.title);
            } else {
                commands.add(payload.toString());
            }
            StringBuilder sb = new StringBuilder();
            for(String s : commands) {
                sb.append(s);
                sb.append(' ');
            }

            notification = GROUP_DISPLAY_ID_INFO.createNotification(sb.toString(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, projects[0]);


            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.directory(baseDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder out = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line, previous = null;
            boolean itWasOk = true;
            Pattern pattern = Pattern.compile("([I|E|✔]) ([A-Za-z0-9]+): (\\w+)");
            while((line = br.readLine()) != null) {
                if (!line.equals(previous)) {
                    previous = line;
                    if(line.contains("Exception")) {
                        itWasOk = false;
                    }
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String s = "T";
                        //if(matcher.groupCount() == 1) {
                        //    tnode = new TestNode(matcher.group(1), MyIcons.ACTION_ICON, file, baseDir, codeceptFile);
                        //    node.add(new DefaultMutableTreeNode(tnode));
                        //}
                    }
/*

I CourseTypeCest: Test save from request
✔ BlockCest: Test find (0.1s)
✔ BlockCest: Test get by page and name (0.2s)
E BlockCest: Test add field


 */


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
