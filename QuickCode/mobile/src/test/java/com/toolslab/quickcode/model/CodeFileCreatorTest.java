package com.toolslab.quickcode.model;


import org.junit.Test;

import static com.google.android.gms.vision.barcode.Barcode.AZTEC;
import static com.google.android.gms.vision.barcode.Barcode.PDF417;
import static com.google.android.gms.vision.barcode.Barcode.QR_CODE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class CodeFileCreatorTest {

    @Test
    public void supportedBarcodeFormatsAreCorrect() {
        assertThat(CodeFileCreator.getSupportedBarcodeFormats(), is(QR_CODE | PDF417 | AZTEC));
    }

}