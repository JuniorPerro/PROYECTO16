package pe.edu.uni.PROYECTO16.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.PROYECTO16.dto.CamaConsultaDto;
import pe.edu.uni.PROYECTO16.service.ConsultaCamaService;

import java.util.List;

@RestController
@RequestMapping("/clinica/camas")
public class ConsultaCamaRest {

    @Autowired
    private ConsultaCamaService consultaCamaService;

    @GetMapping("/piso/{piso}")
    public ResponseEntity<List<CamaConsultaDto>> consultarCamasPorPiso(@PathVariable int piso) {
        List<CamaConsultaDto> camas = consultaCamaService.ConsultaporPiso(piso);
        return ResponseEntity.ok(camas);
    }

    @GetMapping("/pabellon/{pabellon}")
    public ResponseEntity<List<CamaConsultaDto>> consultarCamasPorPabellon(@PathVariable String pabellon) {
        List<CamaConsultaDto> camas = consultaCamaService.ConsultaporPabellon(pabellon);
        return ResponseEntity.ok(camas);
    }
}


