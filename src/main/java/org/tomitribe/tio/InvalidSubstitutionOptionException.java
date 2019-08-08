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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvalidSubstitutionOptionException extends InvalidSubstitutionException {
    public InvalidSubstitutionOptionException(final int option, final String statement) {
        super("Invalid Substitution option '" + option + "': " + statement + "\nValid options: " + opts(), statement);
    }

    private static String opts() {
        final List<String> list = Stream.of(Substitution.ReplaceMode.values())
                .map(option -> option.getFlag() + "")
                .collect(Collectors.toList());
        return Join.join(" ", list);
    }
}
