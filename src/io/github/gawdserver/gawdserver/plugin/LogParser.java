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

import io.github.gawdserver.api.player.PlayerList;
import io.github.gawdserver.api.player.Sender;
import io.github.gawdserver.api.utils.Chat;
import io.github.gawdserver.gawdserver.Main;

import java.util.Arrays;

public class LogParser {
	private static final int USERNAME = 3;
	private static final int ACTION = 4;
	private static final String commandPrefix = Main.config.getCommandPrefix();

	private static void playerCommandOrChat(String username, String[] segment) {
		// Command
		if (segment[ACTION].startsWith(commandPrefix)) {
			String command = segment[ACTION].substring(commandPrefix.length());
			if (EventManager.commands.containsKey(command)) {
				String[] arguments = Arrays.copyOfRange(segment, 5, segment.length);
				EventManager.playerCommand(username, command, arguments);
				return;
			}
		}

		// Chat
		String[] text = Arrays.copyOfRange(segment, ACTION, segment.length);
		EventManager.playerChat(username, Chat.toString(text));
	}

	private static void serverCommandOrChat(Sender sender, String[] segment) {
		int offset = 0;
		if (Sender.RCON.equals(sender)) {
			offset = 1;
		}
		// Command
		if (segment[ACTION+offset].startsWith(commandPrefix)) {
			String command = segment[ACTION+offset].substring(commandPrefix.length());
			if (EventManager.commands.containsKey(command)) {
				String[] arguments = Arrays.copyOfRange(segment, 5+offset, segment.length);
				EventManager.serverCommand(sender, command, arguments);
				return;
			}
		}

		// Chat
		String[] text = Arrays.copyOfRange(segment, ACTION, segment.length);
		EventManager.serverChat(Chat.toString(text));
	}

	public static void parse(String line) {
		// Split for processing
		String[] segment = line.split(" ");

		// Too short, Server Log
		if (segment.length < 5) {
			EventManager.serverLog(line);
			return;
		}

		// Check for UUIDs
		if (10 == segment.length && "[User".equals(segment[1]) && "Authenticator".equals(segment[2])) {
			//System.out.printf("[UUID Mapping] Username: %s UUID: %s%n", segment[7], segment[9]);
			PlayerList.addPlayerID(segment[7], segment[9]);
			return;
		}

		// Player Access Event
		if (7 == segment.length && "the".equals(segment[5]) && "game".equals(segment[6])) {
			if ("joined".equals(segment[ACTION])) {
				//System.out.printf("[Login Event] Username: %s%n", segment[USERNAME]);
				PlayerList.playerLogin(segment[USERNAME]);
				EventManager.playerConnect(segment[USERNAME]);
				return;
			}
			if ("left".equals(segment[ACTION])) {
				//System.out.printf("[Logout Event] Username: %s%n", segment[USERNAME]);
				PlayerList.playerLogout(segment[USERNAME]);
				EventManager.playerDisconnect(segment[USERNAME]);
				return;
			}
		}

		// Server Chat
		if (segment.length > 5 && "[Rcon]".equals(segment[ACTION])) {
			serverCommandOrChat(Sender.RCON, segment);
			return;
		}

		// Server Chat
		if ("[@]".equals(segment[USERNAME])) {
			serverCommandOrChat(Sender.COMMANDBLOCK, segment);
			return;
		}

		// Server Chat
		if ("[Server]".equals(segment[USERNAME])) {
			serverCommandOrChat(Sender.SERVER, segment);
			return;
		}

		// Does not contain username, Server Log
		if (
				!(
					(segment[USERNAME].startsWith("<") && segment[USERNAME].endsWith(">"))
				||
					(segment[USERNAME].startsWith("[") && segment[USERNAME].endsWith("]"))
				)
		)
		{
			EventManager.serverLog(line);
			return;
		}

		// Strip prefix/suffix from username
		String username = segment[USERNAME].substring(1, segment[USERNAME].length() - 1);

		// Player Chat
		playerCommandOrChat(username, segment);
	}
}
