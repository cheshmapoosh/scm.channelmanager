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

// $Id: PasswordPolicy.java,v 1.1.6.1 2014/06/29 10:50:25 changizi Exp $

/**
 * The Interface PasswordPolicy.
 */
public interface PasswordPolicy {
	
	/**
	 * Checks for password policy.
	 * 
	 * @return true, if successful
	 */
	public boolean hasPasswordPolicy();
	
	/**
	 * Sets the password policy.
	 * 
	 * @param flag the new password policy
	 */
	public void setPasswordPolicy(boolean flag);
	
	/**
	 * Gets the password length.
	 * 
	 * @return the password length
	 */
	public int getPasswordLength();
	
	/**
	 * Sets the password length.
	 * 
	 * @param length the new password length
	 */
	public void setPasswordLength(int length);
	
	/**
	 * Gets the password repetition.
	 * 
	 * @return the password repetition
	 */
	public int getPasswordRepetition();
	
	/**
	 * Sets the password repetition.
	 * 
	 * @param repetition the new password repetition
	 */
	public void setPasswordRepetition(int repetition);
	
	/**
	 * Gets the password alphabet.
	 * 
	 * @return the password alphabet
	 */
	public int getPasswordAlphabet();
	
	/**
	 * Sets the password alphabet.
	 * 
	 * @param alphabet the new password alphabet
	 */
	public void setPasswordAlphabet(int alphabet);
	
	/**
	 * Gets the password first alphabet.
	 * 
	 * @return the password first alphabet
	 */
	public int getPasswordFirstAlphabet();
	
	/**
	 * Sets the password first alphabet.
	 * 
	 * @param alphabet the new password first alphabet
	 */
	public void setPasswordFirstAlphabet(int alphabet);
	
	/**
	 * Gets the password last alphabet.
	 * 
	 * @return the password last alphabet
	 */
	public int getPasswordLastAlphabet();
	
	/**
	 * Sets the password last alphabet.
	 * 
	 * @param alphabet the new password last alphabet
	 */
	public void setPasswordLastAlphabet(int alphabet);
}

