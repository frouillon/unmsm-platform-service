package gs.unmsm.platform.modules.usuario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "area")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}