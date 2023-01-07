package br.com.p8projects.filehub.system;

public class FilePathUtils {

    public static String getNewPathDirectoryRename(String pathDir, String newName) {
        String newPath = "";
        int lastSlash = pathDir.substring(0, pathDir.length()-1).lastIndexOf("/");
        if(lastSlash != -1) {
            String preDirPath = pathDir.substring(0, lastSlash);
            newPath = ("".equals(preDirPath)) ? "" : preDirPath + "/";
        }
        return newPath + newName + "/";
    }

}
