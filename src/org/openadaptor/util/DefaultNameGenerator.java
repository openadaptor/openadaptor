/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.util;


/**
 * Trivial implementation of INameGenerator.
 * Generates automatic names with prefix and suffix.
 * Will generate names with increasing numeric component, unless
 * a partial name is supplied in which case it simple prepends 
 * and appends the prefix and suffix respectively.
 * 
 * @author higginse
 *
 */
public class DefaultNameGenerator implements INameGenerator{
  protected String prefix; //Holds prefix value.
  protected String suffix; //Holds suffix value.
  protected int nextKey=1;
  
  
  public void setPrefix(String prefix){
    if (prefix==null) {
      throw new RuntimeException("Null prefix value is not permitted - use empty String instead");
    }
    this.prefix=prefix;
  }
  public void setSuffix(String suffix){
    if (suffix==null) {
      throw new RuntimeException("Null suffix value is not permitted - use empty String instead");
    }
    this.suffix=suffix;
  }
  /**
   * Generates a name using monotonically increasing numeric
   * key value.
   * @return String containing generated name.
   */
  public synchronized String generateName() {
    return generateName(String.valueOf(nextKey++));
  }
  /**
   * Generates a name using prefix+partialName+suffix.
   * @param partialName Any non-null String value
   * @return String containing prefix+partialName+suffix
   */
  public String generateName(String partialName){
    if(partialName==null) {
      throw new RuntimeException("Null partialName value is not permitted");
    }    
    return prefix+partialName+suffix;
  }
  
  /**
   * Create a NameGenerator using supplied prefix and suffix
   * @param prefix
   * @param suffix
   */
  public DefaultNameGenerator(String prefix,String suffix) {
    setPrefix(prefix);
    setSuffix(suffix);
  }
  /**
   * Create a NameGenerator using supplied prefix, and default
   * Suffix (DEFAUTL_SUFFIX).
   * @param prefix
   */
  public DefaultNameGenerator(String prefix) {
    this(prefix,DEFAULT_SUFFIX);
  }
  /**
   * Create a NameGenerator using default prefix (DEFAULT_PREFIX)
   * and default suffix (DEFAULT_SUFFIX)
   *
   */
  public DefaultNameGenerator() {
    this(DEFAULT_PREFIX,DEFAULT_SUFFIX);
  }
}
