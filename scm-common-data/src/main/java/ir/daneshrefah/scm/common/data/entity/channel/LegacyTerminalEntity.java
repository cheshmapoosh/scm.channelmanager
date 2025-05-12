package ir.daneshrefah.scm.common.data.entity.channel;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "CHANNEL")
public class LegacyTerminalEntity extends AbstractEntity<Integer> {

    @Id
    @Column(name = "CHANNEL_ID")
    private Integer id;
    private Integer parentId;
    private String code;
    private String name;
    private BigDecimal maxPersWithdrawalPerDay;
    private BigDecimal maxWithdrawalPerDay;
    private Boolean published;
    private Boolean active;
}
