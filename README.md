# SmartContract

An application to create, deploy and sign electronic purchase contracts on the Ethereum blockchain:

The application has the following dependencies :
* junit:junit:4.12
* com.android.support:appcompat-v7:23.4.0
* org.web3j:core-android:2.1.0
* org.jdeferred:jdeferred-core:1.2.4
* com.android.support:recyclerview-v7:23.4.0
* com.android.support:cardview-v7:23.4.0
* com.android.support:design:23.4.0
* com.google.code.gson:gson:2.8.0
* com.github.nisrulz:qreader:2.0.1
* com.github.kenglxn.QRGen:android:2.2.0
* com.googlecode.ez-vcard:ez-vcard:0.10.2

\*In order to build the library, the Android Support Library v7:23.4.0 or higher must be installed.

## Setup Instructions

### Install from release

* Download and install the latest release from the release page 

### Build the application from source:

1. Check out the SmartContract project in your desired destination folder.
2. Build the application .apk file with either of those two options:
   * Command line (recommended):
     1. Step into the root folder of the checked out project, i.e., SmartContract.
     2. Execute the following command *./gradlew build* to build all the resources (*./gradlew clean* in order to clean all the build files)
   * Android Studio:
     1. Import the checked out project into Android Studio.
     2. Go to *Build* --> *Rebuild Project*
3. The SmartContract application file is in the following path /app/build/outputs/apk.

# Directory Structure

```
.
├── app                                 <- gitignore and gradle build files for the project
|   |── src                             <- source code of the application and the unit tests
|   |   |── android test                <- Android unit tests of the application
|   |   |   |── mocks                   <- Custom instrumentation runner and TestAppContext
|   |   |   |── ViewHelper              <- Custom ViewAssertions and matchers
|   |   |── main                        <- source code of the application
|   |   |   |── app                     <- source code of the Android Activities, Fragments and services
|   |   |   |   |── account             <- Activites and Fragments for account managing
|   |   |   |   |── common              <- ActivityBase class + AppContext custom application class
|   |   |   |   |   |── broadcast       <- BroadcastService 
|   |   |   |   |   |── controls        <- Custom ImgeView control
|   |   |   |   |   |── dialog          <- Dialogs to display messages and images
|   |   |   |   |   |── permission      <- Permission provider
|   |   |   |   |   |── service         <- Provider for the Account- and ContractServices
|   |   |   |   |   |── setting         <- SettingProvider
|   |   |   |   |   |── transaction     <- TransactionHandler
|   |   |   |   |   |── validation      <- Classes to validate text fields
|   |   |   |   |── detail
|   |   |   |   |   |── create          <- Activity and Fragments to deploy contracts
|   |   |   |   |   |── display         <- Activity and Fragments to display and interact with contracts
|   |   |   |   |── overview            <- Activity and Fragments to display an overview of contracts
|   |   |   |   |── p2p                 <- UI components and service classes to import/export contracts
|   |   |   |   |── profile             <- Activity and Fragments to display and edit user profiles
|   |   |   |   |── qrcode              <- Activity to scan QR codes
|   |   |   |   |── setting             <- Activity and Framgent to mange SharedPreferences
|   |   |   |── library                 <- cource code of the Java library code (contract wrappers + service classes)
|   |   |   |   |── async               <- Helper class to create Promises
|   |   |   |   |   |── promise         <- Promise wrapper interface and class
|   |   |   |   |── contract            <- Java wrapper classes and interfaces for the smart contracts
|   |   |   |   |── datamodel           <- Classes to represent contracts and accounts on the local file system
|   |   |   |   |── peer                <- Java server and client code to import/export contracts
|   |   |   |   |── service             <- Factory to create service classes
|   |   |   |   |   |── account         <- Service interfaces and classes to manage accounts
|   |   |   |   |   |── connection      <- Service to check the connection to the host
|   |   |   |   |   |── contract        <- Service interfaces and classes to manage contracts
|   |   |   |   |   |── exchange        <- Service to convert currencies to ether and vice versa
|   |   |   |   |   |── serialization   <- JSON serialization classes
|   |   |   |   |── util                <- Helper classes
|   |   |── test               <_ unit  tests of the application

