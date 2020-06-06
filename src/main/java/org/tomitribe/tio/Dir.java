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

import org.tomitribe.util.dir.Filter;
import org.tomitribe.util.dir.Walk;

import java.io.File;
import java.util.stream.Stream;

public interface Dir extends org.tomitribe.util.dir.Dir {

    /**
     * Recursively find all plain files, excluding directories and symlinks
     */
    @Walk
    @Filter(IsFile.class)
    Stream<File> searchFiles();

    /**
     * JAX-RS and CREST compatible constructor.  Do not delete.
     */
    static Dir from(final String path) {
        return from(new File(path));
    }

    /**
     *
     * @param file instance representing the root directory of an actual Git clone
     * @return a strongly-typed perspective of the Git clone
     */
    static Dir from(final File file) {
        return org.tomitribe.util.dir.Dir.of(Dir.class, file);
    }
}
