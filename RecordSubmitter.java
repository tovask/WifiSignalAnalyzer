import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Properties;

public class RecordSubmitter {

    private InetAddress serverConnectAddress;
    private int serverConnectPort;
    private DatagramSocket socket;
    private String tableName;

    public RecordSubmitter() throws IOException {
        Properties properties = new Properties();
        try {
            properties .load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            //e.printStackTrace();  ignore
        }
        try {
            this.serverConnectAddress = InetAddress.getByName(properties.getProperty("serverConnectAddress"));
        } catch (UnknownHostException e) {
            this.serverConnectAddress = InetAddress.getLoopbackAddress(); // fallback
        }
        try {
            this.serverConnectPort = Integer.parseInt(properties.getProperty("serverConnectPort"));
        } catch (NumberFormatException e) {
            this.serverConnectPort = 9876; // fallback
        }
        this.socket = new DatagramSocket();
        this.tableName = getTableName();
    }

    private String getTableName() throws IOException {
        String sendData = "init";
        DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, serverConnectAddress, serverConnectPort);
        byte[] receiveData = new byte[1024];
        String tableName = null;
        System.out.println("Client: connecting to "+sendPacket.getSocketAddress()+" ...");
        do {
            socket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.setSoTimeout(5 * 1000);
                socket.receive(receivePacket);
                socket.setSoTimeout(0);
                tableName = new String(receivePacket.getData(), 0, receivePacket.getLength());
            } catch (SocketTimeoutException e) {
                System.out.println("Client: timeout when waiting for tableName, re-trying...");
            }

        } while (tableName==null);
        System.out.println("Client: table name: "+tableName);
        return tableName;
    }

    public void submitRecord(String value) throws IOException {
        String sendData = "'"+tableName+"',"+value;
        DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, serverConnectAddress, serverConnectPort);
        socket.send(sendPacket);
    }
}
