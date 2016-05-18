# Alfresco ONLYOFFICE™ integration plugin

This plugin enables users to edit office documents from Alfresco Share using ONLYOFFICE™ Document Server.

The plugin will create a new **Edit in ONLYOFFICE** menu option within the document library for Office documents. This allows multiple users to collaborate in real time and to save back those changes to Alfresco.

Tested with Enterprise 5.0.\* and Community 5.1.\*


## Compiling

You will need:

* Gradle,

* Java 7 SDK or above,

* Parashift's Alfresco amp plugin which can be found [here](https://bitbucket.org/parashift/alfresco-amp-plugin),

* Run `gradle amp` from the `share` and `repo` directories.


## Installing ONLYOFFICE™ Document Server

You will need an instance of ONLYOFFICE™ Document Server that is resolvable and connectable both from Alfresco and any end clients. If that is not the case, use the official ONLYOFFICE™ Document Server documentation page: [Document Server for Linux](http://helpcenter.onlyoffice.com/server/linux/document/linux-installation.aspx). ONLYOFFICE™ Document Server must also be able to POST to Alfresco directly.

The easiest way to start an instance of ONLYOFFICE™ Document Server is to use [Docker](https://github.com/ONLYOFFICE/Docker-DocumentServer).


## Installing Alfresco ONLYOFFICE™ integration plugin

To start using ONLYOFFICE™ Document Server with Alfresco, the following steps must be performed for Ubuntu 14.04:

* Remove gradle in case it has already been installed (it is needed to install the latest available version later at the next step):
```bash
sudo apt-get remove gradle
```

* Add the repository and install the latest version:
```bash
sudo add-apt-repository ppa:cwchien/gradle
sudo apt-get update
sudo apt-get install gradle
```

* The latest stable Oracle Java version is necessary for the successful build. If you do not have it installed, use the following commands to install Oracle Java 8: 
```bash
sudo add-apt-repository 
ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```

* Switch Java alternatives to Oracle Java:
```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
sudo update-alternatives --config javaws 
```

* Build the necessary dependencies:
```bash
git clone https://github.com/yeyan/alfresco-amp-plugin.git
cd alfresco-amp-plugin
gradle publish
```

* Download the Alfresco ONLYOFFICE™ integration plugin source code: 
```bash
cd ..
git clone https://github.com/ONLYOFFICE/onlyoffice-alfresco.git
```

* Compile packages in the `repo` and `share` directories: 
```bash
cd onlyoffice-alfresco/repo/
gradle amp
cd ../share/
gradle amp
```

* Upload the compiled packages from `./build/amp` to the `amps/` and `amps_share/` directories accordingly for your Alfresco installation.

* Run the `bin/apply_amps.sh` script in Alfresco installation. You will see the two new modules being installed during the installation process. Press Enter to continue the installation.

* Add the **onlyoffice.url** property to `alfresco-global.properties`: 
```
onlyoffice.url=http://documentserver/
```

* Restart Alfresco:
```bash
alfresco.sh stop
alfresco.sh start
```

The module can be checked in administrator tools in Alfresco 5.1 or at `/share/page/modules/deploy` in Alfresco 5.0.


## How it works

The ONLYOFFICE integration follows the API documented here https://api.onlyoffice.com/editors/basic:

* User navigates to a document within Alfresco Share and selects the `Edit in ONLYOFFICE` action.
* Alfresco Share makes a request to the repo end (URL of the form: `/parashift/onlyoffice/prepare?nodeRef={nodeRef}`).
* Alfresco Repo end prepares a JSON object for the Share with the following properties:
  * **docUrl**: the URL that ONLYOFFICE™ Document Server uses to download the document (includes the `alf_ticket` of the current user),
  * **callbackUrl**: the URL that ONLYOFFICE™ Document Server informs about status of the document editing;
  * **onlyofficeUrl**: the URL that the client needs to reply to ONLYOFFICE™ Document Server (provided by the onlyoffice.url property);
  * **key**: the UUID+Modified Timestamp to instruct ONLYOFFICE™ Document Server whether to download the document again or not;
  * **docTitle**: the document Title (name).
* Alfresco Share takes this object and constructs a page from a freemarker template, filling in all of those values so that the client browser can load up the editor.
* The client browser makes a request for the javascript library from ONLYOFFICE™ Document Server and sends ONLYOFFICE™ Document Server the docEditor configuration with the above properties.
* Then ONLYOFFICE™ Document Server downloads the document from Alfresco and the user begins editing.
* ONLYOFFICE™ Document Server sends a POST request to the `callback` URL to inform Alfresco that a user is editing the document.
* Alfresco locks the document, but still allows other users with write access the ability to collaborate in real time with ONLYOFFICE™ Document Server by leaving the Action present.
* When all users and client browsers are done with editing, they close the editing window.
* After 10 seconds of inactivity, ONLYOFFICE™ Document Server sends a POST to the `callback` URL letting Alfresco know that the clients have finished editing the document and closed it.
* Alfresco downloads the new version of the document, replacing the old one.
