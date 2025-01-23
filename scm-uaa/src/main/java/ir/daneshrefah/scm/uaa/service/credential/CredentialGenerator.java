package ir.daneshrefah.scm.uaa.service.credential;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2025-01-23
 */
public interface CredentialGenerator {

    /**
     * Generate numeric password.
     *
     * @return the string
     */
    String generateNumericPassword();

    /**
     * Generate alpha numeric password.
     *
     * @return the string
     */
    String generateAlphaNumericPassword();

    /**
     * Generate least printable password.
     *
     * @return the string
     */
    String generateLeastPrintablePassword();

    /**
     * Generate non confusing password.
     *
     * @return the string
     */
    String generateNonConfusingPassword();

    /**
     * Generate numeric username.
     *
     * @return the string
     */
    String generateNumericUsername();

    /**
     * Generate alpha numeric username.
     *
     * @return the string
     */
    String generateAlphaNumericUsername();
}
