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

import java.io.File;

class MatchTest {

    @Test
    void uniqueMatchText() {
    }

    @Test
    void path() {
        final Dir dir = Dir.from(Files.tmpdir());
        final File file = dir.file("orange/sailboat.txt");
        final Match match = new Match(dir, file, new Line(42, "The Answer"), "Answer");

        Assert.assertEquals("orange/sailboat.txt", match.path());
    }

    @Test
    void userPath() {
        final Dir dir = Dir.from(Files.tmpdir());
        final File file = dir.file("orange/sailboat.txt");
        final Match match = new Match(dir, file, new Line(42, "The Answer"), "Answer");

        Assert.assertEquals(file.getAbsolutePath(), match.userPath());
    }

    @Test
    void testToString() {
        final Dir dir = Dir.from(Files.tmpdir());
        final File file = dir.file("orange/sailboat.txt");
        final Match match = new Match(dir, file, new Line(42, "The Answer"), "Answer");
        Assert.assertEquals(file.getAbsolutePath() + ":42:Answer:The Answer", match.toString());
    }
}