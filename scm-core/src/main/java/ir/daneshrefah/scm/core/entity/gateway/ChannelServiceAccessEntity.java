package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CHANNEL_SERVICE_ACCESS", schema = "REF")
public class ChannelServiceAccessEntity extends AbstractEntity<Long> {
    @Id
    @SequenceGenerator(name = "CHANNEL_SERVICE_ACCESS_id_gen", sequenceName = "SQCONSTANTS", allocationSize = 1)
    @Column(name = "CHANNEL_SERVICE_ACCESS_ID", nullable = false, precision = 22)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "CHANNEL_ID", nullable = false)
    private ChannelEntity channel;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "EB_SERVICE_ID", nullable = false)
    private ServiceEntity service;

    @Column(name = "FIXED_VALUE")
    private Integer fixedValue;

    @Column(name = "RATED_VALUE")
    private Integer ratedValue;

    @Column(name = "WITHDRAWAL_AMOUNT")
    private Boolean withdrawalAmount;

    @NotNull
    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = false;

}