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
