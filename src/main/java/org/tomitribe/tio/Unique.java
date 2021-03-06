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
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tomitribe.util.PrintString;
import org.tomitribe.util.hash.XxHash64;

import java.util.function.Function;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Unique<T> implements Comparable<Unique> {

    @EqualsAndHashCode.Include
    private final long id;

    private final T value;

    @Override
    public int compareTo(final Unique that) {
        return Long.compare(id, that.id);
    }

    public static <T> Unique<T> of(final T t, final Function<T, ?>... fields) {
        final PrintString out = new PrintString();
        for (final Function<T, ?> field : fields) {
            out.print(field.apply(t));
            out.write('\000');
        }
        final long hash = XxHash64.hash(out.toString());
        return new Unique<>(hash, t);
    }
}
