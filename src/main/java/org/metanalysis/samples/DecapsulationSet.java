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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A decapsulation set of a field.
 *
 * Namely, for field named {@code field} added in a previous transaction, a set
 * of accessor methods (methods named {@code getField}, {@code isField} or
 * {@code setField}) which were added in a latter transaction.
 */
final class DecapsulationSet {
    /**
     * An immutable data class containing the id of a source node and the id of
     * the transaction in which the node was added to the project.
     */
    static final class Node {
        private final String id;
        private final String transactionId;

        Node(String id, String transactionId) {
            this.id = id;
            this.transactionId = transactionId;
        }

        /** Returns the id of this source node. */
        String getId() {
            return id;
        }

        /** Returns the transaction id in which this source node was added. */
        String getTransactionId() {
            return transactionId;
        }

        @Override
        public int hashCode() {
            return id.hashCode() * 37 + transactionId.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Node)) {
                return false;
            }
            Node other = (Node) o;
            return id.equals(other.id)
                    && transactionId.equals(other.transactionId);
        }

        @Override
        public String toString() {
            return id + " (" + transactionId + ")";
        }
    }

    private final Node field;
    private final Set<Node> accessors = new LinkedHashSet<>();

    DecapsulationSet(Node field) {
        this.field = field;
    }

    /** Returns the decapsulated field. */
    Node getField() {
        return field;
    }

    /** Returns an immutable view of the decapsulating accessors. */
    Set<Node> getAccessors() {
        return Collections.unmodifiableSet(accessors);
    }

    /**
     * Adds the given {@code accessor} to the decapsulation set of this field.
     */
    void addAccessor(Node accessor) {
        accessors.add(accessor);
    }

    /**
     * Removes the accessor with the given id from the decapsulation set of this
     * field.
     */
    void removeAccessor(String accessorId) {
        for (Node accessor : accessors) {
            if (accessor.id.equals(accessorId)) {
                accessors.remove(accessor);
                return;
            }
        }
    }
}
