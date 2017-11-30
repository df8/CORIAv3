# CORIAv3

CORIA is a framework for analyzing huge network graphs. You can deploy the application in any supported application server such as Tomcat, Glassfish or JBoss

**Please note, that this version is not final and will change (a bit) in the next days**

## Build from Source
To build CORIA from the here provided sources simply follow the steps:

1. Download and install the Java JDK and JRE 1.8 or newer from Oracle
2. Download and install Maven on your machine
3. Download and unpack the sources
4. In the root directory of the sources execute the following command on a CLI:

> mvn clean install

5. The result is a file inside the **coria-api/target** directory named **coria.war**
6. Deploy this file to the server of your choice (seee below)

## Deploy the Application

1. Install the application server of your choice (example: Tomcat)
2. Install a database server (Redis or MySQL as of now)
2. Downlaod and install Python 3 on the machine (recommended but not mandatory)
2. Deploy the **coria.war** file as created above into the application server according to the servers manual
3. find the application.properties file in the unpacked application directory, usually under:
>WEB-INF\classes\
4. Configure the application according to your machine setup including credentials to DB and a local writable directory
5. Run server and navigate to 
>http://localhost:port/coria

## Problems or Questions
If you have trouble building or installing the application or find any bugs, contact me here or open a ticket on gothub

Sebastian
