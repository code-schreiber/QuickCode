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

}
