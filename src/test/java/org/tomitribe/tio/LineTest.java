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

import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    void testToString() {
        final Line line = new Line(123, "Orange submarine");
        Assert.assertEquals("123: Orange submarine", line.toString());
    }

    @Test
    void getNumber() {
        final Line line = new Line(123, "Orange submarine");
        Assert.assertEquals(123, line.getNumber());
    }

    @Test
    void getText() {
        final Line line = new Line(123, "Orange submarine");
        Assert.assertEquals("Orange submarine", line.getText());
    }
}