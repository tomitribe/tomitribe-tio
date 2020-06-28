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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scala {


    public static Java parse(final File file) {
        final Java java = Java.parse(file);
        final List<String> imports = java.getImports().stream()
                .flatMap(Scala::expandImport)
                .filter(Scala::isInteresting)
                .collect(Collectors.toList());

        return Java.builder()
                .file(java.getFile())
                .packageName(java.getPackageName())
                .imports(imports)
                .build();
    }

    public static Stream<Match> imports(final Dir dir, final File file) {
        return Java.imports(dir, file)
                .flatMap(Scala::expandImport)
                .filter(Scala::isInteresting)
                ;
    }

    private static Stream<String> expandImport(final String line) {
        if (!line.contains("{")) return Stream.of(line.replace("_", "*"));

        final List<String> parts = new ArrayList<>(Arrays.asList(line.split(" *[{},] *")));
        final String prefix = parts.remove(0);

        return parts.stream()
                .map(s -> prefix + s)
                ;
    }

    public static boolean isInteresting(final String s) {
        if (s.startsWith("scala.")) return false;
        return true;
    }

    private static Stream<Match> expandImport(final Match match) {
        return expandImport(match.getMatch())
                .map(s -> match.toBuilder().match(s).build());
    }

    public static boolean isInteresting(final Match s) {
        return isInteresting(s.getMatch());
    }

}
