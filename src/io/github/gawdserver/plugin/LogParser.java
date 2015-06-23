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
package io.github.gawdserver.plugin;

import io.github.gawdserver.api.player.PlayerList;
import io.github.gawdserver.api.utils.Chat;

import java.util.Arrays;

import static io.github.gawdserver.launcher.Launch.config;

public class LogParser {
	private static final int USERNAME = 3;
	private static final int ACTION = 4;

	public static void parse(String line) {
		// Split for processing
		String[] segment = line.split(" ");

		// Too short, Server Log
		if (segment.length < 5) {
			EventManager.serverLog(line);
			return;
		}

		// Check for UUIDs
		if (segment.length == 10 && segment[1].equals("[User") && segment[2].equals("Authenticator")) {
			System.out.printf("[UUID Mapping] Username: %s UUID: %s%n", segment[7], segment[9]);
			PlayerList.addPlayerID(segment[7], segment[9]);
			return;
		}

		// Player Access Event
		if (segment.length == 7 && segment[5].equals("the") && segment[6].equals("game")) {
			if (segment[ACTION].equals("joined")) {
				System.out.printf("[Login Event] Username: %s%n", segment[USERNAME]);
				PlayerList.playerLogin(segment[USERNAME]);
				EventManager.playerConnect(segment[USERNAME]);
				return;
			}
			if (segment[ACTION].equals("left")) {
				System.out.printf("[Logout Event] Username: %s%n", segment[USERNAME]);
				PlayerList.playerLogout(segment[USERNAME]);
				EventManager.playerDisconnect(segment[USERNAME]);
				return;
			}
		}

		// Server Chat
		if (segment[USERNAME].equals("[Server]")) {
			String[] text = Arrays.copyOfRange(segment, ACTION, segment.length);
			EventManager.serverChat(Chat.toString(text));
			return;
		}

		// Does not contain username, Server Log
		if (!(segment[USERNAME].startsWith("<") && segment[USERNAME].endsWith(">"))) {
			EventManager.serverLog(line);
			return;
		}

		// Strip prefix/suffix from username
		String username = segment[USERNAME].substring(1, segment[USERNAME].length() - 1);

		// Command
		if (segment[ACTION].startsWith(config.getCommandPrefix())) {
			String command = segment[ACTION].substring(config.getCommandPrefixLegnth());
			if (EventManager.commands.containsKey(command)) {
				String[] arguments = Arrays.copyOfRange(segment, 5, segment.length);
				EventManager.playerCommand(username, command, arguments);
				return;
			}
		}

		// Player Chat
		String[] text = Arrays.copyOfRange(segment, ACTION, segment.length);
		EventManager.playerChat(username, Chat.toString(text));
	}
}
