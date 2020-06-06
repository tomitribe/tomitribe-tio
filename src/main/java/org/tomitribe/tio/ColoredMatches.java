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

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.util.PrintString;

import java.util.stream.Stream;

public class ColoredMatches {
    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        final Object response = crestContext.proceed();

        if (!(response instanceof Stream)) return response;

        final Stream<Match> stream = (Stream<Match>) response;

        return stream.map(this::color);
    }

    public String color(final Match match) {
        final PrintString out = new PrintString();
        out.print(Colors.foreground(2, match.userPath()));
        out.println();

        out.print(Colors.foreground(3, match.getLine().getNumber()));
        out.print(":");
        out.println(match.getLine().getText()
                .replace(
                        match.getMatch(),
                        Colors.color(232, 3, match.getMatch())
                )
        );

        return out.toString();
    }
}
