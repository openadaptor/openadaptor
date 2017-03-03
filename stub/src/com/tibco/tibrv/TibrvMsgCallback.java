package com.tibco.tibrv;

public interface TibrvMsgCallback {
  void onMsg(TibrvListener listener, TibrvMsg msg);
}
