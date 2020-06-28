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
