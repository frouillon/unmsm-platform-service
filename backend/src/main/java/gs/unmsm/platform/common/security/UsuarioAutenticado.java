package gs.unmsm.platform.common.security;

/**
 * Identidad extraída del JWT; es el principal de Spring Security en cada petición autenticada.
 */
public record UsuarioAutenticado(Long id, String nombres, String rol, Long areaId) {
}
