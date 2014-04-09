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
import java.util.*;

class FireSensor
{
	public static void main(String args[])
	{
		String MsgMgrIP;				// Message Manager IP address
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		int MsgId = 0;					// User specified message ID
		MessageManagerInterface em = null;// Interface object to the message manager
		boolean HeaterState = false;	// Heater state: false == off, true == on
		boolean ChillerState = false;	// Chiller state: false == off, true == on
		boolean CurrentFireState;		// Current fire State
		int	Delay = 2500;				// The loop delay (2.5 seconds)
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

			float WinPosX = 0.5f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow fw = new MessageWindow("Fire Sensor", WinPosX, WinPosY );

			fw.WriteMessage("Registered with the message manager." );

	    	try
	    	{
				fw.WriteMessage("   Participant id: " + em.GetMyId() );
				fw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				fw.WriteMessage("Error:: " + e);

			} // catch

			fw.WriteMessage("\nInitializing Fire Simulation::" );

			CurrentFireState = CoinToss();

			fw.WriteMessage("   Current Fire State:: " + CurrentFireState );
			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			fw.WriteMessage("Beginning Simulation... ");


			while ( !Done )
			{
				// Post the current FireState

				PostFireState( em, CurrentFireState );

				fw.WriteMessage("Current Fire State::  " + CurrentFireState);

				// Get the message queue

				try
				{
					eq = em.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					fw.WriteMessage("Error getting message queue::" + e );

				} // catch

				// Here we wait for a 2.5 seconds before we start the next sample

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					fw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main

	/***************************************************************************
	* CONCRETE METHOD:: CoinToss
	* Purpose: This method provides a random true or false value used for
	* determining tfire state
	*
	* Arguments: None.
	*
	* Returns: boolean
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private boolean CoinToss()
	{
		Random r = new Random();

		return(r.nextBoolean());

	} // CoinToss

	/***************************************************************************
	* CONCRETE METHOD:: PostFireState
	* Purpose: This method posts the specified Fire State value to the
	* specified message manager. This method assumes an message ID of 11.
	*
	* Arguments: MessageManagerInterface ei - this is the messagemanger interface
	*			 where the message will be posted.
	*
	*			 float temperature - this is the temp value.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void PostFireState(MessageManagerInterface ei, boolean fireState )
	{
		// Here we create the message.

		Message msg = new Message( (int) 11, String.valueOf(fireState) );

		// Here we send the message to the message manager.

		try
		{
			ei.SendMessage( msg );
			//System.out.println( "Sent Temp Message" );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Fire State:: " + e );

		} // catch

	} // PostFireState

} // FireSensor