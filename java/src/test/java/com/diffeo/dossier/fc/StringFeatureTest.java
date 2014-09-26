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

public class StringFeatureTest {
    @Test
    public void canChange() {
        StringFeature sf = new StringFeature("foo");
        assertThat(sf.getValue(), is("foo"));
        sf.setValue("bar");
        assertThat(sf.getValue(), is("bar"));
    }

    @Test
    public void cantChange() {
        StringFeature sf = new StringFeature("foo");
        sf.setReadOnly(true);
        assertThat(sf.getValue(), is("foo"));
        try {
            sf.setValue("bar");
            assertThat("UnsupportedOperationException", is("thrown"));
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}
