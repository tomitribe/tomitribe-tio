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

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Match;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.tomitribe.tio.MatchAsserts.assertMatch;

public class GroovyTest {

    @Test
    public void parse() throws IOException {
        final String string = "import groovy.io.FileType\n" +
                "import groovy.text.SimpleTemplateEngine\n" +
                "import groovy.transform.BaseScript\n" +
                "import org.apache.commons.cli.Option\n" +
                "import org.yaml.snakeyaml.Yaml\n" +
                "\n" +
                "@BaseScript MainScript mainScript\n" +
                "\n" +
                "/** Parameter processing: **/\n" +
                "def cli = new CliBuilder(usage: 'summary-report-builder.groovy [options]')\n";

        final File dir = Files.tmpdir();
        final File file = new File(dir, "CliBuilder.groovy");
        IO.copy(IO.read(string), file);

        final Java java = Groovy.parse(file);

        assertEquals(null, java.getPackageName());

        final Iterator<String> imports = java.getImports().iterator();
        assertEquals("org.apache.commons.cli.Option", imports.next());
        assertEquals("org.yaml.snakeyaml.Yaml", imports.next());
        assertFalse(imports.hasNext());
    }


    @Test
    public void importsStream() throws Exception {
        final File dir = Files.tmpdir();
        final File file = new File(dir, "Foo.java");
        final String string = "import groovy.io.FileType\n" +
                "import groovy.text.SimpleTemplateEngine\n" +
                "import groovy.transform.BaseScript\n" +
                "import org.apache.commons.cli.Option\n" +
                "import org.yaml.snakeyaml.Yaml\n" +
                "\n" +
                "@BaseScript MainScript mainScript\n" +
                "\n" +
                "/** Parameter processing: **/\n" +
                "def cli = new CliBuilder(usage: 'summary-report-builder.groovy [options]')\n";
        IO.copy(IO.read(string), file);

        final Iterator<Match> imports = Groovy.imports(Dir.from(dir), file)
                .collect(Collectors.toList())
                .iterator();

        assertMatch(4, "org.apache.commons.cli.Option", imports.next());
        assertMatch(5, "org.yaml.snakeyaml.Yaml", imports.next());
        Assert.assertFalse(imports.hasNext());
    }

}