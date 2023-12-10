package com.serverd.app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class DirectorySchema {

    HashMap<String,String> dirs = new HashMap<>();

    public static String PLUGIN_DIR = "plugins";
    public static String PLUGINS_DATA_DIR = "pluginsdata";
    public static String APP_DATA_DIR = "appdata";
    public static String SERVERD_ROOT_DIR = ".";

    public DirectorySchema() {
        add(PLUGIN_DIR);
        add(PLUGINS_DATA_DIR);
        add(APP_DATA_DIR);
        add(SERVERD_ROOT_DIR);
    }

    public void init(File workdir) throws IOException {
        for (String id : dirs.keySet()) {
            File file = new File(workdir,dirs.get(id));
            if (!file.exists() && !file.mkdirs())
                throw new IOException("Failed to create " + file.getPath());
        }
    }

    public void add(String path) {
        add(path, path);
    }

    public void add(String id, String path) {
        if (dirs.containsKey(id))
            throw new IllegalArgumentException("Directory schema already contains directory with this ID");

        dirs.put(id,path);
    }

    public DirectorySchema pluginDir(String dir) {
        dirs.put(PLUGIN_DIR,dir);
        return this;
    }

    public DirectorySchema pluginDataDir(String dir) {
        dirs.put(PLUGINS_DATA_DIR,dir);
        return this;
    }

    public DirectorySchema appDataDir(String dir) {
        dirs.put(APP_DATA_DIR,dir);
        return this;
    }
    public DirectorySchema serverdRootDir(String dir) {
        dirs.put(SERVERD_ROOT_DIR,dir);
        return this;
    }

    public File get(File workdir,String id) {
        return new File(workdir,dirs.get(id).replace("/",File.pathSeparator));
    }
}
