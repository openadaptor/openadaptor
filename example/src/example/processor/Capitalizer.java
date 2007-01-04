package example.processor;

import java.util.List;

import org.oa3.core.IDataProcessor;

/**
 * This is a custom Processor
 *  
 * @author perryj
 *
 */
public class Capitalizer implements IDataProcessor {

  public Object[] process(Object data) {
    return new Object[] {data.toString().toUpperCase()};
  }

  public void reset(Object context) {
  }

  public void validate(List exceptions) {
  }
}
