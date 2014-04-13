%ECHO OFF
%ECHO Starting Security System
PAUSE
%ECHO ECS Monitoring Console
START "MUSEUM ENVIRONMENTAL CONTROL SYSTEM CONSOLE" /NORMAL java ECSConsole %1
%ECHO Starting Temperature Controller Console
START "TEMPERATURE CONTROLLER CONSOLE" /MIN /NORMAL java TemperatureController %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY CONTROLLER CONSOLE" /MIN /NORMAL java HumidityController %1
START "TEMPERATURE SENSOR CONSOLE" /MIN /NORMAL java TemperatureSensor %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY SENSOR CONSOLE" /MIN /NORMAL java HumiditySensor %1


%ECHO Starting Security Console
START "MUSEUM SECURITY CONSOLE" /NORMAL java systemA/SecurityConsole %1
%ECHO Starting Security Controller Console
START "SECURITY CONTROLLER" /MIN /NORMAL java systemA/SecurityController %1
%ECHO Starting Door Sensor Console
START "DOOR SENSOR CONSOLE" /MIN /NORMAL java systemA/DoorSensor %1
%ECHO Starting Window Sensor Console
START "WINDOW SENSOR CONSOLE" /MIN /NORMAL java systemA/WindowSensor %1
%ECHO Starting Motion Sensor Console
START "MOTION SENSOR CONSOLE" /MIN /NORMAL java systemA/MotionSensor %1