package pe.edu.uni.PROYECTO16.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.uni.PROYECTO16.dto.TrasladoDto;
import pe.edu.uni.PROYECTO16.service.TrasladoService;

@RestController
@RequestMapping("/clinica/traslado")
public class TrasladoRest {

    @Autowired
    private TrasladoService trasladoService;

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarTraslado(@RequestBody TrasladoDto dto) {
        return trasladoService.registrarTraslado(dto);
    }
}