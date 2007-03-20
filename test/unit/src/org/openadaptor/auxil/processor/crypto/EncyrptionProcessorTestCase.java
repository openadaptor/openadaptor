package org.openadaptor.auxil.processor.crypto;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.openadaptor.auxil.processor.crypto.EncryptionProcessor;
import org.openadaptor.auxil.processor.crypto.PasswordBasedEncryptionProcessor;

import junit.framework.TestCase;


public class EncyrptionProcessorTestCase extends TestCase {

  public void test() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {
    KeyGenerator generator = KeyGenerator.getInstance("DES");
    SecretKey key = generator.generateKey();
    
    EncryptionProcessor encryptor = new EncryptionProcessor();
    encryptor.setKey(key);
    encryptor.setAlgorithm("DES");
    encryptor.setMode(Cipher.ENCRYPT_MODE);
    List exceptions = new ArrayList();
    encryptor.validate(exceptions);
    assertTrue(exceptions.isEmpty());
    
    EncryptionProcessor decryptor = new EncryptionProcessor();
    decryptor.setKey(key);
    decryptor.setAlgorithm("DES");
    decryptor.setMode(Cipher.DECRYPT_MODE);
    decryptor.validate(exceptions);
    assertTrue(exceptions.isEmpty());
    
    {
      String test = "mary had a little lamb";
      String encryptedString = (String) encryptor.process(test)[0];
      String decryptedString = (String) decryptor.process(encryptedString)[0];
      assertTrue(test.equals(decryptedString));
    }
    
    {
      String test = "its fleece was white as snow";
      byte[] encryptedBytes = (byte[]) encryptor.process(test.getBytes())[0];
      byte[] decryptedBytes = (byte[]) decryptor.process(encryptedBytes)[0];
      assertTrue(test.equals(new String(decryptedBytes)));
    }
  }
  
  public void testPBE() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {

    PasswordBasedEncryptionProcessor encryptor = new PasswordBasedEncryptionProcessor();
    encryptor.setPassword("foobar");
    encryptor.setAlgorithm("PBEWithMD5AndDES");
    encryptor.setMode(Cipher.ENCRYPT_MODE);
    List exceptions = new ArrayList();
    encryptor.validate(exceptions);
    assertTrue(exceptions.isEmpty());
    
    PasswordBasedEncryptionProcessor decryptor = new PasswordBasedEncryptionProcessor();
    decryptor.setPassword("foobar");
    decryptor.setAlgorithm("PBEWithMD5AndDES");
    decryptor.setMode(Cipher.DECRYPT_MODE);
    decryptor.validate(exceptions);
    assertTrue(exceptions.isEmpty());
    
    {
      String test = "mary had a little lamb";
      String encryptedString = (String) encryptor.process(test)[0];
      String decryptedString = (String) decryptor.process(encryptedString)[0];
      assertTrue(test.equals(decryptedString));
    }
    
    {
      String test = "its fleece was white as snow";
      byte[] encryptedBytes = (byte[]) encryptor.process(test.getBytes())[0];
      byte[] decryptedBytes = (byte[]) decryptor.process(encryptedBytes)[0];
      assertTrue(test.equals(new String(decryptedBytes)));
    }
  }
}
