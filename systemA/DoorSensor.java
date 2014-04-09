package systemA;

import InstrumentationPackage.*;
import MessagePackage.*;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import common.Configuration;
import common.Device;

class DoorSensor{

	private  MessageManagerInterface managerInterface = null;	// Interface object to the message manager

	private  MessageWindow mw;

	public DoorSensor(String title, float winPosX, float winPosY, final MessageManagerInterface em) {
		
		managerInterface = em;
		mw = new MessageWindow(title, winPosX, winPosY);
		JButton button = new JButton();
		button.setText("Door Sensor");
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
		Message msg = new Message( Configuration.DOOR_SENSOR_ID, "DB" );
		// Here we send the message to the message manager.
		try{
			managerInterface.SendMessage( msg );
			mw.WriteMessage("Door break detected!");
		} // try
		catch (Exception e){
			System.out.println( "Error Posting Door Break:: " + e );
		} // catch
	}

	public static void main(String args[]){
		String MsgMgrIP;					// Message Manager IP address
		MessageManagerInterface em = null;
		MessageQueue eq = null;			// Message Queue
		Message Msg = null;				// Message object
		int	Delay = 2500;				// The loop delay (2.5 seconds)
		
		boolean done = false;
		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

		if ( args.length == 0 ){
			// message manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try{
				// Here we create an message manager interface object. This assumes
				// that the message manager is on the local machine

				em = new MessageManagerInterface();
			}

			catch (Exception e){
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} else {

			// message manager is not on the local system

			MsgMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP );

			try{
				// Here we create an message manager interface object. This assumes
				// that the message manager is NOT on the local machine

				em = new MessageManagerInterface( MsgMgrIP );
			}

			catch (Exception e){
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (em != null){
			float winPosX = 0.5f; 	//This is the X position of the message window in terms
			//of a percentage of the screen height
			float winPosY = 0.60f;	//This is the Y position of the message window in terms
			//of a percentage of the screen height

			DoorSensor ds = new DoorSensor("Door Sensor", winPosX, winPosY, em);

			Device device = new Device(Configuration.normal_period, "Door Sensor", "Door sensor will alarm if any door break is detect.");
			device.run();
			
			while ( !done ){
				// Get the message queue
				try{
					eq = em.GetMessageQueue();
				} // try

				catch( Exception e ){
					ds.mw.WriteMessage("Error getting message queue::" + e );
				} // catch

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ ){
					Msg = eq.GetMessage();

					if ( Msg.GetMessageId() == 99 ){
						done = true;
						ds.mw.WriteMessage("Received End Message" );
						try{
							em.UnRegister();
						} // try
						catch (Exception e){
							ds.mw.WriteMessage("Error unregistering: " + e);
						} // catch
						ds.mw.WriteMessage("\n\nSimulation Stopped. \n");
					} // if
				} // for


				// Here we wait for a 2.5 seconds before we start the next sample

				try{
					Thread.sleep( Delay );
				} // try

				catch( Exception e ){
					ds.mw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while
		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main

} // Door Sensor