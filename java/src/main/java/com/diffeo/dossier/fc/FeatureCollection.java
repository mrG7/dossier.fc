package com.diffeo.dossier.fc;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A collection of named features.
 *
 * A feature collection is a simple dictionary mapping a feature name
 * to {@link Feature} objects.  Each feature can be a simple string,
 * such as {@link StringFeature}, but it more often is a set of
 * strings with associated counts, such as {@link StringCounter}, and
 * these can be transformed to vector-based features for the benefit
 * of machine-learning algorithms.
 *
 * In addition to the dictionary of features, the collection also
 * has a read-only flag.  If it is true, then {@link #getFeatures}
 * will return an unmodifiable collection, and all of the contained
 * features will also be marked read-only.  This flag is persisted
 * with the collection.
 */
public class FeatureCollection {
    private Map<String, Feature> features;
    private boolean readOnly;

    /**
     * Create a new feature collection.
     *
     * There are no features in it, and it is not read-only.
     */
    public FeatureCollection() {
        this.features = new HashMap<String, Feature>();
        this.readOnly = false;
    }

    /**
     * Get the dictionary of features.
     *
     * If the feature collection is read-only, the returned map
     * is unmodifiable.  Otherwise it can be changed freely.
     *
     * @return  Map from feature name to feature representation
     */
    public Map<String, Feature> getFeatures() {
        if (readOnly)
            return Collections.unmodifiableMap(features);
        else
            return features;
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
     * Setting this flag also sets the read-only flag on all of the
     * contained features.
     *
     * @param ro  New value of read-only flag
     */
    public void setReadOnly(boolean ro) {
        for (Feature v: features.values()) {
            v.setReadOnly(ro);
        }
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
        FeatureCollection rhs = (FeatureCollection)obj;
        return new EqualsBuilder()
            .append(readOnly, rhs.readOnly)
            .append(features, rhs.features)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(171, 85)
            .append(readOnly)
            .append(features)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("readOnly", readOnly)
            .append("features", features)
            .toString();
    }

    /* JSON/CBOR serialization */

    public static final int CBOR_STRING_COUNTER = 55800;
    public static final int CBOR_SPARSE_VECTOR = 55801;

    @JsonPropertyOrder({"v", "ro"})
    public static class Metadata {
        public static final String FC01_VERSION = "fc01";
        public String v;
        @JsonInclude(value=JsonInclude.Include.NON_NULL)
        public Integer ro;
    }

    public static class FeatureDeserializer
        extends StdDeserializer<Feature> {
        public FeatureDeserializer() {
            super(Feature.class);
        }
        
        @Override
        public Feature deserialize(JsonParser jp,
                                   DeserializationContext dctx)
            throws IOException {
            // If we are in fact coming from CBOR, then we need
            // to see if we have a tag.
            if (jp instanceof CBORParser) {
                int tag = ((CBORParser)jp).getCurrentTag();
                if (tag == CBOR_STRING_COUNTER) {
                    return dctx.readValue(jp, StringCounter.class);
                }
                if (tag != -1) {
                    throw new InvalidFormatException
                        ("unexpected CBOR tag " + tag, jp, Feature.class);
                }
            }
            // Not a CBOR parser, or no tag.  Defaults:
            JsonToken token = jp.getCurrentToken();
            if (token == JsonToken.VALUE_STRING) {
                return dctx.readValue(jp, StringFeature.class);
            }
            if (token == JsonToken.START_OBJECT) {
                return dctx.readValue(jp, StringCounter.class);
            }
            throw new InvalidFormatException
                ("unexpected object " + token, jp, Feature.class);
        }
    }

    @JsonFormat(shape=JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"metadata", "features"})
    public static class JsonRep {
        public Metadata metadata;
        @JsonDeserialize(contentUsing=FeatureDeserializer.class)
        public Map<String, Feature> features;
    }

    @JsonValue
    public JsonRep asJson() {
        JsonRep rep = new JsonRep();
        rep.metadata = new Metadata();
        rep.metadata.v = Metadata.FC01_VERSION;
        rep.metadata.ro = readOnly ? new Integer(1) : null;
        rep.features = features;
        return rep;
    }

    @JsonCreator
    public static FeatureCollection fromJson(JsonRep rep)
        throws JsonMappingException {
        if (rep.metadata == null) {
            throw new InvalidFormatException
                ("missing metadata", rep, FeatureCollection.class);
        }
        if (!(rep.metadata.v.equals(Metadata.FC01_VERSION))) {
            throw new InvalidFormatException
                ("invalid FC version " + rep.metadata.v,
                 rep, FeatureCollection.class);
        }
        if (rep.metadata.ro != null &&
            !(rep.metadata.ro.equals(new Integer(1)))) {
            throw new InvalidFormatException
                ("invalid RO flag " + rep.metadata.ro,
                 rep, FeatureCollection.class);
        }
        if (rep.features == null) {
            throw new InvalidFormatException
                ("missing content", rep, FeatureCollection.class);
        }

        FeatureCollection fc = new FeatureCollection();
        fc.features = rep.features;
        if (rep.metadata.ro != null) {
            fc.setReadOnly(true);
        }
        return fc;
    }
}
