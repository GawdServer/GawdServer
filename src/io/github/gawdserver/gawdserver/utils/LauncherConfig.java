/**
 * GawdServer - A new way to serve Minecraft
 * Copyright (C) 2015  GawdServer <http://gawdserver.github.io>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.gawdserver.gawdserver.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.gawdserver.gawdserver.launcher.ProcessLauncher;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LauncherConfig {
    private static final File configFile = new File("Launcher.json");
    private final String javaPath;
    private final List<String> javaArgs;
    private final File serverDir;
    private final List<String> classPath;
    private final String mainClass;
    private final List<String> serverArgs;

    private LauncherConfig() {
        javaPath = ProcessLauncher.getJavaDir();
        javaArgs = new ArrayList<>();
        javaArgs.add("-Xmx1024M");
        javaArgs.add("-Xms1024M");
        javaArgs.add("-Djline.terminal=jline.UnsupportedTerminal");
        javaArgs.add("-XX:+UseConcMarkSweepGC");
        javaArgs.add("-XX:+CMSClassUnloadingEnabled");
        javaArgs.add("-XX:MaxPermSize=128M");
        serverDir = null;
        classPath = new ArrayList<>();
        classPath.add("minecraft_server.jar");
        mainClass = "net.minecraft.server.MinecraftServer";
        serverArgs = new ArrayList<>();
        serverArgs.add("nogui");
    }

    private LauncherConfig(
            String javaPath,
            List<String> javaArgs,
            File serverDir,
            List<String> classPath,
            String mainClass,
            List<String> serverArgs
    ) {
        this.javaPath = javaPath;
        this.javaArgs = javaArgs;
        this.serverDir = serverDir;
        this.classPath = classPath;
        this.mainClass = mainClass;
        this.serverArgs = serverArgs;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public List<String> getJavaArgs() {
        return javaArgs;
    }

    public File getServerDir() {
        return serverDir;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public String getMainClass() {
        return mainClass;
    }

    public List<String> getServerArgs() {
        return serverArgs;
    }

    public static LauncherConfig load() {
        try {
            return new Gson().fromJson(new FileReader(configFile), LauncherConfig.class);
        } catch (FileNotFoundException e) {
            System.out.println("[GawdServer] Missing launcher configuration. Using defaults.");
            LauncherConfig defaults = new LauncherConfig();
            defaults.save();
            return defaults;
        }
    }

    private void save() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.enableComplexMapKeySerialization();
        builder.serializeNulls();
        builder.disableHtmlEscaping();
        Gson gson = builder.create();
        try {
            // Write Default Config
            FileWriter fw = new FileWriter(configFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(this));
            bw.close();
        } catch (IOException e) {
            System.out.println("[GawdServer] Error saving launcher configuration.");
            System.out.println(e.getMessage());
        }
    }
}
