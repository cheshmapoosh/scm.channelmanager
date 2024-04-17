package ir.daneshrefah.scm.common.data.entity.terminal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CHANNEL")
public class LegacyTerminalEntity {
    @Id
    @Column(name = "CHANNEL_ID")
    private Long id;
    @Column(name = "PARENT_ID")
    private Long parentId;
    @Column(name = "ACTIVE")
    private Boolean active;
    @Column(name = "PUBLISHED")
    private Boolean published;
    @Column(name = "MAX_WITHDRAWAL_PER_DAY")
    private Long maxWithdrawalPerDay;
    @Column(name = "MAX_PERS_WITHDRAWAL_PER_DAY")
    private Long maxPersWithdrawalPerDay;
    @Column(name = "NAME")
    private String name;
    @Column(name = "CODE")
    private String code;

}
