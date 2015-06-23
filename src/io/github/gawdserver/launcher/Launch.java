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
package io.github.gawdserver.launcher;

import io.github.gawdserver.api.Server;
import io.github.gawdserver.api.plugin.PluginDir;
import io.github.gawdserver.api.plugin.PluginQueue;
import io.github.gawdserver.plugin.EventManager;
import io.github.gawdserver.utils.Config;
import io.github.gawdserver.utils.LauncherConfig;
import io.github.gawdserver.api.launcher.Launcher;
import io.github.gawdserver.plugin.PluginLoader;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Launch implements Launcher, ServerExit {
    public static Config config;
    private BufferedWriter mcInput;

    public static void main(String[] args) {
        // Initialize this instance
        Launch launcher = new Launch();

        // Initialize the API
        Server.setLauncher(launcher);

        // Load Config
        config = Config.loadConfig();

        PluginQueue.setQueue(Executors.newFixedThreadPool(config.getThreads()));

        // Load Plugins
        PluginDir.setPluginDir(config.getPluginDir());
        PluginLoader.loadPlugins();

        // Enable Plugins
        EventManager.enablePlugins();

        // Start the Minecraft server
        launcher.launchServer();

        // Listen for console commands
        launcher.listenForCommands();
    }

    public void sendCommand(String command) {
        try {
            mcInput.write(command + "\n");
            mcInput.flush();
        } catch (IOException e) {
            System.out.println("[Launcher] Error sending command to server process.");
        }
    }

    private void listenForCommands() {
        try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            for (String input; (input = consoleInput.readLine()) != null; ) {
                if (input.startsWith(config.getCommandPrefix())) {
                    input = input.substring(config.getCommandPrefixLegnth());
                    String[] command = input.split(" ");
                    if (EventManager.commands.containsKey(command[0])) {
                        String[] arguments = Arrays.copyOfRange(command, 1, command.length);
                        EventManager.serverCommand(command[0], arguments);
                    }
                    continue;
                }
                sendCommand(input);
            }
        } catch (Exception ex) {
            System.out.println("[Launcher] IO error trying to read command input!");
            System.out.println(ex.getMessage());
        }
    }

    private void launchServer() {
        System.out.println("[Launcher] Starting Minecraft Server...");
        LauncherConfig config = LauncherConfig.loadConfig();

        // Java Executable
        ProcessLauncher processLauncher = new ProcessLauncher(config.getJavaPath());
        // Working Directory
        processLauncher.directory(config.getServerDir());
        // Java Args
        processLauncher.addCommands(config.getJavaArgs());
        // Class Path
        processLauncher.addCommands("-cp");
        processLauncher.addCommands(config.getClassPath());
        // Main Class
        processLauncher.addCommands(config.getMainClass());
        // Server Args
        processLauncher.addCommands(config.getServerArgs());

        System.out.println(processLauncher.toString());

        try {
            ServerProcess process = processLauncher.start();
            System.out.println(process.toString());
            process.safeSetExitRunnable(this);

            // Prepare the command sender
            mcInput = new BufferedWriter(new OutputStreamWriter(process.getRawProcess().getOutputStream()));
        } catch (IOException ex) {
            System.out.println("[Launcher] Error starting Minecraft server!");
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void onServerExit(ServerProcess process) {
        int exitCode = process.getExitCode();
        if (exitCode == 0) {
            System.out.printf("[Launcher] Server ended with no troubles detected (exit code %d)%n", exitCode);
        } else {
            System.out.printf("[Launcher] Server ended with bad state (exit code %d)%n", exitCode);
        }
        process.stop();

        // Disable Plugins
        EventManager.disablePlugins();

        // Shutdown the Event Queue
        PluginQueue.shutdown();
        try {
            // Wait up to 1 minute for plugins to shutdown.
            PluginQueue.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            System.out.println("Error waiting for plugins to shutdown.");
            ex.printStackTrace();
        }
        System.exit(exitCode);
    }
}