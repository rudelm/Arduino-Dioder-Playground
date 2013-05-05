/**
 * 
 */
package net.centurio.java.dioder;

import java.util.Random;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Java Sample project to test serial communication with Arduino Mega over Serial
 * connection.
 * @author rudelm
 * 
 */
public class Main {

	public static SerialPort serialPort = null;
	public static int HeaderFirstByte = 0xBA;
	public static int HeaderSecondByte = 0xBE;
	public static int CorrectMessageLength = 15;
	public static int CorrectMessageBodyLength = 12;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: hard coded, could be read from arguments or from list of available ports
		serialPort = new SerialPort("/dev/tty.usbmodem26211");
		try {
			System.out.println("Port opened: " + serialPort.openPort());
			System.out.println("Params setted: "
					+ serialPort.setParams(
							SerialPort.BAUDRATE_57600,
							SerialPort.DATABITS_8, 
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE));

			// Preparing a mask. In a mask, we need to specify the types of
			// events that we want to track.
			// Well, for example, we need to know what came some data, thus in
			// the mask must have the
			// following value: MASK_RXCHAR. If we, for example, still need to
			// know about changes in states
			// of lines CTS and DSR, the mask has to look like this:
			// SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS +
			// SerialPort.MASK_DSR
			int mask = SerialPort.MASK_RXCHAR; // + SerialPort.MASK_CTS +
												// SerialPort.MASK_DSR;//Prepare
												// mask
			// Set the prepared mask
			serialPort.setEventsMask(mask);
			// Add an interface through which we will receive information about
			// events
			serialPort.addEventListener(new SerialPortReader());

			// The Arduino needs time after the first contact to initialize
			// itself
			Thread.sleep(1000);

			// send 100 random commands to simulate the effect
			for (int i = 0; i < 100; i++) {
				byte[] messageBody = new byte[CorrectMessageBodyLength];
				new Random().nextBytes(messageBody);
				byte checkSum = 0;
				for (int l = 0; l < CorrectMessageBodyLength; l++) {
					// bitwise XOR of byte values
					checkSum ^= messageBody[l];
				}
				// create a correct message with all values
				byte[] message = new byte[CorrectMessageLength];
				// Assign Header
				message[0] = (byte) HeaderFirstByte;
				message[1] = (byte) HeaderSecondByte;
				for (int m = 2; m < message.length - 1; m++) {
					// assign the 12 bytes from the messageBody
					message[m] = (byte) messageBody[m - 2];
				}
				// add the checkSum
				message[14] = (byte) checkSum;
				System.out.println("Message successfully writen to port: "
						+ serialPort.writeBytes(message));
				// wait 25ms for the response before next message will be send
				Thread.sleep(25);
			}
			System.out.println("Port closed: " + serialPort.closePort());
		} catch (SerialPortException e) {
			System.out.println("Something failed during communication: " + e);
		} catch (InterruptedException e) {
			System.out.println("Something failed during the sleep commands: "
					+ e);
			e.printStackTrace();
		}
	}

	static class SerialPortReader implements SerialPortEventListener {

		public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR()) {// If data is available
				if (event.getEventValue() > 0) {// Check bytes count in the
												// input buffer
					// Read data, if bytes are available
					try {
						byte buffer[] = serialPort.readBytes();
						System.out.print(new String(buffer));
					} catch (SerialPortException ex) {
						System.out.println(ex);
					}
				}
			}
		}
	}

}
