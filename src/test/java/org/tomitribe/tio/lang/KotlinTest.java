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