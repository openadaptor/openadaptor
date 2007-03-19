/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.auxil.processor.crypto;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Uses PBE (Password Based Encyrption) to encyrpt and decrypt data. Must be configured
 * with a password, the algorithm defaults to {@value #DEFAULT_ALGORITHM}. refer to
 * base class for other behaviour.
 * 
 * @author perryj
 *
 */
public class PasswordBasedEncryptionProcessor extends EncryptionProcessor {

  private static final byte[] SALT = {
    (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
    (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
  };
  
  private static final int ITERATIONS = 20;

  private static final String DEFAULT_ALGORITHM = "PBEWithMD5AndDES";
  
  private String password;

  public PasswordBasedEncryptionProcessor() {
    super();
    setAlgorithm(DEFAULT_ALGORITHM);
  }

  public PasswordBasedEncryptionProcessor(String id) {
    super(id);
    setAlgorithm(DEFAULT_ALGORITHM);
  }

  public void setPassword(final String password) {
    this.password = password;
  }
  
  public void validate(List exceptions) {
    if (password == null) {
      exceptions.add(new ValidationException("password property not set", this));
    } else {
      try {
        KeySpec spec = new PBEKeySpec(password.toCharArray());
        setKey(SecretKeyFactory.getInstance(getAlgorithm()).generateSecret(spec));
        setParams(new PBEParameterSpec(SALT, ITERATIONS));
      } catch (GeneralSecurityException e) {
        e.printStackTrace();
        throw new ProcessingException("GeneralSecurityException", e, this);
      }
    }
    super.validate(exceptions);
  }
}
