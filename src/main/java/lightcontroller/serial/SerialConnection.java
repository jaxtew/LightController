package lightcontroller.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortIOException;
import lightcontroller.Main;

import java.io.IOException;

public class SerialConnection
{
    private static final String ID_MESSAGE = "yuh";

    private final SerialPort port;

    private boolean open;
    private boolean connected;

    public SerialConnection(SerialPort port) throws SerialPortIOException
    {
        this.port = port;
        this.open = false;
        port.setComPortParameters(9600, 8, 1, 0);
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        if (!port.openPort()) throw new SerialPortIOException("Failed to open serial port " + port.getSystemPortName());
        open = true;

        port.addDataListener(new SerialPortDataListener()
        {
            @Override
            public int getListeningEvents()
            {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent)
            {
                if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                try
                {
                    Thread.sleep(100); // allow buffer to fill
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                byte[] data = new byte[serialPortEvent.getSerialPort().bytesAvailable()];
                serialPortEvent.getSerialPort().readBytes(data, data.length);
                StringBuilder message = new StringBuilder();
                for (byte b : data)
                {
                    message.append((char) b);
                }
                if(message.toString().trim().equals(ID_MESSAGE))
                {
                    connected = true;
                    port.removeDataListener();
                }
            }
        });
    }

    public void write(byte[] data)
    {
        long time = System.currentTimeMillis();
        while(System.currentTimeMillis() - time < 75); // delay

        try
        {
            port.getOutputStream().write(data);
            port.getOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void write(String message)
    {
        if(Main.DEBUG) System.out.println("Write: " + message);
        write(message.getBytes());
    }

    public void close() throws SerialPortIOException
    {
        if (!open) return;
        if (!port.closePort()) throw new SerialPortIOException("Failed to close serial port " + port.getSystemPortName());
        open = false;
    }

    public SerialPort getPort()
    {
        return port;
    }

    public boolean isConnected()
    {
        return connected;
    }
}
