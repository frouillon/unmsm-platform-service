package gs.unmsm.platform.common.exception;

/**
 * Regla de negocio incumplida sobre un recurso existente (p. ej. transición de estado inválida). Se responde con 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
