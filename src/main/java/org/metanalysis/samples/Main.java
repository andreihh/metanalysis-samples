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

package org.metanalysis.samples;

import org.metanalysis.core.repository.PersistentRepository;
import org.metanalysis.core.repository.Repository;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        // Connect to the repository. This might throw an `IOException`.
        Repository repository = PersistentRepository.load();

        // If the repository is `null`, then the project was not previously
        // persisted.
        if (repository == null) {
            System.err.println("Repository was not persisted!");
            System.exit(1);
        }

        // Analyze the decapsulations based on the repository history.
        Map<String, DecapsulationSet> decapsulations =
                DecapsulationAnalyzer.analyze(repository.getHistory());

        // Check the decapsulations for each field.
        for (DecapsulationSet decapsulationSet : decapsulations.values()) {
            Set<DecapsulationSet.Node> accessors =
                    decapsulationSet.getAccessors();
            // No need to print a field if it wasn't decapsulated.
            if (!accessors.isEmpty()) {
                System.out.println("- " + decapsulationSet.getField());
                for (DecapsulationSet.Node accessor : accessors) {
                    System.out.println("  - " + accessor);
                }
            }
        }
    }
}
