package pe.edu.uni.PROYECTO16.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.PROYECTO16.dto.ReporteFiltroDto;
import pe.edu.uni.PROYECTO16.service.ReportesService;

import java.io.IOException;

@RestController
@RequestMapping("/clinica/reportes")
public class ReportesRest {

    @Autowired
    private ReportesService reportesService;

    /**
     * Genera y exporta reporte de ocupación hospitalaria, duración de estancias y altas.
     * Recibe filtros: rango fechas, segmento (servicio o especialidad) y tipoReporte (PDF o Excel).
     * Devuelve archivo para descarga.
     */
    @PostMapping(value = "/generar")
    public ResponseEntity<?> generarReporte(@RequestBody ReporteFiltroDto filtro) {
        try {
            return reportesService.generarReporte(filtro);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al generar el archivo del reporte.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en la solicitud: " + e.getMessage());
        }
    }
}
