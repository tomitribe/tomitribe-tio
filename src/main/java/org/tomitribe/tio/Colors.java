/*
 * Copyright (c) 2019 Tomitribe and Contributors
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.tio;

import java.util.function.Function;

public class Colors {

    private Colors() {
    }

    public static Function<String, String> foreground(int foreground) {
        return s -> foreground(foreground, s);
    }

    public static String foreground(int foreground, final Object string) {
        return String.format("\033[38;5;%sm%s\033[0m", foreground, string);
    }

    public static String color(int foreground, int background, final Object string) {
        return String.format("\033[48;5;%sm\033[38;5;%sm%s\033[0m", background, foreground, string);
    }

    public static String background(int background, final Object string) {
        return String.format("\033[48;5;%sm%s\033[0m", background, string);
    }
}
