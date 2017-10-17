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
        val decapsulationSet =
                DecapsulationSet(Node("Main.java:version", "0"))
        decapsulationSet
                .addAccessor(Node("Main.java:getVersion()", "1"))
        val expectedDecapsulations =
                mapOf(decapsulationSet.field.id to decapsulationSet)

        val transactions = listOf(
                transaction("0") {
                    addSourceUnit("Main.java") {
                        variable("version") {}
                    }
                },
                transaction("1") {
                    addFunction("Main.java:getVersion()") {}
                },
                transaction("2") {
                    addFunction("Main.java:setVersion()") {}
                },
                transaction("3") {
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
