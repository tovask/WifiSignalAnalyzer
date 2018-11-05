# Wifi Signal Analyzer
Measure and store WiFi signal strength for detect changes in the environment

## Run the server
```bash
javac *.java && java -classpath .:/usr/share/java/mysql-connector-java.jar RecordStoreServer
```

## Run the client
```bash
javac *.java && java CapturePackets
```

<br><br><br><br>

### Server setup
```bash
$ sudo apt install mysql-server
$ sudo mysql_secure_installation
$ sudo mysql
#    ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'strongrootpassword';
$ mysql -u root -p
#    create database mydb;
  
$ sudo apt install libmysql-java
# https://docs.oracle.com/javase/8/docs/api/java/sql/DriverManager.html
  
# optional:
$ sudo apt install mysql-workbench
```
