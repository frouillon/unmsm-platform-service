package gs.unmsm.platform.modules.solicitud.dto;

import gs.unmsm.platform.modules.solicitud.model.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría disponible para registrar solicitudes.")
public record CategoriaResponse(
        @Schema(description = "Identificador de la categoría.", example = "3")
        Long id,

        @Schema(description = "Nombre de la categoría.", example = "Soporte informático")
        String nombre,

        @Schema(description = "Área responsable de atenderla.", example = "Tecnologías de la información")
        String area,

        @Schema(description = "Indica si la categoría exige pabellón, nivel y ambiente.", example = "false")
        boolean requiereUbicacion
) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getArea().getNombre(),
                categoria.exigeUbicacion());
    }
}
