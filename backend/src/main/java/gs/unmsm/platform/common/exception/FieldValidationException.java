package gs.unmsm.platform.common.exception;

import lombok.Getter;

/** Error de negocio atribuible a un campo concreto del formulario (respuesta 400 con fieldErrors). */
@Getter
public class FieldValidationException extends RuntimeException {

    private final String field;

    public FieldValidationException(String field, String message) {
        super(message);
        this.field = field;
    }
}
