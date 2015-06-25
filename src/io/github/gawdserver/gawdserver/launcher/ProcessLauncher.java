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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ProcessLauncher {
    private final List<String> commands;
    private File directory;

    public ProcessLauncher(String jvmPath) {
        if (jvmPath == null) {
            jvmPath = getJavaDir();
        }
        this.commands = new ArrayList<>();
        commands.add(jvmPath);
    }

    public ServerProcess start() throws IOException {
        return new ServerProcess(
                new ProcessBuilder(commands)
                        .directory(directory)
                        .redirectErrorStream(true)
                        .start()
        );
    }

    public void addCommand(String command) {
        this.commands.add(command);
    }

    public void addCommands(List<String> commands) {
        this.commands.addAll(commands);
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String toString() {
        return String.format("ProcessLauncher[commands=%s, directory=%s]", commands, directory);
    }

    public static String getJavaDir() {
        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home") + separator + "bin" + separator;
        if ((System.getProperty("os.name").toLowerCase().startsWith("win"))
                && (new File(path + "javaw.exe").isFile())) {
            return path + "javaw.exe";
        }
        return path + "java";
    }
}
