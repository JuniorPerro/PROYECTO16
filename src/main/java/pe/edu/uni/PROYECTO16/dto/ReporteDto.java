package pe.edu.uni.PROYECTO16.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReporteDto {
    private LocalDateTime FechaInicio;
    private LocalDateTime FechaFin;
    private String nombreSegmento;
    private int ingresos;
    private int egresos;
    private double ocupacionPromedio;
    private double estanciaPromedio;

}