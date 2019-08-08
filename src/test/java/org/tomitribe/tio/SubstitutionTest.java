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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SubstitutionTest {

    @Test
    public void parse() {
        final Substitution substitution = Substitution.parse("s/apple/orange/");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");

        assertEquals("Apple does not make oranges, they make computers", replaced);
    }

    @Test
    public void replacementGroups() {
        final Substitution substitution = Substitution.parse("s/(does) not/$1/");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");

        assertEquals("Apple does make apples, they make computers", replaced);
    }

    @Test
    public void chainedSubstitutions() {
        final Substitution substitution = Substitution.parse("s/(does) not/$1/; s/, they/. They also/");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");

        assertEquals("Apple does make apples. They also make computers", replaced);
    }

    @Test
    public void caseInsensitive() {
        final Substitution substitution = Substitution.parse("s/(does) not/$1/; s/, THEY/. They also/i");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");

        assertEquals("Apple does make apples. They also make computers", replaced);
    }

    @Test
    public void escaping() {
        final Substitution substitution = Substitution.parse("s/http:\\/\\/www.example.com/https:\\/\\/tomitribe.com/");

        final String replaced = substitution.apply("<a href=\"http://www.example.com\">");

        assertEquals("<a href=\"https://tomitribe.com\">", replaced);
    }

    @Test
    public void replaceFullString() {
        final Substitution substitution = Substitution.parse("s/.*apple.*/orange/");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");

        assertEquals("orange", replaced);
    }

    @Test
    public void lookup() {
        final Substitution substitution = Substitution.parse("s/.*they.*/orange/n");

        { // positive replacement
            final String replaced = substitution.apply("Apple does not make apples, they make computers");
            assertEquals("orange", replaced);
        }
        { // negative replacement, should return null vs the original string
            final String replaced = substitution.apply("Apple does not make apples, it makes computers");
            assertNull(replaced);
        }
    }

    @Test
    public void replaceFirst() {
        final Substitution substitution = Substitution.parse("s/o/0/f");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");
        assertEquals("Apple d0es not make apples, they make computers", replaced);
    }

    @Test
    public void replaceFirstIsDefault() {
        final Substitution substitution = Substitution.parse("s/o/0/");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");
        assertEquals("Apple d0es not make apples, they make computers", replaced);
    }

    @Test
    public void replaceAll() {
        final Substitution substitution = Substitution.parse("s/o/0/g");

        final String replaced = substitution.apply("Apple does not make apples, they make computers");
        assertEquals("Apple d0es n0t make apples, they make c0mputers", replaced);
    }

    @Test
    public void foo() {
        final Substitution substitution = Substitution.parse("s/.*1\\.8.*/Java 8/");

        final String replaced = substitution.apply("java_home: /Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home");
        assertEquals("Java 8", replaced);
    }

    /**
     * If the second statement does not match anything, it's ok
     */
    @Test
    public void secondStatementDoesNotMatch() {
        final Substitution substitution = Substitution.parse("s/.*1\\.8.*/Java 8/; s/.*1\\.7.*/Java 7/");

        final String replaced = substitution.apply("java_home: /Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home");
        assertEquals("Java 8", replaced);
    }


}