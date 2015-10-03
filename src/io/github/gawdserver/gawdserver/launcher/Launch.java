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
package io.github.gawdserver.gawdserver.launcher;

import io.github.gawdserver.api.launcher.Launcher;
import io.github.gawdserver.api.plugin.PluginQueue;
import io.github.gawdserver.gawdserver.Main;
import io.github.gawdserver.gawdserver.plugin.EventManager;
import io.github.gawdserver.gawdserver.utils.LauncherConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Launch implements Launcher, ServerExit {
    private BufferedWriter mcInput;

    public void sendCommand(String command) {
        try {
            mcInput.write(command + "\n");
            mcInput.flush();
        } catch (IOException ex) {
            Main.logger.log(Level.SEVERE, "Error sending command to server.", ex);
        }
    }

    public void launchServer() {
        Main.logger.info("Starting Minecraft Server...");
        // Load Launcher Config
        LauncherConfig launchConfig = LauncherConfig.load();
        // Java Executable
        ProcessLauncher processLauncher = new ProcessLauncher(launchConfig.getJavaPath());
        // Working Directory
        processLauncher.setDirectory(launchConfig.getServerDir());
        // Java Args
        processLauncher.addCommands(launchConfig.getJavaArgs());
        // Class Path
        processLauncher.addCommand("-cp");
        processLauncher.addCommands(launchConfig.getClassPath());
        // Main Class
        processLauncher.addCommand(launchConfig.getMainClass());
        // Server Args
        processLauncher.addCommands(launchConfig.getServerArgs());

        Main.logger.info(processLauncher.toString());

        try {
            ServerProcess process = processLauncher.start();
            Main.logger.info(process.toString());
            process.safeSetExitRunnable(this);

            // Prepare the command sender
            mcInput = new BufferedWriter(new OutputStreamWriter(process.getProcess().getOutputStream()));
        } catch (IOException ex) {
            Main.logger.log(Level.SEVERE, "Error starting Minecraft server!", ex);
        }
    }

    @Override
    public void onServerExit(ServerProcess process) {
        int exitCode;
        try {
            exitCode = process.getExitCode();
            if (exitCode == 0) {
                Main.logger.info("Server ended with no troubles detected (exit code 0)");
            } else {
                Main.logger.log(Level.WARNING, "Server ended with bad state (exit code {0})", exitCode);
            }
        } catch (IllegalThreadStateException ex) {
            exitCode = -1;
            Main.logger.warning("Server process hasn't exited");
        }
        process.stop();

        // Disable Plugins
        EventManager.disablePlugins();

        // Shutdown the Event Queue
        PluginQueue.shutdown();
        try {
            // Wait up to 1 minute for plugins to shutdown.
            PluginQueue.awaitTermination(Main.config.getPluginShutdownWait(), TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Main.logger.log(Level.SEVERE, "Error waiting for plugins to shutdown.", ex);
        }
        System.exit(exitCode);
    }
}