package de.peeeq.wurstscript.gui;

public class WurstGuiLogger extends WurstGui {

  @Override
  public void sendProgress(String whatsRunningNow) {
    // ignore
  }

  @Override
  public void sendFinished() {
    // ignore
  }

  @Override
  public void showInfoMessage(String message) {
    System.out.println(message);
  }
}
