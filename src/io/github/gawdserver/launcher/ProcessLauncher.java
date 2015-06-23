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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ProcessLauncher {
    private final String jvmPath;
    private final List<String> commands;
    private File directory;

    public ProcessLauncher(String jvmPath, String... commands) {
        if (jvmPath == null) {
            jvmPath = getJavaDir();
        }
        this.jvmPath = jvmPath;
        this.commands = new ArrayList<>(commands.length);
        addCommands(commands);
    }

    public ServerProcess start()
            throws IOException {
        List<String> full = getFullCommands();
        return new ServerProcess(full, new ProcessBuilder(full).directory(directory).redirectErrorStream(true).start());
    }

    public List<String> getFullCommands() {
        List<String> result = new ArrayList<>(commands);
        result.add(0, getJavaPath());
        return result;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void addCommands(String... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    public void addSplitCommands(String commands) {
        addCommands(commands.split(" "));
    }

    public void directory(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }

    public String getJavaPath() {
        return jvmPath;
    }

    public String getJavaDir() {
        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home") + separator + "bin" + separator;
        if ((System.getProperty("os.name").toLowerCase().startsWith("win"))
                && (new File(path + "javaw.exe").isFile())) {
            return path + "javaw.exe";
        }
        return path + "java";
    }

    public String toString() {
        return String.format("ProcessLauncher[java=%s, commands=%s, directory=%s]", jvmPath, commands, directory);
    }
}
