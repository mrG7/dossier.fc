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

public class StringCounterTest {
    @Test
    public void billClintonDocstring() {
        StringCounter name = new StringCounter();
        name.getStrings().put("Bill Clinton", 1);
        name.add("William Jefferson Clinton", 1);

        assertThat(name.getStrings(),
                   hasEntry("Bill Clinton", new Integer(1)));
        assertThat(name.getStrings(),
                   hasEntry("William Jefferson Clinton", new Integer(1)));
        assertThat(name.getStrings().size(), is(2));
    }

    @Test
    public void readOnlyAdd() {
        StringCounter name = new StringCounter();
        name.add("John Smith", 1);
        assertThat(name.getStrings(),
                   hasEntry("John Smith", new Integer(1)));

        name.add("John Smith", 1);
        assertThat(name.getStrings(),
                   hasEntry("John Smith", new Integer(2)));

        name.setReadOnly(true);

        try {
            name.add("John Smith", 1);
            assertThat("UnsupportedOperationException", is("raised"));
        } catch (UnsupportedOperationException e) {
        }
        try {
            name.add("Big JS", 1);
            assertThat("UnsupportedOperationException", is("raised"));
        } catch (UnsupportedOperationException e) {
        }

        name.setReadOnly(false);

        name.add("John Smith", 2);
        assertThat(name.getStrings(),
                   hasEntry("John Smith", new Integer(4)));
        assertThat(name.getStrings(), not(hasKey("Big JS")));
    }

    @Test
    public void serializeToJson() throws JsonProcessingException {
        StringCounter name = new StringCounter();
        name.add("John Smith", 1);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(name);
        assertThat(json, is(equalTo("{\"John Smith\":1}")));
    }

    @Test
    public void deserializeFromJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringCounter name = mapper.readValue("{\"John Smith\":2,\"JS\":1}",
                                              StringCounter.class);
        assertThat(name.getStrings(),
                   hasEntry("John Smith", new Integer(2)));
        assertThat(name.getStrings(),
                   hasEntry("JS", new Integer(1)));
        assertThat(name.getStrings().size(), is(2));
    }

    @Test
    public void serializeToCbor() throws JsonProcessingException {
        StringCounter name = new StringCounter();
        name.add("John Smith", 1);

        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] cbor = mapper.writeValueAsBytes(name);
        byte[] ref = {
            (byte)0xd9, (byte)0xd9, (byte)0xf8, // tag 55080
            (byte)0xbf, // map of ??? items
            (byte)0x6a, 0x4a, 0x6f, 0x68, 0x6e, 0x20, 0x53, 0x6d,
            0x69, 0x74, 0x68, // string "John Smith"
            (byte)0x01, // integer 1
            (byte)0xff, // end map
        };
        assertThat(cbor, is(equalTo(ref)));
    }

    @Test
    public void deserializeFromCbor() throws IOException {
        CBORFactory cborf = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(cborf);
        byte[] ref = {
            (byte)0xd9, (byte)0xd9, (byte)0xf8, // tag 55080
            (byte)0xa2, // map of 2 items
            (byte)0x61, 0x61, // string "a"
            (byte)0x01, // integer 1
            (byte)0x62, 0x62, 0x63, // string "bc"
            (byte)0x11, // integer 17
        };
        StringCounter name = mapper.readValue(ref, StringCounter.class);
        assertThat(name.isReadOnly(), is(false));
        assertThat(name.getStrings().size(), is(2));
        assertThat(name.getStrings(), hasEntry("a", new Integer(1)));
        assertThat(name.getStrings(), hasEntry("bc", new Integer(17)));
    }
}
