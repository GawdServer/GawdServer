/**
 * GawdServer - A new way to serve Minecraft
 * Copyright (C) 2015  CoolV1994 <http://coolv1994.tk>
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
package tk.coolv1994.gawdserver.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * Created by Vinnie on 2/4/2015.
 */
public class Config {
    public static final File configFile = new File("./GawdServer.conf");
    private final String javaPath;
    private final String[] javaArgs;
    private final String serverDir;
    private final String[] classPath;
    private final String mainClass;
    private final String[] serverArgs;

    public Config() {
        this.javaPath = null;
        this.javaArgs = new String[]{"-Xmx1024M", "-Xms1024M", "-Djline.terminal=jline.UnsupportedTerminal", "-XX:+UseConcMarkSweepGC", "-XX:+CMSClassUnloadingEnabled", "-XX:MaxPermSize=256M"};
        this.serverDir = null;
        this.classPath = new String[]{"minecraft_server.jar"};
        this.mainClass = "net.minecraft.server.MinecraftServer";
        this.serverArgs = new String[]{"nogui"};
    }

    public Config(String javaPath, String[] javaArgs, String serverDir, String[] classPath, String mainClass, String[] serverArgs) {
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

    public String[] getJavaArgs() {
        return javaArgs;
    }

    public File getServerDir() {
        if (serverDir != null)
            return new File(serverDir);
        return null;
    }

    public String[] getClassPath() {
        return classPath;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String[] getServerArgs() {
        return serverArgs;
    }

    public static Config loadConfig() {
        try {
            return new Gson().fromJson(new FileReader(configFile), Config.class);
        } catch (FileNotFoundException e) {
            Config defaults = new Config();
            defaults.saveConfig();
            return defaults;
        }
    }

    public void saveConfig() {
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
            System.out.println("Error saving configuration.\n" + e.getMessage());
        }
    }
}
