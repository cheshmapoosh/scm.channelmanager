package ir.daneshrefah.scm.core.entity.condition;

import ir.daneshrefah.scm.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_CONDITION")
@Setter
@Getter
public class ConditionEntity extends AbstractEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CONDITION_ID")
    private String id;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "DESC")
    private String desc;
    @Column(name = "TYPE")
    private Integer type;
    @Column(name = "CURRENCY_VALUE")
    private Long currencyValue;
    @Column(name = "REATE_VALUE")
    private Boolean reateValue;

}
