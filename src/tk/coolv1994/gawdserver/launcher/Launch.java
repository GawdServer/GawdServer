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
package tk.coolv1994.gawdserver.launcher;

import tk.coolv1994.gawdapi.Gawd;
import tk.coolv1994.gawdapi.launcher.Launcher;
import tk.coolv1994.gawdserver.plugin.PluginManager;
import tk.coolv1994.gawdserver.threads.CommandThread;
import tk.coolv1994.gawdserver.utils.Config;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vinnie on 9/25/14.
 */
public class Launch implements Launcher, ServerExit {
    private static Launch launcher;
    private static Config config;
    private static ServerProcess process;
    private static BufferedWriter mcInput;

    public static void main(String[] args) {
        // Initialize this instance
        launcher = new Launch();

        // Initialize the API
        Gawd.setLauncher(launcher);

        // Load Config
        config = Config.loadConfig();

        // Load Plugins
        PluginManager.loadPlugins();

        // Enable Plugins
        PluginManager.enablePlugins();

        // Start the Minecraft server
        launcher.launchServer();

        // Prepare the command sender
        mcInput = new BufferedWriter(new OutputStreamWriter(process.getRawProcess().getOutputStream()));

        // Listen for console commands
        try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            for (String input; (input = consoleInput.readLine()) != null; ) {
                if (input.startsWith("!")) {
                    input = input.substring(1);
                    String[] command = input.split(" ");
                    if (PluginManager.commands.containsKey(command[0])) {
                        System.out.println("[GawdServer]: Console ran command " + Arrays.toString(command));
                        PluginManager.queue.submit(new CommandThread(command));
                    }
                    continue;
                }
                launcher.sendCommand(input);
            }
        } catch (Exception ex) {
            System.out.println("[Launcher] IO error trying to read command input!\n" + ex.getMessage());
        }
    }

    public void sendCommand(String command) {
        try {
            mcInput.write(command + "\n");
            mcInput.flush();
        } catch (IOException e) {
            System.out.println("[Launcher] Error sending command to server process.");
        }
    }

    public void launchServer() {
        System.out.println("[Launcher] Starting Minecraft Server...");
        ProcessLauncher processLauncher = new ProcessLauncher(config.getJavaPath());
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

        try {
            process = processLauncher.start();
            System.out.println(process.toString());
            process.safeSetExitRunnable(this);
        } catch (IOException e) {
            System.out.println("[Launcher] Error starting Minecraft server!\n" + e.getMessage());
            return;
        }
    }

    @Override
    public void onServerExit(ServerProcess process) {
        int exitCode = process.getExitCode();
        if (exitCode == 0) {
            System.out.println("[Launcher] Server ended with no troubles detected (exit code " + exitCode + ")");
        } else {
            System.out.println("[Launcher] Server ended with bad state (exit code " + exitCode + ")");
        }
        // Disable Plugins
        PluginManager.disablePlugins();

        // Shutdown the Event Queue
        PluginManager.queue.shutdown();
        try {
            // Wait up to 1 minute for plugins to shutdown.
            PluginManager.queue.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Error waiting for plugins to shutdown.");
            e.printStackTrace();
        }
        System.exit(exitCode);
    }
}