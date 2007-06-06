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
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.openadaptor.auxil.connector.iostream.EncodingAwareComponent;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Base processor component for encrypting and decrypting data. Use javax crypto API and
 * must be configured with a algorithm name, a SecretKey and a mode (encrypt or decrypt).
 * 
 * If incoming data is bytes then output is bytes, otherwise it assumes the incoming data is
 * a string and outputs a string. If the base64 property is set (and it is by default) then
 * it base64 encodes after encryption and base64 decode before encryption.
 * 
 * @author perryj
 */
public class EncryptionProcessor extends EncodingAwareComponent implements IDataProcessor {

  private String algorithm;
  private int mode = Integer.MIN_VALUE;
  private SecretKey key;
  private AlgorithmParameterSpec params;
  private boolean base64 = true;
  
  private Cipher cipher;
  
  public EncryptionProcessor() {
  }

  public EncryptionProcessor(String id) {
    super(id);
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public void setKey(SecretKey key) {
    this.key = key;
  }

  public void setParams(AlgorithmParameterSpec params) {
    this.params = params;
  }

  private void setMode(int mode) {
    this.mode = mode;
  }

  public void setMode(String s) {
    if (s.equalsIgnoreCase("encrypt")) {
      setMode(Cipher.ENCRYPT_MODE);
    } else if (s.equalsIgnoreCase("decrypt")) {
      setMode(Cipher.DECRYPT_MODE);
    } else {
      throw new RuntimeException("mode " + s + " not recognised, use encrypt or decrypt");
    }
  }

  public Object[] process(Object data) {
    
    // get bytes
    byte[] inBytes;
    if (data instanceof byte[]) {
      inBytes = (byte[]) data;
    } else if (data instanceof String) {
      inBytes = getBytes((String)data);
    } else {
      inBytes = getBytes(data.toString());
    }
    
    // base64 decode bytes before decryption
    if (mode == Cipher.DECRYPT_MODE && base64) {
      inBytes = Base64.decodeBase64(inBytes);
    }
    
    // encrypt/decrypt
    byte[] outBytes;
    try {
      outBytes = cipher.doFinal(inBytes);
    } catch (GeneralSecurityException e) {
      throw new ProcessingException("GeneralSecurityException", e, this);
    }
    
    // base64 encode after encrypt
    if (mode == Cipher.ENCRYPT_MODE && base64) {
      outBytes = Base64.encodeBase64(outBytes);
    }

    // if we got bytes in then return bytes, otherwise return a string
    if (data instanceof byte[]) {
      return new Object[] {outBytes};
    } else {
      return new Object[] {createString(outBytes)};
    }
  }

  public void validate(List exceptions) {
    if (key == null) {
      exceptions.add(new ValidationException("key property not set", this));
    }
    if (algorithm == null) {
      exceptions.add(new ValidationException("algorithm property not set", this));
    }
    if (mode == Integer.MIN_VALUE) {
      exceptions.add(new ValidationException("mode property not set", this));
    }
    
    if (exceptions.isEmpty()) {
      try {
        cipher = Cipher.getInstance(algorithm);
        cipher.init(mode, key, params);
      } catch (GeneralSecurityException e) {
        throw new ProcessingException("GeneralSecurityException", e, this);
      }
    }
  }

  protected String getAlgorithm() {
    return algorithm;
  }

  public void reset(Object context) {
  }
}
