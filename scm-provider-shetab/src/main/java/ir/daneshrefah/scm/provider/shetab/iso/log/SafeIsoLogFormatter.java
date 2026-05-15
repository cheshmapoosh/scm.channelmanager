package ir.daneshrefah.scm.provider.shetab.iso.log;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SafeIsoLogFormatter {

    private static final Set<Integer> FULLY_HIDDEN_FIELDS = new HashSet<Integer>(
            Arrays.asList(
                    52,   // PIN block
                    55,   // EMV/ICC data - may contain sensitive data
                    64,   // MAC
                    128   // MAC
            )
    );

    private static final Set<Integer> MASKED_FIELDS = new HashSet<Integer>(
            Arrays.asList(
                    2,    // PAN
                    14,   // Expiry
                    35,   // Track 2
                    45,   // Track 1
                    102,  // Account identification 1
                    103   // Account identification 2
            )
    );

    private static final int MAX_FIELD_VALUE_LENGTH = 120;

    private SafeIsoLogFormatter() {
    }

    public static String format(ISOMsg msg) {
        return format(msg, -1L);
    }

    public static String format(ISOMsg msg, long elapsedMs) {
        if (msg == null) {
            return "null";
        }

        StringBuilder out = new StringBuilder(512);

        out.append("mti=").append(safeMti(msg));
        out.append(" pc=").append(safeGetString(msg, 3, "-"));
        out.append(" stan=").append(safeGetString(msg, 11, "-"));
        out.append(" rrn=").append(safeGetString(msg, 37, "-"));
        out.append(" rc=").append(safeGetString(msg, 39, "-"));

        if (elapsedMs >= 0) {
            out.append(" elapsedMs=").append(elapsedMs);
        }

        out.append(" fields={");

        boolean first = true;
        int maxField = msg.getMaxField();

        for (int field = 0; field <= maxField; field++) {
            if (!msg.hasField(field)) {
                continue;
            }

            if (!first) {
                out.append(",");
            }

            first = false;
            out.append(field).append("=");

            try {
                ISOComponent component = msg.getComponent(field);
                out.append(formatFieldValue(field, component));
            } catch (Exception e) {
                out.append("[UNREADABLE]");
            }
        }

        out.append("}");

        return out.toString();
    }

    private static String formatFieldValue(int field, ISOComponent component) throws ISOException {
        if (component == null) {
            return "null";
        }

        Object value = component.getValue();

        if (value instanceof ISOMsg) {
            return "[COMPOSITE_MSG]";
        }

        if (FULLY_HIDDEN_FIELDS.contains(field)) {
            return hiddenValue(value);
        }

        String textValue;

        if (value instanceof byte[]) {
            textValue = ISOUtil.hexString((byte[]) value);
        } else {
            textValue = String.valueOf(value);
        }

        if (MASKED_FIELDS.contains(field)) {
            return maskField(field, textValue);
        }

        return limit(textValue);
    }

    private static String hiddenValue(Object value) {
        int len = 0;

        if (value instanceof byte[]) {
            len = ((byte[]) value).length;
        } else if (value != null) {
            len = String.valueOf(value).length();
        }

        return "[HIDDEN,len=" + len + "]";
    }

    private static String maskField(int field, String value) {
        if (value == null) {
            return "null";
        }

        switch (field) {
            case 2:
                return maskPan(value);

            case 35:
            case 45:
                return maskTrackData(value);

            case 14:
                return "**/**";

            case 102:
            case 103:
                return maskAccount(value);

            default:
                return "[MASKED]";
        }
    }

    private static String maskPan(String pan) {
        String digits = pan.replaceAll("\\D", "");

        if (digits.length() < 10) {
            return "******";
        }

        String first6 = digits.substring(0, 6);
        String last4 = digits.substring(digits.length() - 4);

        return first6 + "******" + last4;
    }

    private static String maskTrackData(String track) {
        if (track == null || track.length() == 0) {
            return "";
        }

        int separatorIndex = track.indexOf('=');
        if (separatorIndex < 0) {
            separatorIndex = track.indexOf('D');
        }

        if (separatorIndex > 0) {
            String pan = track.substring(0, separatorIndex);
            return maskPan(pan) + "[TRACK_DATA_MASKED]";
        }

        return "[TRACK_DATA_MASKED]";
    }

    private static String maskAccount(String account) {
        if (account == null) {
            return "null";
        }

        if (account.length() <= 4) {
            return "****";
        }

        return "****" + account.substring(account.length() - 4);
    }

    private static String limit(String value) {
        if (value == null) {
            return "null";
        }

        String normalized = value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ');

        if (normalized.length() <= MAX_FIELD_VALUE_LENGTH) {
            return normalized;
        }

        return normalized.substring(0, MAX_FIELD_VALUE_LENGTH) +
                "...[truncated,len=" + normalized.length() + "]";
    }

    private static String safeMti(ISOMsg msg) {
        try {
            return msg.getMTI();
        } catch (Exception e) {
            return "-";
        }
    }

    private static String safeGetString(ISOMsg msg, int field, String defaultValue) {
        try {
            String value = msg.getString(field);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}