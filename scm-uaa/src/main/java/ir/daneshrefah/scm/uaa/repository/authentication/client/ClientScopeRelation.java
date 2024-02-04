package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SUA_CLIENT_SCOPE")
public class ClientScopeRelation extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_SCOPE_ID")
    private Long id;
    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "CLIENT_ID")
    private ClientEntity client;
    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "SCOPE_ID")
    private ScopeEntity scope;

}
