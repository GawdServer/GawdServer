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
package tk.coolv1994.gawdserver.plugin;

import tk.coolv1994.gawdapi.events.Command;
import tk.coolv1994.gawdapi.events.PlayerAccessEvent;
import tk.coolv1994.gawdapi.perms.PermissionManager;
import tk.coolv1994.gawdapi.plugin.Plugin;
import tk.coolv1994.gawdapi.plugin.PluginInfo;
import tk.coolv1994.gawdapi.perms.Permissions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Vinnie on 12/19/2014.
 */
public class PluginManager {
    // # of Threads used is 2x your Processor Cores
    public static final int cpuCores = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService queue = Executors.newFixedThreadPool(cpuCores * 2);
    // Plugin Name - Main Class
    public static final Map<String, Plugin> plugins = new HashMap<>();
    // Command Name - Method
    public static final Map<String, Command> commands = new HashMap<>();
    // Event Name - Method
    public static final Map<String, PlayerAccessEvent> accessEvent = new HashMap<>();

    public static void playerConnect(final String username) {
        for (final Map.Entry<String, PlayerAccessEvent> event : accessEvent.entrySet()) {
            queue.submit(() -> event.getValue().playerConnect(username));
        }
    }

    public static void playerDisconnect(final String username) {
        for (final Map.Entry<String, PlayerAccessEvent> event : accessEvent.entrySet()) {
            queue.submit(() -> event.getValue().playerDisconnect(username));
        }
    }

    public static void enablePlugins() {
        for (final Map.Entry<String, Plugin> plugin : plugins.entrySet()) {
            System.out.println("[PluginManager] Enabling " + plugin.getKey() + "...");
            queue.submit(() -> plugin.getValue().startup());
        }
    }

    public static void disablePlugins() {
        for (final Map.Entry<String, Plugin> plugin : plugins.entrySet()) {
            System.out.println("[PluginManager] Disabling " + plugin.getKey() + "...");
            queue.submit(() -> plugin.getValue().shutdown());
        }
    }

    private static void loadCommands(URLClassLoader loader, HashMap<String, String> commandMap) {
        for (Map.Entry<String, String> command : commandMap.entrySet()) {
            try {
                Command onCommand = (Command) loader.loadClass(command.getValue()).newInstance();
                commands.put(command.getKey(), onCommand);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadEvents(URLClassLoader loader, HashMap<String, String> eventMap) {
        for (Map.Entry<String, String> event : eventMap.entrySet()) {
            try {
                switch (event.getKey()) {
                    case "playerAccess":
                        Object onEvent = loader.loadClass(event.getValue()).newInstance();
                        accessEvent.put(event.getKey(), (PlayerAccessEvent) onEvent);
                        break;
                    case "permissions":
                        Permissions.setManager((PermissionManager) loader.loadClass(event.getValue()).newInstance());
                        break;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadPlugins() {
        System.out.println("[PluginManager] Loading plugins...");
        Gson gson = new Gson();
        File pluginDir = new File("plugins");
        pluginDir.mkdir();
        File[] pluginFiles = pluginDir.listFiles();
        for (File pluginFile : pluginFiles) {
            // Only accept .jar files
            if (pluginFile.isDirectory() || !pluginFile.getName().endsWith(".jar")) {
                continue;
            }
            try {
                URL url = new URL("file", null, pluginFile.getAbsolutePath());
                URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
                InputStream pluginJson = classLoader.getResourceAsStream("plugin.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(pluginJson));
                PluginInfo pluginInfo = gson.fromJson(reader, PluginInfo.class);

                if (pluginInfo.getName() == null || pluginInfo.getVersion() == null || pluginInfo.getMainClass() == null) {
                    throw new Exception("Invalid plugin.json");
                }

                System.out.println("[PluginManager] Loading " + pluginInfo.toString());

                Class mainClass = classLoader.loadClass(pluginInfo.getMainClass());
                plugins.put(pluginInfo.getName(), (Plugin) mainClass.newInstance());

                if (pluginInfo.getEvents() != null)
                    loadEvents(classLoader, pluginInfo.getEvents());

                if (pluginInfo.getCommands() != null)
                    loadCommands(classLoader, pluginInfo.getCommands());

            } catch (NullPointerException e) {
                System.out.println("[PluginManager] Error reading plugin.json for plugin " + pluginFile.getName());
            } catch (JsonSyntaxException e) {
                System.out.println("[PluginManager] Error reading plugin.json for plugin " + pluginFile.getName());
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}