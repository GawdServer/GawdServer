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

import tk.coolv1994.gawdapi.plugin.Plugin;

/**
 * Created by Vinnie on 2/3/2015.
 */
public class PluginThread extends Thread {
    private Plugin plugin;
    private String event;

    public PluginThread(Plugin plugin, String event) {
        this.plugin = plugin;
        this.event = event;
    }

    public void run() {
        switch (event) {
            case "startup":
                plugin.startup();
                break;
            case "shutdown":
                plugin.shutdown();
                break;
        }
    }
}
