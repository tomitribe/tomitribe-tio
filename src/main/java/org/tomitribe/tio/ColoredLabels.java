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

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.util.PrintString;

import java.util.stream.Stream;

public class ColoredLabels {

    private final ColoredMatches coloredMatches = new ColoredMatches();

    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        final Object response = crestContext.proceed();

        if (!(response instanceof Stream)) return response;

        final Stream<Label> stream = (Stream<Label>) response;

        return stream.map(this::color);
    }

    public String color(final Label label) {
        final PrintString out = new PrintString();
        out.print(coloredMatches.color(label.getMatch()));
        out.print(Colors.foreground(52, label.getLabel()));
        return out.toString();
    }
}
