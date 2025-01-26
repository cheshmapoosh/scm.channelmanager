package ir.daneshrefah.scm.uaa.service.credential;

import java.security.SecureRandom;
import java.util.Vector;

// TODO: Auto-generated Javadoc

/**
 * Generates a random String using a cryptographically
 * secure random number generator.
 * <p>
 * The alphabet (characters used in the passwords generated)
 * may be specified, and the random number generator can be
 * externally supplied.
 * <p>
 * Care should be taken when using methods that limit the types
 * of passwords may be generated.  Using an alphabet that is too
 * small, using passwords that are too short, requiring too many
 * of a certain type of character, or not allowing repetition,
 * may decrease security.
 * <p>
 * More information about this class is available from <a href=
 * "http://ostermiller.org/utils/RandPass.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller
 */

public class RandPass {

    /** Default length for passwords. */
    public static int DEFAULT_PASSWORD_LENGTH = 12;

    /** Alphabet consisting of upper and lowercase letters A-Z and the digits 0-9. */
    public static final char[] NUMBERS_AND_LETTERS_ALPHABET = {
        'A','B','C','D','E','F','G','H',
        'I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X',
        'Y','Z','a','b','c','d','e','f',
        'g','h','i','j','k','l','m','n',
        'o','p','q','r','s','t','u','v',
        'w','x','y','z','0','1','2','3',
        '4','5','6','7','8','9',
        };

    /** Alphabet consisting of all the printable ASCII characters. */
    public static final char[] PRINTABLE_ALPHABET = {
        '!','\"','#','$','%','&','\'','(',
        ')','*','+',',','-','.','/','0',
        '1','2','3','4','5','6','7','8',
        '9',':',';','<','?','@','A','B',
        'C','D','E','F','G','H','I','J',
        'K','L','M','N','O','P','Q','R',
        'S','T','U','V','W','X','Y','Z',
        '[','\\',']','^','_','`','a','b',
        'c','d','e','f','g','h','i','j',
        'k','l','m','n','o','p','q','r',
        's','t','u','v','w','x','y','z',
        '{','|','}','~',
        };

    /** Alphabet consisting of the lowercase letters A-Z and the digits 0-9. */
    public static final char[] LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET = {
        'a','b','c','d','e','f','g','h',
        'i','j','k','l','m','n','o','p',
        'q','r','s','t','u','v','w','x',
        'y','z','0','1','2','3','4','5',
        '6','7','8','9',
        };

    /** Alphabet consisting of the non confusing lowercase letters A-Z without letters I,L,O and the digits 1-9. */
    public static final char[] NON_CONFUSING_LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET = {
        'a','b','c','d','e','f','g','h',
        'j','k','m','n','p',
        'q','r','s','t','u','v','w','x',
        'y','z','1','2','3','4','5',
        '6','7','8','9',
        };

    /** Alphabet consisting of the lowercase letters A-Z and the digits 0-9. */
    public static final char[] UPPERCASE_LETTERS_AND_NUMBERS_ALPHABET = {
            'A','B','C','D','E','F','G','H',
            'J','K','M','N','P','Q','R','S',
            'T','W','X','Y','Z',
            '0','1','2','3','4','5','6','7',
            '8','9',
        };

    /** Alphabet consisting of upper and lowercase letters A-Z and the digits 0-9 but with characters that are often mistaken for each other when typed removed. (I,L,O,U,V,i,l,o,u,v,0,1) */
    public static final char[] NONCONFUSING_ALPHABET = {
        'A','B','C','D','E','F','G','H',
        'J','K','M','N','P','Q','R','S',
        'T','W','X','Y','Z','a','b','c',
        'd','e','f','g','h','j','k','m',
        'n','p','q','r','s','t','w','x',
        'y','z','2','3','4','5','6','7',
        '8','9',
        };

    /** Alphabet consisting of upper and lowercase letters A-Z and the digits 0-9 but with characters that are often mistaken for each other when typed removed. (I,L,O,U,V,i,l,o,u,v,0,1) */
    public static final char[] LEAST_NONCONFUSING_ALPHABET = {
        'A','B','C','D','E','F','G','H',
        'J','K','M','N','P','Q','R','S',
        'T','W','X','Y','Z','a','b','c',
        'd','e','f','g','h','j','k','m',
        'n','p','q','r','s','t','w','x',
        'y','z','2','3','4','5','6','7',
        '8','9',
         '+','=','@','_','-',
        };

    /** The Constant NUMERIC. */
    public static final char[] NUMERIC = {
        '0','1','2','3','4','5','6','7',
        '8','9'};

    /** The requirements. */
    private Vector requirements = null;

    /** Random number generator used. */
    protected SecureRandom rand;

    /** One less than the maximum number of repeated characters that are allowed in a password. Set to -1 to disable this feature. */
    protected int repetition = -1;

    /** Set of characters which may be used in the generated passwords. <p> This value may not be null or have no elements. */
    protected char[] alphabet;

    /** Set of characters which may be used for the first character in the generated passwords. <p> This value may be null but it mus have at least one element otherwise. */
    protected char[] firstAlphabet = null;

    /** Set of characters which may be used for the last character in the generated passwords. <p> This value may be null but it mus have at least one element otherwise. */
    protected char[] lastAlphabet = null;

    /** The touched. */
    private boolean[] touched = null;

    /** The available. */
    private int[] available = null;

    /**
     * Create a new random password generator
     * with the default secure random number generator
     * and default NONCONFUSING alphabet for all characters.
     */
    public RandPass() {
        this.rand = new SecureRandom();
        this.repetition = BPIO.passwordRepetition;
        this.DEFAULT_PASSWORD_LENGTH = BPIO.passwordLength;

        switch (BPIO.passwordAlphabet) {
            default:
            case BPIO.NUMBERS_AND_LETTERS_ALPHABET:
                alphabet = NUMBERS_AND_LETTERS_ALPHABET;
                break;
            case BPIO.PRINTABLE_ALPHABET:
                alphabet = PRINTABLE_ALPHABET;
                break;
            case BPIO.LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET:
                alphabet = LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET;
                break;
            case BPIO.UPPERCASE_LETTERS_AND_NUMBERS_ALPHABET:
                alphabet = UPPERCASE_LETTERS_AND_NUMBERS_ALPHABET;
                break;
            case BPIO.NONCONFUSING_ALPHABET:
                alphabet = NONCONFUSING_ALPHABET;
                break;
        }

        switch (BPIO.passwordFirstAlphabet) {
            case BPIO.NUMBERS_AND_LETTERS_ALPHABET:
                firstAlphabet = NUMBERS_AND_LETTERS_ALPHABET;
                break;
            case BPIO.PRINTABLE_ALPHABET:
                firstAlphabet = PRINTABLE_ALPHABET;
                break;
            case BPIO.LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET:
                firstAlphabet = LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET;
                break;
            case BPIO.NONCONFUSING_ALPHABET:
                firstAlphabet = NONCONFUSING_ALPHABET;
                break;
            default:
                break;
        }

        switch (BPIO.passwordLastAlphabet) {
            case BPIO.NUMBERS_AND_LETTERS_ALPHABET:
                lastAlphabet = NUMBERS_AND_LETTERS_ALPHABET;
                break;
            case BPIO.PRINTABLE_ALPHABET:
                lastAlphabet = PRINTABLE_ALPHABET;
                break;
            case BPIO.LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET:
                lastAlphabet = LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET;
                break;
            case BPIO.NONCONFUSING_ALPHABET:
                lastAlphabet = NONCONFUSING_ALPHABET;
                break;
            default:
                break;
        }
    }

    /**
     * Create a new random password generator
     * with the given secure random number generator
     * and default NONCONFUSING alphabet for all characters.
     *
     * @param rand Secure random number generator to use when generating passwords.
     */
    public RandPass(SecureRandom rand){
        this(rand, NUMBERS_AND_LETTERS_ALPHABET);
    }

    /**
     * Create a new random password generator
     * with the default secure random number generator
     * and given alphabet for all characters.
     *
     * @param alphabet Characters allowed in generated passwords.
     */
    public RandPass(char[] alphabet){
        this(new SecureRandom(), alphabet);
    }

    /**
     * Create a new random password generator
     * with the given secure random number generator
     * and given alphabet for all characters.
     *
     * @param rand Secure random number generator to use when generating passwords.
     * @param alphabet Characters allowed in generated passwords.
     */
    public RandPass(SecureRandom rand, char[] alphabet){
        this.rand = rand;
        this.alphabet = alphabet;
    }

    /**
     * Require that a certain number of characters from an
     * alphabet be present in generated passwords.
     *
     * @param alphabet set of letters that must be present
     * @param num number of letters from the alphabet that must be present.
     */
    public void addRequirement(char[] alphabet, int num) {
        if (requirements == null)
            requirements = new Vector();
        requirements.add(new Requirement(alphabet, num));
    }

    /**
     * Set the alphabet used by this random password generator.
     *
     * @param alphabet Characters allowed in generated passwords.
     *
     * @throws NullPointerException if the alphabet is null.
     * @throws ArrayIndexOutOfBoundsException if the alphabet has no elements.
     */
    public void setAlphabet(char[] alphabet) {
        if (alphabet == null)
            throw new NullPointerException("Null alphabet");
        if (alphabet.length == 0)
            throw new ArrayIndexOutOfBoundsException("No characters in alphabet");

        this.alphabet = alphabet;
    }

    /**
     * Set the random number generator used by this random password generator.
     *
     * @param rand Secure random number generator to use when generating passwords.
     */
    public void setRandomGenerator(SecureRandom rand) {
        this.rand = rand;
    }

    /**
     * Set the alphabet used by this random password generator for the first character
     * of passwords.
     * <p>
     * If the alphabet for the first character is set to null or has no elements, the main alphabet will
     * be used for the first character.
     *
     * @param alphabet Characters allowed for the first character of the passwords.
     */
    public void setFirstAlphabet(char[] alphabet) {
        if (alphabet == null || alphabet.length == 0) {
            this.firstAlphabet = null;
        }
        else {
            this.firstAlphabet = alphabet;
        }
    }

    /**
     * Set the alphabet used by this random password generator for the last character
     * of passwords.
     * <p>
     * If the alphabet for the last character is set to null or has no elements, the main alphabet will
     * be used for the last character.
     *
     * @param alphabet Characters allowed for the last character of the passwords.
     */
    public void setLastAlphabet(char[] alphabet) {
        if (alphabet == null || alphabet.length == 0) {
            this.lastAlphabet = null;
        }
        else {
            this.lastAlphabet = alphabet;
        }
    }

    /**
     * Set the maximum number of characters that may appear in sequence more than
     * once in a password.	Your alphabet must be large enough to handle this
     * option.	If your alphabet is {'a', 'b'} and you want 8 character passwords
     * in which no character appears twice (repetition 1) you are out of luck.
     * In such instances your request for no repetition will be ignored.
     * <p>
     * For example setRepetition(3) will allow a password ababab but not allow
     * abcabc.
     * <p>
     * Using this method can greatly reduce the pool of passwords that are generated.
     * For example if only one repetition is allowed then the pool of passwords
     * is the permutation of the alphabet rather than the combination.
     *
     * @param rep Maximum character repetition.
     */
    public void setMaxRepetition(int rep) {
        this.repetition = rep - 1;
    }

    /**
     * Fill the given buffer with random characters.
     * <p>
     * Using this method, the password character array can easily
     * be reused for efficiency, or overwritten with new random
     * characters for security.
     * <p>
     * NOTE: If it is possible for a hacker to examine memory to find passwords,
     * the password should be overwritten in memory as soon as possible after i
     * is no longer in use.
     *
     * @param pass buffer that will hold the password.
     *
     * @return the buffer, filled with random characters.
     */
    public char[] getPassChars(char[] pass) {
        int length = pass.length;
        for (int i=0; i<length; i++) {
            char[] useAlph = alphabet;

            if( i == 0 && firstAlphabet != null ) {
                useAlph = firstAlphabet;
            }
            else if( i == (length-1) && lastAlphabet != null ) {
                useAlph = lastAlphabet;
            }

            int size = avoidRepetition(useAlph, pass, i, repetition, useAlph.length);
            pass[i] = useAlph[rand.nextInt(size)];
        }

        if (requirements != null)
            applyRequirements(pass);

        return(pass);
    }

    /**
     * Apply requirements.
     *
     * @param pass the pass
     */
    private void applyRequirements(char[] pass) {
        int size = requirements.size();

        if (size > 0) {
            int length = pass.length;
            if (touched == null || touched.length < length)
                touched = new boolean[length];

            if (available == null || available.length < length)
                available = new int[length];

            for (int i=0; i<length; i++) {
                touched[i] = false;
            }

            for (int reqNum=0; reqNum<size; reqNum++) {
                Requirement req = (Requirement)requirements.elementAt(reqNum);

                // set the portion of this alphabet available for use.
                int reqUsedInd = req.alphabet.length;

                // figure out how much of this requirement is already fulfilled
                // and what is available to fulfill the rest of it.
                int fufilledInd = 0;
                int availableInd = 0;

                for (int i = 0; i < length; i++) {
                    if (arrayContains(req.alphabet, pass[i]) && fufilledInd < req.num) {
                        fufilledInd++;
                        touched[i] = true;

                        if (repetition >= 0) {
                            // move already used characters so they can'
                            // be used again.  This prevents repetition.
                            reqUsedInd -= moveto(req.alphabet, reqUsedInd, pass[i]);
                            // allow repetition if we have no other choice
                            if (reqUsedInd < 0)
                                reqUsedInd = req.alphabet.length;
                        }
                    }
                    else if (!touched[i]) {
                        available[availableInd] = i;
                        availableInd++;
                    }
                }

                // fulfill the requirement
                int toDo = req.num - fufilledInd;
                for (int i=0; i<toDo && availableInd>0; i++) {
                    // pick a random available slot
                    // and a random member of the available alphabet
                    int slot = rand.nextInt(availableInd);
                    char passChar = req.alphabet[rand.nextInt(reqUsedInd)];
                    pass[available[slot]] = passChar;
                    touched[available[slot]] = true;

                    // make the slot no longer available
                    availableInd--;
                    available[slot] = available[availableInd];
                    if (repetition >= 0) {
                        // move already used characters so they can'
                        // be used again.  This prevents repetition.
                        reqUsedInd -= moveto(req.alphabet, reqUsedInd, passChar);
                        // allow repetition if we have no other choice
                        if(reqUsedInd < 0)
                            reqUsedInd = req.alphabet.length;
                    }
                }
            }
        }
    }

    /**
     * Array contains.
     *
     * @param alph the alph
     * @param c the c
     *
     * @return true, if successful
     */
    private static boolean arrayContains(char[] alph, char c) {
        for(char alphabet:alph)
            if (alphabet == c)
                return true;
        return false;
    }

    /**
     * Avoid repetition (if possible) by moving all characters that would cause repetition to
     * the end of the alphabet and returning the size of the alphabet that may be used.
     *
     * @param alph the alph
     * @param pass the pass
     * @param passSize the pass size
     * @param repetition the repetition
     * @param alphSize the alph size
     *
     * @return the int
     */
    private static int avoidRepetition(char[] alph, char[] pass, int passSize, int repetition, int alphSize) {
        if (repetition > -1) {
            // limit the alphabet to those characters that
            // will not cause repeating sequences
            int repPos = 0;

            while ((repPos = findRep(pass, repPos, passSize, repetition)) != -1) {
                // shuffle characters that would cause repetition
                // to the end of the alphabet and adjust the size
                // so that they will not be used.
                alphSize -= moveto(alph, alphSize, pass[repPos + repetition]);
                repPos++;
            }

            if (alphSize == 0)
                alphSize = alph.length;
        }

        return alphSize;
    }

    /**
     * Find a repetition of the desired length.	 The characters to search
     * for are located at pass[end-length] to pass[end]
     *
     * @param pass the pass
     * @param start the start
     * @param end the end
     * @param length the length
     *
     * @return int
     */
    private static int findRep(char[] pass, int start, int end, int length) {
        for (int i=start; i<end-length; i++) {
            boolean onTrack = true;
            for (int j=0; onTrack && j<length; j++) {
                if (pass[i+j] != pass[end-length+j])
                    onTrack = false;
            }

            if(onTrack)
                return i;
        }
        return -1;
    }

    /**
     * move all of the given character to the end of the array
     * and return the number of characters moved.
     *
     * @param alph the alph
     * @param numGood the num good
     * @param c the c
     *
     * @return int
     */
    private static int moveto(char[] alph, int numGood, char c) {
        int count = 0;
        for (int i = 0; i < numGood; i++) {
            if (alph[i] == c) {
                numGood--;
                char temp = alph[numGood];
                alph[numGood] = alph[i];
                alph[i] = temp;
                count++;
            }
        }
        return count;
    }

    /**
     * Generate a random password of the given length.
     * <p>
     * NOTE: If it is possible for a hacker to examine memory to find passwords,
     * the password should be overwritten in memory as soon as possible after i
     * is no longer in use.
     *
     * @param length The desired length of the generated password.
     *
     * @return a random password
     */
    public char[] getPassChars(int length) {
        return(getPassChars(new char[length]));
    }

    /**
     * Generate a random password of the default length (8).
     * <p>
     * NOTE: If it is possible for a hacker to examine memory to find passwords,
     * the password should be overwritten in memory as soon as possible after i
     * is no longer in use.
     *
     * @return a random password
     */
    public char[] getPassChars() {
         return(getPassChars(DEFAULT_PASSWORD_LENGTH));
    }

    /**
     * Generate a random password of the given length.
     * <p>
     * NOTE: Strings can not be modified.  If it is possible
     * for a hacker to examine memory to find passwords, getPassChars()
     * should be used so that the password can be zeroed out of memory
     * when no longer in use.
     *
     * @param length The desired length of the generated password.
     *
     * @return a random password
     *
     * @see #getPassChars(int)
     */
    public String getPass(int length) {
        return(new String(getPassChars(new char[length])));
    }

    /**
     * Generate a random password of the default length (8).
     * <p>
     * NOTE: Strings can not be modified.  If it is possible
     * for a hacker to examine memory to find passwords, getPassChars()
     * should be used so that the password can be zeroed out of memory
     * when no longer in use.
     *
     * @return a random password
     *
     * @see #getPassChars()
     */
    public String getPass() {
        return(getPass(DEFAULT_PASSWORD_LENGTH));
    }

    /**
     * The Class Requirement.
     */
    private class Requirement {

        /** The alphabet. */
        public char[] alphabet;

        /** The num. */
        public int num;

        /**
         * Instantiates a new requirement.
         *
         * @param alphabet the alphabet
         * @param num the num
         */
        public Requirement(char[] alphabet, int num) {
            this.alphabet = alphabet;
            this.num = num;
        }
    }
}