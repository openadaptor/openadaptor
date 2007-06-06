package org.openadaptor.thirdparty.xstream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class XStreamConverterTestCase extends TestCase {

  public void test() {
    Person p = new Person();
    p.forename = "donald";
    p.surname = "duck";
    p.age = 55;
    p.insideLeg = 11;
    
    ObjectToXmlConverter processor = new ObjectToXmlConverter();
    processor.addAlias("person", p.getClass());
    processor.validate(new ArrayList());
    String xml = (String) processor.process(p)[0];
    
    XmlToObjectConverter proc2 = new XmlToObjectConverter();
    proc2.addAlias("person", p.getClass());
    proc2.validate(new ArrayList());
    Person p2 = (Person) proc2.process(xml)[0];
    
    assertTrue(p.equals(p2));
  }
  
  public void testMap() {
    Person p = new Person();
    p.forename = "donald";
    p.surname = "duck";
    p.age = 55;
    p.insideLeg = 11;
    
    ObjectToXmlConverter processor = new ObjectToXmlConverter();
    Map map = new HashMap();
    map.put("person", p.getClass());
    processor.setAliasMap(map);
    processor.validate(new ArrayList());
    String xml = (String) processor.process(p)[0];
    
    Map map2 = new HashMap();
    map2.put("person", p.getClass().getName());
    
    XmlToObjectConverter proc2 = new XmlToObjectConverter();
    proc2.setAliasMap(map2);
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
