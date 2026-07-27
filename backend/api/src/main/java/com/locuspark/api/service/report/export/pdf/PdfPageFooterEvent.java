package com.locuspark.api.service.report.export.pdf;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;

public class PdfPageFooterEvent extends PdfPageEventHelper {

    private final String generatedAtLabel;
    private PdfTemplate totalPagesTemplate;

    public PdfPageFooterEvent(String generatedAtLabel) {
        this.generatedAtLabel = generatedAtLabel;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        totalPagesTemplate = writer.getDirectContent().createTemplate(30, 12);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte content = writer.getDirectContent();
        float y = document.bottom() - 20;

        content.setFontAndSize(PdfFonts.FOOTER_FONT.getCalculatedBaseFont(false), 7);
        content.showTextAligned(Element.ALIGN_LEFT, "Página " + writer.getPageNumber() + " de", document.left(), y, 0);
        content.showTextAligned(Element.ALIGN_RIGHT, generatedAtLabel, document.right(), y, 0);

        content.addTemplate(totalPagesTemplate, document.left() + 62, y);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        totalPagesTemplate.beginText();
        totalPagesTemplate.setFontAndSize(PdfFonts.FOOTER_FONT.getCalculatedBaseFont(false), 7);
        totalPagesTemplate.setTextMatrix(0, 0);
        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber()));
        totalPagesTemplate.endText();
    }
}
