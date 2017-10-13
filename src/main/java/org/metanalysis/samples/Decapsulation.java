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

/**
 * A decapsulation of a field.
 *
 * Namely, for an existing field named {@code field}, a getter (method named
 * {@code getField} or {@code isField}) or setter (method named {@code
 * setField}) was added in a transaction.
 */
final class Decapsulation {
    private final String fieldId;
    private final String accessorId;
    private final String transactionId;

    Decapsulation(
            String fieldId, String accessorId, String transactionId) {
        this.fieldId = fieldId;
        this.accessorId = accessorId;
        this.transactionId = transactionId;
    }

    /** Returns the id of the decapsulated field */
    String getFieldId() {
        return fieldId;
    }

    /** Returns the id of the decapsulating accessor. */
    String getAccessorId() {
        return accessorId;
    }

    /** Returns the id of the transaction when the decapsulation occurred. */
    String getTransactionId() {
        return transactionId;
    }

    @Override
    public int hashCode() {
        return fieldId.hashCode() * 37 * 37
                + accessorId.hashCode() * 37
                + transactionId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Decapsulation)) {
            return false;
        }
        Decapsulation other = (Decapsulation) o;
        return fieldId.equals(other.fieldId)
                && accessorId.equals(other.accessorId)
                && transactionId.equals(other.transactionId);
    }
}
