package systemA;

/******************************************************************************************************************
* File:ECSMonitor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class monitors the environmental control systems that control museum temperature and humidity. In addition to
* monitoring the temperature and humidity, the ECSMonitor also allows a user to set the humidity and temperature
* ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	static private void Heater(MessageManagerInterface ei, boolean ON )
*	static private void Chiller(MessageManagerInterface ei, boolean ON )
*	static private void Humidifier(MessageManagerInterface ei, boolean ON )
*	static private void Dehumidifier(MessageManagerInterface ei, boolean ON )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;

import java.util.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import common.Configuration;

class SecurityMonitor extends Thread
{
	private MessageManagerInterface em = null;	// Interface object to the message manager
	private String MsgMgrIP = null;				// Message Manager IP address
	boolean WindowBreak = false;				// Counter for window break
	boolean DoorBreak = false;					// Counter for Door break
	boolean MotionDetected = false;				// Counter for motion detected
	boolean Registered = true;					// Signifies that this class is registered with an message manager.
	MessageWindow mw = null;					// This is the message window
	boolean Armed = true;						// flag to disarm the system
	private JButton Close_win;
	private JButton Close_door;
	private JButton Close_motion;
	Indicator dr;
	Indicator wi;
	Indicator mo;
	
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
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
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
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		int MsgId = 0;					// User specified message ID
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag

		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the other
			
			// Initialize all buttons
			Close_win = new JButton();
			Close_win.setText("Shutdown window alarm");
			Close_win.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						WinbuttonActionPerformed(evt);
				}
			});
			Close_win.setEnabled(true);
			
			Close_door = new JButton();
			Close_door.setText("Shutdown door alarm");
			Close_door.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						DoorbuttonActionPerformed(evt);
				}
			});
			Close_door.setEnabled(true);
			
			Close_motion = new JButton();
			Close_motion.setText("Shutdown motion alarm");
			Close_motion.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						MotionbuttonActionPerformed(evt);
				}
			});
			Close_motion.setEnabled(true);
			
			JFrame frame1 = new JFrame("JAVA");
			frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame1.setSize(200,50);
			frame1.getContentPane().add(Close_win);
			frame1.setVisible(true);
			
			JFrame frame2 = new JFrame("JAVA");
			frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame2.setSize(200,50);
			frame2.getContentPane().add(Close_door);
			frame2.setVisible(true);
			
			JFrame frame3 = new JFrame("JAVA");
			frame3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame3.setSize(200,50);
			frame3.getContentPane().add(Close_motion);
			frame3.setVisible(true);
			
			mw = new MessageWindow("Security Monitoring Console", 0, 0);
			wi = new Indicator ("Window", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 1 );
			mo = new Indicator ("Motion", mw.GetX()+ mw.Width(), (int)(mw.Height()/3), 1 );
			dr = new Indicator ("Door", mw.GetX()+ mw.Width(), 0, 1 );

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
				// We are looking for MessageIDs = 1 or 2. Message IDs of 1 are temperature
				// readings from the temperature sensor; message IDs of 2 are humidity sensor
				// readings. Note that we get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most. If there are more, it is the last message
				// that will effect the status of the temperature and humidity controllers
				// as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();
					
					if( !Armed )
					{
						continue;
					}
					
					if ( Msg.GetMessageId() == Configuration.Door_break ) // door breaking
					{
						dr.SetLampColorAndMessage("DOOR", 3);
						mw.WriteMessage("Door Breaking has been discovered!" );
						DoorBreak = true;
					} // if

					if ( Msg.GetMessageId() == Configuration.Window_break ) // Window breaking
					{
						wi.SetLampColorAndMessage("WINDOW", 3);
						mw.WriteMessage("Window Breaking has been discovered!" );
						WindowBreak = true;
					} // if
					
					if ( Msg.GetMessageId() == Configuration.Motion_Detected ) // Motion
					{
						mo.SetLampColorAndMessage("MOTION", 3);
						mw.WriteMessage("Motion has been discovered!" );
						MotionDetected = true;
					} // if

					// If the message ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the message manager.

					if ( Msg.GetMessageId() == 100 )
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

				    	mw.WriteMessage( "\n\nSecurity Simulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

						dr.dispose();
						wi.dispose();
						mo.dispose();

					} // if

				} // for

				if( !DoorBreak )
				{
					dr.SetLampColorAndMessage("Door", 1);
				}
				
				if( !WindowBreak )
				{
					wi.SetLampColorAndMessage("Window", 1);
				}
				
				if( !MotionDetected )
				{
					mo.SetLampColorAndMessage("Motion", 1);
				}

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

		msg = new Message( (int) 100, "XXX" );

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
	
	public void ArmSystem()
	{
		Armed = true;
		mw.WriteMessage("   Status Changed: System Armed " );
		Message msg = new Message( Configuration.Arm, "Arm" );
		try
		{
			em.SendMessage( msg );
			//System.out.println( "Sent Temp Message" );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Message Arm System:: " + e );

		} // catch
	}
	
	public void Disarm()
	{
		Armed = false;
		mw.WriteMessage("   Status Changed: System Disarmed " );
		
		Message msg = new Message( Configuration.Disarm, "Disarm" );
		try
		{
			em.SendMessage( msg );
			//System.out.println( "Sent Temp Message" );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Message Arm System:: " + e );

		} // catch
	}
	
	private void WinbuttonActionPerformed(ActionEvent evt) {

		WindowBreak = false;

	}
	
	private void DoorbuttonActionPerformed(ActionEvent evt) {

		DoorBreak = false;
	}
	
	private void MotionbuttonActionPerformed(ActionEvent evt) {

		MotionDetected = false;

	}
} // ECSMonitor