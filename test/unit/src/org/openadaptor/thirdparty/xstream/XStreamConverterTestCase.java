package org.openadaptor.thirdparty.xstream;

import java.util.ArrayList;

import junit.framework.TestCase;

public class XStreamConverterTestCase extends TestCase {

  public void test() {
    Person p = new Person();
    p.forename = "donald";
    p.surname = "duck";
    p.age = 55;
    p.insideLeg = 11;
    
    Object2XmlConverter processor = new Object2XmlConverter();
    processor.addAlias("person", p.getClass());
    processor.validate(new ArrayList());
    String xml = (String) processor.process(p)[0];
    
    Xml2ObjectConverter proc2 = new Xml2ObjectConverter();
    proc2.addAlias("person", p.getClass());
    proc2.validate(new ArrayList());
    Person p2 = (Person) proc2.process(xml)[0];
    
    assertTrue(p.equals(p2));
  }
  
  public class Person {
    String forename;
    String surname;
    int age;
    int insideLeg;
    
    public boolean equals(Object o) {
      Person p = (Person) o;
      return forename.equals(p.forename)
      && surname.equals(p.surname)
      && age == p.age 
      && insideLeg == p.insideLeg;
    }
  }
}
