package systemA;

import InstrumentationPackage.*;
import MessagePackage.*;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

class MotionSensor
{

	private MessageManagerInterface managerInterface = null;	// Interface object to the message manager

	private MessageWindow mw;

	
	public MotionSensor(String title, float winPosX, float winPosY, final MessageManagerInterface em){
		managerInterface = em;
		mw = new MessageWindow(title, winPosX, winPosY);
		JButton button = new JButton();
		button.setText("Window Sensor");
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

		Message msg = new Message( Configuration.MOTION_SENSOR_ID, "MD" );
		// Here we send the message to the message manager.
		try
		{
			managerInterface.SendMessage( msg );
			mw.WriteMessage("Suspicious motion detected!");
		} // try
		catch (Exception e)
		{
			System.out.println( "Error Posting Motion detection:: " + e );
		} // catch

	}

	public static void main(String args[])
	{
		String MsgMgrIP;					// Message Manager IP address
		MessageManagerInterface em = null;
		MessageQueue eq = null;			// Message Queue
		Message Msg = null;				// Message object
		boolean done = false;
		int	Delay = 2500;				// The loop delay (2.5 seconds)


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
			float winPosX = 0.5f; 	//This is the X position of the message window in terms
			//of a percentage of the screen height
			float winPosY = 0.60f;	//This is the Y position of the message window in terms
			//of a percentage of the screen height

			MotionSensor ms = new MotionSensor("Motion Sensor", winPosX, winPosY, em);




			while (!done) {
				try
				{
					eq = em.GetMessageQueue();
				} // try

				catch( Exception e )
				{
					ms.mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();
					// If the message ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the message manager.

					if ( Msg.GetMessageId() == 99 )
					{
						done = true;

						try
						{
							em.UnRegister();

						} // try

						catch (Exception e)
						{
							ms.mw.WriteMessage("Error unregistering: " + e);

						} // catch

						ms.mw.WriteMessage("\n\nSimulation Stopped. \n");

					} // if
					try
					{
						Thread.sleep( Delay );

					} // try

					catch( Exception e )
					{
						ms.mw.WriteMessage("Sleep error:: " + e );

					} // catch
				} // for
			}


		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main

} // Motion Sensor