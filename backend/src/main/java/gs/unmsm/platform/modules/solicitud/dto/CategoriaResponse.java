package gs.unmsm.platform.modules.solicitud.dto;

import gs.unmsm.platform.modules.solicitud.model.Categoria;

public record CategoriaResponse(Long id, String nombre, String area, boolean requiereUbicacion) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getArea().getNombre(),
                categoria.exigeUbicacion());
    }
}
