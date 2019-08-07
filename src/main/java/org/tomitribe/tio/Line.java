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

import lombok.Data;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Data
@Getter
public class Line {

    final int number;

    final String text;

    public Line(final int number, final String text) {
        this.number = number;
        this.text = text;
    }

    public static Function<String, Line> counter() {
        final AtomicInteger count = new AtomicInteger();
        return s -> new Line(count.incrementAndGet(), s);
    }

    @Override
    public String toString() {
        return number + ": " + text;
    }
}
