package ir.daneshrefah.scm.utils.calendar.shamsi.constant;

/**
 * @author dariush abdolahi
 * @version 1.0.0
 */
public enum ShamsiMonth {

    FARVARDIN("FAR", "FAR"),
    ORDIBEHESHT("ORD", "ORD"),
    KHORDAD("KHORDAD", "KHORDAD"),
    TIR("TIR", "TIR"),
    MORDAD("MORDAD", "MOR"),
    SHAHRIVAR("SHAHRIVAR", "SHAHRIVAR"),
    MEHR("MEHR", "MEHR"),
    ABAN("ABAN", "ABAN"),
    AZAR("AZAR", "AZAR"),
    DEY("DEY", "DEY"),
    BAHMAN("BAHMAN", "BAH"),
    ESFAND("ESFAND", "ESF");

    private final String persianName;
    private final String shortName;


    ShamsiMonth(String persianName, String shortName) {
        this.persianName = persianName;
        this.shortName = shortName;
    }

    public static ShamsiMonth of(int month) {
        if (month > 12 || month < 1) {
            throw new IllegalArgumentException("month is out of valid range [1-12]");
        }
        return ShamsiMonth.values()[month - 1];
    }

    public String getPersianName() {
        return persianName;
    }

    public String getShortName() {
        return shortName;
    }

    public int getValue() {
        return ordinal() + 1;
    }


    public int length(boolean leapYear) {
        int value = getValue();
        return value < 7 ? 31 : (value != 12 ? 30 : (leapYear ? 30 : 29));
    }


    public int maxLength() {
        return length(true);
    }


    public int minLength() {
        return length(false);
    }


    public ShamsiMonth plus(long months) {
        int amount = (int) (months % 12);
        amount = (amount + 12) % 12;
        return ShamsiMonth.values()[(ordinal() + amount) % 12];
    }


    public ShamsiMonth minus(long months) {
        return plus(-months);
    }


    public int daysToFirstOfMonth() {
        int val = getValue();
        return (val <= 6) ? (31 * (val - 1)) : ((30 * (val - 1 - 6)) + 186);
    }
}
