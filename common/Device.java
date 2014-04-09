package common;

import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;

public class Device implements Runnable{

	private String description;
	private String deviceName;
	private int heartbeat;
	private MessageManagerInterface msgInterface;
	public Device(int heartbeat, String deviceName, String descrption){
		this.heartbeat = heartbeat;
		this.description = descrption;
		this.deviceName = deviceName;
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
