package com.toolslab.quickcode.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeUtilsTest {

    @Test
    public void createCommaSeparatedStringFromList() {
        List<String> input = new ArrayList<>();
        input.add("1");
        input.add("2");
        input.add("3");
        String expectedResult = "1, 2, 3";

        String result = TypeUtils.createCommaSeparatedStringFromList(input);

        assertThat(result, is(expectedResult));
    }

    @Test
    public void createCommaSeparatedStringFromSingletonList() {
        List<String> input = new ArrayList<>();
        input.add("1");
        String expectedResult = "test should fail";

        String result = TypeUtils.createCommaSeparatedStringFromList(input);

        assertThat(result, is(expectedResult));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void createCommaSeparatedStringFromNull() {
        List<String> input = null;
        String expectedResult = "";

        String result = TypeUtils.createCommaSeparatedStringFromList(input);

        assertThat(result, is(expectedResult));
    }

    @Test
    public void createCommaSeparatedStringFromEmptyList() {
        List<String> input = new ArrayList<>();
        String expectedResult = "";

        String result = TypeUtils.createCommaSeparatedStringFromList(input);

        assertThat(result, is(expectedResult));
    }

    @Test
    public void isValid() {
        String input = "a valid string";

        boolean result = TypeUtils.isValid(input);

        assertThat(result, is(true));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void nullIsInvalid() {
        String input = null;

        boolean result = TypeUtils.isValid(input);

        assertThat(result, is(false));
    }

    @Test
    public void emptyStringIsInvalid() {
        String input = "";

        boolean result = TypeUtils.isValid(input);

        assertThat(result, is(false));
    }

    @Test
    public void nullStringIsInvalid() {
        String input = "null";

        boolean result = TypeUtils.isValid(input);

        assertThat(result, is(false));
    }

    @Test
    public void isNotEmpty() {
        String input = "I'm not an empty string";

        boolean result = TypeUtils.isEmpty(input);

        assertThat(result, is(false));
    }

    @Test
    public void isEmpty() {
        String input = "";

        boolean result = TypeUtils.isEmpty(input);

        assertThat(result, is(true));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void isEmptyWhenNull() {
        String input = null;

        boolean result = TypeUtils.isEmpty(input);

        assertThat(result, is(true));
    }

}
