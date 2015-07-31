#LATCH INSTALLATION GUIDE FOR JENKINS


##PREREQUISITES
 * Java version 1.7 or later.

 * Jenkins version 1.560 or later.

 * To get the **"Application ID"** and **"Secret"**, (fundamental values for integrating Latch in any application), it’s necessary to register a developer account in [Latch's website](https://latch.elevenpaths.com"https://latch.elevenpaths.com"). On the upper right side, click on **"Developer area"**.


##DOWNLOADING THE JENKINS PLUGIN
* When the account is activated, the user will be able to create applications with Latch and access to developer documentation, including existing SDKs and plugins. The user has to access again to [Developer area](https://latch.elevenpaths.com/www/developerArea"https://latch.elevenpaths.com/www/developerArea"), and browse his applications from **"My applications"** section in the side menu.

* When creating an application, two fundamental fields are shown: **"Application ID"** and **"Secret"**, keep these for later use. There are some additional parameters to be chosen, as the application icon (that will be shown in Latch) and whether the application will support OTP (One Time Password) or not.

* From the side menu in developers area, the user can access the **"Documentation & SDKs"** section. Inside it, there is a **"SDKs and Plugins"** menu. Links to different SDKs in different programming languages and plugins developed so far, are shown.

* You can download the plugin by getting the executable of the [GitHub repository](https://github.com/ElevenPaths/latch-plugin-jenkins"https://github.com/ElevenPaths/latch-plugin-jenkins") under the Releases section or just download the source code and generate the plugin by yourself. Go to the root folder, where the pom.xml is allocated, and run the next command on the command line:
```
	mvn package
```


##INSTALLING THE PLUGIN IN JENKINS
* Once the administrator has downloaded the plugin, it has to be added as a plugin in its administration panel in Jenkins. Click on **"Manage Jenkins"** and **"Manage Plugins"**. In the **"Advanced"** tab it will show a form where you can browse and select previously downloaded or generated HPI file.

* Go to **"Configure Global Security"**, inside **"Manage Jenkins"** and introduce **"Application ID"** and **"Secret"** previously generated. The administrator can now save the changes clicking on **"Save"**. If everything is ok, a confirmation message will be received.

* From now on, on user's profile settings, a new textbox will appear, inside **"Configure"** menu, where the token generated from the app should be introduced.


##UNINSTALLING THE PLUGIN IN JENKINS
* To remove the plugin, the administrator has to click on **"Installed"** tab inside of **"Manage Plugins"** and press the **"Uninstall"** link below the **"Latch"** plugin, and then wait until Jenkins is restarted.


##USE OF LATCH MODULE FOR THE USERS
**Latch does not affect in any case or in any way the usual operations with an account. It just allows or denies actions over it, acting as an independent extra layer of security that, once removed or without effect, will have no effect over the accounts, that will remain with its original state.**

###Pairing a user in Jenkins
The user needs the Latch application installed on the phone, and follow these steps:

* **Step 1:** Logged in your own Jenkins account and go to **"Configure"** in your profile menu.

* **Step 2:** From the Latch app on the phone, the user has to generate the token, pressing on **"Add a new service"** at the bottom of the application, and pressing **"Generate new code"** will take the user to a new screen where the pairing code will be displayed.

* **Step 3:** The user has to type the characters generated on the phone into the **"Latch"** text box displayed on the web page. Click on **"Pair"** button.

* **Step 4:** Now the user may lock and unlock the account, preventing any unauthorized access.

###Unpairing a user in Jenkins
* The user should access their Jenkins account and under the **"Configure"** sectionof the user's profile click the **"Unpair** button. He will receive a notification indicating that the service has been unpaired.



#### TROUBLESHOOTING ####

*A javax.net.ssl.SSLHandshakeException with a nested sun.security.validator.ValidatorException is thrown when invoking an API call.*

This exception is normally thrown when the JDK doesn't trust the CA that signs the digital certificate used in Latch's website (https://latch.elevenpaths.com). You may need to install the CA (http://www.startssl.com/certs/ca.pem) as a trusted certificate in your JDK's truststore (normally in jre/lib/security/cacerts) using the keytool utility.
```
$ sudo keytool -import -trustcacerts -file ca.pem -keystore jre/lib/security/cacerts -alias "ElevenPaths"
```
