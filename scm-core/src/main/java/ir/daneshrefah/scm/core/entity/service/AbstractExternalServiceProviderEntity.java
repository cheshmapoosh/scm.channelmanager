package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProviderMetadata;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.core.converter.ExternalServiceProviderMetadataConverter;
import ir.daneshrefah.scm.core.converter.ServiceProviderProtocolConverter;
import ir.daneshrefah.scm.core.entity.asset.AssetProviderEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractExternalServiceProviderEntity extends AbstractDefaultEntity<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SERVICE_PROVIDER_ID")
    private String id;
    private String code;
    private String title;
    @Column(name = "PROTOCOL", insertable = false, updatable = false)
    @Convert(converter = ServiceProviderProtocolConverter.class)
    private ServiceProviderProtocol protocol;
    private String providerClassName;
//    @Convert(converter = ExternalServiceProviderMetadataConverter.class)
//    private AbstractExternalServiceProviderMetadata metadata;
    @ManyToOne
    @JoinColumn(name = "CORE_BANKING_SYSTEM_ID")
    private AssetProviderEntity assetProvider;
//    private String customerProviderClassName;
//    @Column(name = "CUSTOMER_PROVIDE_METHOD_CODE"/*, insertable = false, updatable = false*/)
//    @Convert(converter = CustomerProvideMethodConverter.class)
//    private CustomerProvideMethod customerProvideMethod;

    public abstract AbstractExternalServiceProviderMetadata getMetadata();
    public abstract void setMetadata(AbstractExternalServiceProviderMetadata metadata);

}
