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