package ir.daneshrefah.scm.common.model.customer;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
// REF.CARD_TYPE
public class CardType implements Serializable {

    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private String productCode;
    private String owner;
    private Integer maxCardsPerUser;
    private Integer order;
}
