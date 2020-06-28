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

public class ScalaTest {

    @Test
    public void parse() throws IOException {
        final String string = "/*\n" +
                " * Copyright 2012 Twitter Inc.\n" +
                " *\n" +
                " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                " * you may not use this file except in compliance with the License.\n" +
                " * You may obtain a copy of the License at\n" +
                " *\n" +
                " *      http://www.apache.org/licenses/LICENSE-2.0\n" +
                " *\n" +
                " * Unless required by applicable law or agreed to in writing, software\n" +
                " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                " * See the License for the specific language governing permissions and\n" +
                " * limitations under the License.\n" +
                " */\n" +
                "package com.twitter.zipkin.collector.processor\n" +
                "\n" +
                "import com.twitter.finagle.Service\n" +
                "import com.twitter.scrooge.BinaryThriftStructSerializer\n" +
                "import com.twitter.zipkin.common.{Annotation, Span}\n" +
                "import com.twitter.zipkin.conversions.thrift._\n" +
                "import com.twitter.zipkin.thriftscala\n" +
                "import org.specs.Specification\n" +
                "import org.specs.mock.{JMocker, ClassMocker}\n" +
                "\n" +
                "class ScribeFilterSpec extends Specification with JMocker with ClassMocker {\n" +
                "  val serializer = new BinaryThriftStructSerializer[thriftscala.Span] {\n" +
                "    def codec = thriftscala.Span\n" +
                "  }\n";

        final File dir = Files.tmpdir();
        final File file = new File(dir, "ScribeFilterSpec.scala");
        IO.copy(IO.read(string), file);

        final Java java = Scala.parse(file);

        assertEquals("com.twitter.zipkin.collector.processor", java.getPackageName());

        final Iterator<String> imports = java.getImports().iterator();
        assertEquals("com.twitter.finagle.Service", imports.next());
        assertEquals("com.twitter.scrooge.BinaryThriftStructSerializer", imports.next());
        assertEquals("com.twitter.zipkin.common.Annotation", imports.next());
        assertEquals("com.twitter.zipkin.common.Span", imports.next());
        assertEquals("com.twitter.zipkin.conversions.thrift.*", imports.next());
        assertEquals("com.twitter.zipkin.thriftscala", imports.next());
        assertEquals("org.specs.Specification", imports.next());
        assertEquals("org.specs.mock.JMocker", imports.next());
        assertEquals("org.specs.mock.ClassMocker", imports.next());
        assertFalse(imports.hasNext());
    }

    @Test
    public void importsStream() throws Exception {
        final File dir = Files.tmpdir();
        final File file = new File(dir, "Foo.java");
        final String string = "/*\n" +
                " * Copyright 2012 Twitter Inc.\n" +
                " *\n" +
                " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                " * you may not use this file except in compliance with the License.\n" +
                " * You may obtain a copy of the License at\n" +
                " *\n" +
                " *      http://www.apache.org/licenses/LICENSE-2.0\n" +
                " *\n" +
                " * Unless required by applicable law or agreed to in writing, software\n" +
                " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                " * See the License for the specific language governing permissions and\n" +
                " * limitations under the License.\n" +
                " */\n" +
                "package com.twitter.zipkin.collector.processor\n" +
                "\n" +
                "import com.twitter.finagle.Service\n" +
                "import com.twitter.scrooge.BinaryThriftStructSerializer\n" +
                "import com.twitter.zipkin.common.{Annotation, Span}\n" +
                "import com.twitter.zipkin.conversions.thrift._\n" +
                "import com.twitter.zipkin.thriftscala\n" +
                "import org.specs.Specification\n" +
                "import org.specs.mock.{JMocker, ClassMocker}\n" +
                "\n" +
                "class ScribeFilterSpec extends Specification with JMocker with ClassMocker {\n" +
                "  val serializer = new BinaryThriftStructSerializer[thriftscala.Span] {\n" +
                "    def codec = thriftscala.Span\n" +
                "  }\n";
        IO.copy(IO.read(string), file);

        final Iterator<Match> imports = Scala.imports(Dir.from(dir), file)
                .collect(Collectors.toList())
                .iterator();

        assertMatch(18, "com.twitter.finagle.Service", imports.next());
        assertMatch(19, "com.twitter.scrooge.BinaryThriftStructSerializer", imports.next());
        assertMatch(20, "com.twitter.zipkin.common.Annotation", imports.next());
        assertMatch(20, "com.twitter.zipkin.common.Span", imports.next());
        assertMatch(21, "com.twitter.zipkin.conversions.thrift.*", imports.next());
        assertMatch(22, "com.twitter.zipkin.thriftscala", imports.next());
        assertMatch(23, "org.specs.Specification", imports.next());
        assertMatch(24, "org.specs.mock.JMocker", imports.next());
        assertMatch(24, "org.specs.mock.ClassMocker", imports.next());
        Assert.assertFalse(imports.hasNext());
    }

}