/******************************************************************************************************************
* File:SecurityMonitor.java
* Course: 17655
* Project: Assignment A3
* Versions:
*	1.0 March 2014 - Initial rewrite of original assignment 3.
*
* Description:
*
* This class monitors the security control systems that control museum fire alarm. 
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	static private void FireAlarm(MessageManagerInterface ei, boolean ON )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.*;

class SecurityMonitor extends Thread
{
	private MessageManagerInterface em = null;	// Interface object to the message manager
	private String MsgMgrIP = null;				// Message Manager IP address
        private boolean FireAlarm = false;				// Signifies FireAlarm Status
	private boolean Sprinkler = false;				// Signifies Sprinkler Status
	boolean Registered = true;					// Signifies that this class is registered with an message manager.
	MessageWindow mw = null;					// This is the message window
	Indicator fi;								// FireAlarm indicator
        Indicator si;								// Sprinkler indicator

	public SecurityMonitor()
	{
		// message manager is on the local system

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is on the local machine

			em = new MessageManagerInterface();

		}

		catch (Exception e)
		{
			System.out.println("ECSMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public SecurityMonitor( String MsgIpAddress )
	{
		// message manager is not on the local system

		MsgMgrIP = MsgIpAddress;

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine

			em = new MessageManagerInterface( MsgMgrIP );
		}

		catch (Exception e)
		{
			System.out.println("ECSMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		int MsgId = 0;					// User specified message ID
		boolean CurrentFireAlarmState = false;	// Current FireAlarm state as reported by the sensor
		boolean CurrentSprinklerState= false;		// Current Sprinkler state as reported by the humidity sensor
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
		boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

		if (em != null)
		{
			// Now we create the Security Monitor status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the FireAlarm/Sprinkler is on/off.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("ECS Monitoring Console", 0, 0);
			fi = new Indicator ("FIRE_ALARM UNK", mw.GetX()+ mw.Width(), 0);
			si = new Indicator ("SPRINKLER UNK", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 2 );

			mw.WriteMessage( "Registered with the message manager." );

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

			while ( !Done )
			{
				// Here we get our message queue from the message manager

				try
				{
					eq = em.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 11 . Message IDs of 11 are FireAlarm
				// readings from the Fire sensor; Note that we get all the messages at once... 
                                //there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();

					if ( Msg.GetMessageId() == 11 ) // Temperature reading
					{
						try
						{
							CurrentFireAlarmState = true; // Would be needed additional check

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading temperature: " + e);

						} // catch

					} // if
                                        
                                        
                                        // Update Sprinkler State
                                        
                                        
					// If the message ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the message manager.

					if ( Msg.GetMessageId() == 99 )
					{
						Done = true;

						try
						{
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

						fi.dispose();
						si.dispose();

					} // if

				} // for

				mw.WriteMessage("FireAlarm State:: " + CurrentFireAlarmState + "Sprinkler State:: " + CurrentSprinklerState );

				// This delay slows down the sample rate to Delay milliseconds

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

	/***************************************************************************
	* CONCRETE METHOD:: IsRegistered
	* Purpose: This method returns the registered status
	*
	* Arguments: none
	*
	* Returns: boolean true if registered, false if not registered
	*
	* Exceptions: None
	*
	***************************************************************************/

	public boolean IsRegistered()
	{
		return( Registered );

	} // SetTemperatureRange

	/***************************************************************************
	* CONCRETE METHOD:: Halt
	* Purpose: This method posts an message that stops the environmental control
	*		   system.
	*
	* Arguments: none
	*
	* Returns: none
	*
	* Exceptions: Posting to message manager exception
	*
	***************************************************************************/

	public void Halt()
	{
		mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );

		// Here we create the stop message.

		Message msg;

		msg = new Message( (int) 99, "XXX" );

		// Here we send the message to the message manager.

		try
		{
			em.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

} // SecurityMonitor