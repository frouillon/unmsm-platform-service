package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.dto.CategoriaResponse;
import gs.unmsm.platform.modules.solicitud.dto.PrioridadResponse;
import gs.unmsm.platform.modules.solicitud.repository.CategoriaRepository;
import gs.unmsm.platform.modules.solicitud.repository.PrioridadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoService {

    private final CategoriaRepository categorias;
    private final PrioridadRepository prioridades;

    public List<CategoriaResponse> categoriasActivas() {
        return categorias.findByActivoTrueOrderByNombreAsc().stream().map(CategoriaResponse::from).toList();
    }

    public List<PrioridadResponse> prioridades() {
        return prioridades.findAllByOrderBySlaDiasDesc().stream().map(PrioridadResponse::from).toList();
    }
}
