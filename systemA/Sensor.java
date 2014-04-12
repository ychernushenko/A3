package systemA;

import InstrumentationPackage.*;
import MessagePackage.*;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import common.Configuration;
import common.Device;

class Sensor{

	private  MessageManagerInterface managerInterface = null;	// Interface object to the message manager
	private  MessageWindow mw;

	private String name;
	private String description;
	private int messageID;
	private String messageText;
	public Sensor(String title, float winPosX, float winPosY, final MessageManagerInterface em, String nameStr, String des, int ID, String message) {
		name = nameStr;
		description = des;
		messageID = ID;
		managerInterface = em;
		messageText  = message;
		mw = new MessageWindow(title, winPosX, winPosY);
		JButton button = new JButton();
		button.setText(name);
		button.setEnabled(true);
		button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonActionPerformed(evt);
			}
		});
		JFrame buttonFrame = new JFrame();
		buttonFrame.setBounds(mw.GetX(), mw.GetY(), 50, 80);
		JPanel buttonPanel = new JPanel();
		buttonFrame.add(buttonPanel);
		buttonPanel.add(button);
		buttonFrame.setVisible(true);
	}

	private void buttonActionPerformed(ActionEvent evt) {
		Message msg = new Message( messageID, "**" );
		// Here we send the message to the message manager.
		try{
			managerInterface.SendMessage( msg );
			mw.WriteMessage(messageText);
		} // try
		catch (Exception e){
			System.out.println( "Error IN "+name+":: " + e );
		} // catch
	}

	public void execute(){
		MessageQueue eq = null;			// Message Queue
		Message Msg = null;				// Message object
		int	Delay = 2500;				// The loop delay (2.5 seconds)
		
		boolean done = false;
	

		if (managerInterface != null){
			float winPosX = 0.5f; 	//This is the X position of the message window in terms
			//of a percentage of the screen height
			float winPosY = 0.60f;	//This is the Y position of the message window in terms
			//of a percentage of the screen height

			//Sensor ds = new Sensor("Door Sensor", winPosX, winPosY, managerInterface, name, description, messageID, messageText);

			Device device = new Device(Configuration.normal_period, name, description, managerInterface);
			device.start();
			
			while ( !done ){
				// Get the message queue
				try{
					eq = managerInterface.GetMessageQueue();
				} // try

				catch( Exception e ){
					mw.WriteMessage("Error getting message queue::" + e );
				} // catch

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ ){
					Msg = eq.GetMessage();

					if ( Msg.GetMessageId() == 99 ){
						done = true;
						mw.WriteMessage("Received End Message" );
						try{
							managerInterface.UnRegister();
						} // try
						catch (Exception e){
							mw.WriteMessage("Error unregistering: " + e);
						} // catch
						mw.WriteMessage("\n\nSimulation Stopped. \n");
					} // if
				} // for


				// Here we wait for a 2.5 seconds before we start the next sample

				try{
					Thread.sleep( Delay );
				} // try

				catch( Exception e ){
					mw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while
		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if
	}
	
} // Sensor