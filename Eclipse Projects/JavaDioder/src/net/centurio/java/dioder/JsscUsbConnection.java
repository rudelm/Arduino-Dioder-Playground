package net.centurio.java.dioder;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author Heshan
 */
public class JsscUsbConnection {

    /**
     * @param args the command line arguments
     */
    static SerialPort serialPort;

    public static void main(String[] args) {
        serialPort = new SerialPort("/dev/tty.usbmodem26211");
        try {
            System.out.println("port open :" + serialPort.openPort());//Open port
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    static class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR()) {//If data is available
                //System.out.println(event.getEventValue());
                if (event.getEventValue() > 1) {//Check bytes count in the input buffer

                    //Read data, if 10 bytes available
                    try {
                        byte buffer[] = serialPort.readBytes(1);
                        System.out.print(new String(buffer));
                    } catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            } else if (event.isCTS()) {//If CTS line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("CTS - ON");
                } else {
                    System.out.println("CTS - OFF");
                }
            } else if (event.isDSR()) {///If DSR line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("DSR - ON");
                } else {
                    System.out.println("DSR - OFF");
                }
            }
        }
    }
}

