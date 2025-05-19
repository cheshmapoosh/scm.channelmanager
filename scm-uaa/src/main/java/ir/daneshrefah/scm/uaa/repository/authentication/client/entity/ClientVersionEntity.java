package ir.daneshrefah.scm.uaa.repository.authentication.client.entity;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import ir.daneshrefah.scm.uaa.repository.converter.ClientVersionStatusConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

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
public class ClientVersionEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_VERSION_ID")
    private Long id;
    private boolean isForced;
    private String signature;
    @Convert(converter = ClientVersionStatusConverter.class)
    private ClientVersionStatus status;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "CLIENT_ID")
    private ClientEntity client;

    @Column(name = "CREATOR", updatable = false)
    @CreatedBy
    private String creator;
    @Column(name = "LAST_EDITOR")
    @LastModifiedBy
    private String lastEditor;
    @Column(name = "CREATE_DATE", updatable = false)
    @CreatedDate
    private LocalDateTime createDate;
    @Column(name = "LAST_EDIT_DATE")
    @LastModifiedDate
    private LocalDateTime lastEditDate;
    @Version
    private String version;

}
