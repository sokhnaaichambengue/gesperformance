package sn.territoires.performances.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** Transforme un ReportData en PDF ou en fichier Excel. */
@Component
public class ReportRenderer {

    public byte[] pdf(ReportData data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD, new Color(11, 42, 74));
            Font sous = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            Font entete = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, Color.WHITE);
            Font corps = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);

            doc.add(new Paragraph(data.titre(), titre));
            Paragraph p = new Paragraph(data.sousTitre(), sous);
            p.setSpacingAfter(12);
            doc.add(p);

            PdfPTable table = new PdfPTable(data.entetes().size());
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            for (String h : data.entetes()) {
                PdfPCell c = new PdfPCell(new Phrase(h, entete));
                c.setBackgroundColor(new Color(11, 42, 74));
                c.setPadding(5);
                table.addCell(c);
            }
            for (List<String> ligne : data.lignes()) {
                for (String v : ligne) {
                    PdfPCell c = new PdfPCell(new Phrase(v == null ? "" : v, corps));
                    c.setPadding(4);
                    c.setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.addCell(c);
                }
            }
            doc.add(table);
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur de génération du PDF", e);
        }
        return out.toByteArray();
    }

    public byte[] excel(ReportData data) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Rapport");
            org.apache.poi.ss.usermodel.Font gras = wb.createFont();
            gras.setBold(true);
            CellStyle styleTitre = wb.createCellStyle();
            styleTitre.setFont(gras);
            int r = 0;
            Cell t = sheet.createRow(r++).createCell(0);
            t.setCellValue(data.titre());
            t.setCellStyle(styleTitre);
            sheet.createRow(r++).createCell(0).setCellValue(data.sousTitre());
            r++;
            Row h = sheet.createRow(r++);
            for (int i = 0; i < data.entetes().size(); i++) {
                Cell c = h.createCell(i);
                c.setCellValue(data.entetes().get(i));
                c.setCellStyle(styleTitre);
            }
            for (List<String> ligne : data.lignes()) {
                Row row = sheet.createRow(r++);
                for (int i = 0; i < ligne.size(); i++) row.createCell(i).setCellValue(ligne.get(i) == null ? "" : ligne.get(i));
            }
            for (int i = 0; i < data.entetes().size(); i++) sheet.setColumnWidth(i, 24 * 256);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erreur de génération du fichier Excel", e);
        }
    }
}
