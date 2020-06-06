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
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Count<T> implements Comparable<Count<T>> {
    private final T value;
    private final long count;

    @Override
    public int compareTo(final Count<T> o) {
        return Long.compare(this.count, o.count);
    }

    @Override
    public String toString() {
        return String.format("%s %s", count, value);
    }

    public static Stream<Count<String>> count(final Stream<String> imports) {
        return imports
                .collect(Collectors.groupingBy(s -> s))
                .entrySet().stream()
                .map(entry -> new Count<>(entry.getKey(), entry.getValue().size()))
                .sorted();
    }

    public static <T> Map<T, Long> toMap(final List<Count<T>> counts) {
        final Map<T, Long> map = new HashMap<>();
        for (final Count<T> count : counts) {
            map.put(count.getValue(), count.getCount());
        }
        return map;
    }
}
