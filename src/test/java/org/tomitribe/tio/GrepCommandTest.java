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
package org.tomitribe.tio;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;
import org.tomitribe.util.Join;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GrepCommandTest {

    @Test
    public void test() throws Exception {
        final File tmpdir = Files.tmpdir();
        final String s = "    type: EXCLUSIVE\n" +
                "    java_home: /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home\n" +
                "  - hostname: miles\n" +
                "    name: davis\n" +
                "    jvm_options: \"\"\n" +
                "    type: EXCLUSIVE\n" +
                "    java_home: /Library/Java/JavaVirtualMachines/jdk1.7.0_102.jdk/Contents/Home\n" +
                "  - hostname: mingus\n" +
                "    description: Mac Pro For iOS Builds\n" +
                "    label: core-mingus core-miles-ios-app-submission core-jazz-ios-pro\n";

        final File file = new File(tmpdir, "greptest.yml");
        IO.copy(IO.read(s), file);

        final Dir dir = Dir.from(tmpdir);

        final GrepCommand command = new GrepCommand();
        final List<Label> labels = command.label(null, null,
                Pattern.compile("jdk"), Substitution.parse("s/.*1.8.*/Java 8/; s/.*7.*/Java 7/"), dir)
                .collect(Collectors.toList());

        Assert.assertEquals(4, labels.size());

        final Label label = labels.get(0);
        Assert.assertEquals("Java 8", label.getLabel());

        final List<String> list = labels.stream().map(Label::getLabel).collect(Collectors.toList());
        Assert.assertEquals("Java 8\n" +
                "Java 8\n" +
                "Java 7\n" +
                "Java 7", Join.join("\n", list));

    }

}