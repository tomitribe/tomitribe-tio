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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CountTest {

    @Test
    public void count() {
        final List<String> strings = Arrays.asList(
                "red",
                "green",
                "blue",
                "red",
                "red",
                "blue"
        );

        final List<Count<String>> counted = Count.count(strings.stream()).collect(Collectors.toList());
        assertEquals(new Count<>("green", 1), counted.get(0));
        assertEquals(new Count<>("blue", 2), counted.get(1));
        assertEquals(new Count<>("red", 3), counted.get(2));
    }

    @Test
    public void toMap() {
        final List<String> strings = Arrays.asList(
                "red",
                "green",
                "blue",
                "red",
                "red",
                "blue"
        );

        final List<Count<String>> counted = Count.count(strings.stream()).collect(Collectors.toList());
        final Map<String, Long> map = Count.toMap(counted);

        assertEquals(1, (long) map.get("green"));
        assertEquals(2, (long) map.get("blue"));
        assertEquals(3, (long) map.get("red"));
    }
}