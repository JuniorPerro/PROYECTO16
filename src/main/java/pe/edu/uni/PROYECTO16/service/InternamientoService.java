package pe.edu.uni.PROYECTO16.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.PROYECTO16.dto.InternamientoConsultaDto;
import pe.edu.uni.PROYECTO16.dto.InternamientoDto;
import pe.edu.uni.PROYECTO16.dto.RespuestaInternamientoDto;

import java.util.List;
import java.util.Objects;

@Service

public class InternamientoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Registra un internamiento de paciente si las validaciones pasan.
     * Cambia el estado de la cama si el internamiento queda "Activo".
     */
    
    @Transactional(rollbackFor = Exception.class)
    public RespuestaInternamientoDto registrarInternamiento(InternamientoDto bean) {

        // Validaciones
        ValidarPaciente(bean.getIdPaciente());
        ValidarMedico(bean.getIdMedico());
        ValidarCama(bean.getIdCama());
        ValidarInternamientoPaciente(bean.getIdPaciente());

        validarCamaPabellonMedico(bean.getIdCama(), bean.getIdMedico());

        ValidarPersonalRegistro(bean.getIdPersonalRegistro());
        ValidarDiagnosticoPaciente(bean.getIdDiagnostico(), bean.getIdPaciente());
        ValidarMedicoDiagnostico(bean.getIdMedico(), bean.getIdDiagnostico());
        ValidarExistenciaDiagnostico(bean.getIdPaciente());

        String estadoInternamiento = DeterminarEstadoInternamiento(bean.getIdCama());

        // Insertar internamiento
        jdbcTemplate.update("""
            INSERT INTO INTERNAMIENTO (FECHA_INGRESO, ESTADO, ID_PACIENTE, ID_CAMA, ID_MEDICO, ID_PERSONAL_REGISTRO, ID_DIAGNOSTICO)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, bean.getFechaIngreso(), estadoInternamiento, bean.getIdPaciente(), bean.getIdCama(), bean.getIdMedico(), bean.getIdPersonalRegistro(),bean.getIdDiagnostico());

        // Obtener ID generado de internamiento (asumiendo SQL Server, usamos SCOPE_IDENTITY)
        Integer idInternamiento = jdbcTemplate.queryForObject("SELECT CAST(SCOPE_IDENTITY() AS INT)", Integer.class);

        // Si internamiento es Activo, actualizar estado cama a Ocupada
        if ("Activo".equalsIgnoreCase(estadoInternamiento)) {
            jdbcTemplate.update("UPDATE CAMA SET ESTADO = 'Ocupada' WHERE ID_CAMA = ?", bean.getIdCama());
        }

        // Preparar respuesta
        RespuestaInternamientoDto respuesta = new RespuestaInternamientoDto();
        respuesta.setIdInternamiento(idInternamiento != null ? idInternamiento : -1);
        respuesta.setEstadoInternamiento(estadoInternamiento);
        respuesta.setMensaje(estadoInternamiento.equals("Activo")
                ? "Internamiento registrado con cama bloqueada (Estado Activo)."
                : "Internamiento registrado en estado Pendiente. La cama no está disponible.");

        return respuesta;
    }

    public List<InternamientoConsultaDto> listarInternamientos() {
        String sql = "SELECT ID_INTERNAMIENTO as idInternamiento, " +
                "CONVERT(varchar, FECHA_INGRESO, 126) as fechaIngreso, " +  // formato ISO 8601
                "ESTADO as estado, ID_PACIENTE as idPaciente, ID_MEDICO as idMedico, " +
                "ID_CAMA as idCama, ID_PERSONAL_REGISTRO as idPersonalRegistro, ID_DIAGNOSTICO as idDiagnostico " +
                "FROM INTERNAMIENTO";

        List<InternamientoConsultaDto> lista = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(InternamientoConsultaDto.class));
        return lista;
    }

    private void ValidarPaciente(int idPaciente){
        String sql = "select count(1) cont from PACIENTE where ID_PACIENTE = ?";
        int cont = jdbcTemplate.queryForObject(sql,Integer.class,idPaciente);
        if(cont==0){
            throw new RuntimeException("Paciente con id " + idPaciente + " no existe.");
        }
    }

    private void ValidarMedico(int idMedico){
        String sql = "select count(1) cont from MEDICO where ID_MEDICO = ?";
        int cont = jdbcTemplate.queryForObject(sql,Integer.class,idMedico);
        if(cont==0){
            throw new RuntimeException("Medico con id " + idMedico + " no existe.");
        }
    }

    private void ValidarCama(int idCama){
        String sql = "select count(1) cont from CAMA where ID_CAMA = ?";
        int cont = jdbcTemplate.queryForObject(sql,Integer.class,idCama);
        if(cont==0){
            throw new RuntimeException("Cama con id " + idCama + " no existe.");
        }
    }




    private void ValidarInternamientoPaciente(int idPaciente) {
        String sql = "SELECT COUNT(1) FROM INTERNAMIENTO WHERE ID_PACIENTE = ? AND ESTADO IN ('Activo', 'Pendiente')";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idPaciente);
        if (cont > 0) {
            throw new RuntimeException("El paciente ya tiene un internamiento activo o pendiente.");
        }
    }

    private void ValidarPersonalRegistro(int idPersonal) {
        String sql = "SELECT COUNT(1) FROM PERSONAL WHERE ID_PERSONAL = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idPersonal);
        if (cont == 0) {
            throw new RuntimeException("El ID del personal de registro no existe.");
        }

        sql = "SELECT ROL FROM PERSONAL WHERE ID_PERSONAL = ?";
        String rol = jdbcTemplate.queryForObject(sql, String.class, idPersonal);

        if (!Objects.equals(rol, "Enfermera")) {
            throw new RuntimeException("Solo el personal autorizado puede registrar internamientos.");
        }
    }

    private void ValidarDiagnosticoPaciente(int idDiagnostico, int idPaciente) {
        String sql = "SELECT COUNT(1) FROM DIAGNOSTICO WHERE ID_DIAGNOSTICO = ? AND ID_PACIENTE = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idDiagnostico, idPaciente);
        if (cont == 0) {
            throw new RuntimeException("El diagnóstico no pertenece al paciente indicado.");
        }
    }



    private void ValidarExistenciaDiagnostico(int idPaciente) {
        String sql = "SELECT COUNT(1) FROM DIAGNOSTICO WHERE ID_PACIENTE = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idPaciente);
        if (cont == 0) {
            throw new RuntimeException("No se encontró diagnóstico para el paciente.");
        }
    }


    private String DeterminarEstadoInternamiento(int idCama) {
        String estadoCama = jdbcTemplate.queryForObject(
                "SELECT ESTADO FROM CAMA WHERE ID_CAMA = ?", String.class, idCama);

        if ("Disponible".equalsIgnoreCase(estadoCama)) {
            return "Activo";
        } else {
            return "Pendiente";
        }
    }

    private void ValidarMedicoDiagnostico(int idMedico, int idDiagnostico) {
        String sql = "SELECT COUNT(1) FROM DIAGNOSTICO WHERE ID_DIAGNOSTICO = ? AND ID_MEDICO = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idDiagnostico, idMedico);
        if (cont == 0) {
            throw new RuntimeException("El médico no está asociado con el diagnóstico especificado.");
        }
    }

    private void validarCamaPabellonMedico(int idCama, int idMedico) {
        String sql = """
        SELECT COUNT(*) FROM CAMA c
        JOIN HABITACION h ON c.ID_HABITACION = h.ID_HABITACION
        JOIN ESPECIALIDAD_PABELLON ep ON h.PABELLON = ep.PABELLON
        JOIN MEDICO m ON m.ESPECIALIDAD = ep.ESPECIALIDAD
        WHERE c.ID_CAMA = ? AND m.ID_MEDICO = ?
    """;

        int count = jdbcTemplate.queryForObject(sql, Integer.class, idCama, idMedico);
        if (count == 0) {
            throw new RuntimeException("La cama seleccionada no corresponde al pabellón asignado a la especialidad del médico.");
        }
    }






}
