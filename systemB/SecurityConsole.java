package systemB;
/******************************************************************************************************************
* File:ECSConsole.java
* Course: 17655
* Project: Assignment 3
* Versions:
*	1.0 February 2014 - Initial rewrite of original assignment 3.
*
* Description: This class is the Security console for the museum environmental control system. This process consists of two
* threads. The SecurityMonitor object is a thread that is started that is responsible for the monitoring and control of
* the museum Security systems.
*
* Parameters: None
*
* Internal Methods: None
*
******************************************************************************************************************/
import common.Configuration;
import common.Device;

import TermioPackage.*;
import MessagePackage.*;

public class SecurityConsole
{
	public static void main(String args[])
	{
    	Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		Message Msg = null;					// Message object
		boolean Error = false;				// Error flag
		SecurityMonitor Monitor = null;			// The environmental control system monitor
                
		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length != 0 )
 		{
			// message manager is not on the local system

			Monitor = new SecurityMonitor( args[0] );

		} else {

			Monitor = new SecurityMonitor();

		} // if


		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

 		
		if (Monitor.IsRegistered() )
		{
			Monitor.start(); // Here we start the monitoring and control thread

			Device device = new Device(Configuration.normal_period, "Security console", "Fire alarm monitor will display fire alarms and let user enable sprinklers.");
			device.run();
			
			while (!Done)
			{
				// Here, the main thread continues and provides the main menu

				System.out.println( "\n\n\n\n" );
				System.out.println( "Security Console: \n" );

				if (args.length != 0)
					System.out.println( "Using message manger at: " + args[0] + "\n" );
				else
					System.out.println( "Using local message manger \n" );

				System.out.println( "Select an Option: \n" );
				System.out.println( "1: Set Fire Alarm off" );
				System.out.println( "2: Set Sprinkler off" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////

				if ( Option.equals( "1" ) )
				{

				} // if

				//////////// option 2 ////////////

				if ( Option.equals( "2" ) )
				{
				} // if
			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

  	} // main

} // ECSConsole
