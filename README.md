#pdi-xero

This is a PDI step plugin for extracting data from [Xero](http://www.xero.com) accounting.

## License

See the LICENSE.TXT file for license rights and limitations.

## Binary Installation

A binary of version 1.0.5 can be downloaded from [here](https://pdi-xero.s3.amazonaws.com/pdi-xero-marketplace-1.0.5.zip) (SHA1 ```8792c1d528c7ea400b8387b9a6385fdd54f6a2a3```).  Copy this to the ```.../plugins/steps``` directory in your PDI (5.2) installation and then restart PDI.  Upon restart you should be able to find the _pdi-xero_ step installed.  The _pdi-xero_ step has a name "Xero GET" and an icon;

![Icon](src/main/resources/net/bulletin/pdi/xero/step/resources/icon.svg)

Se below for instructions on building and installing from source.

## Glossary

### PDI (Kettle)

"Pentaho Data Integration" (PDI) is an "extract-transform-load" (ETL) software package.  PDI is also sometimes known as "Kettle".  You can find out more about PDI [here](http://community.pentaho.com/).  ETL systems are designed to extract data from one or more sources, transform the data for some purpose and then to store the data into a form which is convenient to some end use.  A typical use for ETL is to prepare data for analysis or reporting.

### Xero

Xero is a cloud-based accounting service.  You can find out more about Xero [here](http://www.xero.com/).

## Overview

PDI processes typically consist of "jobs" and "transforms".  A "job" may have a number of "transforms" and a "transform" has a number of interconnected "steps" that operate on streams of data.  This software is a "step" that supports extracting data from the Xero cloud-based accounting service into the PDI environment so that it can be stored for some other use such as reporting.

### Fetching XML from Xero

The Xero [API](http://developer.xero.com/) allows the Xero data in an account to be queried using an HTTPS "GET" request.  This PDI step supports making such a "GET" request and bringing that data into a PDI workflow.  There are a number of endpoint URLs that can be used in this way such as;

* .../api.xro/2.0/Contacts
* .../api.xro/2.0/Invoices

By default, these endpoints will return an XML payload containing relevant data.  The _pdi-zero_ step will stream the XML and is able to break the XML into chunks that are then placed into the PDI stream within the transform.  The stream can be consumed by downstream steps.  Typically, the built-in PDI step "Get data from XML" would consume the stream from the _pdi-xero_ step in order to pick-out individual fields from the chunk of XML.

### Container Elements and Breaking-up The XML for the Stream

The _pdi-zero_ step is able to be configured with "container elements" that indicate how to demarcate the chunks of XML arriving from the Xero API over HTTP.  For example; a container element of ```/Response/Contacts/Contact``` will break the inbound XML up into small XML documents rooted at the "Contact" element.  There will be one item on the PDI stream for each chunk.  If this behaviour is undesirable, configure a "container element" of "/" which will result in the XML being chunked at the root element.

### Incremental Updates

ETL processes will often want to "continuously update" from a source to keep a data warehouse fresh, but it would be inefficient to download all of the data on each update.  To avoid this, the _pdi-xero_ step uses the Xero API "If-Modified-Since" header field in order to only pull data that has been modified since a given date.  If this is not configured then all data will be returned.

### Where

The Xero API supports a "[where](http://developer.xero.com/documentation/getting-started/http-requests-and-responses/)" query parameter that provides the ability to filter the result set.  The _pdi-xero_ step supports this.

## Source Build and Installation

The project is managed and built with [Apache Maven](https://maven.apache.org/).  Building the plugin requires Java 1.6 or better.  To build the project, from the top-level of the project;

```
mvn package
```

### Step Build Product

This will produce a file such as ```pdi-xero-step-X.Y.Z.jar``` in the ```pdi-xero-step``` module.  Copy this to the ```.../plugins/steps``` directory in your PDI (5.2) installation and then restart PDI.  Upon restart you should be able to find the _pdi-xero_ step installed.  The _pdi-xero_ step has a name "Xero GET" and an icon;

![Icon](src/main/resources/net/bulletin/pdi/xero/step/resources/icon.svg)

### Marketplace Build Product

The module ```pdi-xero-marketplace``` module will produce a build product that can be vended in the Pentaho marketplace.

## Configuration

The step has a handful of configurations;

|Item|Meaning|
|---|---|
|Stream XML Field Name|This is the field name into which the XML chunks are added to the stream|
|Xero URL|The URL to communicate with Xero on|
|Consumer Key|This is the key used to identify your account with Xero|
|Key File|This is a path to the key file that is used to authenticate your account with Xero - _see "Key File" section below_|
|Container Elements|_See "Container Elements and Breaking-up The XML for the Stream" section above_|
|If-Modified-Since Date|This is a string of the form ```YYYY-MM-dd HH:mm:ss``` in the local PDI time-zone.|
|Where|The where clause to use.  This will be added to the Xero API URL and will be URL encoded automatically.|

### Key File

Setting up API access to the Xero environment from external software entails the [creation](http://developer.xero.com/documentation/advanced-docs/public-private-keypair/) of a key-pair.  To use the private-key with the _pdi-xero_ step so that it is able to communicate with Xero, you will need to post-process it once more;

```
openssl pkcs8 -topk8 -nocrypt -in privatekey.pem -out privatekey.pcks8
```

The resulting file will have the following approximate form;

```
-----BEGIN PRIVATE KEY-----
abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123
456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv
abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123
456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv
abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123
456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv
abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123
456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv
abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123
456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv
abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123
456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv
abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123
456789abcdefghi=
-----END PRIVATE KEY-----
```

## Useful Links

* [PDI Source](https://github.com/pentaho/pentaho-kettle)
* [Extending Pentaho Data Integration](http://infocenter.pentaho.com/help/index.jsp?topic=%2Fcat_dev_guides%2Ftop_dev_guides.html)
* [Pentaho plugin sample code](https://github.com/pentaho/pdi-sdk-plugins)
