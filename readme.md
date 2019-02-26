### **Setup**

#### **Database**
1.  Install Mysql 5.5. 
2.  Run the creation script in the mysql folder
3.  Create a user aion using: 
        _CREATE USER 'aion'@'localhost' IDENTIFIED BY 'password' 
        GRANT ALL PRIVILEGES ON aion.* TO 'aion'@'localhost';_

#### **Configuration**
Modify the configuration. 

The DB user and password can be added modified either through the config.json or the .env file. 

##### **Config.json based configuration**
1.  Add the DB user and password. 
2.  Add a comma separated list of api sockets eg. ["127.0.0.1:8547", "192.168.1.3:8547"] to the API section
3.  Remove the settings for the DB_USER, DB_PASSWORD and JAVA_API_URI from the .env file 

##### **.env based setup**
1.  Add the DB_USER and DB_PASSWORD
2. Add a colon separated list of api sockets eg. "127.0.0.1:8547;192.168.1.3:8547"

#### **Build**

1.  Move config.json to the root of the aion-etl project 
2   Run _cd aion-etl_
3   Run _chmod u+x gradlew_
4.  Run _./gradlew clean build -x test_
5.  The build will be found in the build/distribution folder

#### **Run**
1.  Run _source config/.env_ 
2.  Run _cd aion-etl/build/distribution_
3.  Extract the build
4.  Enter the build directory
5.  Run _./bin/aion-etl_


