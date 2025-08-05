package ir.daneshrefah.scm.common.constant;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TerminalType {

    IB("IB", (short) 210),
    MB("MB", (short) 211),
    SCM("SCM", (short) 1304),
    HTP("HTP", (short) 2009),
    NIB("NIB", (short) 2012),
    CMC("CMC", (short) 22),
    CIB("CIB", (short) 1144),
    CAC("CAC", (short) 1),
    OTP("OTP", (short) 2),
    TUP("TUP", (short) 3),
    PKI("PKI", (short) 4),
    FAC("FAC", (short) 5),
    NSC("NSC", (short) 6),
    USD("USD", (short) 7),
    USD_8("USD", (short) 8),
    IBN("IBN", (short) 9),
    NAB("NAB", (short) 10),
    IVR("IVR", (short) 11),
    IVR_12("IVR", (short) 12),
    IPG("IPG", (short) 13),
    IPG_14("IPG", (short) 14),
    PFM("PFM", (short) 15),
    PFM_16("PFM", (short) 16),
    PFM_17("PFM", (short) 17),
    HC("HC", (short) 18),
    HC_19("HC", (short) 19),
    MOP("MOP", (short) 20),
    DOT("DOT", (short) 21),
    CIE("CIE", (short) 23),
    CID("CID", (short) 24),
    JOC("JOC", (short) 25),
    PCK("PCK", (short) 26),
    NBK("NBK", (short) 27),
    MB_212("MB", (short) 212),
    MB_213("MB", (short) 213),
    IB_216("IB", (short) 216),
    ATM("ATM", (short) 863),
    IB_921("IB", (short) 921),
    IB_922("IB", (short) 922),
    CIB_923("CIB", (short) 923),
    CIB_1144("CIB", (short) 1144),
    CIB_1145("CIB", (short) 1145),
    NBK_1284("NBK", (short) 1284),
    BRS("BRS", (short) 2000),
    FCL("FCL", (short) 2001),
    BIN("BIN", (short) 218),
    SAP("SAP", (short) 219),
    BRK("BRK", (short) 2002),
    SMT("SMT", (short) 2004),
    OB("OB", (short) 2005),
    ARZ("ARZ", (short) 2006),
    CBI("CBI", (short) 2007),
    OII("OII", (short) 2008),
    CHK("CHK", (short) 2010),
    NIB_2013("NIB", (short) 2013),
    NIB_2014("NIB", (short) 2014),
    NIB_2015("NIB", (short) 2015),
    FI("FI", (short) 2016);

    private final String terminalCode;
    private final Short legacyTerminalId;

    public static Optional<TerminalType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(terminalCode -> terminalCode.name().equals(code))
                .findFirst();
    }

    public static TerminalType findByLegacyTerminalCode(Integer legacyTerminalId) {
        if (Objects.isNull(legacyTerminalId)) {
            throw new InvalidInputException("legacyTerminalId");
        }
        return Arrays.stream(TerminalType.values()).filter(terminalType -> terminalType.getLegacyTerminalId().equals(legacyTerminalId))
                .findFirst().orElse(null);
    }

    public static TerminalType findByTerminalCode(String terminalCode) {
        if (Objects.isNull(terminalCode)) {
            throw new InvalidInputException("terminalCode");
        }
        return Arrays.stream(TerminalType.values()).filter(terminalType -> terminalType.getTerminalCode().equalsIgnoreCase(terminalCode))
                .findFirst().orElse(null);
    }
}