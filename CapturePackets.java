import java.io.*;

public class CapturePackets {

    public static void main(String args[]) throws IOException, InterruptedException {

        RecordSubmitter rs = new RecordSubmitter(); // throws IOException


        //sudo tshark -i wlan0 -l -T fields -E quote=s -E separator=, -f "link[0] == 0x80" -e frame.number -e frame.time_epoch -e wlan_radio.channel -e wlan_radio.signal_dbm -e wlan.bssid -e wlan_mgt.fixed.beacon -e wlan_mgt.ssid -c 5 -a duration:3

        String command = "";

        command += "sudo tshark -i wlan0 -l -T fields -E quote=s -E separator=, ";
        //command += "-c 5 ";
        //command += "-a duration:3 ";
        command += "-f link[0]==0x80 "; // IEEE 802.11 Beacon frame

        command += "-e frame.number ";
        command += "-e frame.time_epoch ";

        //command += "-e radiotap.channel.freq ";
        //command += "-e radiotap.dbm_antsignal ";
        command += "-e wlan_radio.channel ";
        //command += "-e wlan_radio.frequency ";
        command += "-e wlan_radio.signal_dbm ";
        //command += "-e wlan_radio.duration ";
        //command += "-e wlan_radio.preamble ";

        //command += "-e wlan.sa "; // Source address
        //command += "-e wlan.sa_resolved ";
        //command += "-e wlan.da "; // Destination address
        //command += "-e wlan.ta "; // Transmitter address
        //command += "-e wlan.ra "; // Receiver address
        command += "-e wlan.bssid ";
        //command += "-e wlan.seq "; // Sequence number (read https://link.springer.com/chapter/10.1007/11663812_16)

        command += "-e wlan_mgt.fixed.beacon "; // Beacon interval in time unit (TU: unit of time equal to 1024 microseconds), typically 100 TU
        command += "-e wlan_mgt.ssid ";


        System.out.println("setting up the interface");
        Runtime.getRuntime().exec("sudo iwconfig wlan0 channel 3").waitFor(); // throws IOException, InterruptedException
        Runtime.getRuntime().exec("sudo ifconfig wlan0 down").waitFor(); // throws IOException, InterruptedException
        Runtime.getRuntime().exec("sudo ifconfig wlan0 up").waitFor(); // throws IOException, InterruptedException
        Runtime.getRuntime().exec("sudo iwconfig wlan0 mode Monitor").waitFor(); // throws IOException, InterruptedException

        System.out.println("start capturing...");
        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command); // throws IOException
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s;
        while ((s = stdInput.readLine()) != null) { // throws IOException
            //System.out.println(s);
            try {
                rs.submitRecord(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("---");
        while ((s = stdError.readLine()) != null) { // throws IOException
            System.out.println(s);
        }


    }
}
