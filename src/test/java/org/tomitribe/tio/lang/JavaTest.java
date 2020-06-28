package org.tomitribe.tio.lang;

import org.junit.jupiter.api.Test;
import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Match;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;
import org.tomitribe.util.PrintString;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.tomitribe.tio.MatchAsserts.assertMatch;
import static org.tomitribe.tio.lang.Java.java;

public class JavaTest {

    private final List<Java> javas = Arrays.asList(// No imports will survive for this one
            java("com.example.color.green",
                    "com.example.color.red.Crimson",
                    "com.example.color.red.Ruby"),
            // Just 'Snow' will survive.  We should see two 'green' Java instances
            java("com.example.color.green",
                    "com.example.color.white.Snow",
                    "com.example.color.red.Crimson",
                    "com.example.color.red.Ruby"),
            // 2 Snow and 1 Sun imports will survive.
            java("com.example.color.red",
                    "com.example.color.green.Forrest",
                    "com.example.color.white.Snow",
                    "com.example.color.white.Snow",
                    "com.example.color.yellow.Sun",
                    "com.example.color.green.Emerald",
                    "com.example.color.blue.Navy"),
            // Charcoal will survive
            java("com.example.color.blue",
                    "com.example.color.red.Crimson",
                    "com.example.color.black.Charcoal",
                    "com.example.color.green.Forrest"
            ));

    @Test
    public void isInternal() {

        final Predicate<String> isInternal = Java.isInternal(javas);

        assertTrue(isInternal.test("com.example.color.red.Crimson"));
        assertFalse(isInternal.test("com.example.color.white.Crimson"));
        assertFalse(isInternal.test("com.example.color.black.Charcoal"));
        assertTrue(isInternal.test("com.example.color.blue.Navy"));
        assertTrue(isInternal.test("com.example.color.green.Emerald"));
        assertTrue(isInternal.test("com.example.color.green.Forrest"));
        assertTrue(isInternal.test("com.example.color.red.Crimson"));
        assertTrue(isInternal.test("com.example.color.red.Ruby"));
        assertFalse(isInternal.test("com.example.color.white.Snow"));
        assertFalse(isInternal.test("com.example.color.yellow.Sun"));
    }

    /**
     * Any import pointing to a package that exists inside the Java list should
     * be removed leaving only the imports to external classes.
     *
     * If a class is imported multiple times, each reference should remain.
     */
    @Test
    public void externalImports() {

        final List<Java> filtered = Java.externalImports(this.javas);

        /*
         * Easy way to test all results.
         */
        final PrintString expected = new PrintString();
        for (final Java java : filtered) {
            expected.println(java.getPackageName());
            for (final String anImport : java.getImports()) {
                expected.println("  " + anImport);
            }
        }

        assertEquals("com.example.color.green\n" +
                "com.example.color.green\n" +
                "  com.example.color.white.Snow\n" +
                "com.example.color.red\n" +
                "  com.example.color.white.Snow\n" +
                "  com.example.color.white.Snow\n" +
                "  com.example.color.yellow.Sun\n" +
                "com.example.color.blue\n" +
                "  com.example.color.black.Charcoal\n", expected.toString());

    }

    @Test
    public void importsStream() throws Exception {
        final File dir = Files.tmpdir();
        final File file = new File(dir, "Foo.java");
        final String content = "package com.example.jtier.ra.cli;\n" +
                "\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "import org.tomitribe.util.PrintString;\n" +
                "\n" +
                "import java.util.Arrays;\n" +
                "import java.util.List;\n" +
                "import java.util.function.Predicate;\n" +
                "\n" +
                "import static com.example.jtier.ra.lang.Java.java;\n" +
                "import static org.junit.Assert.assertEquals;\n" +
                "import static org.junit.Assert.assertFalse;\n" +
                "import static org.junit.Assert.assertTrue;\n" +
                "\n" +
                "public class JavaTest {\n" +
                "\n";
        IO.copy(IO.read(content), file);

        final Iterator<Match> imports = Java.imports(Dir.from(dir), file)
                .collect(Collectors.toList())
                .iterator();

        assertMatch(3, "org.junit.jupiter.api.Test", imports.next());
        assertMatch(4, "org.tomitribe.util.PrintString", imports.next());
        assertMatch(10, "com.example.jtier.ra.lang.Java", imports.next());
        assertMatch(11, "org.junit.Assert", imports.next());
        assertFalse(imports.hasNext());
    }

}