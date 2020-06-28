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

import lombok.Builder;
import lombok.Data;
import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Grep;
import org.tomitribe.tio.Match;
import org.tomitribe.tio.Unique;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
public class Java {
    private final File file;
    private final String packageName;
    private final List<String> imports;
    protected static final Pattern PACKAGE = Pattern.compile("(.*)\\.[A-Z].*");

    Java(final File file, String packageName, List<String> imports) {
        this.packageName = packageName;
        this.imports = imports;
        this.file = file;
    }

    public static Java parse(final File file) {
        final String content = slurp(file);

        final String packag = Stream.of(content.split("\n"))
                .filter(s -> s.startsWith("package "))
                .map(Java::removePackage)
                .findFirst()
                .orElse(null);

        final List<String> imports = Stream.of(content.split("\n"))
                .filter(s -> s.startsWith("import "))
                .map(Java::removeImport)
                .map(Java::removeStatic)
                .filter(Java::isInteresting)
                .distinct()
                .collect(Collectors.toList());

        return Java.builder()
                .packageName(packag)
                .imports(imports)
                .file(file)
                .build();
    }

    public static Stream<Match> imports(final Dir dir, final File file) {
        final Grep grep = Grep.builder()
                .dir(dir)
                .matcher(Pattern.compile("^import +([^;/]+)"))
                .build();

        return grep.file(file)
                .map(Java::removeStatic)
                .filter(Java::isInteresting)
                .map(Match::uniqueMatchText)
                .distinct()
                .map(Unique::getValue)
                ;
    }


    public static boolean isInteresting(final Match match) {
        return isInteresting(match.getMatch());
    }

    public static boolean isInteresting(final String s) {
        if (s.startsWith("java.util.logging")) return true;
        if (s.startsWith("java.")) return false;
        return true;
    }

    public static String removeImport(final String s) {
        if (!s.startsWith("import ")) return s;
        return s.replaceAll("import +(.*);?", "$1");
    }

    public static String removePackage(final String s) {
        if (!s.startsWith("package ")) return s;
        return s.replaceAll("package +(.*);?", "$1");
    }

    public static String removeStatic(final String s) {
        if (!s.startsWith("static ")) return s;
        return s.replaceAll("static +(.*)\\.[^.]+", "$1");
    }

    public static Match removeStatic(final Match match) {
        return Match.builder()
                .dir(match.getDir())
                .file(match.getFile())
                .line(match.getLine())
                .match(removeStatic(match.getMatch()))
                .build();
    }

    public static Predicate<String> isInternal(final Collection<Java> javas) {
        return isInternal(javas.stream());
    }

    public static Predicate<String> isInternal(final Stream<Java> stream) {
        final Optional<Predicate<String>> optionalPredicate = (Optional<Predicate<String>>) stream
                .map(Java::getPackageName)
                .filter(Objects::nonNull)
                .sorted()
                .distinct()
                .map(packageName -> (Predicate<String>) importedClass -> importedClass.startsWith(packageName))
                .reduce(Predicate::or);

        return optionalPredicate.orElseGet(() -> s -> false);
    }

    /**
     * Filter the list of Java instances to remove any imports that are to
     * other Java classes inside the list.  Leave only imports to external
     * Java classes.
     */
    public static List<Java> externalImports(final List<Java> javas) {
        final Stream<Java> importsStream = externalImportsStream(javas.stream());

        return importsStream.collect(Collectors.toList());

    }

    public static Stream<Java> externalImportsStream(final Stream<Java> javaStream) {
        final List<Java> list = javaStream.collect(Collectors.toList());
        final Predicate<String> externalOnly = isInternal(list.stream()).negate();

        final Function<Java, Java> trimImports = java -> {
            final List<String> externalImports = java.getImports().stream()
                    .filter(externalOnly)
                    .collect(Collectors.toList());

            return Java.builder()
                    .packageName(java.getPackageName())
                    .imports(externalImports)
                    .build();
        };

        return list.stream().map(trimImports);
    }

    public static Java java(final String packageName, final String... imports) {
        return builder()
                .packageName(packageName)
                .imports(Arrays.asList(imports))
                .build();
    }

    public static String asPackage(final String className) {
        return PACKAGE.matcher(className).replaceAll("$1");
    }

    public static String slurp(final File file) {
        final String content;
        try {
            content = IO.slurp(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
        return content;
    }
}
