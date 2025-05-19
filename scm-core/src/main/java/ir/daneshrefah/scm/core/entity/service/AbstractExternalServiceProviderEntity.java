package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.data.entity.AbstractStringAuditableEntity;
import ir.daneshrefah.scm.common.data.entity.asset.AssetProviderEntity;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import ir.daneshrefah.scm.core.converter.ServiceProviderProtocolConverter;
import ir.daneshrefah.scm.core.converter.ServiceProviderStatusConverter;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_SERVICE_PROVIDER")
@DiscriminatorColumn(name = "PROTOCOL",discriminatorType =  DiscriminatorType.INTEGER)
public abstract class AbstractExternalServiceProviderEntity extends AbstractStringAuditableEntity<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SERVICE_PROVIDER_ID")
    private String id;
    private String code;
    private String title;
    @Convert(converter = ServiceProviderStatusConverter.class)
    private ServiceProviderStatus status;
    @Column(name = "PROTOCOL", insertable = false, updatable = false)
    @Convert(converter = ServiceProviderProtocolConverter.class)
    private ServiceProviderProtocol protocol;
    private String providerClassName;
    @ManyToOne
    @JoinColumn(name = "CORE_BANKING_SYSTEM_ID")
    private AssetProviderEntity assetProvider;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "SRV_PROVIDER_ID")
    private List<ResponseEntity> responseConditions;
    @OneToMany(fetch = FetchType.EAGER,orphanRemoval = true)
    @JoinTable(name = "TBL_SCM_PARAMETER_SERVICE_PROVIDER_RELATION"
            , joinColumns = @JoinColumn(name = "SERVICE_PROVIDER_ID")
            , inverseJoinColumns = @JoinColumn(name = "PARAMETER_ID"))
    private List<ParameterEntity> parameters;

}
