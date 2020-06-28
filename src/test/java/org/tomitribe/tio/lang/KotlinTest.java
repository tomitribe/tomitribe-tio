package org.tomitribe.tio.lang;

import org.junit.jupiter.api.Test;
import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Match;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tomitribe.tio.MatchAsserts.assertMatch;

public class KotlinTest {

    @Test
    public void parse() throws IOException {
        final String string = "package com.github.foo.java.function.generator\n" +
                "\n" +
                "import org.junit.Test\n" +
                "import kotlin.reflect.KCallable\n" +
                "import kotlin.reflect.KClass\n" +
                "import kotlin.reflect.KFunction\n" +
                "import kotlin.reflect.KFunction1\n" +
                "import kotlin.reflect.full.isSubclassOf\n" +
                "import kotlin.reflect.full.isSuperclassOf\n" +
                "import org.test.Message as testMessage\n" +
                "\n" +
                "class LambdaAndMethodRefExamination\n" +
                "{\n";

        final File dir = Files.tmpdir();
        final File file = new File(dir, "LambdaAndMethodRefExamination.kt");
        IO.copy(IO.read(string), file);

        final Java java = Kotlin.parse(file);

        assertEquals("com.github.foo.java.function.generator", java.getPackageName());

        final Iterator<String> imports = java.getImports().iterator();
        assertEquals("org.junit.Test", imports.next());
        assertEquals("org.test.Message", imports.next());
        assertFalse(imports.hasNext());

    }

    @Test
    public void importsStream() throws Exception {
        final File dir = Files.tmpdir();
        final File file = new File(dir, "Foo.java");
        final String string = "package com.github.foo.java.function.generator\n" +
                "\n" +
                "import org.junit.Test\n" +
                "import kotlin.reflect.KCallable\n" +
                "import kotlin.reflect.KClass\n" +
                "import kotlin.reflect.KFunction\n" +
                "import kotlin.reflect.KFunction1\n" +
                "import kotlin.reflect.full.isSubclassOf\n" +
                "import kotlin.reflect.full.isSuperclassOf\n" +
                "import org.test.Message as testMessage\n" +
                "\n" +
                "class LambdaAndMethodRefExamination\n" +
                "{\n";
        IO.copy(IO.read(string), file);

        final Iterator<Match> imports = Kotlin.imports(Dir.from(dir), file)
                .collect(Collectors.toList())
                .iterator();

        assertMatch(3, "org.junit.Test", imports.next());
        assertMatch(10, "org.test.Message", imports.next());
        assertFalse(imports.hasNext());
    }
}