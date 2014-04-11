package common;

public class Configuration {
	// Message IDs for heart beat message.
	public final  static  int critical_period   = 1000;
	public final  static  int normal_period = 2000;
	public final  static  int HEARTBEAT = 2014;



	// Message IDs for system A.
	public final  static  int SECURITY_MONITOR_ID  = 9000;
	public final  static  int SECURITY_CONTROLLER_ID  = 9001;
	public final  static  int DOOR_SENSOR_ID  = 9002;
	public final  static  int WINDOW_SENSOR_ID  = 9003;
	public final  static  int MOTION_SENSOR_ID  = 9004;

	public final  static  int Window_break = 9101;
	public final  static  int Door_break = 9102;
	public final  static  int Motion_Detected = 9103;
	
	public final  static  int Arm = 9111;
	public final  static  int Disarm = 9222;

	//Message IDs for system B.
	
	public final  static  int FIRE_CONTROLLER_ID = 8000;
	public final  static  int SPRINKLER_CONTROLLER_ID = 8100;
	
	public final  static  int FIRE_SENSOR_ID = 8200;
	
	public final  static  int Fire_detected = 8001;
	
	
	//Message IDs for system C.
	

}
