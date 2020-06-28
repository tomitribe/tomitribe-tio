package org.tomitribe.tio.lang;


import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Match;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Kotlin {

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
