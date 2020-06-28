package org.tomitribe.tio.lang;


import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Match;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Groovy {

    public static Java parse(final File file) {
        final Java java = Java.parse(file);
        final List<String> imports = java.getImports().stream()
                .filter(Groovy::isInteresting)
                .collect(Collectors.toList());

        return Java.builder()
                .file(java.getFile())
                .packageName(java.getPackageName())
                .imports(imports)
                .build();
    }

    public static boolean isInteresting(final String s) {
        if (s.startsWith("groovy.")) return false;
        return true;
    }

    public static Stream<Match> imports(final Dir dir, final File file) {
        return Java.imports(dir, file)
                .filter(Groovy::isInteresting)
                ;
    }

    public static boolean isInteresting(final Match match) {
        return isInteresting(match.getMatch());
    }

}
