package ir.daneshrefah.scm.common.model.customer;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
// REF.CARD
@Getter
@Setter
public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cardNumber;
    private CardType cardType;

}
