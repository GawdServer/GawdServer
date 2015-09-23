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
package io.github.gawdserver.gawdserver;

import io.github.gawdserver.api.Server;
import io.github.gawdserver.api.player.Sender;
import io.github.gawdserver.api.plugin.PluginDir;
import io.github.gawdserver.api.plugin.PluginQueue;
import io.github.gawdserver.gawdserver.launcher.Launch;
import io.github.gawdserver.gawdserver.plugin.EventManager;
import io.github.gawdserver.gawdserver.plugin.PluginLoader;
import io.github.gawdserver.gawdserver.utils.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class Main {
	public static Config config;

	public static void main(String[] args) {
		// Initialize this instance
		Launch launcher = new Launch();

		// Initialize the API
		Server.setLauncher(launcher);

		// Load Config
		config = Config.load();

		// Initialize event queue
		PluginQueue.setQueue(Executors.newFixedThreadPool(config.getThreads()));

		// Set Plugin Directory
		PluginDir.setPluginDir(config.getPluginDir());

		// Load Plugins
		PluginLoader.loadPlugins();

		// Enable Plugins
		EventManager.enablePlugins();

		// Start the Minecraft server
		launcher.launchServer();

		// Listen for console commands
		listenForCommands();
	}

	private static void listenForCommands() {
		try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
			for (String input; (input = consoleInput.readLine()) != null; ) {
				if (input.startsWith(config.getCommandPrefix())) {
					input = input.substring(config.getCommandPrefix().length());
					String[] command = input.split(" ");
					if (EventManager.commands.containsKey(command[0])) {
						String[] arguments = Arrays.copyOfRange(command, 1, command.length);
						EventManager.serverCommand(Sender.CONSOLE, command[0], arguments);
					}
					continue;
				}
				Server.sendCommand(input);
			}
		} catch (Exception ex) {
			System.out.println("[GawdServer] IO error trying to read command input!");
			System.out.println(ex.getMessage());
		}
	}
}
