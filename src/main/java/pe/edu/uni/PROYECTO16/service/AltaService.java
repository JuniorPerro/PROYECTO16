package pe.edu.uni.PROYECTO16.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.PROYECTO16.dto.AltaDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class AltaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> darAltaInternamiento(AltaDto altaDto) {
        try {
            Map<String, Object> internamiento = validarExistenciaInternamiento(altaDto.getIdInternamiento());
            validarEstadoActivo(internamiento, altaDto.getIdInternamiento());
            validarPersonalAutorizado(altaDto.getIdPersonalRegistro());

            actualizarEstadoInternamiento(altaDto.getIdInternamiento());
            registrarAlta(altaDto);
            liberarCama((Integer) internamiento.get("ID_CAMA"));

            return ResponseEntity.ok("El internamiento con ID " + altaDto.getIdInternamiento() + " se ha dado de alta correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al dar de alta: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno al procesar la alta.");
        }
    }

    private Map<String, Object> validarExistenciaInternamiento(int idInternamiento) {
        String sql = "SELECT ESTADO, ID_CAMA FROM INTERNAMIENTO WHERE ID_INTERNAMIENTO = ?";
        Map<String, Object> result;
        try {
            result = jdbcTemplate.queryForMap(sql, idInternamiento);
        } catch (Exception e) {
            throw new RuntimeException("El internamiento con ID " + idInternamiento + " no existe.");
        }
        return result;
    }

    private void validarEstadoActivo(Map<String, Object> internamiento, int idInternamiento) {
        String estadoActual = (String) internamiento.get("ESTADO");
        if ("Finalizado".equalsIgnoreCase(estadoActual )) {
            throw new RuntimeException("El internamiento con ID " + idInternamiento + " se encuentra actualmente finalizado.");
        }
    }

    private void validarPersonalAutorizado(String idPersonalRegistro) {
        String sql = "SELECT COUNT(*) FROM PERSONAL WHERE ID_PERSONAL = ? AND ROL = 'Admision Hospitalaria'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idPersonalRegistro);
        if (count == null || count == 0) {
            throw new RuntimeException("El personal con ID " + idPersonalRegistro + " no esta autorizado para dar altas.");
        }
    }

    private void actualizarEstadoInternamiento(int idInternamiento) {
        String updateInternamiento = "UPDATE INTERNAMIENTO SET ESTADO = 'Finalizado' WHERE ID_INTERNAMIENTO = ?";
        jdbcTemplate.update(updateInternamiento, idInternamiento);
    }

    private void registrarAlta(AltaDto altaDto) {
        String insertAlta = "INSERT INTO ALTA (ID_INTERNAMIENTO, MOTIVO_EGRESO, ID_PERSONAL_REGISTRO, FECHA_SALIDA) " +
                "VALUES (?, ?, ?, ?)";
        String fechaSalida = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        jdbcTemplate.update(insertAlta,
                altaDto.getIdInternamiento(),
                altaDto.getMotivoEgreso(),
                altaDto.getIdPersonalRegistro(),
                fechaSalida);
    }

    private void liberarCama(Integer idCama) {
        String updateCama = "UPDATE CAMA SET ESTADO = 'Disponible' WHERE ID_CAMA = ?";
        jdbcTemplate.update(updateCama, idCama);
    }
}
