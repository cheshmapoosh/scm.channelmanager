package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "MEMBERSHIP_CHANNEL_SERVICE_ACCESS")
public class MembershipTerminalServiceAccessEntity extends AbstractEntity<Integer> {

    @Id
    @Column(name = "MCSAS_ID")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "CHANNEL_EB_ACCESS_ID")
    private ChannelServiceAccessEntity channelServiceAccess;
    @ManyToOne
    @JoinColumn(name = "MCS_ID")
    private MembershipTerminalAccessEntity membershipTerminalAccess;
    private Integer archiveNo;
    private BigDecimal maxWithdrawalPerTransaction;
}
