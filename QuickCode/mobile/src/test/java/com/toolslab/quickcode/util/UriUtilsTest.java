package com.toolslab.quickcode.util;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UriUtilsTest {

    @Mock
    private ContentResolver mockContentResolver;

    @Mock
    private Uri mockUri;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void isSupportedImportFilePdf() {
        when(mockContentResolver.getType(mockUri)).thenReturn(UriUtils.TYPE_ABSOLUTE_APPLICATION_PDF);

        boolean result = UriUtils.isSupportedImportFile(mockContentResolver, mockUri);

        assertThat(result, is(true));
    }

    @Test
    public void isSupportedImportFileText() {
        when(mockContentResolver.getType(mockUri)).thenReturn(UriUtils.TYPE_ABSOLUTE_TEXT_PLAIN);

        boolean result = UriUtils.isSupportedImportFile(mockContentResolver, mockUri);

        assertThat(result, is(true));
    }

    @Test
    public void isSupportedImportFileImage() {
        when(mockContentResolver.getType(mockUri)).thenReturn(UriUtils.TYPE_RELATIVE_IMAGE + "png");

        boolean result = UriUtils.isSupportedImportFile(mockContentResolver, mockUri);

        assertThat(result, is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void isSupportedImportFileForNull() {
        Uri uri = null;

        boolean result = UriUtils.isSupportedImportFile(mockContentResolver, uri);

        assertThat(result, is(false));
    }

    @Test
    public void isSupportedImportFileForInvalid() {
        when(mockContentResolver.getType(mockUri)).thenReturn("invalid");

        boolean result = UriUtils.isSupportedImportFile(mockContentResolver, mockUri);

        assertThat(result, is(false));
    }

    @Ignore("Ignore until MimeTypeMap is injected to UriUtils")
    @Test
    public void describeFileType() {
        when(mockContentResolver.getType(mockUri)).thenReturn("invalid");
        String expected = "test should fail first";

        String result = UriUtils.describeFileType(mockContentResolver, mockUri);

        assertThat(result, is(expected));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void describeFileTypeForNull() {
        Uri uri = null;

        String result = UriUtils.describeFileType(mockContentResolver, uri);

        assertThat(result, is(nullValue()));
    }

}
