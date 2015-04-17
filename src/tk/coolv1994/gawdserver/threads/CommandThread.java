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
package tk.coolv1994.gawdserver.threads;

import tk.coolv1994.gawdapi.player.Console;
import tk.coolv1994.gawdserver.plugin.PluginManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Vinnie on 2/3/2015.
 */
public class CommandThread implements Runnable {
    private String command;
    private String username;
    private String[] arguments;

    public CommandThread(String[] command) {
        this.command = command[0];
        this.username = Console.CONSOLE;
        List<String> args = new LinkedList<>();

        for (int i = 0; i < command.length; i++)
            if (i > 0)
                args.add(command[i]);

        arguments = args.toArray(new String[args.size()]);
    }

    public CommandThread(String command, String username, String[] arguments) {
        this.command = command;
        this.username = username;
        this.arguments = arguments;
    }

    public void run() {
        if (PluginManager.commands.containsKey(command))
            PluginManager.commands.get(command).onCommand(username, arguments);
    }
}
