/*
$ sudo apt install mysql-server
$ sudo mysql_secure_installation
$ sudo mysql
    ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'strongrootpassword';
$ mysql -u root -p
    create database mydb;

$ sudo apt install libmysql-java

https://docs.oracle.com/javase/8/docs/api/java/sql/DriverManager.html

$ sudo apt install mysql-workbench


*/
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DBManager {

    private String mysqlUser;
    private String mysqlPassword;
    private String mysqlDatabase;
    private String mysqlAddress;
    private String mysqlPort;
    private String mysqlParameters;

    private Connection con = null;

    public DBManager(){
        Properties properties = new Properties();
        try {
            properties .load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            //e.printStackTrace();  ignore
        }
        this.mysqlUser = properties.getProperty("mysqlUser","root");
        this.mysqlPassword = properties.getProperty("mysqlPassword","");
        this.mysqlDatabase = properties.getProperty("mysqlDatabase","mydb");
        this.mysqlAddress = properties.getProperty("mysqlAddress","localhost");
        this.mysqlPort = properties.getProperty("mysqlPort","3306");
        this.mysqlParameters = properties.getProperty("mysqlParameters","useSSL=false");
    }

    private Connection getConnection() throws SQLException {
        if (con==null || !con.isValid(0)) {
            con = DriverManager.getConnection("jdbc:mysql://"+mysqlAddress+":"+mysqlPort+"/"+mysqlDatabase+"?"+mysqlParameters, mysqlUser, mysqlPassword);
        }
        return con;
    }

    public String createTable() throws SQLException {
        String tableName = "records_"+String.valueOf(System.currentTimeMillis());
        String query = "CREATE TABLE `"+this.mysqlDatabase+"`.`"+tableName+"` (\n" +
                "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                "  `frame_number` VARCHAR(45),\n" +
                "  `time` VARCHAR(45),\n" +
                "  `channel` VARCHAR(45),\n" +
                "  `signal_dbm` VARCHAR(45),\n" +
                "  `bssid` VARCHAR(45),\n" +
                "  `beacon_interval` VARCHAR(45),\n" +
                "  `ssid` VARCHAR(45),\n" +
                "  PRIMARY KEY (`id`));\n";
        getConnection().createStatement().executeUpdate(query);
        return tableName;
    }

    public void storeRecord(String tableName, String[] datas) throws SQLException {
        String statement = "INSERT INTO `"+this.mysqlDatabase+"`.`"+tableName+"` " +
                "(`frame_number`,`time`,`channel`,`signal_dbm`,`bssid`,`beacon_interval`,`ssid`) " +
                "VALUES (?,?,?,?,?,?,?);";
        PreparedStatement pstmt = getConnection().prepareStatement(statement);
        pstmt.setString(1,datas[1]); // frame.number
        pstmt.setString(2,datas[2]); // frame.time_epoch
        pstmt.setString(3,datas[3]); // wlan_radio.channel
        pstmt.setString(4,datas[4]); // wlan_radio.signal_dbm
        pstmt.setString(5,datas[5]); // wlan.bssid
        pstmt.setString(6,datas[6]); // wlan_mgt.fixed.beacon
        pstmt.setString(7,datas[7]); // wlan_mgt.ssid
        pstmt.executeUpdate();	// should return 1
    }

    public String[] getRecords(String tableName) throws SQLException {
        ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM `"+this.mysqlDatabase+"`.`"+tableName+"`;");
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        List<String> results = new ArrayList<String>();

        while (rs.next()) {
            String row = "";
            for(int i=1;i<=columnCount;i++) {
                row += "\t"+rsmd.getColumnName(i)+" : "+rs.getString(i)+",";
            }
            results.add(row);
        }

        return results.toArray(new String[results.size()]);
    }

    public String getRecordCount(String tableName) throws SQLException {
        ResultSet rs = getConnection().createStatement().executeQuery("SELECT COUNT(*) FROM `"+this.mysqlDatabase+"`.`"+tableName+"`;");
        rs.next();
        return rs.getString(1);
    }
}
