package pe.edu.uni.PROYECTO16.dto;

import lombok.Data;

@Data
public class InternamientoConsultaDto {
    private int idInternamiento;
    private String fechaIngreso;
    private String estado;
    private Integer idPaciente;           // cambia de int a Integer
    private Integer idMedico;             // cambia de int a Integer
    private Integer idCama;               // cambia de int a Integer
    private Integer idPersonalRegistro;  // cambia de int a Integer
    private Integer idDiagnostico;        // cambia de int a Integer
}

