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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@AllArgsConstructor
public class PatternMatcher implements Function<Line, Stream<Match.Progress>> {

    private final Pattern pattern;

    @Override
    public Stream<Match.Progress> apply(final Line line) {
        // Most often we get no matches
        // Very rarely do we have more than one and need the array
        // As an optimization we initialize them lazily
        // If the file is 1000 lines, we potentially avoid 1000 needless array creations
        String firstMatch = null;
        List<String> matches = null;

        final Matcher matcher = pattern.matcher(line.getText());
        final int group = matcher.groupCount() > 0 ? 1 : 0;
        while (matcher.find()) {

            final String match = matcher.group(group);

            if (firstMatch == null && matches == null) {

                firstMatch = match;

            } else if (matches == null) {

                matches = new ArrayList<>(5);
                matches.add(firstMatch);
                matches.add(match);
                firstMatch = null;

            } else {

                matches.add(match);

            }
        }

        if (firstMatch != null) { // we only found one match

            return Stream.of(Match.builder().line(line).match(firstMatch));

        } else if (matches != null) { // we found many matches

            return matches.stream().map(s -> Match.builder().line(line).match(s));

        } else { // no matches found

            return Stream.of();

        }
    }

    public static PatternMatcher from(final String regex) {
        return from(Pattern.compile(regex));
    }

    public static PatternMatcher from(final Pattern pattern) {
        return new PatternMatcher(pattern);
    }
}
