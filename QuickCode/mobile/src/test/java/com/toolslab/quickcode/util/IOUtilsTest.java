package com.toolslab.quickcode.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IOUtilsTest {

    @Test
    public void inputStreamToString() {
        String input = "\n  Dummy string with different characters \uD83E\uDD14\n text ✈️ \n • text \n";
        String expected = input.trim();
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());

        String result = IOUtils.inputStreamToString(inputStream);

        assertThat(result, is(expected));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void inputStreamToStringWithNull() {
        InputStream input = null;
        String expected = "";

        String result = IOUtils.inputStreamToString(input);

        assertThat(result, is(expected));
    }

    @Test
    public void inputStreamToStringWithEmptyString() {
        String input = "";
        String expected = "";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());

        String result = IOUtils.inputStreamToString(inputStream);

        assertThat(result, is(expected));
    }

}
