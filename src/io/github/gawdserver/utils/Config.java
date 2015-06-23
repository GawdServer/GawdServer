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
package io.github.gawdserver.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class Config {
    private static final File configFile = new File("GawdServer.json");
    private final File pluginDir;
    private final String commandPrefix;
    private final int commandPrefixLength;
    private final int threads;

    public Config() {
        pluginDir = new File("plugins");
        commandPrefix = "!";
        commandPrefixLength = commandPrefix.length();

        threads = 2 * Runtime.getRuntime().availableProcessors();
    }

    public Config(File pluginDir, String commandPrefix, int threads) {
        this.pluginDir = pluginDir;
        this.commandPrefix = commandPrefix;
        this.commandPrefixLength = this.commandPrefix.length();
        this.threads = threads;
    }

    public File getPluginDir() {
        return pluginDir;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public int getCommandPrefixLegnth() {
        return commandPrefixLength;
    }

    public int getThreads() {
        return threads;
    }

    public static Config loadConfig() {
        try {
            return new Gson().fromJson(new FileReader(configFile), Config.class);
        } catch (FileNotFoundException e) {
            System.out.println("[GawdServer] Missing configuration. Using defaults.");
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
            System.out.println("[GawdServer] Error saving configuration.");
            System.out.println(e.getMessage());
        }
    }
}
