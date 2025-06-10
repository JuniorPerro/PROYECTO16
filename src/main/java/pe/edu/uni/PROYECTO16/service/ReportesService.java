//package pe.edu.uni.PROYECTO16.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//import pe.edu.uni.PROYECTO16.dto.ReporteFiltroDto;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.sql.Date;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import com.itextpdf.io.font.constants.StandardFonts;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.element.Table;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.kernel.pdf.PdfDocument;
//
//@Service
//public class ReportesService {
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    public ResponseEntity<byte[]> generarReporte(ReporteFiltroDto filtro) throws IOException {
//        LocalDate fechaInicio = filtro.getFechaInicio();
//        LocalDate fechaFin = filtro.getFechaFin();
//        String segmento = filtro.getSegmento();
//        String tipoReporte = filtro.getTipoReporte();
//
//        // Validaciones estrictas con mensajes claros
//        if (fechaInicio == null) {
//            return ResponseEntity.badRequest()
//                    .body(("Error: La fecha de inicio es obligatoria.").getBytes());
//        }
//        if (fechaFin == null) {
//            return ResponseEntity.badRequest()
//                    .body(("Error: La fecha de fin es obligatoria.").getBytes());
//        }
//        if (fechaFin.isBefore(fechaInicio)) {
//            return ResponseEntity.badRequest()
//                    .body(("Error: El rango de fechas no es correcto. La fecha de fin no puede ser anterior a la fecha de inicio.").getBytes());
//        }
//        if (segmento == null || !"especialidad".equalsIgnoreCase(segmento)) {
//            return ResponseEntity.badRequest()
//                    .body(("Error: El segmento es inválido. Solo se permite 'especialidad'.").getBytes());
//        }
//        if (tipoReporte == null ||
//                !(tipoReporte.equalsIgnoreCase("PDF") || tipoReporte.equalsIgnoreCase("Excel"))) {
//            return ResponseEntity.badRequest()
//                    .body(("Error: Tipo de reporte inválido. Solo se permite 'PDF' o 'Excel'.").getBytes());
//        }
//
//        // Obtención de datos del reporte
//        List<Map<String, Object>> datosReporte = obtenerDatosReporte(fechaInicio, fechaFin);
//
//        byte[] archivo;
//        String contentType;
//        String extension;
//        if ("pdf".equalsIgnoreCase(tipoReporte)) {
//            archivo = generarPdf(datosReporte, fechaInicio, fechaFin);
//            contentType = MediaType.APPLICATION_PDF_VALUE;
//            extension = "pdf";
//        } else {
//            archivo = generarExcel(datosReporte, fechaInicio, fechaFin);
//            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//            extension = "xlsx";
//        }
//
//        String nombreArchivo = String.format("Reporte_Ocupacion_%s_%s.%s", fechaInicio, fechaFin, extension);
//        return ResponseEntity.ok()
//                .header("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"")
//                .contentType(MediaType.parseMediaType(contentType))
//                .body(archivo);
//    }
//
//    private List<Map<String, Object>> obtenerDatosReporte(LocalDate inicio, LocalDate fin) {
//        String sql = """
//            SELECT
//                m.ESPECIALIDAD AS SEGMENTO,
//                COUNT(DISTINCT CASE WHEN i.FECHA_INGRESO BETWEEN ? AND ? THEN i.ID_INTERNAMIENTO END) AS INGRESOS,
//                COUNT(DISTINCT CASE WHEN i.ESTADO = 'Finalizado' AND i.FECHA_INGRESO BETWEEN ? AND ? THEN i.ID_INTERNAMIENTO END) AS EGRESOS,
//                AVG(DATEDIFF(day, i.FECHA_INGRESO, ISNULL(a.FECHA_SALIDA, GETDATE()))) AS ESTANCIA_PROMEDIO
//            FROM INTERNAMIENTO i
//            JOIN MEDICO m ON i.ID_MEDICO = m.ID_MEDICO
//            LEFT JOIN ALTA a ON a.ID_INTERNAMIENTO = i.ID_INTERNAMIENTO
//            WHERE i.FECHA_INGRESO BETWEEN ? AND ?
//            GROUP BY m.ESPECIALIDAD
//            ORDER BY m.ESPECIALIDAD
//        """;
//
//        return jdbcTemplate.queryForList(sql, Date.valueOf(inicio), Date.valueOf(fin),
//                Date.valueOf(inicio), Date.valueOf(fin),
//                Date.valueOf(inicio), Date.valueOf(fin));
//    }
//
//    private byte[] generarExcel(List<Map<String, Object>> datos, LocalDate inicio, LocalDate fin) throws IOException {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Reporte");
//            int rowIdx = 0;
//            Row headerRow = sheet.createRow(rowIdx++);
//            String[] headers = new String[] {"Segmento", "Ingresos", "Egresos", "Estancia Promedio (días)"};
//            for (int i = 0; i < headers.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headers[i]);
//            }
//            for (Map<String, Object> row : datos) {
//                Row dataRow = sheet.createRow(rowIdx++);
//                dataRow.createCell(0).setCellValue((String) row.get("SEGMENTO"));
//                dataRow.createCell(1).setCellValue(((Number)row.get("INGRESOS")).intValue());
//                dataRow.createCell(2).setCellValue(((Number)row.get("EGRESOS")).intValue());
//                dataRow.createCell(3).setCellValue(row.get("ESTANCIA_PROMEDIO") != null ? ((Number)row.get("ESTANCIA_PROMEDIO")).doubleValue() : 0.0);
//            }
//            for (int i = 0; i < headers.length; i++) {
//                sheet.autoSizeColumn(i);
//            }
//            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//                workbook.write(bos);
//                return bos.toByteArray();
//            }
//        }
//    }
//
//    private byte[] generarPdf(List<Map<String, Object>> datos, LocalDate inicio, LocalDate fin) throws IOException {
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            PdfWriter writer = new PdfWriter(baos);
//            PdfDocument pdf = new PdfDocument(writer);
//            Document document = new Document(pdf);
//
//            document.add(new Paragraph("Reporte de Ocupación Hospitalaria")
//                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
//                    .setFontSize(18));
//            document.add(new Paragraph("Periodo: " + inicio + " a " + fin));
//            document.add(new Paragraph("Segmentado por: Especialidad"));
//
//            float[] columnWidths = {120F, 80F, 80F, 150F};
//            Table table = new Table(columnWidths);
//
//            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Segmento")));
//            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Ingresos")));
//            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Egresos")));
//            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Estancia Promedio (días)")));
//
//            for (Map<String, Object> row : datos) {
//                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph((String) row.get("SEGMENTO"))));
//                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(row.get("INGRESOS")))));
//                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(row.get("EGRESOS")))));
//                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(row.get("ESTANCIA_PROMEDIO") != null ? String.format("%.2f", ((Number) row.get("ESTANCIA_PROMEDIO")).doubleValue()) : "0.0")));
//            }
//
//            document.add(table);
//            document.close();
//
//            return baos.toByteArray();
//        }
//    }
//}
//


package pe.edu.uni.PROYECTO16.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pe.edu.uni.PROYECTO16.dto.ReporteFiltroDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;

@Service
public class ReportesService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ResponseEntity<byte[]> generarReporte(ReporteFiltroDto filtro) throws IOException {
        LocalDate fechaInicio = filtro.getFechaInicio();
        LocalDate fechaFin = filtro.getFechaFin();
        String segmento = filtro.getSegmento();
        String tipoReporte = filtro.getTipoReporte();

        // Validaciones estrictas con mensajes claros
        if (fechaInicio == null) {
            return ResponseEntity.badRequest()
                    .body(("Error: La fecha de inicio es obligatoria.").getBytes());
        }
        if (fechaFin == null) {
            return ResponseEntity.badRequest()
                    .body(("Error: La fecha de fin es obligatoria.").getBytes());
        }
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest()
                    .body(("Error: El rango de fechas no es correcto. La fecha de fin no puede ser anterior a la fecha de inicio.").getBytes());
        }
        if (segmento == null || !"especialidad".equalsIgnoreCase(segmento)) {
            return ResponseEntity.badRequest()
                    .body(("Error: El segmento es inválido. Solo se permite 'especialidad'.").getBytes());
        }
        if (tipoReporte == null ||
                !(tipoReporte.equalsIgnoreCase("PDF") || tipoReporte.equalsIgnoreCase("Excel"))) {
            return ResponseEntity.badRequest()
                    .body(("Error: Tipo de reporte inválido. Solo se permite 'PDF' o 'Excel'.").getBytes());
        }

        // Obtención de datos del reporte con ocupacion promedio diaria
        List<Map<String, Object>> datosReporte = obtenerDatosReporte(fechaInicio, fechaFin);

        byte[] archivo;
        String contentType;
        String extension;
        if ("pdf".equalsIgnoreCase(tipoReporte)) {
            archivo = generarPdf(datosReporte, fechaInicio, fechaFin);
            contentType = MediaType.APPLICATION_PDF_VALUE;
            extension = "pdf";
        } else {
            archivo = generarExcel(datosReporte, fechaInicio, fechaFin);
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            extension = "xlsx";
        }

        String nombreArchivo = String.format("Reporte_Ocupacion_%s_%s.%s", fechaInicio, fechaFin, extension);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(archivo);
    }

    private List<Map<String, Object>> obtenerDatosReporte(LocalDate inicio, LocalDate fin) {
        String sql = """
                WITH Dias AS (
                    SELECT CAST(? AS DATE) AS Dia
                    UNION ALL
                    SELECT DATEADD(DAY, 1, Dia)
                    FROM Dias
                    WHERE Dia < CAST(? AS DATE)
                ),
                InternamientosActivos AS (
                    SELECT
                        ep.ESPECIALIDAD,
                        d.Dia,
                        COUNT(DISTINCT i.ID_INTERNAMIENTO) AS CantidadCamasOcupadas
                    FROM Dias d
                    LEFT JOIN INTERNAMIENTO i ON i.FECHA_INGRESO <= d.Dia
                    LEFT JOIN ALTA a ON i.ID_INTERNAMIENTO = a.ID_INTERNAMIENTO
                    LEFT JOIN CAMA c ON i.ID_CAMA = c.ID_CAMA
                    LEFT JOIN (
                        SELECT\s
                            PABELLON,
                            ESPECIALIDAD
                        FROM ESPECIALIDAD_PABELLON
                    ) ep ON RIGHT(c.NUMERO_CAMA, 1) = ep.PABELLON
                    WHERE
                        (i.ESTADO <> 'Finalizado' OR a.FECHA_SALIDA >= d.Dia OR a.FECHA_SALIDA IS NULL)
                    GROUP BY ep.ESPECIALIDAD, d.Dia
                ),
                TotalCamasPorEspecialidad AS (
                    SELECT
                        ep.ESPECIALIDAD,
                        COUNT(c.ID_CAMA) AS TotalCamas
                    FROM CAMA c
                    LEFT JOIN ESPECIALIDAD_PABELLON ep ON RIGHT(c.NUMERO_CAMA, 1) = ep.PABELLON
                    GROUP BY ep.ESPECIALIDAD
                ),
                OcupacionDiaria AS (
                    SELECT
                        ia.ESPECIALIDAD AS SEGMENTO,
                        AVG(CAST(ia.CantidadCamasOcupadas AS FLOAT) / NULLIF(tc.TotalCamas, 0) * 100) AS OCUPACION_PROMEDIO_DIARIA
                    FROM InternamientosActivos ia
                    JOIN TotalCamasPorEspecialidad tc ON ia.ESPECIALIDAD = tc.ESPECIALIDAD
                    GROUP BY ia.ESPECIALIDAD
                )
                SELECT
                    ep.ESPECIALIDAD AS SEGMENTO,
                    COUNT(DISTINCT CASE WHEN i.FECHA_INGRESO BETWEEN ? AND ? THEN i.ID_INTERNAMIENTO END) AS INGRESOS,
                    COUNT(DISTINCT CASE WHEN i.ESTADO = 'Finalizado' AND i.FECHA_INGRESO BETWEEN ? AND ? THEN i.ID_INTERNAMIENTO END) AS EGRESOS,
                    ISNULL(o.OCUPACION_PROMEDIO_DIARIA, 0) AS OCUPACION_PROMEDIO_DIARIA,
                    AVG(DATEDIFF(day, i.FECHA_INGRESO, ISNULL(a.FECHA_SALIDA, GETDATE()))) AS ESTANCIA_PROMEDIO
                FROM INTERNAMIENTO i
                LEFT JOIN ALTA a ON a.ID_INTERNAMIENTO = i.ID_INTERNAMIENTO
                LEFT JOIN CAMA c ON i.ID_CAMA = c.ID_CAMA
                LEFT JOIN ESPECIALIDAD_PABELLON ep ON RIGHT(c.NUMERO_CAMA, 1) = ep.PABELLON
                LEFT JOIN OcupacionDiaria o ON o.SEGMENTO = ep.ESPECIALIDAD
                WHERE i.FECHA_INGRESO BETWEEN ? AND ?
                GROUP BY ep.ESPECIALIDAD, o.OCUPACION_PROMEDIO_DIARIA
                ORDER BY ep.ESPECIALIDAD
                OPTION (MAXRECURSION 0);
                
        """;

        return jdbcTemplate.queryForList(sql,
                Date.valueOf(inicio), // 1: inicio CTE
                Date.valueOf(fin),    // 2: fin CTE
                Date.valueOf(inicio), // 3: ingresos BETWEEN
                Date.valueOf(fin),    // 4: ingresos BETWEEN
                Date.valueOf(inicio), // 5: egresos BETWEEN
                Date.valueOf(fin),    // 6: egresos BETWEEN
                Date.valueOf(inicio), // 7: último BETWEEN
                Date.valueOf(fin)     // 8: último BETWEEN
        );

    }



    private byte[] generarExcel(List<Map<String, Object>> datos, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte");
            int rowIdx = 0;
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = new String[] {"Segmento", "Ingresos", "Egresos", "Ocupación Promedio Diaria (%)", "Estancia Promedio (días)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            for (Map<String, Object> row : datos) {
                Row dataRow = sheet.createRow(rowIdx++);
                dataRow.createCell(0).setCellValue((String) row.get("SEGMENTO"));
                dataRow.createCell(1).setCellValue(((Number)row.get("INGRESOS")).intValue());
                dataRow.createCell(2).setCellValue(((Number)row.get("EGRESOS")).intValue());
                double ocupPromedio = 0.0;
                if (row.get("OCUPACION_PROMEDIO_DIARIA") != null) {
                    ocupPromedio = ((Number)row.get("OCUPACION_PROMEDIO_DIARIA")).doubleValue();
                }
                // Limitar a 2 decimales
                dataRow.createCell(3).setCellValue(String.format("%.2f", ocupPromedio));

                double estanciaPromedio = 0.0;
                if (row.get("ESTANCIA_PROMEDIO") != null) {
                    estanciaPromedio = ((Number)row.get("ESTANCIA_PROMEDIO")).doubleValue();
                }
                dataRow.createCell(4).setCellValue(String.format("%.2f", estanciaPromedio));
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                return bos.toByteArray();
            }
        }
    }

    private byte[] generarPdf(List<Map<String, Object>> datos, LocalDate inicio, LocalDate fin) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Reporte de Ocupación Hospitalaria")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(18));
            document.add(new Paragraph("Periodo: " + inicio + " a " + fin));
            document.add(new Paragraph("Segmentado por: Especialidad"));
            document.add(new Paragraph("\n"));

            float[] columnWidths = {120F, 60F, 60F, 110F, 110F};
            Table table = new Table(columnWidths);

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Segmento")));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Ingresos")));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Egresos")));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Ocupación Promedio Diaria (%)")));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Estancia Promedio (días)")));

            for (Map<String, Object> row : datos) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph((String) row.get("SEGMENTO"))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(row.get("INGRESOS")))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(row.get("EGRESOS")))));
                String ocupProm = row.get("OCUPACION_PROMEDIO_DIARIA") != null
                        ? String.format("%.2f", ((Number) row.get("OCUPACION_PROMEDIO_DIARIA")).doubleValue())
                        : "0.00";
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(ocupProm)));
                String estProm = row.get("ESTANCIA_PROMEDIO") != null
                        ? String.format("%.2f", ((Number) row.get("ESTANCIA_PROMEDIO")).doubleValue())
                        : "0.00";
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(estProm)));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        }
    }
}

