package gs.unmsm.platform.modules.trazabilidad.model;

import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_origen_id")
    private Estado estadoOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_destino_id", nullable = false)
    private Estado estadoDestino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "rol_usuario", nullable = false, length = 50)
    private String rolUsuario;

    @Column(name = "nota_tecnica", columnDefinition = "TEXT")
    private String notaTecnica;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }
}
