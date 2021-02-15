# Tableau Usage Tracker 

# Installation and Setup Guide



## Table of contents

1. [Requirements](#Requirements)
2. [Installation](#Installation)
3. [Running standalone](#Running standalone)
4. [Deploy to Tomcat server](#Deploy to Tomcat server)

## Requirements
Hardware

- Recommended: 4 vCPU, 8GB RAM, 50 GB disk

Software

- [Java 8+ JRE](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Postgres DB 9.6+](https://www.postgresql.org/download/) or [SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)
- `tableau-tracker-{version}-installer.jar`

## Installation

1. Double click on tableau-tracker-{version}-installer.jar. (Or run `java -jar tableau-tracker-{version}-installer.jar` in command line.)

2. Select the destination folder where to install.

3. Produce the following configuration (in the brackets the environment variable name is present):

   - **Server port** [`TT_PORT`]: The port where this application should run on. (Only needed if running standalone.)

   - **Database type** [`TT_DB_DRIVER`]: The type of the database. Now supported: Postgres, SQLServer. (In the environment variable the whole jdbc driver name is necessary, so either 'org.postgresql.Driver' or 'com.microsoft.sqlserver.jdbc.SQLServerDriver')

   - **Database url** [`TT_DB_URL`]: The URL of the Postgres DB.

   - **Database user** [`TT_DB_USER`]: The username to connect with to the database.

   - **Database password** [`TT_DB_PASS`]: The password to connect with to the database.
   
   - **Database schema name** [`TT_DB_SCHEMA`]: The schema name of the database.

   - **SSL enabled** [`TT_SSL_ENABLED`]: Enable if running standalone and SSL should be supported.

     The below configuration only needed in case of SSL + standalone usage.

   - **SSL port** [`TT_SSL_PORT`]: The SSL port to run on.

   - **Keystore path** [`TT_SSL_KS`]: Contains the absolute path to the keystore file (either keystore.jks or keystore.p12).

   - **Keystore key alias** [`TT_SSL_KA`]: The alias mapped to the certificate.

   - **Keystore password** [`TT_SSL_KSP`]: The password used to generate the certificate.
   
   - **Private key Password** [`TT_SSL_PKP`]: Password for the SSL private key.


     (The configurations can be modified after the installation either by configuring the `setenv.sh`, in case of UNIX based systems, or the `setenv.bat` file, in case of Windows OS.)

4. Finish the installation.

## Running standalone

You can run the application in standalone mode or just [deploy it to a Tomcat server if needed](#Deploy to Tomcat server). 

If you want to run the standalone mode, you just have to execute the `run_tableautracker` script file, in case of UNIX based systems, or the `run_tableautracker.bat` file, in case of Windows OS.

## Deploy to Tomcat server

Before the deploy, we have to set the environment variables to the application either by:

- Check the `setenv.bat` or `setenv.sh` (based on the used OS) files and set the environment variables defined in them.
- Copy `setenv.bat` and `setenv.sh` files to CATALINA_BASE/bin folder.

Then the `tableau-tracker-{version}.war` file can be deployed to Tomcat to the root path.

## Logging

Application logs into file named `tracker.log`. Logfile is created after first run in application folder. If deployed to Tomcat server, you can find logfile in `<Tomcat folder>/bin`.
