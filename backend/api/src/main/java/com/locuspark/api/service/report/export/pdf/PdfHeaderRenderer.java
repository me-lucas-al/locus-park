package com.locuspark.api.service.report.export.pdf;

import com.locuspark.api.service.report.export.document.ReportDocumentHeader;
import com.locuspark.api.service.report.export.format.DocumentFormatter;
import com.locuspark.api.service.report.export.format.ReportDateFormatter;
import com.locuspark.api.service.report.export.format.ReportLocale;
import org.openpdf.text.BadElementException;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PdfHeaderRenderer {

    public void render(Document document, ReportDocumentHeader header, byte[] logo) throws DocumentException {
        boolean hasLogo = logo != null && logo.length > 0;
        PdfPTable layout = hasLogo ? new PdfPTable(new float[]{1, 4}) : new PdfPTable(new float[]{1});
        layout.setWidthPercentage(100);
        layout.setSpacingAfter(10);

        if (hasLogo) {
            layout.addCell(logoCell(logo));
        }
        layout.addCell(infoCell(header));

        document.add(layout);
    }

    private PdfPCell logoCell(byte[] logo) {
        try {
            Image image = Image.getInstance(logo);
            image.scaleToFit(96, 32);
            PdfPCell cell = new PdfPCell(image, false);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            return cell;
        } catch (BadElementException | IOException ex) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            return cell;
        }
    }

    private PdfPCell infoCell(ReportDocumentHeader header) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.addElement(new Paragraph(header.companyName(), PdfFonts.TITLE_FONT));
        cell.addElement(new Paragraph(infoLine(header), PdfFonts.HEADER_TEXT_FONT));
        return cell;
    }

    private String infoLine(ReportDocumentHeader header) {
        return "CNPJ " + DocumentFormatter.formatCnpj(header.companyCnpj())
                + " - " + header.totalSpots() + " vagas"
                + " - Período: " + ReportDateFormatter.formatDate(header.periodFrom()) + " a " + ReportDateFormatter.formatDate(header.periodTo())
                + " - Gerado em " + ReportDateFormatter.formatDateTime(header.generatedAt()) + " (" + ReportLocale.TIME_ZONE_LABEL + ")";
    }
}
