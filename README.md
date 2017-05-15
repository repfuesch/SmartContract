# SmartContract

An application to create, deploy and sign electronic purchase contracts on the Ethereum blockchain:

The application has the following dependencies :
* junit:junit:4.12
* com.android.support:appcompat-v7:24.2.1 \*
* com.android.support:design:24.2.1 \*
* com.google.code.gson:gson:2.6.2

\*In order to build the library, the Android Support Library v7:24.2.1 or higher must be installed.

## Setup Instructions

### Install from release

### Build the SmartContract application:

1. Check out the SmartContract project in your desired destination folder.
2. Build the application .apk file with either of those two options:
   * Command line (recommended):
     1. Step into the root folder of the checked out project, i.e., SmartContract.
     2. Execute the following command *./gradlew build* to build all the resources (*./gradlew clean* in order to clean all the build files)
   * Android Studio:
     1. Import the checked out project into Android Studio.
     2. Go to *Build* --> *Rebuild Project*
3. The SmartContract application file is in the following path /app/build/outputs/apk.
