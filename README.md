# Tableau-Tracker

Tableau Tracker extension enables you to easily measure and understand how people consume and interact with dashboards. Tableau Tracker can anonymously collect data like clicks and filter changes for every single user so you can create better visualizations and optimize performance.



## How Tableau Tracker Works

#### Anonymous data collection

Tableau Usage Tracker was built for the post-GDPR era, created with privacy by design in mind. It does not collect personally identifiable information from users like ID, name or email address.

#### Track every click in Tableau

Track and analyze filter changes, filter states, window size, workbook id, session time and more right within Tableau.

#### Pre-built Insights Dashboard

Understand behavior patterns instantly using the built-in dashboard created by Starschema's team of Data Visualization Heros.

#### Your Usage Data in your Tableau

Use our Web Data Connector to access your data `/wdc `endpoint and download the example workbook `/example/Tableau_Tracker_Demo.twbx` to see a sample tracking dataset in action.



## Requirements

- [Java 8+ JRE (or JDK for development)](https://www.oracle.com/technetwork/java/javase/downloads/index.html) 
- [Gradle 6+](https://gradle.org/releases/)
- [Postgres DB 9.6+](https://www.postgresql.org/download/)



## Development

Clone the repository and submodules

```bash
$ git clone https://github.com/starschema/tableau-tracker
$ cd tableau-tracker
$ git submodule init
$ git submodule update
```



The backend part of the Tableau Tracker app, developed with Kotlin + Ktor + Exposed. 

The project contains 3 frontend subprojects:

- Tableau Tracker Extension
- Tableau Tracker WDC
- Tableau Tracker Registration

(To host the extension frontend, the application must use SSL, even though it's not mandatory.)

## Build

Execute this command in the repository's root directory to build the project:

```bash
./gradlew build
```

This will produce 4 jar/war file in the `build/libs` folder:

- `tableau-tracker-{version}.jar`: The original jar without dependencies.
- `tableau-tracker-{version}-all.jar`: Executable jar file with embedded Tomcat.
- `tableau-tracker-{version}.war`: Deployable war file (can be deployed to Tomcat).
- `tableau-tracker-{version}-installer.jar`: The installer of the application.

## Configuration

All configuration can be set either by adding the [`environment variables`] below, or by writing them in the [resources/application.conf](resources/application.conf) file directly. 

- **Server port** [`TT_PORT`]: The port where this application should run on. (Only needed if running standalone.)

- **Database url** [`TT_DB_URL`]: The URL of the Postgres DB.

- **Database type** [`TT_DB_DRIVER`]: The type of the database. Now supported: Postgres, SQLServer. (In the environment variable the whole jdbc driver name is necessary, so either 'org.postgresql.Driver' or 'com.microsoft.sqlserver.jdbc.SQLServerDriver')

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

## Running

Execute this command in the repository's root directory to run:

```bash
./gradlew run
```

## Installation / deployment

The application can also be installed with the `tableau-tracker-{version}-installer.jar`or can be deployed to a Tomcat server using the ``tableau-tracker-{version}.war` file produced at build.
Further details: Installation.md 