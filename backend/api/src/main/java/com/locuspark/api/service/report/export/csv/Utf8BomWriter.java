package com.locuspark.api.service.report.export.csv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public final class Utf8BomWriter {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private Utf8BomWriter() {
    }

    public static Writer wrap(ByteArrayOutputStream output) throws IOException {
        output.write(BOM);
        return new OutputStreamWriter(output, StandardCharsets.UTF_8);
    }
}
