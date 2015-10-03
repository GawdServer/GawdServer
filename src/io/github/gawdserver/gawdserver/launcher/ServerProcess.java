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

public class ServerProcess {
    private final Process process;
    private ServerExit onExit;

    public ServerProcess(Process process) {
        this.process = process;

        ProcessMonitorThread monitor = new ProcessMonitorThread(this);
        monitor.start();
    }

    public Process getProcess() {
        return process;
    }

    private boolean isRunning() {
        try {
            process.exitValue();
        } catch (IllegalThreadStateException ex) {
            return true;
        }
        return false;
    }

    private void setExitRunnable(ServerExit onExit) {
        this.onExit = onExit;
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
        return process.exitValue();
    }

    public String toString() {
        return String.format("ServerProcess[isRunning=%b]", isRunning());
    }

    public void stop() {
        process.destroy();
    }
}
