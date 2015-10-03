package io.github.gawdserver.gawdserver.plugin;

import io.github.gawdserver.api.events.ChatEvent;
import io.github.gawdserver.api.events.Command;
import io.github.gawdserver.api.events.LogEvent;
import io.github.gawdserver.api.events.PlayerAccessEvent;
import io.github.gawdserver.api.player.Sender;
import io.github.gawdserver.api.plugin.Plugin;
import io.github.gawdserver.api.plugin.PluginQueue;
import io.github.gawdserver.gawdserver.Main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventManager {
	private static final Logger logger = Logger.getLogger("EventManager");
	// Plugin Name - Class
	public static final Map<String, Plugin> plugins = new HashMap<>();
	public static final Map<String, Command> commands = new HashMap<>();
	public static final Map<String, PlayerAccessEvent> accessEvent = new HashMap<>();
	public static final Map<String, ChatEvent> chatEvent = new HashMap<>();
	public static final Map<String, LogEvent> logEvent = new HashMap<>();

	public static void enablePlugins() {
		for (final Map.Entry<String, Plugin> plugin : plugins.entrySet()) {
			logger.log(Level.INFO, "Enabling plugin {0}...", plugin.getKey());
			PluginQueue.submit(new Runnable() {
				@Override
				public void run() {
					plugin.getValue().startup();
				}
			});
		}
	}

	public static void disablePlugins() {
		for (final Map.Entry<String, Plugin> plugin : plugins.entrySet()) {
			logger.log(Level.INFO, "Disabling plugin {0}...", plugin.getKey());
			PluginQueue.submit(new Runnable() {
				@Override
				public void run() {
					plugin.getValue().shutdown();
				}
			});
		}
	}

	public static void playerCommand(final String username, final String command, final String... arguments) {
		PluginQueue.submit(new Runnable() {
			@Override
			public void run() {
				logger.log(Level.INFO, "{0} used command: {1} {2}", new Object[]{username, command, Arrays.toString(arguments)});
				commands.get(command).playerCommand(username, arguments);
			}
		});
	}

	public static void serverCommand(final Sender sender, final String command, final String... arguments) {
		PluginQueue.submit(new Runnable() {
			@Override
			public void run() {
				logger.log(Level.INFO, "{0} used command: {1} {2}", new Object[]{sender, command, Arrays.toString(arguments)});
				commands.get(command).serverCommand(sender, arguments);
			}
		});
	}

	public static void playerConnect(final String username) {
		for (final Map.Entry<String, PlayerAccessEvent> event : accessEvent.entrySet()) {
			PluginQueue.submit(new Runnable() {
				@Override
				public void run() {
					event.getValue().playerConnect(username);
				}
			});
		}
	}

	public static void playerDisconnect(final String username) {
		for (final Map.Entry<String, PlayerAccessEvent> event : accessEvent.entrySet()) {
			PluginQueue.submit(new Runnable() {
				@Override
				public void run() {
					event.getValue().playerDisconnect(username);
				}
			});
		}
	}

	public static void playerChat(final String username, final String chat) {
		for (final Map.Entry<String, ChatEvent> event : chatEvent.entrySet()) {
			PluginQueue.submit(new Runnable() {
				@Override
				public void run() {
					event.getValue().playerChat(username, chat);
				}
			});
		}
	}

	public static void serverChat(final String chat) {
		for (final Map.Entry<String, ChatEvent> event : chatEvent.entrySet()) {
			PluginQueue.submit(new Runnable() {
				@Override
				public void run() {
					event.getValue().serverChat(chat);
				}
			});
		}
	}

	public static void serverLog(final String log) {
		for (final Map.Entry<String, LogEvent> event : logEvent.entrySet()) {
			PluginQueue.submit(new Runnable() {
				@Override
				public void run() {
					event.getValue().onLog(log);
				}
			});
		}
	}
}
