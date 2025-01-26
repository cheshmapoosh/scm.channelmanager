package ir.daneshrefah.scm.uaa.service.credential;


// TODO: Auto-generated Javadoc
/*
 * Copyright (c) 2003-2004 IOActive Inc.
 * Copyright (c) 2002-2003 Hewlett-Packard Company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Authors:
 *   Michael Eddington (michael.eddington@ioactive.com)
 *
 */

// $Id: BPIO.java,v 1.1.6.1 2014/06/29 10:50:25 changizi Exp $

/**
 * The Class BPIO.
 */
public final class BPIO implements PasswordPolicy {

	/** The Constant NONE_ALPHABET. */
	public static final int	NONE_ALPHABET					= 100;

	/** The Constant NUMBERS_AND_LETTERS_ALPHABET. */
	public static final int	NUMBERS_AND_LETTERS_ALPHABET	= 101;

	/** The Constant PRINTABLE_ALPHABET. */
	public static final int	PRINTABLE_ALPHABET				= 102;

	/** The Constant LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET. */
	public static final int	LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET	= 103;

	/** The Constant UPPERCASE_LETTERS_AND_NUMBERS_ALPHABET. */
	public static final int	UPPERCASE_LETTERS_AND_NUMBERS_ALPHABET	= 105;

	/** The Constant NONCONFUSING_ALPHABET. */
	public static final int	NONCONFUSING_ALPHABET			= 104;

	/** The password length. */
	public static int	passwordLength			= 8;

	/** The password repetition. */
	public static int	passwordRepetition		= -1;

	/** The password alphabet. */
	public static int	passwordAlphabet		= NUMBERS_AND_LETTERS_ALPHABET;

	/** The password first alphabet. */
	public static int	passwordFirstAlphabet	= NONE_ALPHABET;

	/** The password last alphabet. */
	public static int	passwordLastAlphabet	= NONE_ALPHABET;

//	/////////////////////////////////////////////////////
//	/// PasswordPolicy
//	/////////////////////////////////////////////////////

    /* (non-Javadoc)
 * @see SecretSquirrel.PasswordPolicy#hasPasswordPolicy()
 */
public boolean hasPasswordPolicy() {
		return true;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#setPasswordPolicy(boolean)
	 */
	public void setPasswordPolicy(boolean flag) {
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#getPasswordLength()
	 */
	public int getPasswordLength() {
		return passwordLength;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#setPasswordLength(int)
	 */
	public void setPasswordLength(int length) {
		passwordLength = length;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#getPasswordRepetition()
	 */
	public int getPasswordRepetition() {
		return passwordRepetition;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#setPasswordRepetition(int)
	 */
	public void setPasswordRepetition(int repetition) {
		passwordRepetition = repetition;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#getPasswordAlphabet()
	 */
	public int getPasswordAlphabet() {
		return passwordAlphabet;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#setPasswordAlphabet(int)
	 */
	public void setPasswordAlphabet(int alphabet) {
		passwordAlphabet = alphabet;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#getPasswordFirstAlphabet()
	 */
	public int getPasswordFirstAlphabet() {
		return passwordFirstAlphabet;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#setPasswordFirstAlphabet(int)
	 */
	public void setPasswordFirstAlphabet(int alphabet) {
		passwordFirstAlphabet = alphabet;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#getPasswordLastAlphabet()
	 */
	public int getPasswordLastAlphabet() {
		return passwordLastAlphabet;
	}

	/* (non-Javadoc)
	 * @see SecretSquirrel.PasswordPolicy#setPasswordLastAlphabet(int)
	 */
	public void setPasswordLastAlphabet(int alphabet) {
		passwordLastAlphabet = alphabet;
	}
}