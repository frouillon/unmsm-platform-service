package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.FieldValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Guarda en disco las evidencias adjuntas a una solicitud. */
@Component
public class EvidenciaStorage {

    public static final int MAX_ARCHIVOS = 5;
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final String CAMPO = "evidencias";

    public record ArchivoValidado(MultipartFile archivo, String nombreOriginal, TipoArchivo tipo) {
    }

    public record ArchivoGuardado(String nombreOriginal, String uri, String tipoMime, long tamanoBytes) {
    }

    public enum TipoArchivo {
        PNG("image/png", "png", new byte[]{(byte) 0x89, 'P', 'N', 'G'}),
        JPG("image/jpeg", "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        PDF("application/pdf", "pdf", new byte[]{'%', 'P', 'D', 'F'});

        final String mime;
        final String extension;
        final byte[] firma;

        TipoArchivo(String mime, String extension, byte[] firma) {
            this.mime = mime;
            this.extension = extension;
            this.firma = firma;
        }
    }

    private final Path raiz;

    public EvidenciaStorage(@Value("${app.storage.evidencias-dir:uploads/evidencias}") String directorio) {
        this.raiz = Path.of(directorio).toAbsolutePath().normalize();
    }

    /** Valida cantidad, tamaño y tipo real (por la firma del archivo, no por la extensión). */
    public List<ArchivoValidado> validar(List<MultipartFile> archivos) {
        List<MultipartFile> presentes = archivos == null ? List.of()
                : archivos.stream().filter(a -> a != null && !a.isEmpty()).toList();
        if (presentes.size() > MAX_ARCHIVOS) {
            throw new FieldValidationException(CAMPO, "Puedes adjuntar hasta %d archivos.".formatted(MAX_ARCHIVOS));
        }
        List<ArchivoValidado> validados = new ArrayList<>();
        for (MultipartFile archivo : presentes) {
            String nombre = nombreSeguro(archivo.getOriginalFilename());
            if (archivo.getSize() > MAX_BYTES) {
                throw new FieldValidationException(CAMPO, "“%s” supera los 5 MB permitidos.".formatted(nombre));
            }
            TipoArchivo tipo = detectarTipo(archivo);
            if (tipo == null) {
                throw new FieldValidationException(CAMPO, "“%s” no es PNG, JPG ni PDF.".formatted(nombre));
            }
            validados.add(new ArchivoValidado(archivo, nombre, tipo));
        }
        return validados;
    }

    /** Escribe los archivos en {raíz}/{código}/; si la transacción en curso se revierte, se eliminan. */
    public List<ArchivoGuardado> guardar(String codigo, List<ArchivoValidado> archivos) {
        if (archivos.isEmpty()) {
            return List.of();
        }
        Path carpeta = raiz.resolve(codigo);
        List<Path> escritos = new ArrayList<>();
        registrarLimpiezaSiFalla(escritos);
        try {
            Files.createDirectories(carpeta);
            List<ArchivoGuardado> guardados = new ArrayList<>();
            for (ArchivoValidado validado : archivos) {
                String nombreDisco = UUID.randomUUID() + "." + validado.tipo().extension;
                Path destino = carpeta.resolve(nombreDisco);
                try (InputStream in = validado.archivo().getInputStream()) {
                    Files.copy(in, destino);
                }
                escritos.add(destino);
                guardados.add(new ArchivoGuardado(validado.nombreOriginal(), codigo + "/" + nombreDisco,
                        validado.tipo().mime, validado.archivo().getSize()));
            }
            return guardados;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron guardar las evidencias de " + codigo, e);
        }
    }

    /** Ruta en disco de una evidencia guardada; vacía si no existe o si la URI sale de la carpeta raíz. */
    public Optional<Path> ubicar(String uri) {
        Path ruta = raiz.resolve(uri).normalize();
        return ruta.startsWith(raiz) && Files.isRegularFile(ruta) ? Optional.of(ruta) : Optional.empty();
    }

    private void registrarLimpiezaSiFalla(List<Path> escritos) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    escritos.forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                            // Un archivo huérfano no compromete la consistencia de la base de datos.
                        }
                    });
                }
            }
        });
    }

    private static TipoArchivo detectarTipo(MultipartFile archivo) {
        byte[] cabecera;
        try (InputStream in = archivo.getInputStream()) {
            cabecera = in.readNBytes(4);
        } catch (IOException e) {
            return null;
        }
        return Arrays.stream(TipoArchivo.values())
                .filter(t -> cabecera.length >= t.firma.length
                        && Arrays.equals(Arrays.copyOf(cabecera, t.firma.length), t.firma))
                .findFirst()
                .orElse(null);
    }

    private static String nombreSeguro(String original) {
        String nombre = StringUtils.getFilename(StringUtils.cleanPath(original == null ? "" : original));
        nombre = nombre == null ? "" : nombre.replaceAll("[\\p{Cntrl}\\\\/]", "").strip();
        if (nombre.isEmpty()) {
            nombre = "archivo";
        }
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }
}
