package pe.edu.uni.PROYECTO16.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.PROYECTO16.dto.InternamientoConsultaDto;
import pe.edu.uni.PROYECTO16.dto.InternamientoDto;
import pe.edu.uni.PROYECTO16.dto.RespuestaInternamientoDto;
import pe.edu.uni.PROYECTO16.service.InternamientoService;

import java.util.List;

@RestController
@RequestMapping("/clinica/internamiento")
public class InternamientoRest {

    @Autowired
    private InternamientoService internamientoService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarInternamiento(@RequestBody InternamientoDto internamientoDto) {
        try {
            RespuestaInternamientoDto resp = internamientoService.registrarInternamiento(internamientoDto);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Error inesperado
            return ResponseEntity.internalServerError().body("Error interno al registrar internamiento.");
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<List<InternamientoConsultaDto>> listarInternamientos() {
        List<InternamientoConsultaDto> lista = internamientoService.listarInternamientos();
        return ResponseEntity.ok(lista);
    }

}
