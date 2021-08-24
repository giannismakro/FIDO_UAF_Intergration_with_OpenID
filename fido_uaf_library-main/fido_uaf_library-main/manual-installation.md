# FIDO UAF Server

## Configure and Deploy instructions for FIDO UAF Server

### These instructions have been tested on Ubuntu 20.04.02

#### Installing Apache

Update the local package index to reflect the latest upstream changes :

```bash
sudo apt-get update
```

Install the apache2 package :

```bash
sudo apt-get install apache2
```

#### Installing Certbot

First, add the repository :

```bash
sudo add-apt-repository ppa:certbot/certbot
```
Install Certbot's Apache package with apt :

```bash
sudo apt install certbot python3-certbot-apache
```

#### Set Up the SSL Certificate

Set your domain at `/etc/apache2/sites-enabled/000-default.conf` :

```bash
nano /etc/apache2/sites-enabled/000-default.conf
```
Uncomment Servername and set up the hostname of the machine

```config
# Set up your_domain
...
ServerName your_domain;
...
```

#### Obtaining an SSL Certificate 
Certbot provides a variety of ways to obtain SSL certificates through plugins. The Apache plugin will take 
care of reconfiguring Apache and reloading the config whenever necessary. To use this plugin, type the following:

```bash
sudo certbot --apache -d your_domain -d www.your_domain
```

#### Veryfying Certbot Auto-Renewall

To check the status of this service and make sure it's active and running , you can use :

```bash
sudo systemctl status certbot.timer
```
To test the renewal process, you can do a dry run with certbot :
```bash
sudo certbot renew –dry-run
```

#### Installing Tomcat

Install the OpenJDK package by running : 
```bash
sudo apt install default-jdk
```
Install the tomcat:

```bash
sudo apt install tomcat9 unzip build-essential maven
```
#### Installing software-properties-common package
Run the install command to quickly install the packages and dependencies:

```bash
sudo apt install software-properties-common
```
Install curl command on Ubuntu :

```bash
sudo apt install curl
```
Optional get root access :
```bash
sudo -s
```
#### Installing jdk-8u271

Download the **required** file :

[jdk-8u271-linux-x64.tar.gz](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html#license-lightbox)

Create a directory
```bash
mkdir /usr/local/oracle-java-8
```
Change directory to the location where you would like the JDK to be installed, then move the `.tar.gz` archive file to the current directory.

```bash
tar -zxf jdk-8u271-linux-x64.tar.gz -C /usr/local/oracle-java-8
```

Installing New Alternatives :

```bash
update-alternatives --install "/usr/bin/java" "java" "/usr/local/oracle-java-8/jdk1.8.0_271/bin/java" 1500
update-alternatives --install "/usr/bin/javac" "javac" "/usr/local/oracle-java-8/jdk1.8.0_271/bin/javac" 1500
update-alternatives --install "/usr/bin/javaws" "javaws" "/usr/local/oracle-java-8/jdk1.8.0_271/bin/javaws" 1500
```
Check the Java version :
```bash
java -version
```

#### Set apache as reverse proxy

Install aptitude
```bash
apt install aptitude
```

Getting The Essential Build Tools

```bash
aptitude install build-essential
```
Getting The Modules and the Dependencies

```bash
aptitude install libxml2-dev
```
Run the following command to install the modules:
```bash
a2enmod proxy && a2enmod proxy_http && a2enmod proxy_ajp && a2enmod rewrite && a2enmod deflate && a2enmod headers && a2enmod proxy_balancer && a2enmod proxy_connect && a2enmod proxy_html
```
#### Modifying The Default Configuration

In this step, we are going to see how to modify the default configuration file **000-default-le-ssl.conf** inside `/etc/apache2/sites-enabled/000-default-le-ssl.conf` to set up “proxying” functionality.
Run the following command to edit the default Apache virtual host using the nano text editor:

```bash
nano /etc/apache2/sites-enabled/000-default-le-ssl.conf
```
Add the following lines :


```config
# add the following lines
        ProxyPass /fido http://<hostname>:8080/fido
    	ProxyPassReverse /fido http://<hostname>:8080/fido

```


Restart Apache :
```bash
systemctl restart apache2
```


##### MariaDB Installation

Install MariaDB MySQL server if not already installed:

```bash
sudo apt install mariadb-server -y
```

Configure installation

```bash
sudo mysql_secure_installation
```

##### Create Database & User

Create database for FIDO UAF server and user (replace `databasename`, `username` and `password` appropriately).

```bash
$ sudo mariadb
> CREATE DATABASE databasename;
> GRANT ALL ON databasename.* TO 'username'@'localhost' identified by 'password' WITH GRANT OPTION;
> FLUSH PRIVILEGES;
> exit
```

Test user:

```bash
mariadb -u username -p
```

##### Prepare Database Schema

Initialise database schema and data:

```bash
sudo mysql databasename < /path/to/scripts/database-init.mysql.sql
```
### Download FIDO SERVER UNIPI

Download the project
```bash
git clone https://unipifido.ds.unipi.gr/gmakropodis/fido-server-unipi.git
``` 

##### Configure Server

Update `application.properties`, either **before compilation** by editing:

```shell
nano /path-to/fido-uaf-server/src/main/resources/application.properties
```

or **after deployment** by editing:

```shell
sudo nano /var/lib/tomcat*/webapps/fido/WEB-INF/classes/application.properties
sudo service tomcat9 restart
```

Edit the following part (don't forget to change `databasename`, `username` and `password` appropriately).

```config
# If MariaDB Database
hibernate.dialect = org.hibernate.dialect.MySQLDialect
jdbc.driverClassName = org.mariadb.jdbc.Driver
jdbc.url = jdbc:mariadb://localhost:3306/databasename
jdbc.username = username
jdbc.password = password
```

##### Compile the project

Build libraries and the server

Build the fido-uaf-core-crypto library
```bash
cd fido-uaf-core-crypto && mvn clean install && cd ..
``` 
Build the fido-uaf-core-msg library
```bash
cd fido-uaf-core-msg && mvn clean install && cd ..
``` 
Build the fido-uaf-core-tlv library
```bash
cd fido-uaf-core-tlv && mvn clean install && cd ..
``` 
Build the project
```bash
cd fido-uaf-server && mvn clean install && cd ..
```
Deploy .war
```bash
sudo cp ./fido-uaf-server/target/fido-uaf-server-*.war /var/lib/tomcat9/webapps/fido.war
```

Restart tomcat 
```bash
service tomcat9 restart
```
Restart apache
```bash
service apache2 restart
```
