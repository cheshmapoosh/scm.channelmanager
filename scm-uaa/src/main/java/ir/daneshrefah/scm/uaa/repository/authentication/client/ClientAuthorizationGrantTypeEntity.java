package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.common.data.converter.StringSetConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import ir.daneshrefah.scm.uaa.repository.converter.AuthorizationGrantTypeConverter;
import ir.daneshrefah.scm.uaa.repository.converter.ClientVersionStatusConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-13
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SUA_CLIENT_AUTHORIZATION_GRANT_TYPE")
public class ClientAuthorizationGrantTypeEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_AUTH_GRANT_TYPE_ID")
    private Long id;
    @Column(name = "GRANT_TYPE_CODE")
    @Convert(converter = AuthorizationGrantTypeConverter.class)
    private AuthorizationGrantType authorizationGrantType;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "CLIENT_ID")
    private ClientEntity client;

}
