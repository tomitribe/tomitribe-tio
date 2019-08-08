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

import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Perl-style substitution expressions
 *
 * Multiple delimiters are supported to ease escaping
 *
 * s,foo,bar,
 * s/foo/bar/
 * s:foo:bar:
 *
 * Escaping is supported in the event it is necessary
 *
 * s/some\/path/bar/
 *
 * Chaining is supported allowing many manipulations
 *
 * s/one/uno/; s/two/dos/; s/three/tres/
 *
 */
public class Substitution {
    private final Pattern pattern;
    private final String replacement;
    private final Function<String, String> mode;
    private final Function<Match, String> matchOn;

    /**
     * Used when creating a single substitution such as:
     *
     * s/foo/bar/i
     *
     * @param pattern the regex we will search for in the incoming line or matched text
     * @param replacement how we will modify the incoming line or matched text
     * @param replaceMode determines which function will be used, such as replaceFirst and replaceAll
     * @param findMode determines how we will pull data from the Match, do we want the full line or just the matched portion
     */
    private Substitution(final Pattern pattern,
                         final String replacement,
                         final ReplaceMode replaceMode,
                         final FindMode findMode) {
        this.pattern = pattern;
        this.replacement = replacement;

        switch (replaceMode) {
            case FIRST:
                mode = this::replaceFirst;
                break;
            case GLOBAL:
                mode = this::replaceAll;
                break;
            case NULL_IF_NONE:
                mode = this::nullIfNone;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported ReplaceMode: " + replaceMode);
        }

        switch (findMode) {
            case LINE:
                matchOn = this::line;
                break;
            case MATCH:
                matchOn = this::match;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported FindMode: " + findMode);
        }
    }

    /**
     * Used when creating a compound substitution such as:
     *
     * s/foo/bar/i; s/apple/orange/; s,http://,https://,
     *
     * Using the above example, three complete Substitution instances
     * would be created using the above constructor.  After this step
     * is complete each is treated as a functional interface and chained
     * together to create a fourth, compound Substitution using this constructor
     *
     * @param chain a chained Function of each substitution's `String apply(final String s)` method
     * @param matchOn handled entirely by the `String apply(final Match m)` method of the first Substitution
     */
    private Substitution(final Function<Match, String> matchOn, final Function<String, String> chain) {
        this.pattern = null;
        this.replacement = null;
        this.matchOn = matchOn;
        this.mode = chain;
    }

    public String apply(final String s) {
        return mode.apply(s);
    }

    public String apply(final Match match) {
        return mode.apply(matchOn.apply(match));
    }

    private String line(final Match match) {
        return match.getLine().getText();
    }

    private String match(final Match match) {
        return match.getMatch();
    }

    private String replaceAll(final String s) {
        if (s == null) return null;
        final Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll(replacement);
    }

    private String replaceFirst(final String s) {
        if (s == null) return null;
        final Matcher matcher = pattern.matcher(s);
        return matcher.replaceFirst(replacement);
    }

    /**
     * Return null if there is no match.  This line is
     * not wanted.  Negatory, not going to happen.
     * Not needed.  Is that enough Ns?
     */
    private String nullIfNone(final String s) {
        if (s == null) return null;
        final Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.replaceAll(replacement) : null;
    }

    public static Substitution of(final String... statements) {
        return parse(Join.join("; ", (Object[]) statements));
    }

    public static Substitution parse(final String statement) {
        if (statement == null) throw new MissingSubstitutionException();

        final Parser parser = new Parser(statement.trim());

        final List<Substitution> subsitutions = parser.parsed.stream()
                .map(Parser.State::getSubstitution)
                .collect(Collectors.toList());

        if (subsitutions.size() == 0) throw new InvalidSubstitutionException("No substitutions found in statement: ", statement);
        if (subsitutions.size() == 1) return subsitutions.get(0);

        return new Substitution(subsitutions.get(0).matchOn, chain(subsitutions));
    }

    private static Function<String, String> chain(final List<Substitution> subsitutions) {
        return subsitutions.stream()
                .map(substitution -> substitution.mode)
                .reduce(Function::andThen)
                .orElse(s -> s);
    }

    private static boolean isValidDelimiter(final char delimiter) {
        final char[] legal = {'/', '!', ',', ':', ';'};
        for (final char c : legal) {
            if (c == delimiter) return true;
        }
        return false;
    }

    //CHECKSTYLE:OFF
    static class Parser {
        private class State {
            private final StringBuilder findBuffer = new StringBuilder();
            private final StringBuilder replaceBuffer = new StringBuilder();
            private FindMode findMode = FindMode.LINE;
            private ReplaceMode replaceMode = ReplaceMode.FIRST;
            private boolean caseSensitive = true;
            private char delimiter;
            private boolean complete;

            Substitution getSubstitution() {
                if (!complete) throw new IncompleteSubstitutionException(statement);

                int flags = 0;

                if (!this.caseSensitive) {
                    flags = Pattern.CASE_INSENSITIVE;
                }

                final Pattern pattern = Pattern.compile(this.findBuffer.toString(), flags);

                return new Substitution(
                        pattern,
                        this.replaceBuffer.toString(),
                        this.replaceMode,
                        this.findMode
                );
            }
        }

        final List<State> parsed = new ArrayList<State>();

        private State state;
        private final String statement;


        // Used to switch parsing logic
        // which avoid complex if blocks
        private Consumer<Character> parse = this::start;

        Parser(final String statement) {
            this.statement = statement;
            this.statement.chars().forEach(this::parse);
        }

        private void parse(final int i) {
            parse.accept((char) i);
        }

        private void start(final char c) {
            if (c != 's') throw new InvalidSubstitutionPrefixException(statement);
            parse = this::delimiter;
            state = new State();
            this.parsed.add(state);
        }

        private void delimiter(final char c) {
            if (!isValidDelimiter(c)) throw new InvalidSubstitutionDelimiterException(c, statement);
            this.state.delimiter = c;
            parse = this::find;
        }

        private void find(final char c) {
            /*
             * If we hit an escape, hand control to Escape::keepNext
             */
            if (c == '\\') {
                final Escape escape = new Escape(this::find, this.state.findBuffer::append);
                this.parse = escape::keepNext;
            }
            /*
             * If we hit our delimiter, we're done.  Save nothing and hand control to "replace"
             */
            else if (c == state.delimiter) {
                this.parse = this::replace;
            }
            /*
             * Ok, we found a valid char for the find buffer
             */
            else state.findBuffer.append(c);
        }

        private void replace(final char c) {
            /*
             * If we hit an escape, hand control to Escape::keepNext
             */
            if (c == '\\') {
                final Escape escape = new Escape(this::replace, this.state.replaceBuffer::append);
                this.parse = escape::keepNext;
            }
            /*
             * If we hit our delimiter, we're done.  Mark this
             * statement as complete and usable then hand control
             * to "option"
             */
            else if (c == state.delimiter) {
                this.state.complete = true;
                this.parse = this::option;
            }
            /*
             * Ok, we found a valid char for the replace buffer
             */
            else state.replaceBuffer.append(c);
        }

        private void option(final char c) {
            /*
             * We've reached the end of any options we'll see
             * After this point the user may supply another
             * substitution statement
             */
            if (c == ';') {
                this.parse = this::whitespace;
                return;
            }

            for (final ReplaceMode option : ReplaceMode.values()) {
                if (option.getFlag() == c) {
                    this.state.replaceMode = option;
                    return;
                }
            }

            for (final FindMode option : FindMode.values()) {
                if (option.getFlag() == c) {
                    this.state.findMode = option;
                    return;
                }
            }

            if (c == PatternOption.CASE_INSENSITIVE.getFlag()) {
                this.state.caseSensitive = false;
                return;
            }

            throw new InvalidSubstitutionOptionException(c, statement);
        }

        private void whitespace(final char c) {
            if (Character.isWhitespace(c)) return;
            start(c);
        }


        private class Escape {
            private final Consumer<Character> resume;
            private final Consumer<Character> buffer;

            Escape(final Consumer<Character> resume, final Consumer<Character> buffer) {
                this.resume = resume;
                this.buffer = buffer;
            }

            void keepNext(final char c) {
                buffer.accept(c);
                Parser.this.parse = resume;
            }
        }
    }
    //CHECKSTYLE:ON
    
    public interface Flag {
        char getFlag();
    }

    public enum FindMode implements Flag {

        LINE('l'),
        MATCH('m');

        private final char flag;

        FindMode(final char flag) {
            this.flag = flag;
        }

        public char getFlag() {
            return flag;
        }
    }

    public enum ReplaceMode implements Flag {

        NULL_IF_NONE('n'),
        FIRST('f'),
        GLOBAL('g');

        private final char flag;

        ReplaceMode(final char flag) {
            this.flag = flag;
        }

        public char getFlag() {
            return flag;
        }
    }

    public enum PatternOption implements Flag {

        CASE_INSENSITIVE('i');

        private final char flag;

        PatternOption(final char flag) {
            this.flag = flag;
        }

        public char getFlag() {
            return flag;
        }
    }

}
