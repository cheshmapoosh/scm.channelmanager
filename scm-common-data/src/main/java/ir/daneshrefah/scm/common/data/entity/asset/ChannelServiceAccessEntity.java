package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.entity.gateway.CmChannelEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "CHANNEL_SERVICE_ACCESS", schema = "REF")
public class ChannelServiceAccessEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "CHANNEL_SERVICE_ACCESS_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CHANNEL_ID")
    private CmChannelEntity channel;

    @ManyToOne
    @JoinColumn(name = "EB_SERVICE_ID", nullable = false)
    private EbServiceEntity ebService;

    @Column(name = "FIXED_VALUE")
    private Integer fixedValue;

    @Column(name = "RATED_VALUE")
    private Integer ratedValue;

    @Column(name = "WITHDRAWAL_AMOUNT", precision = 15)
    private BigDecimal withdrawalAmount;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active;

}