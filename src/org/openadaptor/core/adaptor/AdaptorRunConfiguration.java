/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.adaptor;

import java.text.ParseException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.node.ReadNode;
import org.quartz.CronTrigger;

/**
 * This component can be used to control running an adaptor, it supports restart
 * on fail and scheduled stop, start and restart using cron expressions.
 * 
 * See <a href="http://www.opensymphony.com/quartz">quartz</a> documentation
 * for cron expression format
 * 
 * @author perryj
 * @see Adaptor
 */
public class AdaptorRunConfiguration {

  private static final long DEFAULT_RESTART_PAUSE = ReadNode.DEFAULT_TIMEOUT_MS + 1000;
  
  private static final Log log = LogFactory.getLog(AdaptorRunConfiguration.class);

  private int threadCount = 0;

  private int restartAfterFailLimit = -1;

  private long restartAfterFailDelayMs = 5 * 1000;

  private Timer timer;

  private String restartAfterFailCronExpression;

  private String stopCronExpression;

  private String startCronExpression;

  private String restartCronExpression;

  private int failCount = 0;

  private boolean exit = false;

  public void run(Adaptor adaptor) {
    if (startCronExpression == null) {
      start(adaptor);
    } else {
      scheduleTask(adaptor, startCronExpression, new StartTask(adaptor));
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

  private String checkCronExpression(String expression) {
    try {
      CronTrigger trigger = new CronTrigger();
      trigger.setCronExpression(expression);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    return expression;
  }

  /**
   * Called when stopping Adaptor via JMX console. 
   */
  public void setExitFlag() {
    if(timer!=null){
      timer.cancel();
    }
    exit = true;
    synchronized (this) {
      this.notify();
    }
  }

  private void start(Adaptor adaptor) {
    if (!exit) {
      Date stopTime = null;
      
      if (stopCronExpression != null) {
        stopTime = scheduleTask(adaptor, stopCronExpression, new StopTask(adaptor));
      } 
      
      if (restartCronExpression != null) {
        scheduleTask(adaptor, restartCronExpression, new RestartTask(adaptor), stopTime);
      }

      StartThread thread = new StartThread(adaptor);
      thread.start();
    }
  }

  private void stop(Adaptor adaptor) {
    adaptor.stop();
  }

  private void scheduleTask(Adaptor adaptor, long delayMs, TimerTask task) {
    if (timer == null) {
      timer = new Timer();
    }
    log.info("scheduled " + task.toString() + " for " + delayMs + "ms time");
    timer.schedule(task, delayMs);
  }

  private Date scheduleTask(Adaptor adaptor, String cronExpression, TimerTask task) {
    return scheduleTask(adaptor, cronExpression, task, new Date());
  }
  
  private Date scheduleTask(Adaptor adaptor, String cronExpression, TimerTask task, Date currentTime) {
    try {
      if (timer == null) {
        timer = new Timer();
      }
      CronTrigger trigger = new CronTrigger();
      trigger.setCronExpression(cronExpression);
      Date time = trigger.getFireTimeAfter(currentTime);
      log.info("scheduled " + task.toString() + " for " + time);
      timer.schedule(task, time);
      return time;
    } catch (Exception e) {
      throw new RuntimeException("failed to schedule task", e);
    }
  }

  /**
   * Thread that starts the adaptor configured in the run configuration.
   */
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
          if (restartAfterFailCronExpression != null) {
            scheduleTask(adaptor, restartAfterFailCronExpression, new StartTask(adaptor));
          } else {
            scheduleTask(adaptor, restartAfterFailDelayMs, new StartTask(adaptor));
          }
        } else {
          setExitFlag();
        }
      }

      // this is a scheduled stop so register start
      else if (startCronExpression != null) {
        scheduleTask(adaptor, startCronExpression, new StartTask(adaptor));
      }

      // there is no restart so set exit flag
      else if (restartCronExpression == null) {
        setExitFlag();
      }
    }
  }

  /**
   * Task for starting the adaptor (runs a few thread).
   */
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

  /**
   * Task for stopping the adaptor (runs a new thread). 
   */
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

  /**
   * Stops adaptor, waits for DEFAULT_RESTART_PAUSE ms and starts adaptor.
   * Runs a new thread.
   */
  public class RestartTask extends TimerTask {
    
    private Adaptor adaptor;

    RestartTask(final Adaptor adaptor) {
      this.adaptor = adaptor;
    }

    public void run() {
      Thread.currentThread().setName("timer");
      log.info("Stopping adaptor..");
      stop(adaptor);
      try {
        log.info("Adaptor stopped. Pausing for " + DEFAULT_RESTART_PAUSE + " ms.");
        Thread.sleep(DEFAULT_RESTART_PAUSE);
      } catch (InterruptedException e) {
        log.error("Thread.sleep() interrupted.", e);
      }
      log.info("Starting adaptor..");
      start(adaptor);
    }

    public String toString() {
      return "restart";
    }
  }
  
  public void setRestartAfterFailCronExpression(String expression) {
    restartAfterFailCronExpression = checkCronExpression(expression);
  }

  /**
   * Sets the delay for restarting the adaptor after it fails. 
   *
   * @param restartDelayMs -  the delay for restarting the adaptor after it fails. 
   */
  public void setRestartAfterFailDelayMs(long restartDelayMs) {
    this.restartAfterFailDelayMs = restartDelayMs;
  }

  /**
   * Sets the limit on the adaptor fail-restart attempts.
   * 
   * @param restartLimit how many times the adaptor will be restarted after it fails.
   */
  public void setRestartAfterFailLimit(int restartLimit) {
    this.restartAfterFailLimit = restartLimit;
  }

  /**
   * Cron expression for starting the adaptor.
   * 
   * @param expression cron expression for starting the adaptor.
   */
  public void setStartCronExpression(String expression) {
    startCronExpression = checkCronExpression(expression);
  }

  /**
   * Cron expression for stopping the adaptor.
   * 
   * @param expression cron expression for stopping the adaptor.
   */
  public void setStopCronExpression(String expression) {
    stopCronExpression = checkCronExpression(expression);
  }

  /**
   * Cron expression for restarting the adaptor.
   * 
   * @param expression cron expression for restarting the adaptor.
   */
  public void setRestartCronExpression(String expression) {
    restartCronExpression = checkCronExpression(expression);
  }
}
