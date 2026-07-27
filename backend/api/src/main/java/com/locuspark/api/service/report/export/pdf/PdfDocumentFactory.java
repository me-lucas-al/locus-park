package com.locuspark.api.service.report.export.pdf;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfPageEvent;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
public class PdfDocumentFactory {

    private static final float MARGIN = 28f;

    public Document create() {
        return new Document(PageSize.A4.rotate(), MARGIN, MARGIN, MARGIN, MARGIN);
    }

    public PdfWriter attachWriter(Document document, OutputStream output, PdfPageEvent pageEvent) throws DocumentException {
        PdfWriter writer = PdfWriter.getInstance(document, output);
        writer.setPageEvent(pageEvent);
        return writer;
    }
}
