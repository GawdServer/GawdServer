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

import java.util.List;

/**
 * Created by Vinnie on 9/25/14.
 */
public class ServerProcess {
    private final List<String> commands;
    private final Process process;
    private ServerExit onExit;

    public ServerProcess(List<String> commands, Process process) {
        this.commands = commands;
        this.process = process;

        ProcessMonitorThread monitor = new ProcessMonitorThread(this);
        monitor.start();
    }

    public Process getRawProcess() {
        return process;
    }

    public List<String> getStartupCommands() {
        return commands;
    }

    public String getStartupCommand() {
        return process.toString();
    }

    public boolean isRunning() {
        try {
            process.exitValue();
        } catch (IllegalThreadStateException ex) {
            return true;
        }
        return false;
    }

    public void setExitRunnable(ServerExit runnable) {
        onExit = runnable;
    }

    public void safeSetExitRunnable(ServerExit runnable) {
        setExitRunnable(runnable);
        if ((!isRunning())
                && (runnable != null)) {
            runnable.onServerExit(this);
        }
    }

    public ServerExit getExitRunnable() {
        return onExit;
    }

    public int getExitCode() {
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException ex) {
            ex.fillInStackTrace();
            throw ex;
        }
    }

    public String toString() {
        return "ServerProcess[commands=" + commands + ", isRunning=" + isRunning() + "]";
    }

    public void stop() {
        process.destroy();
    }
}
