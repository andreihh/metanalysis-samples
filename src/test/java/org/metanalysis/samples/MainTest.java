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

import static org.metanalysis.test.core.repository.Builders.repository;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metanalysis.core.repository.PersistentRepository;
import org.metanalysis.core.repository.Repository;

public class MainTest {
    @Before
    public void setUpRepository() throws IOException {
        final Repository mockRepository = repository(r -> r
                .transaction("1", t -> t
                        .date(123L)
                        .author("<author>")
                        .addSourceUnit("Main.java", u -> u)
                        .addSourceUnit("Test.java", u -> u))
                .transaction("2", t -> t
                        .removeNode("Test.java")));
        // Persist the mock repository to the `.metanalysis` folder in the
        // current working directory.
        PersistentRepository.persist(mockRepository, null);
    }

    @Test
    public void testRepositorySetup() throws IOException {
        Main.main(new String[] {});
    }

    @Test(expected = IOException.class)
    public void testMissingRepositoryThrows() throws IOException {
        PersistentRepository.clean();
        Main.main(new String[] {});
    }

    @After
    public void cleanRepository() throws IOException {
        // Delete the `.metanalysis` folder created in the current working
        // directory.
        PersistentRepository.clean();
    }
}
