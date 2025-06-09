package pe.edu.uni.PROYECTO16.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrasladoDto {

    private String motivo;
    private int idInternamiento;
    private int idCamaOrigen;
    private int idCamaDestino;
    private int idPersonalRegistro;

}
