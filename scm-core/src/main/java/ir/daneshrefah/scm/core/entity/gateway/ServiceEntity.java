package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity(name = "CM_ServiceEntity")
@Table(name = "EB_SERVICE", schema = "REF")
public class ServiceEntity extends AbstractEntity<Short> {
    @Id
    @SequenceGenerator(name = "EB_SERVICE_id_gen", sequenceName = "SQCONSTANTS", allocationSize = 1)
    @Column(name = "EB_SERVICE_ID", nullable = false)
    private Short id;

    @NotNull
    @Column(name = "SERVICE_TYPE", nullable = false)
    private Boolean financial = false;

    @NotNull
    @Column(name = "PUBLISH", nullable = false)
    private Boolean publish = false;

    @NotNull
    @Column(name = "APPLY_SEC_LVL_AUTHENTICATION", nullable = false)
    private Boolean applySecondLevelAuthentication = false;

    @NotNull
    @Column(name = "APPLY_ACCOUNT_AUTHORIZATION", nullable = false)
    private Boolean applyAccountAuthorization = false;

    @Size(max = 100)
    @Convert(disableConversion = true)
    @Column(name = "NAME", length = 100)
    private String name;

    @Size(max = 50)
    @Convert(disableConversion = true)
    @Column(name = "CODE", length = 50)
    private String code;

    @Size(max = 3)
    @Convert(disableConversion = true)
    @Column(name = "ABBREVIATION", length = 3)
    private String abbreviation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "SERVICE_CATEGORY_ID", nullable = false)
    private ServiceCategoryEntity serviceCategory;

    @OneToMany(mappedBy = "service", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOperationEntity> serviceOperations;

    @Size(max = 20)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoutingStrategy routingStrategy;

}