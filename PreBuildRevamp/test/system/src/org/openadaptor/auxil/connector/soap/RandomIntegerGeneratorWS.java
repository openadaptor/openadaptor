package org.openadaptor.auxil.connector.soap;

public class RandomIntegerGeneratorWS implements IRandomIntegerGeneratorWS {

  public Integer getInt() {
    Integer rndInt = null;
    rndInt = new Integer((int)(Math.random() * 100));
    return rndInt;
  }

}
