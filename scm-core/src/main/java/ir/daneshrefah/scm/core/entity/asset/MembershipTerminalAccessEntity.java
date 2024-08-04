package ir.daneshrefah.scm.core.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
public class MembershipTerminalAccessEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "MEMBERSHIP_CHANNEL_ACCESS_ID")
    private Long id;
    private Boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANNEL_ID", referencedColumnName = "LEGACY_TERMINAL_ID")
    private TerminalEntity terminal;
    @ManyToOne
    @JoinColumn(name = "MEMBERSHIP_ID")
    private MembershipEntity membership;
    private Boolean favorite;
}
