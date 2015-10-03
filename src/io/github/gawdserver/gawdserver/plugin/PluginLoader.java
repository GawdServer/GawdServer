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
package io.github.gawdserver.gawdserver.plugin;

import io.github.gawdserver.api.events.ChatEvent;
import io.github.gawdserver.api.events.Command;
import io.github.gawdserver.api.events.LogEvent;
import io.github.gawdserver.api.events.PlayerAccessEvent;
import io.github.gawdserver.api.perms.PermissionManager;
import io.github.gawdserver.api.plugin.Plugin;
import io.github.gawdserver.api.plugin.PluginDir;
import io.github.gawdserver.api.plugin.PluginInfo;
import io.github.gawdserver.api.perms.Permissions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.github.gawdserver.api.plugin.PluginList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginLoader {

    private static final Logger logger = Logger.getLogger("PluginLoader");

    private static boolean loadCommands(URLClassLoader loader, PluginInfo plugin) {
        for (Map.Entry<String, String> command : plugin.getCommands().entrySet()) {
            try {
                Command onCommand = (Command) loader.loadClass(command.getValue()).newInstance();
                EventManager.commands.put(command.getKey(), onCommand);
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "No command class for plugin {0} command {1}", new Object[]{plugin.getName(), command.getKey()});
                return false;
            } catch (InstantiationException | IllegalAccessException e) {
                logger.log(Level.SEVERE, "Error in command class for plugin {0} command {1}", new Object[]{plugin.getName(), command.getKey()});
                return false;
            }
        }
        return true;
    }

    private static boolean loadEvents(URLClassLoader loader, PluginInfo plugin) {
        for (Map.Entry<String, String> event : plugin.getEvents().entrySet()) {
            try {
                Object onEvent = loader.loadClass(event.getValue()).newInstance();
                switch (event.getKey()) {
                    case "playerAccess":
                        EventManager.accessEvent.put(plugin.getName(), (PlayerAccessEvent) onEvent);
                        break;
                     case "chatEvent":
                         EventManager.chatEvent.put(plugin.getName(), (ChatEvent) onEvent);
                        break;
                     case "logEvent":
                         EventManager.logEvent.put(plugin.getName(), (LogEvent) onEvent);
                        break;
                    case "permissions":
                        Permissions.setManager((PermissionManager) onEvent);
                        break;
                }
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "No event class for plugin {0} event {1}", new Object[]{plugin.getName(), event.getKey()});
                return false;
            } catch (InstantiationException | IllegalAccessException e) {
                logger.log(Level.SEVERE, "Error in event class for plugin {0} event {1}", new Object[]{plugin.getName(), event.getKey()});
                return false;
            }
        }
        return true;
    }

    private static void loadPlugin(File pluginJar) {
        try {
            URL url = new URL("file", null, pluginJar.getAbsolutePath());
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
            InputStream pluginJson = classLoader.getResourceAsStream("plugin.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(pluginJson));
            PluginInfo pluginInfo = new Gson().fromJson(reader, PluginInfo.class);

            if (pluginInfo.getName() == null || pluginInfo.getVersion() == null || pluginInfo.getMainClass() == null) {
                logger.log(Level.SEVERE, "Invalid plugin.json for plugin {0}", pluginJar.getName());
                return;
            }

            logger.log(Level.INFO, "Loading {0}", pluginInfo.toString());

            Class mainClass = classLoader.loadClass(pluginInfo.getMainClass());
            EventManager.plugins.put(pluginInfo.getName(), (Plugin) mainClass.newInstance());

            if (pluginInfo.getEvents() != null) {
                if (!loadEvents(classLoader, pluginInfo)) {
                    return;
                }
            }

            if (pluginInfo.getCommands() != null) {
                if (!loadCommands(classLoader, pluginInfo)) {
                    return;
                }
            }

            PluginList.addPlugin(pluginInfo.getName(), pluginInfo.getVersion());

        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, "No plugin.json for plugin {0}", pluginJar.getName());
        } catch (JsonSyntaxException ex) {
            logger.log(Level.SEVERE, "Error parsing plugin.json for plugin {0}", pluginJar.getName());
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "No main class for plugin {0}", pluginJar.getName());
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, "Error in main class for plugin {0}", pluginJar.getName());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading plugin " + pluginJar.getName(), ex);
        }
    }

    public static void loadPlugins() {
        logger.info("Loading plugins...");
        // No plugin dir set
        if (PluginDir.getPluginDir() == null) {
            return;
        }

        // Just made plugin dir so no plugins
        if (PluginDir.getPluginDir().mkdir()) {
            return;
        }

        File[] pluginFiles = PluginDir.getPluginDir().listFiles();
        for (File pluginFile : pluginFiles) {
            // Only accept .jar files
            if (pluginFile.isDirectory() || !pluginFile.getName().endsWith(".jar")) {
                continue;
            }
            loadPlugin(pluginFile);
        }
    }
}