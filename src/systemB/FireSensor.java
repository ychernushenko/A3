package systemB;
/******************************************************************************************************************
* File:FireSensor.java
* Course: 17655
* Project: Assignment A3
* Versions:
*	1.0 March 2014 - Initial rewrite of original assignment 3.
*
* Description:
*
* This class simulates a fire sensor. It polls the message manager for messages corresponding to changes in state
* of Fire.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	float GetRandomNumber()
*	boolean CoinToss()
*   void PostFireState(MessageManagerInterface ei, float fireState )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import common.Configuration;
import common.Device;

class FireSensor
{
	
	private  MessageManagerInterface managerInterface = null;	// Interface object to the message manager

	private  MessageWindow mw;

	public FireSensor(String title, float winPosX, float winPosY, final MessageManagerInterface em) {
		managerInterface = em;
		mw = new MessageWindow(title, winPosX, winPosY);
		JButton button = new JButton();
		button.setText("Fire Sensor");
		button.setEnabled(true);
		button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonActionPerformed(evt);
			}
		});
		JFrame buttonFrame = new JFrame();
		buttonFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buttonFrame.setBounds(mw.GetX()+mw.Width()-200, mw.GetY()+mw.Height()-80, 50, 80);
		JPanel buttonPanel = new JPanel();
		buttonFrame.add(buttonPanel);
		buttonPanel.add(button);
		buttonFrame.setVisible(true);
	}

	private void buttonActionPerformed(ActionEvent evt) {
		Message msg = new Message( Configuration.FIRE_SENSOR_ID, "F1" );
		// Here we send the message to the message manager.
		try{
			managerInterface.SendMessage( msg );
			mw.WriteMessage("Fire Alarm Detected!");
		} // try
		catch (Exception e){
			System.out.println( "Error Posting Fire Alarm:: " + e );
		} // catch
	}
	
	public static void main(String args[])
	{
		String MsgMgrIP;				// Message Manager IP address
		MessageManagerInterface em = null;
		MessageQueue eq = null;			// Message Queue
		int	Delay = 2500;				// The loop delay (2.5 seconds)
		Message Msg = null;				// Message object
		boolean Done = false;			// Loop termination flag

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			// message manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is on the local machine

				em = new MessageManagerInterface();
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} else {

			// message manager is not on the local system

			MsgMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is NOT on the local machine

				em = new MessageManagerInterface( MsgMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.


		if (em != null)
		{

			// We create a message window. Note that we place this panel about 1/2 across
			// and 1/3 down the screen

			float WinPosX = 0.25f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.45f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height
			
			FireSensor fs = new FireSensor("Fire Sensor", WinPosX, WinPosY, em);

			fs.mw.WriteMessage("Registered with the message manager." );

	    	try
	    	{
	    		fs.mw.WriteMessage("   Participant id: " + em.GetMyId() );
	    		fs.mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
	    		fs.mw.WriteMessage("Error:: " + e);

			} // catch

	    	fs.mw.WriteMessage("\nInitializing Fire Simulation::" );
	    	
			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			fs.mw.WriteMessage("Beginning Simulation... ");

			Device device = new Device(Configuration.critical_period, "Fire Sensor", "Initiate fire alarm", em);
			device.start();

			while ( !Done ){
				// Get the message queue
				try{
					eq = em.GetMessageQueue();
				} // try

				catch( Exception e ){
					fs.mw.WriteMessage("Error getting message queue::" + e );
				} // catch

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ ){
					Msg = eq.GetMessage();

					if ( Msg.GetMessageId() == 99 ){
						Done = true;
						fs.mw.WriteMessage("Received End Message" );
						try{
							em.UnRegister();
						} // try
						catch (Exception e){
							fs.mw.WriteMessage("Error unregistering: " + e);
						} // catch
						fs.mw.WriteMessage("\n\nSimulation Stopped. \n");
					} // if
				} // for
				
				
				// Here we wait for a 2.5 seconds before we start the next sample

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					fs.mw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main
} // FireSensor