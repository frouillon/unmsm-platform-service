package gs.unmsm.platform.modules.notificacion.model;

import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "mensaje", nullable = false, length = 255)
    private String mensaje;

    @Column(name = "leida", nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}