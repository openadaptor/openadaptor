package org.oa3.core.adaptor;

import java.text.ParseException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.Trigger;

/**
 * This component can be used to control running an adaptor, if support restart on fail
 * and scheduled stop, start and restart using cron expressions.
 * 
 * @author perryj
 * @see Adaptor
 */
public class AdaptorRunConfiguration {

  private static final Log log = LogFactory.getLog(AdaptorRunConfiguration.class);

  private int threadCount = 0;

  private int restartAfterFailLimit = -1;

  private long restartAfterFailDelayMs = 5 * 1000;

  private CronTrigger restartAfterFailTrigger;

  private Timer timer;

  private CronTrigger stopTrigger;

  private CronTrigger startTrigger;

  private CronTrigger restartTrigger;

  private int failCount = 0;

  private boolean exit = false;

  public void setRestartAfterFailCronExpression(String cronExpression) {
    try {
      restartAfterFailTrigger = new CronTrigger();
      restartAfterFailTrigger.setCronExpression(cronExpression);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public void setRestartAfterFailDelayMs(long restartDelayMs) {
    this.restartAfterFailDelayMs = restartDelayMs;
  }

  public void setRestartAfterFailLimit(int restartLimit) {
    this.restartAfterFailLimit = restartLimit;
  }

  public void setStartCronExpression(String cronExpression) {
    try {
      startTrigger = new CronTrigger();
      startTrigger.setCronExpression(cronExpression);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public void setStopCronExpression(String cronExpression) {
    try {
      stopTrigger = new CronTrigger();
      stopTrigger.setCronExpression(cronExpression);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public void setRestartCronExpression(String cronExpression) {
    try {
      restartTrigger = new CronTrigger();
      restartTrigger.setCronExpression(cronExpression);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public void run(Adaptor adaptor) {
    if (startTrigger == null) {
      start(adaptor);
    } else {
      scheduleTask(adaptor, startTrigger, new StartTask(adaptor));
    }
    waitForExit();
  }

  private void waitForExit() {
    while (!exit) {
      synchronized (this) {
        try {
          this.wait();
        } catch (InterruptedException e) {
        }
      }
    }
    log.info("AdaptorRunConfiguration exiting");
  }

  public void setExitFlag() {
    timer.cancel();
    exit = true;
    synchronized (this) {
      this.notify();
    }
  }

  private void start(Adaptor adaptor) {
    if (!exit) {

      Date stopTime = null;
      
      if (stopTrigger != null) {
        stopTime = scheduleTask(adaptor, stopTrigger, new StopTask(adaptor));
      } 
      
      if (restartTrigger != null) {
        scheduleTask(adaptor, restartTrigger, new RestartTask(adaptor), stopTime);
      }

      StartThread thread = new StartThread(adaptor);
      thread.start();
    }
  }

  private void stop(Adaptor adaptor) {
    adaptor.stop();
  }

  private void scheduleTask(Adaptor adaptor, long delayMs, StartTask task) {
    if (timer == null) {
      timer = new Timer();
    }
    log.info("scheduled " + task.toString() + " for " + delayMs + "ms time");
    timer.schedule(task, delayMs);
  }

  private Date scheduleTask(Adaptor adaptor, Trigger trigger, TimerTask task) {
    return scheduleTask(adaptor, trigger, task, new Date());
  }
  
  private Date scheduleTask(Adaptor adaptor, Trigger trigger, TimerTask task, Date currentTime) {
    if (timer == null) {
      timer = new Timer();
    }
    Date time = trigger.getFireTimeAfter(currentTime);
    log.info("scheduled " + task.toString() + " for " + time);
    timer.schedule(task, time);
    return time;
  }

  public class StartThread extends Thread {

    private Adaptor adaptor;

    public StartThread(Adaptor adaptor) {
      this.adaptor = adaptor;
      setName("run" + (++threadCount));
    }

    public void run() {

      try {
        adaptor.start();
      } catch (Exception ex) {
        log.error("uncaught exception", ex);
      }

      // adaptor failed
      if (adaptor.getExitCode() != 0) {
        failCount++;
        if (restartAfterFailLimit == 0 || failCount < restartAfterFailLimit) {
          if (restartAfterFailTrigger != null) {
            scheduleTask(adaptor, restartAfterFailTrigger, new StartTask(adaptor));
          } else {
            scheduleTask(adaptor, restartAfterFailDelayMs, new StartTask(adaptor));
          }
        } else {
          setExitFlag();
        }
      }

      // this is a scheduled stop so register start
      else if (startTrigger != null) {
        scheduleTask(adaptor, startTrigger, new StartTask(adaptor));
      }

      // there is no restart so set exit flag
      else if (restartTrigger == null) {
        setExitFlag();
      }
    }
  }

  public class StartTask extends TimerTask {
    private Adaptor adaptor;

    StartTask(final Adaptor adaptor) {
      this.adaptor = adaptor;
    }

    public void run() {
      Thread.currentThread().setName("timer");
      log.info("start task fired");
      start(adaptor);
    }

    public String toString() {
      return "start";
    }
  }

  public class StopTask extends TimerTask {
    private Adaptor adaptor;

    StopTask(final Adaptor adaptor) {
      this.adaptor = adaptor;
    }

    public void run() {
      Thread.currentThread().setName("timer");
      log.info("stop task fired");
      stop(adaptor);
    }

    public String toString() {
      return "stop";
    }
  }

  public class RestartTask extends TimerTask {
    private Adaptor adaptor;

    RestartTask(final Adaptor adaptor) {
      this.adaptor = adaptor;
    }

    public void run() {
      Thread.currentThread().setName("timer");
      log.info("restart task firing");
      stop(adaptor);
      start(adaptor);
    }

    public String toString() {
      return "restart";
    }
  }
}
