import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.ValidationException;

public class CustomProcessor2 extends Component implements IDataProcessor {

  private boolean firstError = true;
  
  public Object[] process(Object data) {
    if (data instanceof Map) {
      Map map = (Map) data;
      Object obj = map.get("buySell");
      if("?".equals(obj.toString())){
        if(firstError){
          firstError = false;
          throw new ProcessingException("Error: Buy/Sell not selected", this);
        }
        else{
          throw new RecordException("Error: Buy/Sell not selected");
        }
      }
    } else {
      throw new ProcessingException("data is not a Map", this);
    }
    return new Object[]{data};
  }

  public void reset(Object context) {}

  public void validate(List exceptions) {}

}
