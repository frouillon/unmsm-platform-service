package gs.unmsm.platform.modules.solicitud.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF-13 · RF-14 — Las evidencias se guardan en disco y solo se sirven las que están en su carpeta.
 * <p>
 * Técnica: conjetura de errores (ataque de «path traversal») con su caso de control.
 * El archivo secreto existe de verdad fuera de la carpeta de evidencias: si la protección
 * fallara, la prueba lo encontraría.
 */
class EvidenciaStorageTest {

    @TempDir
    Path carpetaTemporal;

    private Path carpetaEvidencias;
    private Path archivoSecreto;
    private EvidenciaStorage storage;

    @BeforeEach
    void prepararArchivos() throws IOException {
        carpetaEvidencias = carpetaTemporal.resolve("evidencias");
        Files.createDirectories(carpetaEvidencias.resolve("SOL-2026-0001"));
        Files.writeString(carpetaEvidencias.resolve("SOL-2026-0001/foto.png"), "contenido de la evidencia");

        archivoSecreto = Files.writeString(carpetaTemporal.resolve("secreto.txt"), "no debe poder descargarse");

        storage = new EvidenciaStorage(carpetaEvidencias.toString());
    }

    @Test
    void noEntregaArchivosQueEstanFueraDeLaCarpetaDeEvidencias() {
        assertThat(archivoSecreto).exists();

        assertThat(storage.ubicar("../secreto.txt")).isEmpty();
        assertThat(storage.ubicar("SOL-2026-0001/../../secreto.txt")).isEmpty();
        assertThat(storage.ubicar(archivoSecreto.toString())).isEmpty();
    }

    @Test
    void entregaUnaEvidenciaGuardadaEnSuCarpeta() {
        assertThat(storage.ubicar("SOL-2026-0001/foto.png"))
                .contains(carpetaEvidencias.resolve("SOL-2026-0001/foto.png"));
    }
}
