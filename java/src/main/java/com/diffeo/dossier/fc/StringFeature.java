/**
 * dossier.fc Feature Collections
 *
 * This software is released under an MIT/X11 open source license.
 * Copyright 2014 Diffeo, Inc.
 *
 */

package com.diffeo.dossier.fc;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Simple feature implementation that only contains a string.
 *
 * This is used for simple markers and values that are always
 * singletons.  For instance, an entity might have a single entity
 * type from a well-known set, and so a feature collection might
 * contain a <tt>entity_type</tt> feature that is an instance of
 * this class.
 *
 * Two string features are equal if their strings are equal.  Their
 * read-only flag is generally inherited from the containing
 * feature collection.
 */
public class StringFeature implements Feature {
    private String value;
    private boolean readOnly;
    
    public StringFeature(String v) {
        this.value = v;
        this.readOnly = false;
    }

    public StringFeature() {
        this(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public void setValue(String v) {
        if (readOnly) {
            throw new UnsupportedOperationException("read-only string value");
        }

        value = v;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean ro) {
        readOnly = ro;
    }

    /* Object methods */

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        StringFeature rhs = (StringFeature)obj;
        return new EqualsBuilder()
            .append(value, rhs.value)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(933, 241)
            .append(value)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("value", value)
            .toString();
    }
}
