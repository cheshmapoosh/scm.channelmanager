def body = exchange.in.body
println("tcp rs")

if (!body instanceof Map) {
    return
}

def mti = body.get("mti")
println("tcp card inq rs mti : " + mti)
if (!mti.toString().trim().equals("1110")) {
    throw new RuntimeException("tcp card inq rs : mti is null")
}

def fields = body.get("fields")
println("tcp card inq rs fields : " + fields)
if (fields == null) {
    throw new RuntimeException("tcp card inq rs : fields is null")
}

println("tcp card inq rs action code" + fields["39"])
if (fields["39"] == null || !fields["39"].toString().equals("000")) {
    throw new RuntimeException("tcp card inq rs action code : " + fields["39"].toString())
}

def customerNameFamily = fields["44"].toString()
def name = "";
def family = "";

def cvrtIranSystem2Utf = { src ->
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

def convertArabicToPersianUTF = { src ->
    {
        def convertedString = src;
        if (!src.isEmpty()) {
            def arabicLetterKaf = (char) 1603;
            def arabicLetterAlefMaksura = (char) 1609;
            def arabicLetterYeh = (char) 1610;
            def persianLetterKeheh = (char) 1705;
            def persianLetterYeh = (char) 1740;
            convertedString = src.replace(arabicLetterYeh, persianLetterYeh);
            convertedString = convertedString.replace(arabicLetterAlefMaksura, persianLetterYeh);
            convertedString = convertedString.replace(arabicLetterKaf, persianLetterKeheh);
        }

        return convertedString;
    }
}

if (!(customerNameFamily.isEmpty() || customerNameFamily.length() <= 25)) {
    try {
        def tailoredCustomerNameFamily = customerNameFamily[25..-1]
        int nameLen = tailoredCustomerNameFamily[0..1] as int

        int nameStart = 2
        int nameEnd = nameStart + nameLen

        name = tailoredCustomerNameFamily[nameStart..<nameEnd]

        int familyLen = tailoredCustomerNameFamily[nameEnd..<(nameEnd + 2)] as int

        int familyStart = nameEnd + 2
        int familyEnd = familyStart + familyLen

        family = tailoredCustomerNameFamily[familyStart..<familyEnd]
    } catch (Exception e) {
        name = "";
        family = ""
    }
}

return [
        "card"        : [
                "destinationBankName": "",
                "imageUrl"           : ""
        ],
        "customerName": [
                "firstName": convertArabicToPersianUTF(cvrtIranSystem2Utf(name)),
                "lastName" : convertArabicToPersianUTF(cvrtIranSystem2Utf(family))
        ]
]