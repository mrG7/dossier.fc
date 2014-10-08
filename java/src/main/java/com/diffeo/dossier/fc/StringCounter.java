package com.diffeo.dossier.fc;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * String multi-set feature.
 *
 * This class is a {@link Feature} implementation that can be stored
 * in a {@link FeatureCollection}, where the feature potentially has
 * multiple values, with counts associated with each.  The count is
 * typically a word frequency or other similar counter; if all strings
 * are equally valid, the counts can all be 1.
 *
 * <pre>
 * FeatureCollection fc = new FeatureCollection();
 * StringCounter name = new StringCounter();
 * // Both of the following set their feature counts to 1
 * name.getStrings().put("Bill Clinton", 1);
 * name.add("William Jefferson Clinton", 1);
 * fc.getFeatures().put("name", name);
 * </pre>
 */
@JsonSerialize(using=StringCounter.Serializer.class)
public class StringCounter implements Feature {
    private Map<String, Integer> strings;
    private boolean readOnly;

    public StringCounter() {
        strings = new HashMap<String, Integer>();
        readOnly = false;
    }

    /**
     * Get the dictionary of strings and counts.
     *
     * If the feature is read-only, the returned map is unmodifiable.
     * Otherwise it can be changed freely.
     *
     * @return  Map from string name to integer count
     */
    @JsonValue
    public Map<String, Integer> getStrings() {
        if (readOnly) {
            return Collections.unmodifiableMap(strings);
        } else {
            return strings;
        }
    }

    /**
     * Completely replace the dictionary of strings and counts.
     *
     * @param s  New map from string to count
     */
    public void setStrings(Map<String, Integer> s) {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }
        strings = s;
    }

    /**
     * Add some value to the count for a key.
     *
     * If the key is not already contained in the collection, add
     * it as though its count was previously 0.  This can raise an
     * exception of the collection is read-only.
     *
     * @param key  String key to update
     * @param n    Amount to add to the count
     */
    public void add(String key, int n) {
        Map<String, Integer> s = getStrings();
        int count = 0;
        Integer oldCount = s.get(key);
        if (oldCount != null) {
            count = oldCount.intValue();
        }
        count += n;
        s.put(key, new Integer(count));
    }

    /**
     * Get the read-only flag.
     *
     * @return  Current value of read-only flag
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Set the read-only flag.
     *
     * @param ro  New value of read-only flag
     */
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
        StringCounter rhs = (StringCounter)obj;
        return new EqualsBuilder()
            .append(strings, rhs.strings)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(43, 79)
            .append(strings)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("strings", strings)
            .toString();
    }

    /* JSON/CBOR serialization */

    public static class Serializer extends StdSerializer<StringCounter> {
        public Serializer() {
            super(StringCounter.class);
        }

        @Override
        public void serialize(StringCounter value, JsonGenerator jgen,
                              SerializerProvider provider)
            throws IOException {
            if (jgen instanceof CBORGenerator) {
                ((CBORGenerator)jgen).writeTag
                    (FeatureCollection.CBOR_STRING_COUNTER);
            }
            provider.defaultSerializeValue(value.getStrings(), jgen);
        }
    }

    @JsonCreator
    public static StringCounter fromJson(Map<String, Integer> s) {
        StringCounter sc = new StringCounter();
        sc.setStrings(s);
        return sc;
    }
}
