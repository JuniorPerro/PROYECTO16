package pe.edu.uni.PROYECTO16.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InternamientoDto {
    private int idPaciente;
    private int idMedico;
    private int idCama;
    private int idPersonalRegistro;
    private int idDiagnostico;
    private LocalDateTime fechaIngreso = LocalDateTime.now();
}
