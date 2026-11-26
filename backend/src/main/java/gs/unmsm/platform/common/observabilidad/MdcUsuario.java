package gs.unmsm.platform.common.observabilidad;

/** Claves del MDC con la identidad del usuario autenticado (solo id y rol, nunca nombre ni correo). */
public final class MdcUsuario {

    public static final String USUARIO_ID = "usuarioId";
    public static final String ROL = "rol";

    private MdcUsuario() {
    }
}
