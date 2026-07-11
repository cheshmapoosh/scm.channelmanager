package ir.daneshrefah.scm.core.services.card;

public final class CardMaskingUtils {
    private CardMaskingUtils() {
    }

    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return "****";
        }
        return cardNumber.substring(0, 6) + "******" + cardNumber.substring(cardNumber.length() - 4);
    }
}
