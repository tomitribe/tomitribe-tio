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

import lombok.AllArgsConstructor;
import org.tomitribe.util.IO;
import org.tomitribe.util.SizeUnit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.MalformedInputException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class Grep {

    private final Dir dir;
    private final Function<Line, Stream<Match.Progress>> matcher;

    public static boolean excludeGitFiles(final File file) {
        return !file.getAbsolutePath().contains("/.git/");
    }

    public Stream<Match> file(final File file) {
        try {
            return grep(file);
        } catch (BinaryFileException e) {
            System.err.println(e.getMessage());
            return Stream.of();
        } catch (Exception e) {
            System.err.println("Failed " + file.getAbsolutePath());
            e.printStackTrace();
            return Stream.of();
        }
    }

    public Stream<Match> grep(final File file) {
        try { // can't use try-with-resources as it closes the Stream we're returning

            final Stream<String> lines = getLines(file);
            final Function<String, Line> counter = Line.counter();

            final List<Match> matches = lines.map(counter)
                    .flatMap(matcher)
                    .map(progress -> progress.file(file).dir(dir).build())
                    .collect(Collectors.toList());

            /*
             * If you're wondering why we just took a Stream of `Match` turned into
             * a List and then immediately turned it back into a Stream...
             *
             * If we don't the actual reading of the file happens outside this
             * method where we cannot catch file reading exceptions and recover.
             */
            return matches.stream();
        } catch (java.io.UncheckedIOException e) {
            final IOException cause = e.getCause();
            if (cause instanceof MalformedInputException) {
                throw new BinaryFileException(file, cause);
            } else {
                throw e;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read file " + file.getAbsolutePath());
        }
    }

    public static Stream<String> getLines(final File file) throws IOException {
        if (file.length() > SizeUnit.MEGABYTES.toBytes(1)) {
            return java.nio.file.Files.lines(file.toPath());
        } else {
            try (final InputStream in = IO.read(file)) {
                final String slurp = IO.slurp(in);
                return Stream.of(slurp.split("\n"));
            }
        }
    }

    public static <T> Function<File, Stream<T>> skipFailed(final Function<File, Stream<T>> search) {
        return file -> {
            try {
                return search.apply(file);
            } catch (BinaryFileException e) {
                System.err.println(e.getMessage());
                return Stream.of();
            } catch (Exception e) {
                System.err.println("Failed " + file.getAbsolutePath());
                e.printStackTrace();
                return Stream.of();
            }
        };
    }

    public static Builder builder() {
        return new Grep.Builder();
    }

    public static class Builder {

        private Dir dir = Dir.from(new File(""));
        private Predicate<File> filter = file -> true;
        private Function<Line, Stream<Match.Progress>> matcher;
        private Function<Function<File, Stream<Match>>, Function<File, Stream<Match>>> decorator = Builder::none;

        Builder() {

        }

        private static <T> Function<File, Stream<T>> none(final Function<File, Stream<T>> search) {
            return search;
        }

        public Builder dir(final File file) {
            return dir(Dir.from(file));
        }

        public Builder dir(final Dir dir) {
            this.dir = dir;
            return this;
        }

        public Builder filter(final Predicate<File> filter) {
            this.filter = filter;
            return this;
        }

        public Builder matcher(final Pattern regex) {
            return matcher(new PatternMatcher(regex));
        }

        public Builder matcher(final Function<Line, Stream<Match.Progress>> matcher) {
            this.matcher = matcher;
            return this;
        }

        public Grep build() {
            if (matcher == null) throw new IllegalStateException("A Matcher must be specified");
            if (filter == null) filter = file -> true;
            if (dir == null) dir = Dir.from(new File(""));

            return new Grep(dir, matcher);
        }

        public String toString() {
            return "Grep.Builder(dir=" + this.dir + ", filter=" + this.filter + ", matcher=" + this.matcher + ")";
        }
    }
}
