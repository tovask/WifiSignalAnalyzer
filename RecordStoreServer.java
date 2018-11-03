import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class RecordStoreServer extends Thread {

    public static void main(String[] args) {

        final RecordStoreServer server = new RecordStoreServer();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("Exiting...");
                    Thread.currentThread().sleep(1000);
                    server.finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private InetAddress serverListenAddress;
    private int serverListenPort;
    private DatagramSocket socket;

    public RecordStoreServer() {
        Properties properties = new Properties();
        try {
            properties .load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            //e.printStackTrace();  ignore
        }
        try {
            this.serverListenAddress = InetAddress.getByName(properties.getProperty("serverListenAddress"));
        } catch (UnknownHostException e) {
            this.serverListenAddress = InetAddress.getLoopbackAddress(); // fallback
        }
        try {
            this.serverListenPort = Integer.parseInt(properties.getProperty("serverListenPort"));
        } catch (NumberFormatException e) {
            this.serverListenPort = 9876; // fallback
        }
    }

    @Override
    public void run() {
        int stored=0;
        DBManager dBManager = new DBManager();
        try {
            socket = new DatagramSocket(serverListenPort, serverListenAddress);
            System.out.println("Server listenning on "+socket.getLocalSocketAddress());
            byte[] receiveData = new byte[1024];
            while(true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(),0,receivePacket.getLength());
                //System.out.println("Server received: " + receivedMessage);

                if (receivedMessage.equals("init")){
                    try {
                        String tableName = dBManager.createTable();
                        System.out.println("New table created: "+tableName);
                        DatagramPacket sendPacket = new DatagramPacket(tableName.getBytes(), tableName.getBytes().length, receivePacket.getAddress(), receivePacket.getPort());
                        socket.send(sendPacket);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    String[] datas = parseLine(receivedMessage);
                    if(datas.length!=8){
                        continue;
                    }
                    String tableName = datas[0];
                    try {
                        dBManager.storeRecord(tableName,datas);
                        stored++;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            if(!e.toString().equals("java.net.SocketException: Socket closed")){
                e.printStackTrace();
            }
        }
        System.out.println("Total record stored: "+stored);
    }

    private void finish() throws InterruptedException {
        socket.close();
        join();
    }

    private final static Pattern pattern = Pattern.compile("(?:(?:^|,)(?:'(.*?)')?(?=,|$))"); //https://www.debuggex.com/r/sqOsfgHZRPbF8Pu2
    private static String[] parseLine(String line){
        ArrayList<String> allMatches = new ArrayList<String>();
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            allMatches.add(matcher.group(1));
        }
        return allMatches.toArray(new String[allMatches.size()]);
    }
}
