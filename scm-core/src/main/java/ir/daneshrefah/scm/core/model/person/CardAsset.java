package ir.daneshrefah.scm.core.model.person;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
public class CardAsset extends Asset<String> {

    private Card card;

    @Override
    public String getValue() {
        return card.getCardNumber();
    }

    @Override
    public AssetType getType() {
        return AssetType.CARD;
    }

}
