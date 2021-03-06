package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.sensor.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.*;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.hardware.Button;

public class WallFollowingLab {

  // Parameters: adjust these for desired performance

  private static final int bandCenter = 20; // Offset from the wall (cm)
  private static final int bandWidth = 3; // Width of dead band (cm)
  private static final int motorLow = 130; // Speed of slower rotating wheel (deg/sec)
  private static final int motorHigh = 220; // Speed of the faster rotating wheel (deg/seec)

  // Ultrasonic sensor uses S1 port3
  private static final Port usPort = LocalEV3.get().getPort("S4");
  // The right motor uses A port
  // The left motor uses D port
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  // The motor that turns ultrasonic sensor uses B port
  public static final EV3MediumRegulatedMotor usMotor =
		  new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));
  
  public static void main(String[] args) {
    int option = 0;
    Printer.printMainMenu("Waiting for input"); // Set up the display on the EV3 screen
    // turn the Ultrasonic sensor 45 degress to the left to 
    // form a 45 degree angle with the wall
    //usMotor.rotateTo(45);
    
    while (option == 0) // and wait for a button press. The button
      option = Button.waitForAnyPress(); // ID (option) determines what type of control to use

    

    // Setup ultrasonic sensor
    // There are 4 steps involved:
    // 1. Create a port object attached to a physical port (done already above)
    // 2. Create a sensor instance and attach to port
    // 3. Create a sample provider instance for the above and initialize operating mode
    // 4. Create a buffer for the sensor data

    @SuppressWarnings("resource") // Because we don't bother to close this resource
    SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
    SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
                                                              // this instance
    float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are
                                                         // returned

    // Setup Printer
    // This thread prints status information in the background
    Printer printer = null;

    // Setup Ultrasonic Poller // This thread samples the US and invokes
    UltrasonicPoller usPoller = null; // the selected controller on each cycle
    
    // Setup controller objects

    // Depending on which button was pressed, invoke the US poller and printer with the
    // appropriate constructor.

    switch (option) {
      case Button.ID_LEFT: // Bang-bang control selected

    	BangBangController bangbangController =
    	        new BangBangController(bandCenter, bandWidth, motorLow, motorHigh);
        usPoller = new UltrasonicPoller(usDistance, usData, bangbangController);
        printer = new Printer(option, bangbangController);
        break;
      case Button.ID_RIGHT: // Proportional control selected
    	  PController pController = new PController(bandCenter, bandWidth);
        usPoller = new UltrasonicPoller(usDistance, usData, pController);
        printer = new Printer(option, pController);
        break;
      default:
        System.out.println("Error - invalid button"); // None of the above - abort
        System.exit(-1);
        break;
    }

    // Start the poller and printer threads
    usPoller.start();
    printer.start();

    // Wait here forever until button pressed to terminate wallfollower
    Button.waitForAnyPress();
    //Printer.updateLCD("Exiting System");
    System.exit(0);

  }
}