package gs.unmsm.platform.common.config;

import gs.unmsm.platform.common.exception.ApiError;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuración de la documentación OpenAPI que genera springdoc.
 * La especificación resultante se expone en /v3/api-docs (JSON) y /v3/api-docs.yaml,
 * y se visualiza en /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI api(@Value("${app.public-url:http://localhost:8080}") String publicUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("San Marcos Servicios API")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión de solicitudes de servicios de la UNMSM.

                                **Autenticación:** obtén el token en `POST /api/auth/login` y envíalo en cada
                                petición como `Authorization: Bearer <token>`.

                                **Roles:** ESTUDIANTE, TECNICO, SUPERVISOR y ADMINISTRADOR. Cada endpoint indica
                                el rol permitido en su descripción.
                                """)
                        .contact(new Contact().name("Equipo San Marcos Servicios"))
                        .license(new License().name("Uso académico")))
                .servers(List.of(new Server().url(publicUrl).description("Servidor de la API")))
                .tags(List.of(
                        new Tag().name("Autenticación")
                                .description("Inicio de sesión y datos del usuario autenticado."),
                        new Tag().name("Catálogos")
                                .description("Categorías y prioridades disponibles para registrar solicitudes."),
                        new Tag().name("Solicitudes")
                                .description("Registro, consulta, evidencias y flujo de estados de las solicitudes.")))
                .components(componentes())
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }

    private Components componentes() {
        Components componentes = new Components()
                .addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .name(ESQUEMA_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en POST /api/auth/login"))
                .addResponses("BadRequest", error("Datos inválidos o formato incorrecto."))
                .addResponses("Unauthorized", error("Token ausente, inválido o expirado."))
                .addResponses("Forbidden", error("El usuario no tiene permisos para esta operación."))
                .addResponses("NotFound", error("El recurso solicitado no existe."))
                .addResponses("Conflict", error("La operación no está permitida por las reglas de negocio."))
                .addResponses("PayloadTooLarge", error("El archivo supera el tamaño permitido."))
                .addResponses("InternalServerError", error("Error inesperado del servidor."));

        Map<String, Schema> esquemas = ModelConverters.getInstance().readAll(ApiError.class);
        esquemas.forEach(componentes::addSchemas);
        return componentes;
    }

    private io.swagger.v3.oas.models.responses.ApiResponse error(String descripcion) {
        return new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(descripcion)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiError"))));
    }
}
