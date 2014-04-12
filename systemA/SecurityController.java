package systemA;
/******************************************************************************************************************
 * File:HumidityController.java
 * Course: 17655
 * Project: Assignment A3
 * Copyright: Copyright (c) 2009 Carnegie Mellon University
 * Versions:
 *	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
 *
 * Description:
 *
 * This class simulates a device that controls a humidifier and dehumidifier. It polls the message manager for message
 * ids = 4 and reacts to them by turning on or off the humidifier/dehumidifier. The following command are valid
 * strings for controlling the humidifier and dehumidifier:
 *
 *	H1 = humidifier on
 *	H0 = humidifier off
 *	D1 = dehumidifier on
 *	D0 = dehumidifier off
 *
 * The state (on/off) is graphically displayed on the terminal in the indicator. Command messages are displayed in
 * the message window. Once a valid command is recieved a confirmation message is sent with the id of -5 and the command in
 * the command string.
 *
 * Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
 * on the local machine.
 *
 * Internal Methods:
 *	static private void ConfirmMessage(MessageManagerInterface ei, String m )
 *
 ******************************************************************************************************************/
import common.Configuration;
import common.Device;
import InstrumentationPackage.*;
import MessagePackage.*;

class SecurityController
{
	public static void main(String args[])
	{
		String MsgMgrIP;					// Message Manager IP address
		Message Msg = null;					// Message object
		MessageQueue eq = null;				// Message Queue
		MessageManagerInterface em = null;	// Interface object to the message manager
		boolean armed = true;				// state: false == off, true == on
		int	Delay = 2500;					// The loop delay (2.5 seconds)
		boolean Done = false;				// Loop termination flag

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

		// Here we check to see if registration worked. If em is null then the
		// message manager interface was not properly created.

		if (em != null)
		{
			System.out.println("Registered with the message manager." );

			/* Now we create the humidity control status and message panel
			 ** We put this panel about 2/3s the way down the terminal, aligned to the left
			 ** of the terminal. The status indicators are placed directly under this panel
			 */

			float WinPosX = 0.0f; 	//This is the X position of the message window in terms
			//of a percentage of the screen height
			float WinPosY = 0.60f;	//This is the Y position of the message window in terms
			//of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Security Controller Status Console", WinPosX, WinPosY);

			// Now we put the indicators directly under the humitity status and control panel

			Indicator hi = new Indicator ("Disarmed", mw.GetX(), mw.GetY()+mw.Height());

			mw.WriteMessage("Registered with the message manager." );

			try
			{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

			catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			 ** Here we start the main simulation loop
			 *********************************************************************/

			Device device = new Device(Configuration.normal_period, "Security Controller", "Security controller will redirect the security messages if system is armed.", em);
			device.start();
			
			
			while ( !Done )
			{
				try
				{
					eq = em.GetMessageQueue();
				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();

					switch (Msg.GetMessageId()) {
					case Configuration.DOOR_SENSOR_ID:
						mw.WriteMessage("Get door breaking message!" );
						postMessage(em, "Door");
						break;
					case Configuration.WINDOW_SENSOR_ID:
						mw.WriteMessage("Get window breaking message!" );
						postMessage(em, "Window");
						break;
					case Configuration.MOTION_SENSOR_ID:
						mw.WriteMessage("Get motion detected message!" );
						postMessage(em, "Motion");
						break;
					case Configuration.Arm:
						mw.WriteMessage("Arm System message!" );
						armed = true;
						break;
					case Configuration.Disarm:
						mw.WriteMessage("Get Disarm System message!" );
						armed = false;
						break;
					case 99:
						// If the message ID == 99 then this is a signal that the simulation
						// is to end. At this point, the loop termination flag is set to
						// true and this process unregisters from the message manager.
						if ( Msg.GetMessageId() == 99 ){
							Done = true;
							try{
								em.UnRegister();
							}catch (Exception e){
								mw.WriteMessage("Error unregistering: " + e);
							} // catch
							mw.WriteMessage( "\n\nSimulation Stopped. \n");
							hi.dispose();
						} // if
					default:
						break;
					}//switch
				} // for

				// Update the lamp status

				if (armed){
					// Set to green, humidifier is on
					hi.SetLampColorAndMessage("SECURITY ON", 1);
				} else {

					// Set to black, humidifier is off
					hi.SetLampColorAndMessage("SECURITY OFF", 0);

				} // if
				try
				{
					Thread.sleep( Delay );
				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );
				} // catch

			} // while

		} else {
			System.out.println("Unable to register with the message manager.\n\n" );
		} // if

	} // main


	static private void postMessage(MessageManagerInterface ei, String m ){
		// Here we create the message.
		Message msg;
		if( m.equals("Door") )
		{
			msg = new Message( Configuration.Door_break, m );
		}
		else if( m.equals("Window") )
		{
			msg = new Message( Configuration.Window_break, m );
		}
		else
		{
			msg = new Message( Configuration.Motion_Detected , m );
		}
		// Here we send the message to the message manager.
		try{
			ei.SendMessage( msg );
		} // try
		catch (Exception e){
			System.out.println("Error posting Message:: " + e);
		} // catch
	} // PostMessage

} // HumidityControllers