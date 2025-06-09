package pe.edu.uni.PROYECTO16.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.PROYECTO16.dto.TrasladoDto;

@Service
public class TrasladoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> registrarTraslado(TrasladoDto dto) {
        try {
            // Validaciones
            validarInternamiento(dto.getIdInternamiento());
            validarCamaOrigen(dto.getIdCamaOrigen(), dto.getIdInternamiento());
            validarCamaDestino(dto.getIdCamaDestino(), dto.getIdCamaOrigen());
            validarPersonalRegistro(dto.getIdPersonalRegistro());

            // Registrar traslado con fecha actual
            jdbcTemplate.update(
                    "INSERT INTO TRASLADO (FECHA_TRASLADO, MOTIVO, ID_INTERNAMIENTO, ID_CAMA_ORIGEN, ID_CAMA_DESTINO, ID_PERSONAL_REGISTRO) " +
                            "VALUES (GETDATE(), ?, ?, ?, ?, ?)",
                    dto.getMotivo(), dto.getIdInternamiento(),
                    dto.getIdCamaOrigen(), dto.getIdCamaDestino(), dto.getIdPersonalRegistro()
            );

            // Actualizar estado de camas
            jdbcTemplate.update("UPDATE CAMA SET ESTADO = 'Disponible' WHERE ID_CAMA = ?", dto.getIdCamaOrigen());
            jdbcTemplate.update("UPDATE CAMA SET ESTADO = 'Ocupada' WHERE ID_CAMA = ?", dto.getIdCamaDestino());

            // Actualizar cama en internamiento
            jdbcTemplate.update(
                    "UPDATE INTERNAMIENTO SET ID_CAMA = ? WHERE ID_INTERNAMIENTO = ?",
                    dto.getIdCamaDestino(), dto.getIdInternamiento()
            );

            return ResponseEntity.ok("Traslado registrado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private void validarInternamiento(Integer idInternamiento) {
        // Primero verificar si existe
        String sqlExiste = "SELECT COUNT(*) FROM INTERNAMIENTO WHERE ID_INTERNAMIENTO = ?";
        int count = jdbcTemplate.queryForObject(sqlExiste, Integer.class, idInternamiento);

        if (count == 0) {
            throw new RuntimeException("No existe internamiento con ID: " + idInternamiento);
        }

        // Luego verificar el estado
        String sqlEstado = "SELECT ESTADO FROM INTERNAMIENTO WHERE ID_INTERNAMIENTO = ?";
        String estado = jdbcTemplate.queryForObject(sqlEstado, String.class, idInternamiento);

        if (!"Activo".equalsIgnoreCase(estado)) {
            throw new RuntimeException("Internamiento Finalizado con ID: " + idInternamiento);
        }
    }

    private void validarCamaOrigen(Integer idCamaOrigen, Integer idInternamiento) {
        // Verificar existencia
        String sqlExistencia = "SELECT COUNT(*) FROM CAMA WHERE ID_CAMA = ?";
        int count = jdbcTemplate.queryForObject(sqlExistencia, Integer.class, idCamaOrigen);
        if (count == 0) {
            throw new RuntimeException("La cama origen no existe");
        }

        // Verificar que esté ocupada
        String sqlEstado = "SELECT ESTADO FROM CAMA WHERE ID_CAMA = ?";
        String estado = jdbcTemplate.queryForObject(sqlEstado, String.class, idCamaOrigen);
        if (!"OCUPADA".equalsIgnoreCase(estado)) {
            throw new RuntimeException("La cama origen debe estar ocupada");
        }

        // Verificar que pertenece al internamiento
        String sqlInternamiento = "SELECT COUNT(*) FROM INTERNAMIENTO WHERE ID_INTERNAMIENTO = ? AND ID_CAMA = ?";
        count = jdbcTemplate.queryForObject(sqlInternamiento, Integer.class, idInternamiento, idCamaOrigen);
        if (count == 0) {
            throw new RuntimeException("La cama origen no pertenece al internamiento especificado");
        }
    }

    private void validarCamaDestino(Integer idCamaDestino, Integer idCamaOrigen) {
        // Verificar existencia
        String sqlExistencia = "SELECT COUNT(*) FROM CAMA WHERE ID_CAMA = ?";
        int count = jdbcTemplate.queryForObject(sqlExistencia, Integer.class, idCamaDestino);
        if (count == 0) {
            throw new RuntimeException("La cama destino no existe");
        }

        // Verificar que esté disponible
        String sqlEstado = "SELECT ESTADO FROM CAMA WHERE ID_CAMA = ?";
        String estado = jdbcTemplate.queryForObject(sqlEstado, String.class, idCamaDestino);
        if (!"DISPONIBLE".equalsIgnoreCase(estado)) {
            throw new RuntimeException("La cama destino no está disponible");
        }

        // Verificar mismo pabellón
        String sqlPabellon = """
        SELECT CASE 
                 WHEN H1.PABELLON = H2.PABELLON THEN 1 
                 ELSE 0 
               END AS MISMO_PABELLON
        FROM CAMA C1
        JOIN HABITACION H1 ON C1.ID_HABITACION = H1.ID_HABITACION
        JOIN CAMA C2 ON C2.ID_CAMA = ?
        JOIN HABITACION H2 ON C2.ID_HABITACION = H2.ID_HABITACION
        WHERE C1.ID_CAMA = ?
        """;
        Integer mismoPabellon = jdbcTemplate.queryForObject(sqlPabellon, Integer.class, idCamaDestino, idCamaOrigen);
        if (mismoPabellon == null || mismoPabellon != 1) {
            throw new RuntimeException("La cama destino no está en el mismo pabellón que la cama origen");
        }
    }

    private void validarPersonalRegistro(Integer idPersonal) {
        String sql = "SELECT ROL FROM PERSONAL WHERE ID_PERSONAL = ?";

        String rol;
        try {
            rol = jdbcTemplate.queryForObject(sql, String.class, idPersonal);
        } catch (Exception e) {
            throw new RuntimeException("Personal con ID " + idPersonal + " no existe");
        }

        if (!"Enfermeria".equalsIgnoreCase(rol)) {
            throw new RuntimeException("Solo el personal de Enfermeria puede registrar traslados");
        }
    }
}