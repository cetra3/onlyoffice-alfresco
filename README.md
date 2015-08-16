# Alfresco Onlyoffice integration

This Share plugin enables users to edit HTML documents within Onlyoffice lite from Alfresco Share.  This will create a new **Edit in Onlyoffice** action within the document library for HTML documents.  This allows multiple users to collaborate in real time and to save back those changes to Alfresco.

Tested with Enterprise 4.2.\*, 5.0.\* and Community 4.2.\*, 5.0.\*

![image](onlyoffice-alfresco.gif)

## Compiling

You will need:

* Java 7 SDK or above

* Maven or Gradle

### Maven

* Run `mvn package` from the `share` directory

### Gradle

* You will need Parashift's alfresco amp plugin from here: https://bitbucket.org/parashift/alfresco-amp-plugin

* Run `gradle amp` from the `share` directory

## Installation

* Deploy the amp using Alfresco MMT or copy the onlyoffice jar to the WEB-INF/lib/ directory of share

* Update your `share-config-custom.xml` to include two new endpoints, `onlyoffice` and `onlyoffice-api`.  `onlyoffice` is for end users and `onlyoffice-api` is only for Share to update content from Alfresco and setup pads.

* Update your `share-config-custom.xml` to disable CSRF actions for the onlyoffice endpoint, see **CSRF Instructions** below

* Restart Alfresco Share

### Example share-config-custom.xml

This example uses the following:

* 127.0.0.1 as the host
* 9001 as the port 
* an API Key of `CHANGEME`.  

To find the API key, check for the file `APIKEY.txt` on your onlyoffice instance.

#### Example Endpoints

```
<config evaluator="string-compare" condition="Remote">
	<remote>
		<endpoint>
			<id>onlyoffice</id>
			<name>Onlyoffice - unauthenticated access</name>
			<description>Onlyoffice access</description>
			<connector-id>http</connector-id>
			<endpoint-url>http://127.0.0.1:9001</endpoint-url>
			<identity>none</identity>
		</endpoint>
		<endpoint>
			<id>onlyoffice-api</id>
			<name>Onlyoffice - API access</name>
			<description>Onlyoffice access</description>
			<connector-id>onlyoffice</connector-id>
			<endpoint-url>http://127.0.0.1:9001/api/1/</endpoint-url>
			<identity>declared</identity>
			<username>apikey</username>
			<password>CHANGEME</password>
			<unsecure>true</unsecure>
		</endpoint>
	</remote>
</config>
```

## CSRF Instructions

Because CSRF is enabled for all POST requests, we need to override the default CSRF policy to switch it off for POST requests to the onlyoffice proxy.

As discussed in this [blog](http://blogs.alfresco.com/wp/ewinlof/2013/03/11/introducing-the-new-csrf-filter-in-alfresco-share/), there are two options. Both involve overriding configuration in your `share-config-custom.xml` file.

You can switch off the CSRF Policy completely by using this statement (not recommended in production):

```
<config evaluator="string-compare" condition="CSRFPolicy" replace="true">
	<filter/>
</config>
```


Or you can copy the CSRFPolicy configuration from [this file](https://svn.alfresco.com/repos/alfresco-open-mirror/alfresco/COMMUNITYTAGS/V5.0.d/root/projects/slingshot/config/alfresco/share-security-config.xml) and then add the example in the onlyoffice-alfresco readme. So you would end up with a config section like this in your share-config-custom.xml:

```
<config evaluator="string-compare" condition="CSRFPolicy" replace="true">
	<filter>
		<rule>
			<request>
				<method>POST</method>
				<path>/proxy/onlyoffice/.*</path>
			</request>
		</rule>
		<-- OTHER RULE STATEMENTS FROM share-security-config.xml -->
	</filter>
</config>
```

What this accomplishes is that you have the default CSRFPolicy, so you are secure, but you enable POST requests to pass through unfiltered to onlyoffice.  You must include the onlyoffice filter rule first, as `share-security-config.xml` is loaded before `share-config-custom.xml`


## Todo

* Increase security of Onlyoffice.  Right now you will still need the Node's UUID to discover the pad, but we can do better.
* Handle error cases alot better (wrong API Key, etc..)
* Provide some documentation on setting up onlyoffice correctly
* Support text documents
