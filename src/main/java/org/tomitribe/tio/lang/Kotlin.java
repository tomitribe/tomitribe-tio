/*
 * Copyright (c) 2020 Tomitribe and Contributors
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
package org.tomitribe.tio.lang;


import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Match;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Kotlin {

    private Kotlin() {
    }

    public static Java parse(final File file) {
        final Java java = Java.parse(file);
        final List<String> imports = java.getImports().stream()
                .map(Kotlin::removeAs)
                .filter(Kotlin::isInteresting)
                .collect(Collectors.toList());

        return Java.builder()
                .file(java.getFile())
                .packageName(java.getPackageName())
                .imports(imports)
                .build();
    }

    public static Stream<Match> imports(final Dir dir, final File file) {
        return Java.imports(dir, file)
                .map(Kotlin::removeAs)
                .filter(Kotlin::isInteresting)
                ;
    }

    private static String removeAs(final String statement) {
        return statement.replaceAll(" +as +.*", "");
    }

    public static boolean isInteresting(final String s) {
        if (s.startsWith("kotlin.")) return false;
        return true;
    }

    private static Match removeAs(final Match statement) {
        return statement.toBuilder()
                .match(removeAs(statement.getMatch()))
                .build();
    }

    public static boolean isInteresting(final Match s) {
        return isInteresting(s.getMatch());
    }
}
