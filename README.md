# Alfresco ONLYOFFICE integration

This Share plugin enables users to edit Office documents within ONLYOFFICE from Alfresco Share. This will create a new **Edit in ONLYOFFICE** action within the document library for Office documents. This allows multiple users to collaborate in real time and to save back those changes to Alfresco.

Tested with Enterprise 5.0.\*, 5.1.\* and Community 5.1.\*


## Compiling

You will need:

* Java 7 SDK or above

* Gradle

* Parashift's alfresco amp plugin from here: https://bitbucket.org/parashift/alfresco-amp-plugin

* Run `gradle amp` from the `share` and `repo` directories


## Installation

### ONLYOFFICE

You will need an instance of ONLYOFFICE that is resolvable and connectable both from alfresco and any end clients. ONLYOFFICE must also be able to POST to alfresco directly.

The easiest way to start an instance of ONLYOFFICE is to use Docker: https://github.com/ONLYOFFICE/Docker-DocumentServer


### Alfresco

* Deploy the amp to both the repo and share end using alfresco-mmt or other methods

* Add the `onlyoffice.url` property to alfresco-global.properties:
  * e.g: `onlyoffice.url=http://documentserver/`


## How it works

The ONLYOFFICE integration follows the API documented here https://api.onlyoffice.com/editors/basic:

* User navigates to a document within Alfresco Share and selects the `Edit in ONLYOFFICE` action.
* Alfresco Share makes a request to the repo end (URL of the form: `/parashift/onlyoffice/prepare?nodeRef={nodeRef}`).
* Alfresco Repo end prepares a JSON object for Share with the following properties:
  * **docUrl**: the URL that ONLYOFFICE uses to download the document (includes the `alf_ticket` of the current user),
  * **callbackUrl**: the URL that ONLYOFFICE informs about status of the document editing,
  * **onlyofficeUrl**: the URL that the client needs to talk to ONLYOFFICE (given by the onlyoffice.url property),
  * **key**: the UUID+Modified Timestamp to instruct ONLYOFFICE whether to download the document again or not,
  * **docTitle**: the Title (name) of the document.
* Alfresco Share takes this object and constructs a page from a freemarker template, filling in all of those values so that the client browser can load up the editor.
* The client browser makes a request for the javascript library from ONLYOFFICE and sends ONLYOFFICE the docEditor configuration with the properties as above.
* ONLYOFFICE then downloads the document from alfresco and the user begins editing.
* ONLYOFFICE sends a POST request to the callback URL to inform Alfresco that a user is editing the document.
* Alfresco locks the document, but still allows other users with write access the ability to collaborate in real time with ONLYOFFICE by leaving the Action present.
* When all users and client browsers are finished, they close the editing window.
* After 10 seconds of inactivity, ONLYOFFICE sends a POST to the callback URL letting Alfresco know that the clients have finished.
* Alfresco downloads the new version of the document, replacing the old one.
