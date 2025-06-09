package pe.edu.uni.PROYECTO16.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pe.edu.uni.PROYECTO16.dto.CamaConsultaDto;

import java.util.List;

@Service
public class ConsultaCamaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CamaConsultaDto> ConsultaporPiso(int piso) {
        String sql = """
            SELECT c.ID_CAMA AS idCama, c.ESTADO, c.ID_HABITACION AS idHabitacion
            FROM CAMA c
            JOIN HABITACION h ON c.ID_HABITACION = h.ID_HABITACION
            WHERE h.PISO = ?
        """;

        List<CamaConsultaDto> camas = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CamaConsultaDto.class), piso);

        if (camas.isEmpty()) {
            throw new RuntimeException("El piso no existe");
        }

        return camas;
    }

    public List<CamaConsultaDto> ConsultaporPabellon(String pabellon) {
        String sql = """
            SELECT c.ID_CAMA AS idCama, c.ESTADO, c.ID_HABITACION AS idHabitacion
            FROM CAMA c
            JOIN HABITACION h ON c.ID_HABITACION = h.ID_HABITACION
            WHERE h.PABELLON = ?
        """;

        List<CamaConsultaDto> camas = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CamaConsultaDto.class), pabellon);

        if (camas.isEmpty()) {
            throw new RuntimeException("El pabellón no existe");
        }

        return camas;
    }
}
