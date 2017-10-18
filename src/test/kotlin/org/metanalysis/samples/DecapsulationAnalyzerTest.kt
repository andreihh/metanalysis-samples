/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.samples

import org.junit.Assert.assertEquals
import org.junit.Test

import org.metanalysis.samples.DecapsulationSet.Node
import org.metanalysis.test.core.model.transaction

class DecapsulationAnalyzerTest {
    @Test
    fun testSimpleDecapsulation() {
        // Setup expected set of decapsulations.
        val decapsulationSet =
                DecapsulationSet(Node("Main.java:version", "0"))
        decapsulationSet
                .addAccessor(Node("Main.java:getVersion()", "1"))
        val expectedDecapsulations =
                mapOf(decapsulationSet.field.id to decapsulationSet)

        // Setup the list of transactions to analyze.
        val transactions = listOf(
                // `transaction` is an imported static method which receives two
                // parameters: the `id` of the transaction and a lambda (passed
                // outside of the parentheses) to configure the transaction.
                transaction("0") {
                    // We can set the date of the transaction. Implicitly, it is
                    // evaluated as `System.currentTimeMillis()`.
                    date = 0L
                    // We can also set the author of the transaction.
                    // Implicitly, it is `"<unknown-author>"`.
                    author = "<author>"
                    // We can add various edits to the transaction by calling
                    // `add*` with an `id` and configure the added node.
                    addSourceUnit("Main.java") {
                        // This adds an `AddNode` edit with a
                        // `SourceUnit("Main.java")` which also contains a
                        // `Variable("Main.java:version").
                        variable("version") {}
                    }
                },
                transaction("1") {
                    addFunction("Main.java:getVersion()") {}
                },
                transaction("2") {
                    addFunction("Main.java:setVersion()") {
                        modifiers("private", "static")
                    }
                    // We can also add an `edit*` edit.
                    editFunction("Main.java:setVersion()") {
                        // Remove the `"private"` and `"static"` modifiers and
                        // add the `"public"` modifier.
                        modifiers {
                            -"private"
                            -"static"
                            +"public"
                        }
                        // Edit the body of the function (which is currently
                        // empty).
                        body {
                            // Add the line `"{ version = 1; }"` as the first
                            // line of the method body.
                            add(index = 0, value = "{ version = 1; }")
                        }
                    }
                },
                transaction("3") {
                    // Add an edit to remove the specified node.
                    removeNode("Main.java:setVersion()")
                },
                transaction("4") {
                    addType("Main.java:Main") {
                        function("isVersion()") {}
                    }
                }
        )
        val actualDecapsulations = DecapsulationAnalyzer.analyze(transactions)

        assertEquals(expectedDecapsulations, actualDecapsulations)
    }
}
