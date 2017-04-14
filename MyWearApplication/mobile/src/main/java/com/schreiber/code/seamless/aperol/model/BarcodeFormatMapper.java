package com.schreiber.code.seamless.aperol.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.schreiber.code.seamless.aperol.util.Logger;


class BarcodeFormatMapper {

    private BarcodeFormatMapper() {
        // Hide utility class constructor
    }

    @Nullable
    static BarcodeFormat getEncodingFormat(int barcodeFormat) {
        switch (barcodeFormat) {
            case Barcode.CODE_128:
                return BarcodeFormat.CODE_128;
            case Barcode.CODE_39:
                return BarcodeFormat.CODE_39;
            case Barcode.CODE_93:
                return BarcodeFormat.CODE_93;
            case Barcode.CODABAR:
                return BarcodeFormat.CODABAR;
            case Barcode.DATA_MATRIX:
                return BarcodeFormat.DATA_MATRIX;
            case Barcode.EAN_13:
                return BarcodeFormat.EAN_13;
            case Barcode.EAN_8:
                return BarcodeFormat.EAN_8;
            case Barcode.ITF:
                return BarcodeFormat.ITF;
            case Barcode.QR_CODE:
                return BarcodeFormat.QR_CODE;
            case Barcode.UPC_A:
                return BarcodeFormat.UPC_A;
            case Barcode.UPC_E:
                return BarcodeFormat.UPC_E;
            case Barcode.PDF417:
                return BarcodeFormat.PDF_417;
            case Barcode.AZTEC:
                return BarcodeFormat.AZTEC;
            default:
                Logger.logError("Unknown code format:" + barcodeFormat);
                return null;
        }
    }

    @NonNull
    static String getEncodingFormatName(int barcodeFormat) {
        switch (barcodeFormat) {
            case Barcode.CODE_128:
                return "CODE 128";
            case Barcode.CODE_39:
                return "CODE 39";
            case Barcode.CODE_93:
                return "CODE 93";
            case Barcode.CODABAR:
                return "CODABAR";
            case Barcode.DATA_MATRIX:
                return "DATA MATRIX";
            case Barcode.EAN_13:
                return "EAN 13 ";
            case Barcode.EAN_8:
                return "EAN 8";
            case Barcode.ITF:
                return "ITF";
            case Barcode.QR_CODE:
                return "QR CODE";
            case Barcode.UPC_A:
                return "UPC A";
            case Barcode.UPC_E:
                return "UPC E";
            case Barcode.PDF417:
                return "PDF 417";
            case Barcode.AZTEC:
                return "AZTEC";
            default:
                Logger.logError("Unknown code format:" + barcodeFormat);
                return "Unknown code format: " + barcodeFormat;
        }
    }

    static String getContentType(int barcodeValueFormat) {
        switch (barcodeValueFormat) {
            case Barcode.CONTACT_INFO:
                return "CONTACT_INFO";
            case Barcode.EMAIL:
                return "EMAIL";
            case Barcode.ISBN:
                return "ISBN";
            case Barcode.PHONE:
                return "PHONE";
            case Barcode.PRODUCT:
                return "PRODUCT";
            case Barcode.SMS:
                return "SMS";
            case Barcode.TEXT:
                return "TEXT";
            case Barcode.URL:
                return "URL";
            case Barcode.WIFI:
                return "WIFI";
            case Barcode.GEO:
                return "GEO";
            case Barcode.CALENDAR_EVENT:
                return "CALENDAR_EVENT";
            case Barcode.DRIVER_LICENSE:
                return "DRIVER_LICENSE";
            default:
                Logger.logError("Unknown barcodeValueFormat:" + barcodeValueFormat);
                return "Unknown barcodeValueFormat: " + barcodeValueFormat;
        }
    }

}