package ir.daneshrefah.scm.uaa.service.credential;

import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2025-01-23
 */
@Component
public class CredentialGeneratorImpl implements CredentialGenerator {

    /* (non-Javadoc)
     * @see ir.dpi.cm.security.common.CredentialGenerator#generateNumericPassword()
     */
    public String generateNumericPassword() {
        RandPass randPass = new RandPass();
        randPass.setAlphabet(RandPass.NUMERIC);
        return randPass.getPass(8);
    }

    /* (non-Javadoc)
     * @see ir.dpi.cm.security.common.CredentialGenerator#generateAlphaNumericPassword()
     */
    public String generateAlphaNumericPassword() {//@todo
        RandPass randPass = new RandPass();
//        randPass.setMaxRepetition(0);
        return randPass.getPass(8);
    }

    /* (non-Javadoc)
     * @see ir.dpi.cm.security.common.CredentialGenerator#generateLeastPrintablePassword()
     */
    public String generateLeastPrintablePassword() {//@todo
        RandPass randPass = new RandPass();
//        randPass.setMaxRepetition(0);
        randPass.setAlphabet(RandPass.LEAST_NONCONFUSING_ALPHABET);
        return randPass.getPass(8);
    }

    /* (non-Javadoc)
     * @see ir.dpi.cm.security.common.CredentialGenerator#generateNonConfusingPassword()
     */
    public String generateNonConfusingPassword() {//@todo
        RandPass randPass = new RandPass();
        randPass.setAlphabet(RandPass.NON_CONFUSING_LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET);
        return randPass.getPass(8);
    }

    /* (non-Javadoc)
     * @see ir.dpi.cm.security.common.CredentialGenerator#generateNumericUsername()
     */
    public String generateNumericUsername() { //@todo
        RandPass randPass = new RandPass();
//        randPass.setMaxRepetition(0);
        randPass.setAlphabet(RandPass.NUMERIC);
        return randPass.getPass(10);

    }

    /* (non-Javadoc)
     * @see ir.dpi.cm.security.common.CredentialGenerator#generateAlphaNumericUsername()
     */
    public String generateAlphaNumericUsername() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
