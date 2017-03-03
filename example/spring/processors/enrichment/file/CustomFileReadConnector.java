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

import java.io.IOException;
import org.openadaptor.auxil.connector.iostream.reader.FileReadConnector;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IEnrichmentReadConnector;
import org.openadaptor.core.exception.ConnectionException;

/**
 * A custom file reader, based on FileReadConnector.
 */
public class CustomFileReadConnector extends FileReadConnector implements IEnrichmentReadConnector {

  private static String [] FIELD_NAMES = new String[]{"TradeId", "BuyOrSell", "Quantity", "Price"};

  /**
   * Finds a row with a trade id equal to <code>tradeId</code>.
   * If found, splits the data into fields.
   * 
   * @see IReadConnector#next(long)
   */
  public Object[] next(IOrderedMap inputParameters, long timeoutMs) {
    String [] fields = new String[0];
    boolean found = false;
    String tradeId = (String) inputParameters.get(0);

    /* Find the relevant record */
    try {
      while(!found){
        Object data = dataReader.read();
        if (data != null) {
          String dataS = (String) data;
          fields = dataS.split(",");
          if(fields[0].equals(tradeId)){
            found = true;
          }
        } else {
          break;
        }
      }
    } catch (IOException e) {
      throw new ConnectionException("IOException, " + e.getMessage(), e, this);
    }

    /* Convert string array to ordred map */
    IOrderedMap fieldsOM = new OrderedHashMap();
    for(int i=0; i<fields.length; i++){
      fieldsOM.put(FIELD_NAMES[i], fields[i]);
    }

    /* By closing and reopening the input file, reset the input stream to the beginning of the file */
    disconnect();
    connect();

    return new Object[]{fieldsOM};
  }

}
