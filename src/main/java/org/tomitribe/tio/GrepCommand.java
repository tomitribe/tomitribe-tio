/*
 * Copyright (c) 2019 Tomitribe and Contributors
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

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.Join;
import org.tomitribe.util.hash.XxHash64;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrepCommand {

    @Command(value = "grep", interceptedBy = ColoredMatches.class)
    public Stream<Match> grep(@Option("include") Pattern include,
                              @Option("exclude") Pattern exclude,
                              final Pattern regex, final Dir dir) {
        final PatternMatcher matcher = PatternMatcher.from(regex);

        final Predicate<File> fileFilter = Predicates.fileFilter(include, exclude)
                .and(file -> GrepCommand.binaries.asPredicate().negate().test(file.getName()));

        final Grep grep = Grep.builder()
                .dir(dir)
                .matcher(matcher)
                .build();


        return dir.searchFiles()
                .filter(Grep::excludeGitFiles)
                .filter(fileFilter)
                .flatMap(grep::file);
    }

    /**
     * Search for files that contain the first pattern, if it does grep the
     * file for occurrences of the second pattern.
     *
     * For example, one could search for "import javax.ws.rs.GET;" and then
     * subsequently search for uses of "@GET" to see how frequently that annotation
     * is used.
     *
     * Were someone to do a direct search for "@GET" they would get both the
     * JAX-RS and Retrofit 1 and 2 annotations.
     *
     * @param include
     * @param exclude
     * @param first
     * @param second
     * @param dir
     * @return
     */
    @Command(value = "grep-grep", interceptedBy = ColoredMatches.class)
    public Stream<Match> grepMatches(@Option("include") Pattern include,
                                     @Option("exclude") Pattern exclude,
                                     final Pattern first,
                                     final Pattern second,
                                     final Dir dir) {

        final Predicate<File> fileFilter = Predicates.fileFilter(include, exclude)
                .and(file -> GrepCommand.binaries.asPredicate().negate().test(file.getName()));

        final Grep firstGrep = Grep.builder()
                .dir(dir)
                .matcher(PatternMatcher.from(first))
                .build();

        final Grep secondGrep = Grep.builder()
                .dir(dir)
                .matcher(PatternMatcher.from(second))
                .build();

        return dir.searchFiles()
                .filter(Grep::excludeGitFiles)
                .filter(fileFilter)
                .flatMap(firstGrep::file)
                .map(Match::getFile)
                .map(GrepCommand::unique)
                .distinct()
                .map(Unique::getValue)
                .flatMap(secondGrep::file);
    }

    @Command(value = "label", interceptedBy = ColoredLabels.class)
    public Stream<Label> label(@Option("include") Pattern include,
                               @Option("exclude") Pattern exclude,
                               final Pattern regex,
                               final Substitution substitution,
                               final Dir dir) {

        final Function<Match, Label> toLabel = match -> {
            final String label = substitution.apply(match);
            return (label == null) ? null : new Label(match, label);
        };

        return grep(include, exclude, regex, dir)
                .map(toLabel)
                .filter(Objects::nonNull);
    }

    @Command("matches")
    public Stream<String> matches(@Option("include") Pattern include,
                                  @Option("exclude") Pattern exclude,
                                  final Pattern regex, final Dir dir) {
        return grep(include, exclude, regex, dir)
                .map(Match::getMatch);
    }

    @Command("count")
    public Stream<Count<String>> count(@Option("include") Pattern include,
                                       @Option("exclude") Pattern exclude,
                                       @Option("sort") @Default("ascending") Sort sort,
                                       final Pattern regex, final Dir dir) {

        final Comparator<Count> comparator = sort.order(Count::compareTo);

        return Count.count(grep(include, exclude, regex, dir)
                .map(Match::getMatch))
                .sorted(comparator)
                ;
    }

    public static Stream<Count<String>> totalScores(final Stream<Score<Label>> imports) {
        return imports
                .collect(Collectors.groupingBy(labelScore -> labelScore.getValue().getLabel()))
                .entrySet().stream()
                .map(entry -> {
                    final List<Score<Label>> value = entry.getValue();
                    return new Count<>(entry.getKey(), getScore(value));
                })
                .sorted();
    }

    public static int getScore(final List<Score<Label>> value) {
        int total = 0;
        for (final Score<Label> labelScore : value) {
            total += labelScore.getScore();
        }
        return total;
    }


    public static Unique<Label> unique(final Label label) {
        final long hash = XxHash64.hash(Join.join("\000",
                label.getLabel(),
                label.getMatch().getFile().getAbsolutePath(),
                label.getMatch().getLine().getNumber(),
                label.getMatch().getLine().getText()
        ));

        return new Unique<>(hash, label);
    }

    public static Unique<File> unique(final File file) {
        final long hash = XxHash64.hash(Join.join("\000", file.getAbsolutePath()));

        return new Unique<>(hash, file);
    }

    @Command("ls")
    public Stream<String> ls(@Option("include") Pattern include,
                             @Option("exclude") Pattern exclude,
                             final Dir dir) {

        final Predicate<File> fileFilter = Predicates.fileFilter(include, exclude);

        return dir.searchFiles()
                .filter(fileFilter)
                .map(file -> Paths.childPath(new File(""), file));
    }

    public static final Pattern binaries = Pattern.compile("\\.(gem|gif|gz|ico|jks|jpg|mwb|p12|pdf|phar|pkcs12|rdb|twbx|xlsx)$");

}
