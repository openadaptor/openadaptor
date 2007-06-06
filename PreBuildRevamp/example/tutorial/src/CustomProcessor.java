import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ValidationException;

public class CustomProcessor extends Component implements IDataProcessor {

  private String field;
  private Map map = new HashMap();
  
  public void setField(String field) {
    this.field = field;
  }
  
  public void setMap(Map map) {
    this.map.putAll(map);
  }
  
  public Object[] process(Object data) {
    if (data instanceof ISimpleRecord) {
      ISimpleRecord record = ((ISimpleRecord)data);
      Object key = record.get(field);
      Object value = map.get(key);
      if (value != null) {
        record.put(field, value);
        return new Object[] {record};
      } else {
        throw new ProcessingException("No mapping for " + field + "=" + key, this);
      }
    } else {
      throw new ProcessingException("data is not ISimpleRecord", this);
    }
  }

  public void reset(Object context) {
  }

  public void validate(List exceptions) {
    if (field == null) {
      exceptions.add(new ValidationException("field property not set", this));
    }
    if (map.isEmpty()) {
      exceptions.add(new ValidationException("map property is empty", this));
    }
  }

}
