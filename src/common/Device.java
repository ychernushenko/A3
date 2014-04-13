package common;

import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;

public class Device extends Thread{

	private String description;
	private String deviceName;
	private int heartbeat;
	private MessageManagerInterface msgInterface;
	public Device(int heartbeat, String deviceName, String descrption, MessageManagerInterface msgInterface){
		this.heartbeat = heartbeat;
		this.description = descrption;
		this.deviceName = deviceName;
		this.msgInterface = msgInterface;
	}


	@Override
	public void run() {
		while(true){
			Message msg = new Message(Configuration.HEARTBEAT, formatHeartBeat());
			try {
				msgInterface.SendMessage(msg);
			} catch (Exception e){
				System.out.println( "Error Posting Heartbeat:: " + e );
			} // catch
			try {
				Thread.sleep((long) heartbeat);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private String formatHeartBeat(){
		return deviceName+"|"+description;
	}
}
