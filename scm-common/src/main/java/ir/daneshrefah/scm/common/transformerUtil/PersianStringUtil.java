package ir.daneshrefah.scm.common.transformerUtil;

public class PersianStringUtil {

    public static String cvrtUTFToAscii1256Encoding(String src) {
        String dest = "";

        for(int index = 0; index < src.length(); ++index) {
            switch (src.charAt(index)) {
                case '،':
                    dest = dest + '¡';
                    break;
                case '؍':
                case '؎':
                case '؏':
                case 'ؐ':
                case 'ؑ':
                case 'ؒ':
                case 'ؓ':
                case 'ؔ':
                case 'ؕ':
                case 'ؖ':
                case 'ؗ':
                case 'ؘ':
                case 'ؙ':
                case 'ؚ':
                case '\u061c':
                case '؝':
                case '؞':
                case 'ؠ':
                case 'ة':
                case 'ػ':
                case 'ؼ':
                case 'ؽ':
                case 'ؾ':
                case 'ؿ':
                case 'ـ':
                case 'ً':
                case 'ٌ':
                case 'ٍ':
                case 'َ':
                case 'ُ':
                case 'ِ':
                case 'ّ':
                case 'ْ':
                case 'ٓ':
                case 'ٔ':
                case 'ٕ':
                case 'ٖ':
                case 'ٗ':
                case '٘':
                case 'ٙ':
                case 'ٚ':
                case 'ٛ':
                case 'ٜ':
                case 'ٝ':
                case 'ٞ':
                case 'ٟ':
                case '٫':
                case '٬':
                case '٭':
                case 'ٮ':
                case 'ٯ':
                case 'ٰ':
                case 'ٱ':
                case 'ٲ':
                case 'ٳ':
                case 'ٴ':
                case 'ٵ':
                case 'ٶ':
                case 'ٷ':
                case 'ٹ':
                case 'ٺ':
                case 'ٻ':
                case 'ټ':
                case 'ٽ':
                case 'ٿ':
                case 'ڀ':
                case 'ځ':
                case 'ڂ':
                case 'ڃ':
                case 'ڄ':
                case 'څ':
                case 'ڇ':
                case 'ڈ':
                case 'ډ':
                case 'ڊ':
                case 'ڋ':
                case 'ڌ':
                case 'ڍ':
                case 'ڎ':
                case 'ڏ':
                case 'ڐ':
                case 'ڑ':
                case 'ڒ':
                case 'ړ':
                case 'ڔ':
                case 'ڕ':
                case 'ږ':
                case 'ڗ':
                case 'ڙ':
                case 'ښ':
                case 'ڛ':
                case 'ڜ':
                case 'ڝ':
                case 'ڞ':
                case 'ڟ':
                case 'ڠ':
                case 'ڡ':
                case 'ڢ':
                case 'ڣ':
                case 'ڤ':
                case 'ڥ':
                case 'ڦ':
                case 'ڧ':
                case 'ڨ':
                case 'ګ':
                case 'ڬ':
                case 'ڭ':
                case 'ڮ':
                case 'ڰ':
                case 'ڱ':
                case 'ڲ':
                case 'ڳ':
                case 'ڴ':
                case 'ڵ':
                case 'ڶ':
                case 'ڷ':
                case 'ڸ':
                case 'ڹ':
                case 'ں':
                case 'ڻ':
                case 'ڼ':
                case 'ڽ':
                case 'ڿ':
                case 'ۀ':
                case 'ہ':
                case 'ۂ':
                case 'ۃ':
                case 'ۄ':
                case 'ۅ':
                case 'ۆ':
                case 'ۇ':
                case 'ۈ':
                case 'ۉ':
                case 'ۊ':
                case 'ۋ':
                case 'ۍ':
                case 'ێ':
                case 'ۏ':
                case 'ې':
                case 'ۑ':
                case 'ے':
                case 'ۓ':
                case '۔':
                case 'ە':
                case 'ۖ':
                case 'ۗ':
                case 'ۘ':
                case 'ۚ':
                case 'ۛ':
                case 'ۜ':
                case '\u06dd':
                case '۞':
                case '۟':
                case '۠':
                case 'ۡ':
                case 'ۢ':
                case 'ۣ':
                case 'ۤ':
                case 'ۥ':
                case 'ۦ':
                case 'ۧ':
                case 'ۨ':
                case '۩':
                case '۪':
                case '۫':
                case '۬':
                case 'ۭ':
                case 'ۮ':
                case 'ۯ':
                default:
                    dest = dest + src.charAt(index);
                    break;
                case '؛':
                    dest = dest + 'º';
                    break;
                case '؟':
                    dest = dest + '¿';
                    break;
                case 'ء':
                    dest = dest + 'Á';
                    break;
                case 'آ':
                    dest = dest + 'Â';
                    break;
                case 'أ':
                    dest = dest + 'Ã';
                    break;
                case 'ؤ':
                    dest = dest + 'Ä';
                    break;
                case 'إ':
                    dest = dest + 'Å';
                    break;
                case 'ئ':
                    dest = dest + 'Æ';
                    break;
                case 'ا':
                    dest = dest + 'Ç';
                    break;
                case 'ب':
                    dest = dest + 'È';
                    break;
                case 'ت':
                    dest = dest + 'Ê';
                    break;
                case 'ث':
                    dest = dest + 'Ë';
                    break;
                case 'ج':
                    dest = dest + 'Ì';
                    break;
                case 'ح':
                    dest = dest + 'Í';
                    break;
                case 'خ':
                    dest = dest + 'Î';
                    break;
                case 'د':
                    dest = dest + 'Ï';
                    break;
                case 'ذ':
                    dest = dest + 'Ð';
                    break;
                case 'ر':
                    dest = dest + 'Ñ';
                    break;
                case 'ز':
                    dest = dest + 'Ò';
                    break;
                case 'س':
                    dest = dest + 'Ó';
                    break;
                case 'ش':
                    dest = dest + 'Ô';
                    break;
                case 'ص':
                    dest = dest + 'Õ';
                    break;
                case 'ض':
                    dest = dest + 'Ö';
                    break;
                case 'ط':
                    dest = dest + 'Ø';
                    break;
                case 'ظ':
                    dest = dest + 'Ù';
                    break;
                case 'ع':
                    dest = dest + 'Ú';
                    break;
                case 'غ':
                    dest = dest + 'Û';
                    break;
                case 'ف':
                    dest = dest + 'Ý';
                    break;
                case 'ق':
                    dest = dest + 'Þ';
                    break;
                case 'ك':
                    dest = dest + 'ß';
                    break;
                case 'ل':
                    dest = dest + 'á';
                    break;
                case 'م':
                    dest = dest + 'ã';
                    break;
                case 'ن':
                    dest = dest + 'ä';
                    break;
                case 'ه':
                    dest = dest + 'å';
                    break;
                case 'و':
                    dest = dest + 'æ';
                    break;
                case 'ى':
                    dest = dest + 'ì';
                    break;
                case 'ي':
                    dest = dest + 'í';
                    break;
                case '٠':
                    dest = dest + '0';
                    break;
                case '١':
                    dest = dest + '1';
                    break;
                case '٢':
                    dest = dest + '2';
                    break;
                case '٣':
                    dest = dest + '3';
                    break;
                case '٤':
                    dest = dest + '4';
                    break;
                case '٥':
                    dest = dest + '5';
                    break;
                case '٦':
                    dest = dest + '6';
                    break;
                case '٧':
                    dest = dest + '7';
                    break;
                case '٨':
                    dest = dest + '8';
                    break;
                case '٩':
                    dest = dest + '9';
                    break;
                case '٪':
                    dest = dest + '%';
                    break;
                case 'ٸ':
                    dest = dest + '+';
                    break;
                case 'پ':
                    dest = dest + '\u0081';
                    break;
                case 'چ':
                    dest = dest + '\u008d';
                    break;
                case 'ژ':
                    dest = dest + '\u008e';
                    break;
                case 'ک':
                    dest = dest + '\u0098';
                    break;
                case 'ڪ':
                    dest = dest + 'ß';
                    break;
                case 'گ':
                    dest = dest + '\u0090';
                    break;
                case 'ھ':
                    dest = dest + 'À';
                    break;
                case 'ی':
                    dest = dest + 'í';
                    break;
                case 'ۙ':
                    dest = dest + 'á';
                    break;
                case '۰':
                    dest = dest + '0';
                    break;
                case '۱':
                    dest = dest + '1';
                    break;
                case '۲':
                    dest = dest + '2';
                    break;
                case '۳':
                    dest = dest + '3';
                    break;
                case '۴':
                    dest = dest + '4';
                    break;
                case '۵':
                    dest = dest + '5';
                    break;
                case '۶':
                    dest = dest + '6';
                    break;
                case '۷':
                    dest = dest + '7';
                    break;
                case '۸':
                    dest = dest + '8';
                    break;
                case '۹':
                    dest = dest + '9';
            }
        }

        return dest;
    }

    public static String cvrtIranSystem2Utf(String src) {
        String dest = "";

        for(int index = 0; index < src.length(); ++index) {
            char nextChar = src.charAt(index);
            switch (nextChar) {
                case '+':
                    dest = dest + 'ٸ';
                    break;
                case ',':
                case '-':
                case '.':
                case '/':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case ':':
                case ';':
                case '<':
                case '=':
                case '>':
                case '?':
                case '@':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '_':
                case '`':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '{':
                case '|':
                case '}':
                case '~':
                case '\u007f':
                case '°':
                case '±':
                case '²':
                case '³':
                case '´':
                case 'µ':
                case '¶':
                case '·':
                case '¸':
                case '¹':
                case 'º':
                case '»':
                case '¼':
                case '½':
                case '¾':
                case '¿':
                case 'À':
                case 'Á':
                case 'Â':
                case 'Ã':
                case 'Ä':
                case 'Å':
                case 'Æ':
                case 'Ç':
                case 'È':
                case 'É':
                case 'Ê':
                case 'Ë':
                case 'Ì':
                case 'Í':
                case 'Î':
                case 'Ï':
                case 'Ð':
                case 'Ñ':
                case 'Ò':
                case 'Ó':
                case 'Ô':
                case 'Õ':
                case 'Ö':
                case '×':
                case 'Ø':
                case 'Ù':
                case 'Ú':
                case 'Û':
                case 'Ü':
                case 'Ý':
                case 'Þ':
                case 'ß':
                default:
                    dest = dest + src.charAt(index);
                    break;
                case '\u0080':
                    dest = dest + '٠';
                    break;
                case '\u0081':
                    dest = dest + '١';
                    break;
                case '\u0082':
                    dest = dest + '٢';
                    break;
                case '\u0083':
                    dest = dest + '٣';
                    break;
                case '\u0084':
                    dest = dest + '٤';
                    break;
                case '\u0085':
                    dest = dest + '٥';
                    break;
                case '\u0086':
                    dest = dest + '٦';
                    break;
                case '\u0087':
                    dest = dest + '٧';
                    break;
                case '\u0088':
                    dest = dest + '٨';
                    break;
                case '\u0089':
                    dest = dest + '٩';
                    break;
                case '\u008a':
                    dest = dest + ',';
                case '\u008b':
                    break;
                case '\u008c':
                    dest = dest + '?';
                    break;
                case '\u008d':
                    dest = dest + 'آ';
                    break;
                case '\u008e':
                    dest = dest + 'ئ';
                    break;
                case '\u008f':
                    dest = dest + 'ء';
                    break;
                case '\u0090':
                    dest = dest + 'ا';
                    break;
                case '\u0091':
                    dest = dest + 'ا';
                    break;
                case '\u0092':
                    dest = dest + 'ب';
                    dest = dest + ' ';
                    break;
                case '\u0093':
                    dest = dest + 'ب';
                    break;
                case '\u0094':
                    dest = dest + 'پ';
                    dest = dest + ' ';
                    break;
                case '\u0095':
                    dest = dest + 'پ';
                    break;
                case '\u0096':
                    dest = dest + 'ت';
                    dest = dest + ' ';
                    break;
                case '\u0097':
                    dest = dest + 'ت';
                    break;
                case '\u0098':
                    dest = dest + 'ث';
                    dest = dest + ' ';
                    break;
                case '\u0099':
                    dest = dest + 'ث';
                    break;
                case '\u009a':
                    dest = dest + 'ج';
                    dest = dest + ' ';
                    break;
                case '\u009b':
                    dest = dest + 'ج';
                    break;
                case '\u009c':
                    dest = dest + 'چ';
                    dest = dest + ' ';
                    break;
                case '\u009d':
                    dest = dest + 'چ';
                    break;
                case '\u009e':
                    dest = dest + 'ح';
                    dest = dest + ' ';
                    break;
                case '\u009f':
                    dest = dest + 'ح';
                    break;
                case ' ':
                    dest = dest + 'خ';
                    dest = dest + ' ';
                    break;
                case '¡':
                    dest = dest + 'خ';
                    break;
                case '¢':
                    dest = dest + 'د';
                    break;
                case '£':
                    dest = dest + 'ذ';
                    break;
                case '¤':
                    dest = dest + 'ر';
                    break;
                case '¥':
                    dest = dest + 'ز';
                    break;
                case '¦':
                    dest = dest + 'ژ';
                    break;
                case '§':
                    dest = dest + 'س';
                    dest = dest + ' ';
                    break;
                case '¨':
                    dest = dest + 'س';
                    break;
                case '©':
                    dest = dest + 'ش';
                    dest = dest + ' ';
                    break;
                case 'ª':
                    dest = dest + 'ش';
                    break;
                case '«':
                    dest = dest + 'ص';
                    dest = dest + ' ';
                    break;
                case '¬':
                    dest = dest + 'ص';
                    break;
                case '\u00ad':
                    dest = dest + 'ض';
                    dest = dest + ' ';
                    break;
                case '®':
                    dest = dest + 'ض';
                    break;
                case '¯':
                    dest = dest + 'ط';
                    break;
                case 'à':
                    dest = dest + 'ظ';
                    break;
                case 'á':
                    dest = dest + 'ع';
                    dest = dest + ' ';
                    break;
                case 'â':
                    dest = dest + 'ع';
                    dest = dest + ' ';
                    break;
                case 'ã':
                    dest = dest + 'ع';
                    break;
                case 'ä':
                    dest = dest + 'ع';
                    break;
                case 'å':
                    dest = dest + 'غ';
                    dest = dest + ' ';
                    break;
                case 'æ':
                    dest = dest + 'غ';
                    dest = dest + ' ';
                    break;
                case 'ç':
                    dest = dest + 'غ';
                    break;
                case 'è':
                    dest = dest + 'غ';
                    break;
                case 'é':
                    dest = dest + 'ف';
                    dest = dest + ' ';
                    break;
                case 'ê':
                    dest = dest + 'ف';
                    break;
                case 'ë':
                    dest = dest + 'ق';
                    dest = dest + ' ';
                    break;
                case 'ì':
                    dest = dest + 'ق';
                    break;
                case 'í':
                    dest = dest + 'ك';
                    dest = dest + ' ';
                    break;
                case 'î':
                    dest = dest + 'ك';
                    break;
                case 'ï':
                    dest = dest + 'گ';
                    dest = dest + ' ';
                    break;
                case 'ð':
                    dest = dest + 'گ';
                    break;
                case 'ñ':
                    dest = dest + 'ل';
                    dest = dest + ' ';
                    break;
                case 'ò':
                    dest = dest + 'ل';
                    break;
                case 'ó':
                    dest = dest + 'ل';
                    break;
                case 'ô':
                    dest = dest + 'م';
                    dest = dest + ' ';
                    break;
                case 'õ':
                    dest = dest + 'م';
                    break;
                case 'ö':
                    dest = dest + 'ن';
                    dest = dest + ' ';
                    break;
                case '÷':
                    dest = dest + 'ن';
                    break;
                case 'ø':
                    dest = dest + 'و';
                    break;
                case 'ù':
                    dest = dest + 'ه';
                    dest = dest + ' ';
                    break;
                case 'ú':
                    dest = dest + 'ه';
                    break;
                case 'û':
                    dest = dest + 'ه';
                    break;
                case 'ü':
                    dest = dest + 'ي';
                    dest = dest + ' ';
                    break;
                case 'ý':
                    dest = dest + 'ي';
                    dest = dest + ' ';
                    break;
                case 'þ':
                    dest = dest + 'ي';
                    break;
                case 'ÿ':
                    dest = dest + ' ';
            }
        }

        return dest.replaceAll("  ", " ");
    }

    public static String convertArabicToPersianUTF(String src) {
        String convertedString = src;
        if (!src.isEmpty()) {
            char arabicLetterKaf = 1603;
            char arabicLetterAlefMaksura = 1609;
            char arabicLetterYeh = 1610;
            char persianLetterKeheh = 1705;
            char persianLetterYeh = 1740;
            convertedString = src.replace(arabicLetterYeh, persianLetterYeh);
            convertedString = convertedString.replace(arabicLetterAlefMaksura, persianLetterYeh);
            convertedString = convertedString.replace(arabicLetterKaf, persianLetterKeheh);
        }

        return convertedString;
    }

    /*
    *
def cvrtIranSystem2Utf2 = { src ->
    {
        def dest = "";

        for (def index = 0; index < src.length(); ++index) {
            def nextChar = src.charAt(index);
            switch (nextChar) {
                case '+':
                    dest = dest + 'ٸ';
                    break;
                case '\u0080':
                    dest = dest + '٠';
                    break;
                case '\u0081':
                    dest = dest + '١';
                    break;
                case '\u0082':
                    dest = dest + '٢';
                    break;
                case '\u0083':
                    dest = dest + '٣';
                    break;
                case '\u0084':
                    dest = dest + '٤';
                    break;
                case '\u0085':
                    dest = dest + '٥';
                    break;
                case '\u0086':
                    dest = dest + '٦';
                    break;
                case '\u0087':
                    dest = dest + '٧';
                    break;
                case '\u0088':
                    dest = dest + '٨';
                    break;
                case '\u0089':
                    dest = dest + '٩';
                    break;
                case '\u008a':
                    dest = dest + ',';
                case '\u008b':
                    break;
                case '\u008c':
                    dest = dest + '?';
                    break;
                case '\u008d':
                    dest = dest + 'آ';
                    break;
                case '\u008e':
                    dest = dest + 'ئ';
                    break;
                case '\u008f':
                    dest = dest + 'ء';
                    break;
                case '\u0090':
                    dest = dest + 'ا';
                    break;
                case '\u0091':
                    dest = dest + 'ا';
                    break;
                case '\u0092':
                    dest = dest + 'ب';
                    dest = dest + ' ';
                    break;
                case '\u0093':
                    dest = dest + 'ب';
                    break;
                case '\u0094':
                    dest = dest + 'پ';
                    dest = dest + ' ';
                    break;
                case '\u0095':
                    dest = dest + 'پ';
                    break;
                case '\u0096':
                    dest = dest + 'ت';
                    dest = dest + ' ';
                    break;
                case '\u0097':
                    dest = dest + 'ت';
                    break;
                case '\u0098':
                    dest = dest + 'ث';
                    dest = dest + ' ';
                    break;
                case '\u0099':
                    dest = dest + 'ث';
                    break;
                case '\u009a':
                    dest = dest + 'ج';
                    dest = dest + ' ';
                    break;
                case '\u009b':
                    dest = dest + 'ج';
                    break;
                case '\u009c':
                    dest = dest + 'چ';
                    dest = dest + ' ';
                    break;
                case '\u009d':
                    dest = dest + 'چ';
                    break;
                case '\u009e':
                    dest = dest + 'ح';
                    dest = dest + ' ';
                    break;
                case '\u009f':
                    dest = dest + 'ح';
                    break;
                case ' ':
                    dest = dest + 'خ';
                    dest = dest + ' ';
                    break;
                case '¡':
                    dest = dest + 'خ';
                    break;
                case '¢':
                    dest = dest + 'د';
                    break;
                case '£':
                    dest = dest + 'ذ';
                    break;
                case '¤':
                    dest = dest + 'ر';
                    break;
                case '¥':
                    dest = dest + 'ز';
                    break;
                case '¦':
                    dest = dest + 'ژ';
                    break;
                case '§':
                    dest = dest + 'س';
                    dest = dest + ' ';
                    break;
                case '¨':
                    dest = dest + 'س';
                    break;
                case '©':
                    dest = dest + 'ش';
                    dest = dest + ' ';
                    break;
                case 'ª':
                    dest = dest + 'ش';
                    break;
                case '«':
                    dest = dest + 'ص';
                    dest = dest + ' ';
                    break;
                case '¬':
                    dest = dest + 'ص';
                    break;
                case '\u00ad':
                    dest = dest + 'ض';
                    dest = dest + ' ';
                    break;
                case '®':
                    dest = dest + 'ض';
                    break;
                case '¯':
                    dest = dest + 'ط';
                    break;
                case 'à':
                    dest = dest + 'ظ';
                    break;
                case 'á':
                    dest = dest + 'ع';
                    dest = dest + ' ';
                    break;
                case 'â':
                    dest = dest + 'ع';
                    dest = dest + ' ';
                    break;
                case 'ã':
                    dest = dest + 'ع';
                    break;
                case 'ä':
                    dest = dest + 'ع';
                    break;
                case 'å':
                    dest = dest + 'غ';
                    dest = dest + ' ';
                    break;
                case 'æ':
                    dest = dest + 'غ';
                    dest = dest + ' ';
                    break;
                case 'ç':
                    dest = dest + 'غ';
                    break;
                case 'è':
                    dest = dest + 'غ';
                    break;
                case 'é':
                    dest = dest + 'ف';
                    dest = dest + ' ';
                    break;
                case 'ê':
                    dest = dest + 'ف';
                    break;
                case 'ë':
                    dest = dest + 'ق';
                    dest = dest + ' ';
                    break;
                case 'ì':
                    dest = dest + 'ق';
                    break;
                case 'í':
                    dest = dest + 'ك';
                    dest = dest + ' ';
                    break;
                case 'î':
                    dest = dest + 'ك';
                    break;
                case 'ï':
                    dest = dest + 'گ';
                    dest = dest + ' ';
                    break;
                case 'ð':
                    dest = dest + 'گ';
                    break;
                case 'ñ':
                    dest = dest + 'ل';
                    dest = dest + ' ';
                    break;
                case 'ò':
                    dest = dest + 'ل';
                    break;
                case 'ó':
                    dest = dest + 'ل';
                    break;
                case 'ô':
                    dest = dest + 'م';
                    dest = dest + ' ';
                    break;
                case 'õ':
                    dest = dest + 'م';
                    break;
                case 'ö':
                    dest = dest + 'ن';
                    dest = dest + ' ';
                    break;
                case '÷':
                    dest = dest + 'ن';
                    break;
                case 'ø':
                    dest = dest + 'و';
                    break;
                case 'ù':
                    dest = dest + 'ه';
                    dest = dest + ' ';
                    break;
                case 'ú':
                    dest = dest + 'ه';
                    break;
                case 'û':
                    dest = dest + 'ه';
                    break;
                case 'ü':
                    dest = dest + 'ي';
                    dest = dest + ' ';
                    break;
                case 'ý':
                    dest = dest + 'ي';
                    dest = dest + ' ';
                    break;
                case 'þ':
                    dest = dest + 'ي';
                    break;
                case 'ÿ':
                    dest = dest + ' ';
                case ',':
                case '-':
                case '.':
                case '/':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case ':':
                case ';':
                case '<':
                case '=':
                case '>':
                case '?':
                case '@':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '_':
                case '`':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '{':
                case '|':
                case '}':
                case '~':
                case '\u007f':
                case '°':
                case '±':
                case '²':
                case '³':
                case '´':
                case 'µ':
                case '¶':
                case '·':
                case '¸':
                case '¹':
                case 'º':
                case '»':
                case '¼':
                case '½':
                case '¾':
                case '¿':
                case 'À':
                case 'Á':
                case 'Â':
                case 'Ã':
                case 'Ä':
                case 'Å':
                case 'Æ':
                case 'Ç':
                case 'È':
                case 'É':
                case 'Ê':
                case 'Ë':
                case 'Ì':
                case 'Í':
                case 'Î':
                case 'Ï':
                case 'Ð':
                case 'Ñ':
                case 'Ò':
                case 'Ó':
                case 'Ô':
                case 'Õ':
                case 'Ö':
                case '×':
                case 'Ø':
                case 'Ù':
                case 'Ú':
                case 'Û':
                case 'Ü':
                case 'Ý':
                case 'Þ':
                case 'ß':
                default:
                    dest = dest + src.charAt(index);
                    break;
            }
        }

        return dest.replaceAll("  ", " ");
    }
}
*/
}
