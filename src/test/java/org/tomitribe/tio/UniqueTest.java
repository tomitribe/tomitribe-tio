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

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class UniqueTest {

    @Test
    void compareTo() {
    }

    @Test
    void of() {
        {
            final Unique<Line> unique = Unique.of(new Line(42, "The Answer"), Line::getNumber);
            Assert.assertEquals(-3144702342842872238L, unique.getId());
        }
        {
            final Unique<Line> unique = Unique.of(new Line(42, "The Answer"), Line::getNumber, Line::getText);
            Assert.assertEquals(8123560464720744278L, unique.getId());
        }
        {
            final Unique<Line> unique = Unique.of(new Line(42, "The Answer"), Line::getText, Line::getNumber);
            Assert.assertEquals(-42845274149832293L, unique.getId());
        }
    }

    @Test
    void getId() {
    }

    @Test
    void getValue() {
    }

    @Test
    void testEquals() {
        {
            final Unique<Line> a = Unique.of(new Line(42, "The Answer to"), Line::getNumber);
            final Unique<Line> b = Unique.of(new Line(42, "life, the universe and everything"), Line::getNumber);
            final Unique<Line> c = Unique.of(new Line(43, "The Answer to"), Line::getNumber);
            Assert.assertEquals(a, b);
            Assert.assertNotEquals(a, c);
        }
        {
            final Unique<Line> a = Unique.of(new Line(42, "The Answer to"), Line::getNumber, Line::getText);
            final Unique<Line> b = Unique.of(new Line(42, "life, the universe and everything"), Line::getNumber);
            final Unique<Line> c = Unique.of(new Line(43, "The Answer to"), Line::getNumber);
            Assert.assertNotEquals(a, b);
            Assert.assertNotEquals(a, c);
        }
    }
}