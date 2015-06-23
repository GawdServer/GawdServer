package io.github.gawdserver.plugin;

import io.github.gawdserver.api.events.ChatEvent;
import io.github.gawdserver.api.events.Command;
import io.github.gawdserver.api.events.LogEvent;
import io.github.gawdserver.api.events.PlayerAccessEvent;
import io.github.gawdserver.api.plugin.Plugin;
import io.github.gawdserver.api.plugin.PluginQueue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EventManager {
	// Plugin Name - Main Class
	public static final Map<String, Plugin> plugins = new HashMap<>();
	// Command Name - Command Class
	public static final Map<String, Command> commands = new HashMap<>();
	// Plugin Name - Event Class
	public static final Map<String, PlayerAccessEvent> accessEvent = new HashMap<>();
	public static final Map<String, ChatEvent> chatEvent = new HashMap<>();
	public static final Map<String, LogEvent> logEvent = new HashMap<>();

	public static void enablePlugins() {
		for (final Map.Entry<String, Plugin> plugin : plugins.entrySet()) {
			System.out.println("[GawdServer] Enabling " + plugin.getKey() + "...");
			PluginQueue.submit(() -> plugin.getValue().startup());
		}
	}

	public static void disablePlugins() {
		for (final Map.Entry<String, Plugin> plugin : plugins.entrySet()) {
			System.out.println("[GawdServer] Disabling " + plugin.getKey() + "...");
			PluginQueue.submit(() -> plugin.getValue().shutdown());
		}
	}

	public static void playerCommand(final String username, final String command, final String... arguments) {
		PluginQueue.submit(() -> {
			System.out.printf("[GawdServer] %s used command %s %s%n", username, command, Arrays.toString(arguments));
			commands.get(command).playerCommand(username, arguments);
		});
	}

	public static void serverCommand(final String command, final String... arguments) {
		PluginQueue.submit(() -> {
			System.out.printf("[GawdServer] CONSOLE used command %s %s%n", command, Arrays.toString(arguments));
			commands.get(command).serverCommand(arguments);
		});
	}

	public static void playerConnect(final String username) {
		for (final Map.Entry<String, PlayerAccessEvent> event : accessEvent.entrySet()) {
			PluginQueue.submit(() -> event.getValue().playerConnect(username));
		}
	}

	public static void playerDisconnect(final String username) {
		for (final Map.Entry<String, PlayerAccessEvent> event : accessEvent.entrySet()) {
			PluginQueue.submit(() -> event.getValue().playerDisconnect(username));
		}
	}

	public static void playerChat(final String username, final String chat) {
		for (final Map.Entry<String, ChatEvent> event : chatEvent.entrySet()) {
			PluginQueue.submit(() -> event.getValue().playerChat(username, chat));
		}
	}

	public static void serverChat(final String chat) {
		for (final Map.Entry<String, ChatEvent> event : chatEvent.entrySet()) {
			PluginQueue.submit(() -> event.getValue().serverChat(chat));
		}
	}

	public static void serverLog(final String log) {
		for (final Map.Entry<String, LogEvent> event : logEvent.entrySet()) {
			PluginQueue.submit(() -> event.getValue().onLog(log));
		}
	}
}
