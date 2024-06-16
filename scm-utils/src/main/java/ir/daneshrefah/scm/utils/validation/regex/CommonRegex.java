package ir.daneshrefah.scm.utils.validation.regex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;
@RequiredArgsConstructor
@Getter
public enum CommonRegex {
    /**
     <table>
     <td>
     <h2>REGEX ACCEPTED SAMPLE</h2>
     </td>
     <tr>
     <tr>09120034965        </tr>
     <tr>0912 003 4965      </tr>
     <tr>0912-003-4965      </tr>
     <tr>9120034965         </tr>
     <tr>912 003 4965       </tr>
     <tr>912-003-4965       </tr>
     <tr>+989120034965      </tr>
     <tr>+98912 003 4965    </tr>
     <tr>+98912-003-4965    </tr>
     <tr>989120034965       </tr>
     <tr>98912 003 4965     </tr>
     <tr>98912-003-4965     </tr>
     </td>
     </table>
     */
    MOBILE_NUMBER_REGEX (Pattern.compile("\"((0?9)|(\\\\+?989))\\\\d{2}\\\\W?\\\\d{3}\\\\W?\\\\d{4}\""));

    private final Pattern pattern;
}
