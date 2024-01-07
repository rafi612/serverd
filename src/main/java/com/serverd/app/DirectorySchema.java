package com.serverd.app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Directory schema class.
 * Directory schema is used to determinate directory structure of app.
 * This can be passed as argument in constructor of {@link ServerdApplication}
 */
public class DirectorySchema {

    private final HashMap<String,String> dirs = new HashMap<>();

    /** Default plugins dir */
    public static final String PLUGIN_DIR = "plugins";
    /** Default plugins data dir*/
    public static final String PLUGINS_DATA_DIR = "pluginsdata";
    /** Default app data dir */
    public static final String APP_DATA_DIR = "appdata";
    /** Default root dir */
    public static final String SERVERD_ROOT_DIR = ".";

    /**
     * Default constructor.
     */
    public DirectorySchema() {
        add(PLUGIN_DIR);
        add(PLUGINS_DATA_DIR);
        add(APP_DATA_DIR);
        add(SERVERD_ROOT_DIR);
    }

    /**
     * Initializing directory schema.
     * @param workdir Current work dir
     * @throws IOException when I/O error will occur.
     */
    public void init(File workdir) throws IOException {
        for (String id : dirs.keySet()) {
            File file = new File(workdir,dirs.get(id));
            if (!file.exists() && !file.mkdirs())
                throw new IOException("Failed to create " + file.getPath());
        }
    }

    /**
     * Adding path to directory schema with same identifier as path.
     * @param path Directory path
     */
    public void add(String path) {
        add(path, path);
    }

    /**
     * Adding path to directory schema with custom key.
     * @param key Path identifier.
     * @param path Directory path
     */
    public void add(String key, String path) {
        if (dirs.containsKey(key))
            throw new IllegalArgumentException("Directory schema already contains directory with this ID");

        dirs.put(key,path);
    }

    /**
     * Returning {@link File} object of given folder.
     * @param workdir Current work dir.
     * @param key Path identifier.
     * @return {@link File} object of directory.
     */
    public File get(File workdir,String key) {
        return new File(workdir,dirs.get(key).replace("/",File.separator));
    }

    /**
     * Settings plugin dir
     * @param dir Directory path.
     * @return Self instance.
     */
    public DirectorySchema pluginDir(String dir) {
        dirs.put(PLUGIN_DIR,dir);
        return this;
    }

    /**
     * Settings plugin data dir
     * @param dir Directory path.
     * @return Self instance.
     */
    public DirectorySchema pluginDataDir(String dir) {
        dirs.put(PLUGINS_DATA_DIR,dir);
        return this;
    }

    /**
     * Settings app data dir
     * @param dir Directory path.
     * @return Self instance.
     */
    public DirectorySchema appDataDir(String dir) {
        dirs.put(APP_DATA_DIR,dir);
        return this;
    }

    /**
     * Settings root dir
     * @param dir Directory path.
     * @return Self instance.
     */
    public DirectorySchema rootDir(String dir) {
        dirs.put(SERVERD_ROOT_DIR,dir);
        return this;
    }
}
