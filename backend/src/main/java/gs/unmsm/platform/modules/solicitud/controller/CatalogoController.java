package gs.unmsm.platform.modules.solicitud.controller;

import gs.unmsm.platform.modules.solicitud.dto.CategoriaResponse;
import gs.unmsm.platform.modules.solicitud.dto.PrioridadResponse;
import gs.unmsm.platform.modules.solicitud.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() {
        return catalogoService.categoriasActivas();
    }

    @GetMapping("/prioridades")
    public List<PrioridadResponse> prioridades() {
        return catalogoService.prioridades();
    }
}
