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

import tk.coolv1994.gawdapi.player.PlayerList;
import tk.coolv1994.gawdserver.plugin.PluginManager;
import tk.coolv1994.gawdserver.threads.CommandThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by Vinnie on 9/25/14.
 */
public class ProcessMonitorThread extends Thread {

    private final ServerProcess process;

    public ProcessMonitorThread(ServerProcess process) {
        this.process = process;
    }

    public void run() {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getRawProcess().getInputStream()))) {
            for (String line; (line = buffer.readLine()) != null; ) {
                // Print Output
                System.out.println(line);

                // Check for UUIDs
                if (line.indexOf("User Authenticator") == 12) {
                    int usernameEnd = line.indexOf(" ", 57);
                    String username = line.substring(56, usernameEnd);
                    String uuid = line.substring(usernameEnd + 4);
                    PlayerList.addPlayerID(username, uuid);
                    continue;
                }

                if (line.endsWith(" joined the game")) {
                    String username = line.substring(33, line.indexOf(" ", 34));
                    PlayerList.playerLogin(username);
                    PluginManager.playerConnect(username);
                    continue;
                }

                if (line.endsWith(" left the game")) {
                    String username = line.substring(33, line.indexOf(" ", 34));
                    PlayerList.playerLogout(username);
                    PluginManager.playerDisconnect(username);
                    continue;
                }

                // If contains a '!', it's probably a command
                if (line.contains("!")) {
                    String[] split = line.split(" ");
                    // Command Listener
                    if (split.length > 4) {
                        String command = split[4].substring(1);
                        if (PluginManager.commands.containsKey(command) && (split[3].startsWith("<") && split[3].endsWith(">"))) {
                            String username = split[3].replaceAll("^<|>$", "");
                            String[] arguments = new String[0];
                            if (split.length > 5) {
                                arguments = line.substring(line.indexOf(command) + command.length() + 1).split(" ");
                            }
                            System.out.println("[GawdServer] " + username + " ran command " + command + " " + Arrays.toString(arguments));
                            PluginManager.queue.submit(new CommandThread(command, username, arguments));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Error reading from Minecraft Server process. " + ex.getMessage());
        }

        ServerExit onExit = process.getExitRunnable();
        if (onExit != null) {
            onExit.onServerExit(process);
        }
    }
}

