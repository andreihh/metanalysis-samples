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

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.metanalysis.core.repository.InteractiveRepository
import org.metanalysis.core.repository.PersistentRepository
import org.metanalysis.core.repository.PersistentRepository.Companion.persist
import org.metanalysis.test.core.versioning.VcsProxyFactoryMock
import java.io.IOException

class MainTest {
    @Before
    fun setUpRepository() {
        // Setup a mock repository.
        VcsProxyFactoryMock.setRepository {
            // Add a revision to the list of revisions. The ids of the revision
            // are increasing integers starting with `0` and are automatically
            // assigned.
            revision {
                // The date and authors can be set, with implicit defaults
                // similar to the `transaction`.
                date = 123L
                author = "<author>"
                // Change (add) the `Main.java` file to the specified raw
                // string. This is in the JSON format supported by the source
                // model and will be interpreted by the mock parser provided as
                // a service in the test library.
                change("Main.java" to """{
                        "id": "Main.java",
                        "@class": "SourceUnit"
                }""")
                change("Test.java" to """{}""")
            }
            revision {
                // Remove the `Test.java` file.
                change("Test.java" to null)
            }
        }
        // Connect to the mock repository provided as a service by the test
        // library and persist it to the `.metanalysis` folder in the current
        // working directory.
        InteractiveRepository.connect()?.persist()
    }

    @Test
    fun testRepositorySetup() {
        Main.main(emptyArray())
    }

    @Test(expected = IOException::class)
    fun testMissingRepositoryThrows() {
        PersistentRepository.clean()
        Main.main(emptyArray())
    }

    @After
    fun cleanRepository() {
        // Reset the VCS setup for other tests.
        VcsProxyFactoryMock.resetRepository()
        // Delete the `.metanalysis` folder created in the current working
        // directory.
        PersistentRepository.clean()
    }
}
