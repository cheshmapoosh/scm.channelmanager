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
@Table(name = "CARD")
public class CardEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "CARD_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_card")
    @SequenceGenerator(name = "sequence_card", sequenceName = "REF.SQCARD",allocationSize = 1)
    private Long id;
//    MEMBERSHIP_ID               INTEGER      not null,
//    CUSTOMER_ID                 INTEGER      not null,
//    ACTIVE                      SMALLINT,
    private String cardNo;
//    CARD_ADDITIONAL_INFO        VARCHAR(40),
    @ManyToOne
    @JoinColumn(name = "CARD_TYPE")
    private CardTypeEntity cardType;
//    CREATION_DATE               TIMESTAMP(6) not null,
//    LAST_UPDATE_DATE            TIMESTAMP(6),
//    FIXED_MEMBERSHIP            SMALLINT,
    private String cvv2;
//    EXPIRATION_DATE             VARCHAR(10),
//    DEFAULT_CARD                SMALLINT     not null,
//    CREATOR_USERID              INTEGER,
//    EDITOR_USERID               INTEGER,
//    CARD_CREATOR_PERSONNEL_CODE VARCHAR(25),
//    CARD_EDITOR_PERSONNEL_CODE  VARCHAR(25),
//    HAS_CARD_REQUEST            SMALLINT

}
