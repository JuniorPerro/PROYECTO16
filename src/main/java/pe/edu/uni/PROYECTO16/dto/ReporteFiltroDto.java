package pe.edu.uni.PROYECTO16.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReporteFiltroDto {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String segmento; // "servicio" o "especialidad"
    private String tipoReporte; // "PDF" o "Excel"
}
