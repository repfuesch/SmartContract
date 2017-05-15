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

* Download and install the latest release from the [release](.../releases) page 

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

# Usage Instructions
