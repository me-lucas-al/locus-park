package com.locuspark.api.service.report.export.pdf;

import org.openpdf.text.Font;
import org.openpdf.text.pdf.BaseFont;

import java.awt.Color;

public final class PdfFonts {

    public static final Color HEADER_BACKGROUND = new Color(30, 41, 59);
    public static final Color ZEBRA_BACKGROUND = new Color(241, 245, 249);
    public static final Color WHITE = Color.WHITE;

    private static final BaseFont HELVETICA = createBaseFont(BaseFont.HELVETICA);
    private static final BaseFont HELVETICA_BOLD = createBaseFont(BaseFont.HELVETICA_BOLD);

    public static final Font TITLE_FONT = new Font(HELVETICA_BOLD, 16, Font.NORMAL, Color.BLACK);
    public static final Font SECTION_TITLE_FONT = new Font(HELVETICA_BOLD, 9, Font.NORMAL, WHITE);
    public static final Font TABLE_HEADER_FONT = new Font(HELVETICA_BOLD, 8, Font.NORMAL, WHITE);
    public static final Font TABLE_CELL_FONT = new Font(HELVETICA, 8, Font.NORMAL, Color.BLACK);
    public static final Font KPI_LABEL_FONT = new Font(HELVETICA, 8, Font.NORMAL, new Color(100, 116, 139));
    public static final Font KPI_VALUE_FONT = new Font(HELVETICA_BOLD, 12, Font.NORMAL, Color.BLACK);
    public static final Font HEADER_TEXT_FONT = new Font(HELVETICA, 9, Font.NORMAL, Color.BLACK);
    public static final Font FOOTER_FONT = new Font(HELVETICA, 7, Font.NORMAL, new Color(100, 116, 139));

    private PdfFonts() {
    }

    private static BaseFont createBaseFont(String name) {
        try {
            return BaseFont.createFont(name, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao carregar fonte base-14 do PDF: " + name, ex);
        }
    }
}
