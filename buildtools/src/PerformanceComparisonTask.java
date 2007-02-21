package org.openadaptor.util.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PerformanceComparisonTask extends Task {

  private String target1;
  private String target2;
  private long expectedUpLift;
  private int iterations = 1;
  
  public void setExpectedUpLift(int percentage) {
    this.expectedUpLift = percentage;
  }
  
  public void setTarget1(String target1) {
    this.target1 = target1;
  }
  public void setIterations(int iterations) {
    this.iterations = iterations;
  }

  public void setTarget2(String target2) {
    this.target2 = target2;
  }
  
  public void execute() {
    long mean1 = timeExecution(target1, iterations);
    if (target2 != null) {
      long mean2 = timeExecution(target2, iterations);
      long diff = mean1 - mean2;
      int uplift = (int) (((double)diff / ((double)mean1)) * 100);
      log("mean diff = " + diff + ", uplift = " + uplift + "%");
      if (expectedUpLift != 0 && uplift < expectedUpLift) {
        throw new BuildException("% uplift in peformance is less than expected");
      }
    }
  }
  
  private long timeExecution(String target, int num) {
    long total = 0;
    for (int i = 0; i < num; i++) {
      long start = System.currentTimeMillis();
      getProject().executeTarget(target);
      long time = System.currentTimeMillis() - start;
      log("time to execute " + target + " was " + time + "ms");
      total += time;
    }
    return total / num;
  }
}
