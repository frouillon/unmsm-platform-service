package gs.unmsm.platform.common.exception;

import lombok.Getter;

import java.util.Map;

/** Error de negocio atribuible a campos concretos del formulario (respuesta 400 con fieldErrors). */
@Getter
public class FieldValidationException extends RuntimeException {

    private final Map<String, String> fields;

    public FieldValidationException(String field, String message) {
        super(message);
        this.fields = Map.of(field, message);
    }

    public FieldValidationException(String message, Map<String, String> fields) {
        super(message);
        this.fields = Map.copyOf(fields);
    }
}
