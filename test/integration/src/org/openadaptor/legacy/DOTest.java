package org.openadaptor.legacy;
import junit.framework.TestCase;

//import org.openadaptor.dataobjects.InvalidParameterException;
//import org.openadaptor.dataobjects.SimpleDataObject;
//import org.openadaptor.legacy.converter.dataobjects.DataObjectToOrderedMapConvertor;


public abstract class DOTest extends TestCase {

  // no way to get this to compile in IDE without givin precedence to 
  // the legacy openadaptor jar which we do not want to do.
  
  /*
  public void test() throws InvalidParameterException {
    SimpleDataObject parent = new SimpleDataObject("Parent");
    parent.addAttributeValue("string", "foobar");
    parent.addAttributeValue("int", new Integer(10));
    parent.addAttributeValue("double", new Double(1.0));
    SimpleDataObject[] children = new SimpleDataObject[2];
    children[0] = new SimpleDataObject("Child");
    children[0].addAttributeValue("string", "one");
    children[1] = new SimpleDataObject("Child");
    children[1].addAttributeValue("string", "two");
    parent.addAttributeValue("do", children);
    
    DataObjectToOrderedMapConvertor converter = new DataObjectToOrderedMapConvertor();
    converter.process(parent);
  }
  */
}
