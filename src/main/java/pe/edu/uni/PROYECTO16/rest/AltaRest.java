package pe.edu.uni.PROYECTO16.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.PROYECTO16.dto.AltaDto;
import pe.edu.uni.PROYECTO16.service.AltaService;

@RestController
@RequestMapping("/clinica/alta")
public class AltaRest {

    @Autowired
    private AltaService altaService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarAlta(@RequestBody AltaDto altaDto) {
        return altaService.darAltaInternamiento(altaDto);
    }
}
