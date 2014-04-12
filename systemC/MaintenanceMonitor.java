package systemC;

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

import java.sql.Timestamp;

class MaintenanceMonitor extends Thread
{
	private MessageManagerInterface em = null;	// Interface object to the message manager
	private String MsgMgrIP = null;				// Message Manager IP address
	boolean Registered = true;					// Signifies that this class is registered with an message manager.
	MessageWindow mw = null;					// This is the message window
	MessageWindow deviceList = null;					// This is the message window
	private ArrayList<Indicator> indicators;
	private ArrayList<Boolean> status;
	private int deviceNumber = 0;
	private Hashtable<String,Integer> map = new Hashtable<String,Integer>();
	private Hashtable<String,Long> DeviceTime = new Hashtable<String,Long>();
	
	public MaintenanceMonitor()
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

	public MaintenanceMonitor( String MsgIpAddress )
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

	class Checker implements Runnable
	{
		@Override
		public void run() {
			long currentTime;
			Iterator<Map.Entry<String, Long>> it;
			Map.Entry<String, Long> entry;
			String DeviceName;
			int DeviceIndex;
			
			while(true){
				currentTime = System.currentTimeMillis();
				
				//all the devices in the system
				it = DeviceTime.entrySet().iterator();
				
				while (it.hasNext()) {
					  entry = it.next();
					  if( entry.getValue()-currentTime > 10000 )
					  {
						  //Device is down
						  DeviceName = entry.getKey();
						  DeviceIndex = map.get(DeviceName);
						  Indicator in = indicators.get(DeviceIndex);
						  if( status.get(DeviceIndex) )
						  {
							  in.SetLampColorAndMessage(DeviceName, 3);
						  	  mw.WriteMessage("Device "+DeviceName+" is down!" );
						  	  status.set(DeviceIndex,false);
					  	  }
				      }
					  else
					  {
						  DeviceName = entry.getKey();
						  DeviceIndex = map.get(DeviceName);
						  if( !status.get(DeviceIndex) )
						  {
							  //device is back
							  Indicator in = indicators.get(DeviceIndex);
							  in.SetLampColorAndMessage(DeviceName, 1);
						  	  mw.WriteMessage("Device "+DeviceName+" is back online!" );
						  	  status.set(DeviceIndex,true);
					  	  }
					  }
				}
				
				try{
					Thread.sleep(10000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}		
		}
	}
	
	public void run()
	{
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		int MsgId = 0;					// User specified message ID
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		String DeviceName;				// name of the device
		String description;				// Description of the device
		String[] parts;					// used to get device name and description

		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the other
			
			// Initialize all buttons
		
			mw = new MessageWindow("Maintenance Monitoring Console", 0, 0);
			deviceList = new MessageWindow("Device List", 500, 500);
			mw.WriteMessage( "Registered with the message manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );
				deviceList.WriteMessage("  Current Devices List  ");

			} // try

	    	catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/
	    	
	    	Checker deviceCheck = new Checker();
	    	deviceCheck.run();
	    	
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
					
					if ( Msg.GetMessageId() == Configuration.HEARTBEAT ) // recieved a heartbeat
					{
						 parts = Msg.GetMessage().split("|");
						 DeviceName = parts[0];
						 description = parts[1];
						 
						 if( !map.contains(DeviceName) )
						 {
							 CreateDevice(DeviceName);
							 
							 try
						    {
								mw.WriteMessage("  Device:" + DeviceName + "Registered");
								deviceList.WriteMessage("  Device Name:" + DeviceName + "::Description:" + description);

							} // try

						    catch (Exception e)
							{
								System.out.println("Error:: " + e);

							} // catch
						 }
						 else
						 {
							 DeviceTime.put(DeviceName, System.currentTimeMillis());
						 }
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

						for( int j=0;j<indicators.size();j++ )
						{
							indicators.get(i).dispose();
						}

					} // if

				} // for

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
	
	void CreateDevice( String DeviceName )
	{
		if( map.containsKey(DeviceName) )
		{
			return;
		}
		
		int newIndex = deviceNumber;
		Indicator newIn = new Indicator(DeviceName, mw.GetX()+ mw.Width(), (int)(deviceNumber*mw.Height()/2), 1);
		indicators.add(newIndex, newIn);
		status.add(newIndex, true);
		map.put(DeviceName, newIndex);
		DeviceTime.put(DeviceName, System.currentTimeMillis());
		
		deviceNumber++;
	}
} // ECSMonitor