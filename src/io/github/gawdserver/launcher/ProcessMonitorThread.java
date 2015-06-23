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
package io.github.gawdserver.launcher;

import io.github.gawdserver.plugin.LogParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                // Look for Plugin Events
                try {
                    LogParser.parse(line);
                } catch (Exception ex) {
                    System.out.printf("Error parsing log.%n%s%n", ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.out.printf("Error reading from Minecraft Server process.%n%s%n", ex.getMessage());
        }

        ServerExit onExit = process.getExitRunnable();
        if (onExit != null) {
            onExit.onServerExit(process);
        }
    }
}

