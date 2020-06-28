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


import org.tomitribe.tio.Dir;
import org.tomitribe.tio.Match;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Stream;

public class JvmLang {
    private JvmLang() {
    }

    public static Java parse(final File file) {
        final String extension = fileExtension(file);
        if ("scala".equals(extension)) return Scala.parse(file);
        if ("groovy".equals(extension)) return Groovy.parse(file);
        if ("kt".equals(extension)) return Kotlin.parse(file);
        return Java.parse(file);
    }

    public static Stream<Match> imports(final Dir dir, final File file) {
        final String extension = fileExtension(file);
        if ("scala".equals(extension)) return Scala.imports(dir, file);
        if ("groovy".equals(extension)) return Groovy.imports(dir, file);
        if ("kt".equals(extension)) return Kotlin.imports(dir, file);
        return Java.imports(dir, file);
    }

    public static Function<File, Stream<Match>> imports(final Dir dir) {
        return file -> {
            final String extension = fileExtension(file);
            if ("scala".equals(extension)) return Scala.imports(dir, file);
            if ("groovy".equals(extension)) return Groovy.imports(dir, file);
            if ("kt".equals(extension)) return Kotlin.imports(dir, file);
            return Java.imports(dir, file);
        };
    }


    public static String fileExtension(final File file) {
        final int i = file.getName().lastIndexOf('.');
        if (i > 0) return file.getName().substring(i + 1);
        return null;
    }
}
