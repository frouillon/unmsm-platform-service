package gs.unmsm.platform.modules.solicitud.model;

import gs.unmsm.platform.modules.trazabilidad.model.Comentario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evidencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id")
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_id")
    private Comentario comentario;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "uri", nullable = false, length = 500)
    private String uri;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "fecha_carga", nullable = false, updatable = false)
    private LocalDateTime fechaCarga;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCarga == null) {
            this.fechaCarga = LocalDateTime.now();
        }
    }
}