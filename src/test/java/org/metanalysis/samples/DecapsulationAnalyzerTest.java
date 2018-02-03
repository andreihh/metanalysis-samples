/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metanalysis.samples;

import static org.junit.Assert.assertEquals;
import static org.metanalysis.test.core.repository.Builders.repository;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.metanalysis.core.repository.Repository;
import org.metanalysis.samples.DecapsulationSet.Node;

public class DecapsulationAnalyzerTest {
    @Test
    public void testSimpleDecapsulation() {
        // Setup expected set of decapsulations.
        DecapsulationSet decapsulationSet =
                new DecapsulationSet(new Node("Main.java:version", "0"));
        decapsulationSet
                .addAccessor(new Node("Main.java:getVersion()", "1"));
        Map<String, DecapsulationSet> expectedDecapsulations = new HashMap<>();
        expectedDecapsulations
                .put(decapsulationSet.getField().getId(), decapsulationSet);

        // Setup the repository to analyze.
        Repository repository = repository(r -> r
                // We must set the `id` of the transaction and provide a lambda
                // to configure the transaction.
                .transaction("0", t -> t
                        // We can set the date of the transaction. Implicitly,
                        // it is evaluated as `System.currentTimeMillis()`.
                        .date(0L)
                        // We can also set the author of the transaction.
                        // Implicitly, it is `"<unknown-author>"`.
                        .author("<author>")
                        // We can add various edits to the transaction by
                        // calling `add*` with an `id` and a lambda to configure
                        // the added node.
                        .addSourceUnit("Main.java", u -> u
                                // This adds an `AddNode` edit with a
                                // `SourceUnit("Main.java")` which also contains
                                // a `Variable("Main.java:version").
                                .variable("version", v -> v)))
                // We can add multiple transactions to define the repository.
                .transaction("1", t -> t
                        .addFunction("Main.java:getVersion()", f -> f))
                .transaction("2", t -> t
                        .addFunction("Main.java:setVersion()", f -> f
                                .modifiers("private", "static"))
                        // We can also add an `edit*` edit.
                        .editFunction("Main.java:setVersion()", e -> e
                                // Remove the `private` and `static` modifiers
                                // and add the `public` modifier.
                                .modifiers(m -> m
                                        .remove("private")
                                        .remove("static")
                                        .add("public"))
                                // Edit the body of the function (which is
                                // currently empty).
                                .body(b -> b
                                        // Add the line `"{ version = 1; }"` as
                                        // the first line of the method body.
                                        .add(0, "{ version = 1; }"))))
                .transaction("3", t -> t
                        // Add an edit to remove the specified node.
                        .removeNode("Main.java:setVersion()"))
                .transaction("4", t -> t
                        .addType("Main.java:Main", t2 -> t2
                                .function("isVersion()", f -> f))));

        Map<String, DecapsulationSet> actualDecapsulations =
                DecapsulationAnalyzer.analyze(repository.getHistory());

        assertEquals(expectedDecapsulations, actualDecapsulations);
    }
}
