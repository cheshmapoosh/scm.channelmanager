package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Getter
@Setter
@Entity
@Table(name = "MEMBERSHIP_CHANNEL_ACCESS")
@SequenceGenerator(
        name = "membershipChannelAccessSeq",
        sequenceName = "SQMEMBERSHIPEBACCESS",
        allocationSize = 1,
        schema = "REF"
)
public class MembershipTerminalAccessEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "MEMBERSHIP_CHANNEL_ACCESS_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "membershipChannelAccessSeq")
    private Long id;
    private Boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANNEL_ID", referencedColumnName = "LEGACY_TERMINAL_ID")
    private TerminalEntity terminal;
    @ManyToOne
    @JoinColumn(name = "MEMBERSHIP_ID")
    private MembershipEntity membership;
    private Boolean favorite;
    private BigDecimal maxWithdrawalPerDay;
    private LocalDate fromDate;
    private LocalDate toDate;
}
