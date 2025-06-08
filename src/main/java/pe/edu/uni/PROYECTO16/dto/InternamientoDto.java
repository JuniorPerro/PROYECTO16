package pe.edu.uni.PROYECTO16.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InternamientoDto {
    private int idPaciente;
    private int idMedico;
    private int idCama;
    private int idPersonalRegistro; // NUEVO CAMPO
    private int idDiagnostico;
    private LocalDateTime fechaIngreso;    // Si prefieres usar LocalDateTime, también puedo ayudarte con eso
}
