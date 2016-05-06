import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DirWorker {

    static File findFile(File parentDirectory, String filenameToFind, @Nullable File result) {
        if (result != null) {
            return result;
        }
        File[] folderContent = parentDirectory.listFiles();
        for (File item : folderContent != null ? folderContent : new File[0]) {
            if (item.isFile() && item.getName().equals(filenameToFind)) {
                result = item;
                break;
            }
            if(item.isDirectory()) {
                result = findFile(item, filenameToFind, result);
            }
        }
        return result;
    }

    static DefaultMutableTreeNode getDirContent(File testDir, DefaultMutableTreeNode root, @Nullable File baseDir, @Nullable File codeceptFile) {
        File[] elements = testDir.listFiles();
        for(File file : elements != null ? elements : new File[0]) {
            if(file.getName().startsWith("_")) {
                continue;
            }
            if(file.isFile() && file.getName().contains("Cest")) {
                TestNode tnode = new TestNode(file.getName(), MyIcons.FILE_ICON, file, baseDir, codeceptFile);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(tnode);
                try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                    Pattern pattern = Pattern.compile("public.*?function.*?test([0-9A-Za-z_]+)");
                    for (String line; (line = br.readLine()) != null; ) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            if(matcher.groupCount() == 1) {
                                tnode = new TestNode(matcher.group(1), MyIcons.ACTION_ICON, file, baseDir, codeceptFile);
                                node.add(new DefaultMutableTreeNode(tnode));
                            }
                        }
                    }
                } catch (Exception ex) {
                    return null;
                }
                root.add(node);

            } else if (file.isDirectory()) {
                TestNode tnode = new TestNode(file.getName(), MyIcons.FOLDER_ICON, null, baseDir, codeceptFile);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(tnode);
                node = getDirContent(file, node, baseDir, codeceptFile);
                root.add(node);
            }
        }
        return root;
    }
}
