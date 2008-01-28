import java.util.*;
import org.openadaptor.core.*;
import org.openadaptor.core.exception.*;

public class CustomBuySellValidator extends Component implements IDataProcessor {

  private String fieldName="buySell";
  private List legalValues=new ArrayList();
  private boolean discardBadValues=true;

  public void setFieldName(String fieldName) {
    this.fieldName=fieldName;
  }
  public String getFieldName() {
    return fieldName;
  }
  public void setLegalValues(List legalValues) {
    this.legalValues=legalValues;
  }
  public void setDiscardBadValues(boolean discard) {
    this.discardBadValues=discard;
  }

  public Object[] process(Object data) {
    Object[] output=new Object[]{};
    if (data instanceof Map) {
      Map map = (Map) data;
      Object value = map.get(fieldName);
      if (legalValues.contains(value)){
        output=new Object[]{data};
      }
      else { //Not legal - discard or exception!
        if (!discardBadValues) {
          throw new ProcessingException("Error: Illegal value for "+fieldName, this);
        }
      }
    }
    else {
      throw new RecordFormatException("Expected Map data, but got: "+data.getClass().getName());
    }
    return output;
  }

  public void reset(Object context) {}

  public void validate(List exceptions) {
    if (legalValues.isEmpty()) {
      exceptions.add(new ValidationException("legalValues property may not be empty", this));
    }
  }

}
