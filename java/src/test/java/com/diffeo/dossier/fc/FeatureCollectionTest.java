package com.diffeo.dossier.fc;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class FeatureCollectionTest {
    @Test
    public void serializeEmptyFCForm() {
        FeatureCollection fc = new FeatureCollection();
        FeatureCollection.JsonRep rep = fc.asJson();
        assertThat(rep, is(notNullValue()));
        assertThat(rep.metadata, is(notNullValue()));
        assertThat(rep.metadata.v, is(equalTo("fc01")));
        assertThat(rep.metadata.ro, is(nullValue()));
        assertThat(rep.features.isEmpty(), is(true));
    }

    @Test
    public void serializeEmptyFCJson() throws JsonProcessingException {
        FeatureCollection fc = new FeatureCollection();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(fc);
        assertThat(json, is(equalTo("[{\"v\":\"fc01\"},{}]")));
    }

    @Test
    public void deserializeEmptyFCJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FeatureCollection fc = mapper.readValue
            ("[{\"v\":\"fc01\"},{}]", FeatureCollection.class);
        assertThat(fc, is(notNullValue()));
        assertThat(fc.isReadOnly(), is(false));
        assertThat(fc.getFeatures().isEmpty(), is(true));
    }

    @Test
    public void serializeEmptyROFCJson() throws JsonProcessingException {
        FeatureCollection fc = new FeatureCollection();
        fc.setReadOnly(true);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(fc);
        assertThat(json, is(equalTo("[{\"v\":\"fc01\",\"ro\":1},{}]")));
    }

    @Test
    public void deserializeEmptyROFCJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FeatureCollection fc = mapper.readValue
            ("[{\"v\":\"fc01\",\"ro\":1},{}]", FeatureCollection.class);
        assertThat(fc, is(notNullValue()));
        assertThat(fc.isReadOnly(), is(true));
        assertThat(fc.getFeatures().isEmpty(), is(true));
    }

    @Test
    public void serializeEmptyFCCbor() throws JsonProcessingException {
        FeatureCollection fc = new FeatureCollection();
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] cbor = mapper.writeValueAsBytes(fc);
        byte[] ref = {
            (byte)0x9f,  // array of ??? items
            (byte)0xbf,  // map of ??? items
            (byte)0x61, 0x76,  // string "v"
            (byte)0x64, 0x66, 0x63, 0x30, 0x31,  // string "fc01"
            (byte)0xff,  // end metadata map
            (byte)0xbf,  // map of ??? items
            (byte)0xff,  // end content map
            (byte)0xff,  // end array
        };
        assertThat(cbor, is(equalTo(ref)));
    }

    @Test
    public void deserializeEmptyFCCbor() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        // NB: This is a more compact form I expect a sensible
        // writer to spit out
        byte[] ref = {
            (byte)0x82,  // array of 2 items
            (byte)0xa1,  // map of 1 item
            (byte)0x61, 0x76,  // string "v"
            (byte)0x64, 0x66, 0x63, 0x30, 0x31,  // string "fc01"
            (byte)0xa0,  // map of 0 items
        };
        FeatureCollection fc = mapper.readValue
            (ref, FeatureCollection.class);
        assertThat(fc, is(notNullValue()));
        assertThat(fc.isReadOnly(), is(false));
        assertThat(fc.getFeatures().isEmpty(), is(true));
    }

    @Test
    public void deserializeEmptyCbor() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x80,  // array of 0 items
        };
        try {
            mapper.readValue(ref, FeatureCollection.class);
            assertThat("an empty array", is("rejected"));
        } catch (JsonMappingException e) {
            // expected case
        }
    }

    @Test
    public void deserializeEmptyMetadata() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82,  // array of 2 items
            (byte)0xa0,  // map of 0 items
            (byte)0xa0,  // map of 0 items
        };
        try {
            mapper.readValue(ref, FeatureCollection.class);
            assertThat("empty metadata", is("rejected"));
        } catch (JsonMappingException e) {
            // expected case
        }
    }

    @Test
    public void deserializeMissingVersion() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82,  // array of 2 items
            (byte)0xa1,  // map of 1 item
            (byte)0x62, 0x72, 0x6f, // string "ro"
            (byte)0x01,  // integer 1
            (byte)0xa0,  // map of 0 items
        };
        try {
            mapper.readValue(ref, FeatureCollection.class);
            assertThat("missing version", is("rejected"));
        } catch (JsonMappingException e) {
            // expected case
        }
    }

    @Test
    public void deserializeWrongVersion() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82,  // array of 2 items
            (byte)0xa1,  // map of 1 item
            (byte)0x61, 0x76,  // string "v"
            (byte)0x64, 0x46, 0x4F, 0x4F, 0x21,  // string "FOO!"
            (byte)0xa0,  // map of 0 items
        };
        try {
            mapper.readValue(ref, FeatureCollection.class);
            assertThat("incorrect version", is("rejected"));
        } catch (JsonMappingException e) {
            // expected case
        }
    }


    @Test
    public void serializeStringFeature() throws JsonProcessingException {
        StringFeature sf = new StringFeature("foo");
        FeatureCollection fc = new FeatureCollection();
        fc.getFeatures().put("f", sf);

        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] cbor = mapper.writeValueAsBytes(fc);
        byte[] ref = {
            (byte)0x9f,  // array of ??? items
            (byte)0xbf,  // map of ??? items
            (byte)0x61, 0x76,  // string "v"
            (byte)0x64, 0x66, 0x63, 0x30, 0x31,  // string "fc01"
            (byte)0xff,  // end metadata map
            (byte)0xbf,  // map of ??? items
            (byte)0x61, 0x66,  // string "f"
            (byte)0x63, 0x66, 0x6f, 0x6f,  // string "foo"
            (byte)0xff,  // end content map
            (byte)0xff,  // end array
        };
        assertThat(cbor, is(equalTo(ref)));
    }

    @Test
    public void deserializeStringFeature() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82,  // array of 2 items
            (byte)0xa1,  // map of 1 item
            (byte)0x61, 0x76,  // string "v"
            (byte)0x64, 0x66, 0x63, 0x30, 0x31,  // string "fc01"
            (byte)0xa1,  // map of 1 item
            (byte)0x61, 0x66,  // string "f"
            (byte)0x63, 0x66, 0x6f, 0x6f,  // string "foo"
        };
        FeatureCollection fc = mapper.readValue
            (ref, FeatureCollection.class);
        Map<String, Feature> features = fc.getFeatures();
        assertThat(features.size(), is(1));
        assertThat(features, hasKey("f"));
        Feature f = features.get("f");
        assertThat(f, instanceOf(StringFeature.class));
        StringFeature sf = (StringFeature)f;
        assertThat(sf.getValue(), is("foo"));
    }

    @Test
    public void serializeStringCounter() throws JsonProcessingException {
        StringCounter sc = new StringCounter();
        sc.add("a", 1);
        FeatureCollection fc = new FeatureCollection();
        fc.getFeatures().put("sc", sc);

        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] cbor = mapper.writeValueAsBytes(fc);
        byte[] ref = {
            (byte)0x9f, (byte)0xbf, 0x61, 0x76, 0x64, 0x66, 0x63,
            0x30, 0x31, (byte)0xff, (byte)0xbf,  // header
            (byte)0x62, 0x73, 0x63, // string "sc"
            (byte)0xd9, (byte)0xd9, (byte)0xf8, // tag 55080
            (byte)0xbf, // map of ??? items
            (byte)0x61, 0x61, // string "a"
            (byte)0x01, // integer 1
            (byte)0xff, // end StringCounter map
            (byte)0xff, (byte)0xff // footer
        };
        assertThat(cbor, is(equalTo(ref)));
    }

    @Test
    public void deserializeTaggedStringCounter() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82, (byte)0xa1, 0x61, 0x76, 0x64, 0x66, 0x63,
            0x30, 0x31, // header
            (byte)0xa1, // map of 1 item
            (byte)0x64, 0x66, 0x65, 0x61, 0x74, // string "feat"
            (byte)0xd9, (byte)0xd9, (byte)0xf8, // tag 55080
            (byte)0xa1, // map of 1 item
            (byte)0x61, 0x6b, // string "k"
            (byte)0x0a, // integer 10
        };
        FeatureCollection fc = mapper.readValue
            (ref, FeatureCollection.class);
        assertThat(fc.getFeatures(), hasKey("feat"));
        Feature feat = fc.getFeatures().get("feat");
        assertThat(feat, is(instanceOf(StringCounter.class)));
        StringCounter sc = (StringCounter)feat;
        assertThat(sc.getStrings(), hasEntry("k", new Integer(10)));
        assertThat(sc.getStrings().size(), is(1));
    }

    @Test
    public void deserializeUntaggedStringCounter() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82, (byte)0xa1, 0x61, 0x76, 0x64, 0x66, 0x63,
            0x30, 0x31, // header
            (byte)0xa1, // map of 1 item
            (byte)0x61, 0x66, // string "f"
            (byte)0xa2, // map of 1 item
            (byte)0x63, 0x6b, 0x65, 0x79, // string "key"
            (byte)0x0a, // integer 10
            (byte)0x65, 0x76, 0x61, 0x6c, 0x75, 0x65, // string "value"
            (byte)0x0b, // integer 11
        };
        FeatureCollection fc = mapper.readValue
            (ref, FeatureCollection.class);
        assertThat(fc.getFeatures(), hasKey("f"));
        Feature feat = fc.getFeatures().get("f");
        assertThat(feat, is(instanceOf(StringCounter.class)));
        StringCounter sc = (StringCounter)feat;
        assertThat(sc.getStrings(), hasEntry("key", new Integer(10)));
        assertThat(sc.getStrings(), hasEntry("value", new Integer(11)));
        assertThat(sc.getStrings().size(), is(2));
    }

    @Test
    public void deserializeMixedFeatures() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82, (byte)0xa1, 0x61, 0x76, 0x64, 0x66, 0x63,
            0x30, 0x31, // header
            (byte)0xa2, // map of 2 item
            (byte)0x66, 0x73, 0x74, 0x72, 0x69, 0x6e, 0x67, // "string"
            (byte)0x64, 0x6b, 0x6e, 0x6f, 0x74, // "knot"
            (byte)0x62, 0x73, 0x63, // "sc"
            (byte)0xd9, (byte)0xd9, (byte)0xf8, // tag 55080
            (byte)0xa0, // empty map
        };
        FeatureCollection fc = mapper.readValue
            (ref, FeatureCollection.class);

        assertThat(fc.getFeatures(), hasKey("string"));
        Feature string = fc.getFeatures().get("string");
        assertThat(string, is(instanceOf(StringFeature.class)));
        assertThat(((StringFeature)string).getValue(), is("knot"));

        assertThat(fc.getFeatures(), hasKey("sc"));
        Feature sc = fc.getFeatures().get("sc");
        assertThat(sc, is(instanceOf(StringCounter.class)));
        assertThat(((StringCounter)sc).getStrings().size(), is(0));
    }

    @Test
    public void deserializeROCounter() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0x82, // array of 2 items
            (byte)0xa2, // map of 2 items
            (byte)0x61, 0x76, 0x64, 0x66, 0x63, 0x30, 0x31, // "v": "fc01"
            (byte)0x62, 0x72, 0x6f, 0x01, // "ro": 1
            (byte)0xa1, // map of 1 item
            (byte)0x61, 0x63, // "c": ...
            (byte)0xd9, (byte)0xd9, (byte)0xf8, // tag 55080
            (byte)0xa1, // map of 1 item
            (byte)0x61, 0x61, 0x01, // "a": 1
        };
        FeatureCollection fc = mapper.readValue
            (ref, FeatureCollection.class);
        // If every test so far has passed, this should sail through
        StringCounter sc = (StringCounter)(fc.getFeatures().get("c"));

        assertThat(fc.isReadOnly(), is(true));
        assertThat(sc.isReadOnly(), is(true));
        try {
            sc.add("a", 1);
            assertThat("UnsupportedOperationException", is("thrown"));
        } catch(UnsupportedOperationException e) {
        }
        try {
            fc.getFeatures().put("str", new StringFeature("str"));
            assertThat("UnsupportedOperationException", is("thrown"));
        } catch(UnsupportedOperationException e) {
        }
    }
}
