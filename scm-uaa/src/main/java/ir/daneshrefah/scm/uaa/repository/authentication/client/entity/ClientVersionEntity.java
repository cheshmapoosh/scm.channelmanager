package ir.daneshrefah.scm.uaa.repository.authentication.client.entity;

import ir.daneshrefah.scm.common.data.entity.AbstractVersionAbleDefaultEntity;
import ir.daneshrefah.scm.uaa.repository.converter.ClientVersionStatusConverter;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
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
@Table(name = "TBL_SUA_CLIENT_VERSION")
public class ClientVersionEntity extends AbstractVersionAbleDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_VERSION_ID")
    private Long id;
    private String version;
    private boolean isForced;
    private String signature;
    @Convert(converter = ClientVersionStatusConverter.class)
    private ClientVersionStatus status;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "CLIENT_ID")
    private ClientEntity client;

}
