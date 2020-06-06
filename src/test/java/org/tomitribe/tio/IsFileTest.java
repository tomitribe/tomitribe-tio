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

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class IsFileTest {

    @Test
    void acceptDir() {
        final File dir = Files.tmpdir();

        final IsFile isFile = new IsFile();
        Assert.assertFalse(isFile.accept(dir));
    }

    @Test
    void acceptFile() throws IOException {
        final File dir = Files.tmpdir();
        final File file = new File(dir, "red.txt");
        IO.copy(IO.read("red rum"), file);
        final IsFile isFile = new IsFile();
        Assert.assertTrue(isFile.accept(file));
    }
}