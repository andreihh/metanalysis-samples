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

import org.metanalysis.core.model.Project;
import org.metanalysis.core.model.ProjectEdit;
import org.metanalysis.core.model.SourceNode;
import org.metanalysis.core.model.SourceNode.SourceUnit;
import org.metanalysis.core.model.Transaction;

import static org.metanalysis.core.model.Utils.getParentId;
import static org.metanalysis.core.model.Utils.walkSourceTree;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An analyzer which reports decapsulations for fields.
 *
 * @see Decapsulation
 */
final class DecapsulationAnalyzer {
    private DecapsulationAnalyzer() {}

    /**
     * Returns a map containing the decapsulations for each field which occurred
     * in the given {@code transactions}.
     */
    static Map<String, Set<Decapsulation>> analyze(
            Iterable<Transaction> transactions) {
        DecapsulationAnalyzer analyzer = new DecapsulationAnalyzer();
        for (Transaction transaction : transactions) {
            for (ProjectEdit edit : transaction.getEdits()) {
                // Analyze the current edit.
                analyzer.visit(edit, transaction.getId());
            }

            // Update the analyzed project. Don't update for every edit, because
            // adding a field and an accessor in the same transaction is not a
            // decapsulation.
            analyzer.project.apply(transaction);
        }
        return analyzer.decapsulations;
    }

    /**
     * Returns the given {@code string} having its first character converted to
     * lowercase (if it is non-empty and the first character is a letter).
     */
    private static String decapitalize(String string) {
        return !string.isEmpty() && Character.isUpperCase(string.charAt(0))
                ? Character.toLowerCase(string.charAt(0)) + string.substring(1)
                : string;
    }

    /**
     * Returns the name of the given {@code method} by removing the content
     * starting with the first {@code (}.
     */
    private static String removeSignature(String method) {
        int where = method.indexOf('(');
        return method.substring(0, where == -1 ? method.length() : where);
    }

    /**
     * Returns the field name corresponding to the given {@code accessor}
     * signature, or {@code null} if the given string is not an accessor.
     */
    private static String getFieldForAccessor(String accessor) {
        if (accessor.length() > 3
                && (accessor.startsWith("get") || accessor.startsWith("set"))) {
            return decapitalize(removeSignature(accessor).substring(3));
        } else if (accessor.length() > 2 && accessor.startsWith("is")) {
            return decapitalize(removeSignature(accessor).substring(2));
        } else {
            return null;
        }
    }

    /**
     * Returns the qualified field id corresponding to the given {@code
     * accessor}, or {@code null} if the given function is not an accessor.
     */
    private static String getFieldIdForAccessor(
            SourceNode.SourceEntity.Function accessor) {
        // Get the id of the parent source entity.
        String parentId = getParentId(accessor);
        // Get the non-qualified signature of the accessor.
        String accessorSignature = accessor.getSignature();
        // Get the corresponding field name.
        String fieldName = getFieldForAccessor(accessorSignature);
        // If this is an actual accessor, return the parent id concatenated with
        // the field name to obtain the qualified field id.
        return fieldName != null
                ? parentId + SourceNode.ENTITY_SEPARATOR + fieldName
                : null;
    }

    /** The analyzed project. Initially, it is empty. */
    private final Project project =
            new Project(Collections.<SourceUnit>emptyList());

    /** The recorded field decapsulations. */
    private final Map<String, Set<Decapsulation>> decapsulations =
            new HashMap<>();

    /**
     * Processes the given {@code edit} from the given {@code transactionId}.
     */
    private void visit(ProjectEdit.AddNode edit, String transactionId) {
        // Get all the nodes added in this edit.
        List<SourceNode> addedNodes = walkSourceTree(edit.getNode());

        for (SourceNode node : addedNodes) {
            String id = node.getId();

            if (node instanceof SourceNode.SourceEntity.Variable) {
                // If a field was added, create a new entry in the map.
                decapsulations.put(id, new HashSet<Decapsulation>());
            } else if (node instanceof SourceNode.SourceEntity.Function) {
                SourceNode.SourceEntity.Function accessor =
                        (SourceNode.SourceEntity.Function) node;

                // If a function was added, check if it's an accessor for an
                // existing field.
                String fieldId = getFieldIdForAccessor(accessor);
                if (fieldId != null && project.find(fieldId) != null) {
                    // Add the decapsulation.
                    Decapsulation decapsulation =
                            new Decapsulation(fieldId, id, transactionId);
                    decapsulations.get(fieldId).add(decapsulation);
                }
            }
        }
    }

    /**
     * Processes the given {@code edit} from the given {@code transactionId}.
     */
    private void visit(ProjectEdit.RemoveNode edit, String transactionId) {
        // Get all the nodes removed in this edit.
        List<SourceNode> removedNodes =
                walkSourceTree(project.find(edit.getId()));

        for (SourceNode node : removedNodes) {
            String id = node.getId();

            if (node instanceof SourceNode.SourceEntity.Variable) {
                // If a field was removed, remove its entry from the map.
                decapsulations.remove(id);
            } else if (node instanceof SourceNode.SourceEntity.Function) {
                SourceNode.SourceEntity.Function accessor =
                        (SourceNode.SourceEntity.Function) node;

                // If a function was removed, check if it's an accessor for an
                // existing field.
                String fieldId = getFieldIdForAccessor(accessor);
                if (fieldId != null && project.find(fieldId) != null) {
                    // Remove the corresponding decapsulation.
                    Decapsulation decapsulation =
                            new Decapsulation(fieldId, id, transactionId);
                    decapsulations.get(fieldId).remove(decapsulation);
                }
            }
        }
    }

    /**
     * Processes the given {@code edit} from the given {@code transactionId}.
     */
    private void visit(ProjectEdit edit, String transactionId) {
        // We only care about adding and removing nodes.
        if (edit instanceof ProjectEdit.AddNode) {
            visit((ProjectEdit.AddNode) edit, transactionId);
        } else if (edit instanceof ProjectEdit.RemoveNode) {
            visit((ProjectEdit.RemoveNode) edit, transactionId);
        }
    }
}
