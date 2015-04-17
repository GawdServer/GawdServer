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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Vinnie on 2/3/2015.
 */
public class EventThread extends Thread {
    private Method onEvent;
    private String[] arguments;

    public EventThread(Method onEvent, String... arguments) {
        this.onEvent = onEvent;
        this.arguments = arguments;
    }

    public void run() {
        try {
            onEvent.invoke(null, arguments);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
