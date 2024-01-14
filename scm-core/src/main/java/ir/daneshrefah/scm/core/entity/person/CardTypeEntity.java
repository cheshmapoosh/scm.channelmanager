package ir.daneshrefah.scm.core.entity.person;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Getter
@Setter
@Entity
@Table(name = "CARD_TYPE")
public class CardTypeEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_card_type")
    @SequenceGenerator(name = "sequence_card_type", sequenceName = "REF.SQCONSTANTS")
    private Long id;
    private String code;
    private String name;
    private String productCode;
    private String owner;
    private Integer maxCardsPerUser;
    private Integer order;
//    HAS_ADDITIONAL_DATA SMALLINT not null

}
