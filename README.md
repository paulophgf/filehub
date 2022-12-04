<p align="center">
    <img src="https://user-images.githubusercontent.com/32067860/205514546-468d61e5-28a4-4e6f-a172-857330620b79.png" 
    alt="drawing" width="200"/>
</p>

Read this in other languages: [English](README.md), [Portuguese](README.pt-br.md)

------------------------------------------------------------------------------------------------------------------------

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The FileHub is a service that standardizes file management, independent of the storage platform used. 
Moreover, it makes file persistence easier when we think about multiple storage places, serving as requests 
gateway, using a safe and easy way.


<!--------------------------------------------------------------------------------------------------------------------->


## Configuration


&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The FileHub uses an XML configuration file where the properties and some rules are defined. That file can be created 
locally where the service is executed or remotely using a Git repository.
The following table shows the environment variables used to define where the configuration file is:

<table>
    <tbody>
      <tr style="background-color: #E1E1E1; color: black">
        <th colspan="2" style="text-align: left; font-size: small">
            <i style="color: red">*</i> Required
        </th>
      </tr>
      <tr style="background-color: #DAE8FC; color: black">
        <th>Variable name</th>
        <th>Description</th>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_TYPE <i style="color: red">*</i></td>
        <td>Define if the file is locally or remotely.<br>
            Default value: <b>LOCAL_FILE</b><br>
            Possible values:<br>
            <li>LOCAL_FILE</li>
            <li>GIT_FILE</li>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>LOCAL_FILE_PATH</td>
        <td>Used when the configuration file is local. It shows where the configuration file is in the Operational System.<br>Example: C:/filehub/example.xml</td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_GIT_FILE_PATH</td>
        <td>
            Git repository file address (File URL)<br>
            Use <b>raw</b> file URL (plain text)
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_GIT_FILE_TOKEN</td>
        <td>Git repository authentication token</td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>MAX_FILE_SIZE</td>
        <td>Maximum file size allowed.<br>Default value: <b>7000000000</b></td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>MAX_REQUEST_SIZE</td>
        <td>Maximum request size allowed<br>Default value: <b>7000000000</b></td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


## Concepts

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Before executing the service it is necessary to define which storage platforms will be used, in addition to configuring 
the access parameters of each one independently. To do that the FileHub uses an XML file that will be read when the 
service starts. The file contains some elements that will process the requests. Each element will be explained next:


<!--------------------------------------------------------------------------------------------------------------------->


### Storage

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
It is used to represent a storage platform. A storage has an ID to identify it inside the service and a type. Each type 
corresponds to a service or a storage platform, for example, a FTP server, a cloud service like the AWS S3 or a local 
directory where the FileHub is running. In other words, each type has their own properties for access and specifications.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the configuration file the storages are defined inside of tag **storages** like shown in the example below:

````xml
<filehub>
   <storages>
       <storage id="S3-Test" type="AWS_S3">
           <region>us-east-2</region>
           <secretKeyId>G5HG4G66RDYIYE1</secretKeyId>
           <secretKey>6F51E6f1e6F7A2E4F761F61fd51s1F</secretKey>
           <bucket>test</bucket>
       </storage>
       <storage id="FileSystem-Test" type="FILE_SYSTEM">
           <baseDir>C:\Users\user\filehub</baseDir>
       </storage>
   </storages>
</filehub>
````
<p align="center"><sub>Storage declaration example</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
All storage elements have an **ID** and a **type**. The ID will identify the storage and the type will define which 
configuration properties the storage has. The storage types are listed next:


<!--------------------------------------------------------------------------------------------------------------------->


<table>
    <tbody>
      <tr style="background-color: white; color: black">
        <th colspan="1" style="text-align: center">
            <img src="https://user-images.githubusercontent.com/32067860/205457108-8a72b7de-fb67-49d8-8ce0-2e015105dca8.png" alt="drawing" width="80"/>
            <div>Local File System</div>
        </th>
        <td colspan="1">
            <div>It defines as storage a server directory where the FileHub is running.</div>
            <div>**Type:</b> FILE_SYSTEM</div>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td colspan="2">
            <div style="color: blue"><b>Properties:</b></div>
            <li>**baseDir:</b> root directory</li>
        </td>
      </tr>
      <!--######################################################################################-->  
      <tr style="background-color: transparent; border-width: 0px;"><td colspan="2"></td></tr>
      <tr style="background-color: white; color: black">
        <th colspan="1" style="text-align: center">
            <img src="https://user-images.githubusercontent.com/32067860/205457114-e7f44363-a144-4b14-938b-8fcb216546d1.png" alt="drawing" width="80"/>
            <div>Amazon S3</div>
        </th>
        <td colspan="1">
            <div>It defines a S3 bucket as a storage.</div>
            <div><b>Type:</b> AWS_S3</div>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td colspan="2">
            <div style="color: blue">**Properties:**</div>
            <li><b>region:</b> AWS region (e.g.: sa-east-1)</li>
            <li><b>secretKeyId:</b> IAM user ID</li>
            <li><b>secretKey:</b>  IAM user secret</li>
            <li><b>bucket:</b> S3 bucket name</li>
            <li><b>baseDir:</b> root directory</li>
        </td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


### Schema


&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A schema represents a storage set. When any operation is performed on FileHub, either upload or download, it will be 
necessary to inform the system what the schema is that will be considered. The FileHub service doesn’t perform 
operations directly on the storage element. It uses a schema that represents one or more storages.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The schemas are declared inside the **schemas** tag, where possible the declaration of more than one schema. All schema 
records have a **name** that will be the identifier on the request used in FileHub. It is possible to link the storages 
to a specific schema using the **storage-id** tag. The example below shows how to get a schema configuration with two 
storages linked.

````xml
<filehub>
    <storages>
        <storage id="S3-Test" type="AWS_S3">
            <region>us-east-2</region>
            <secretKeyId>G5HG4G66RDYIYE1</secretKeyId>
            <secretKey>6F51E6f1e6F7A2E4F761F61fd51s1F</secretKey>
            <bucket>test</bucket>
        </storage>
        <storage id="FileSystem-Test" type="FILE_SYSTEM">
            <baseDir>C:\Users\user\filehub</baseDir>
        </storage>
    </storages>
    <schemas>
        <schema name="MySchema">
            <storage-id>FileSystem-Test</storage-id>
            <storage-id>S3-Test</storage-id>
        </schema>
    </schemas>
</filehub>
````
<p align="center"><sub>Schema declaration example</sub></p> <br>

### Auto Schemas

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
It is not necessary to declare a schema for each storage to perform storage operations individually. It is possible to 
inform FileHub to perform the file reading, creating a schema for each existing storage. To do that, it uses the 
**generate-schema** attribute, filling as value, the schema’s name that will be created. See the example below:

````xml
<filehub>
    <storages>
        <storage id="S3-Test" type="AWS_S3" generate-schema="s3test">
            <region>us-east-2</region>
            <secretKeyId>G5HG4G66RDYIYE1</secretKeyId>
            <secretKey>6F51E6f1e6F7A2E4F761F61fd51s1F</secretKey>
            <bucket>test</bucket>
        </storage>
        <storage id="FileSystem-Test" type="FILE_SYSTEM">
            <baseDir>C:\Users\user\filehub</baseDir>
        </storage>
    </storages>
</filehub>
````
<p align="center"><sub>Example of schema creation directly on storage</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
It is also possible to use the attribute **generate-schema** on the **storages** element to create a schema 
with all existing storages. See the example below:

````xml
<filehub>
    <storages>
        <storage id="S3-Test" type="AWS_S3" generate-schema="s3test">
            <region>us-east-2</region>
            <secretKeyId>G5HG4G66RDYIYE1</secretKeyId>
            <secretKey>6F51E6f1e6F7A2E4F761F61fd51s1F</secretKey>
            <bucket>test</bucket>
        </storage>
        <storage id="FileSystem-Test" type="FILE_SYSTEM">
            <baseDir>C:\Users\user\filehub</baseDir>
        </storage>
    </storages>
</filehub>
````
<p align="center"><sub>Example of schema creation with all existing storages</sub></p> <br>

> **Warning**
> If an auto schema was created without a configured default trigger, the schema won’t have any kind of security.
<br>


<!--------------------------------------------------------------------------------------------------------------------->


### Trigger

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Triggers are used to guarantee security on operations. They work as **web hooks** that will validate if an operation is 
authorized or not by another service/application.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The trigger element has an **ID** to the identification and a **action** attribute that can assume two possible values:

1. **ALL:** it will consider the trigger to any kind of operation, be from writing
   (creation/updating/exclusion) or reading (download);
2. **UPDATE:** the trigger just will be applied to writing operations (creation/updating/exclusion).

<br>

> **Warning**
> The **default** term is a special value and cannot be used as ID to a trigger.

<br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
When a trigger is configured it is necessary to inform three properties:

1. **header:** it is a header name that should be sent to the authorization service.
2. **url:** it is the service endpoint that will validate if the request is valid or not. The request goal
   is to check if the header value is valid. If the response of that request does not return a 200 (OK)
   code, the operation will be canceled.
3.  **http-method (optional):** define which HTTP method type will be used on the request (GET, HEAD, POST, PUT,
    PATCH, DELETE, OPTIONS). The default value is GET.

<br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the XML configuration file, the triggers are defined inside of the **triggers** tag. A trigger should be linked 
to a schema. That bond is created through the **trigger** attribute used in the schema tag. All storages inside the 
schema consider the trigger on its operations.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
For clarification, see the following configuration example:


````xml
<filehub>
    <storages>
        <storage id="example" type="FILE_SYSTEM">
            <baseDir>C:\Users\user\filehub</baseDir>
        </storage>
    </storages>
    <trigger id="user-auth" action="ALL">
        <url>http://10.0.0.10:8080/auth</url>
        <header>myheader</header>
        <http-method>GET</http-method>
    </trigger>
    <schemas>
        <schema name="test" trigger="user-auth">
            <storage-id>example</storage-id>
        </schema>
    </schemas>
</filehub>
````
<p align="center"><sub>Trigger declaration example</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
We can observe the trigger **user-auth** was created and the schema **test** uses it. In the other words, each operation from 
the storage **example** will call the trigger to check the authorization.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The flowchart below shows the process considering the upload operation to the previous configuration.

<p align="center">
    <img src="https://user-images.githubusercontent.com/32067860/205391431-3c1d8d56-2bd8-48d1-9737-5469f3564cbb.png" alt="drawing" width="80%"/>
</p>
<p align="center">
    <sub>Flowchart of file uploading with trigger</sub>
</p>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The application that uses the FileHub service should send the trigger configured header with a value. When the 
FileHub receives the request, it will call the trigger configured endpoint, transferring the header to the authorization 
service to check the validation. A JWT token is a good example of using that process.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Another purpose of the triggers is to allow the creation of customized paths for the files. To explain that, imagine a 
system where each user has a directory to store their images. We will have URLs similar to the following list:

- /schema/example/user/**paul**/photo01
- /schema/example/user/**paul**/photo02
- /schema/example/user/**john**/photo01
- /schema/example/user/**john**/photo02
- /schema/example/user/**john**/photo03
<br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
You can see that to perform an upload or download operation, the consumer application should use the FileHub to manage 
the user logged identifiers. However, if the consumer application is a web interface, it will be possible to change that 
identifier, implicating the security of file accesses that are managed for FileHub. To deal with this problem, it is 
possible the trigger endpoint returns a parameter list that should be used to replace parts of the URL before completing 
an operation. The following sequence diagram shows that process:

<p align="center">
    <img src="https://user-images.githubusercontent.com/32067860/205391630-604078ed-1b27-4772-918a-6477f924f4e9.png" alt="drawing" width="80%"/>
</p>
<p align="center">
    <sub>Sequence diagram of trigger communication</sub>
</p>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The parameter returned from the Authorization Service response should have the same name as the parameter used in 
operation URL ($user = user).
<br>

> **Note**
> The **default** term is a special value and cannot be used as ID to a trigger.

> **Warning**
> If a trigger has the action attribute configured as the value UPDATE and the authorization header is filled
on the request, the trigger will call the configured endpoint even though.

<br>

#### Default Trigger

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
There is the possibility to create a trigger that will be called on all schemas without an explicit filled trigger. 
To do that, use the **default** attribute on the trigger as shown in the example below:

````xml
<filehub>
    <storages>
        <storage id="example" type="FILE_SYSTEM">
            <baseDir>C:\Users\user\filehub</baseDir>
        </storage>
    </storages>
    <trigger id="user-auth" action="ALL" default="true">
        <url>http://10.0.0.10:8080/auth</url>
        <header>myheader</header>
        <http-method>GET</http-method>
    </trigger>
</filehub>
````
<p align="center"><sub>Default trigger example</sub></p> <br>


<!--------------------------------------------------------------------------------------------------------------------->


## Operations

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
After the understanding of the main FileHub concepts, the next step is to know which operations you can execute by the service.


<!--------------------------------------------------------------------------------------------------------------------->


### Directories

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The directories are used as a way to group and organize the files. The major part of storage deals with the directory 
structure as a special file type, but there are cases such the AWS S3 that uses it as prefixes. In this case, the prefix 
and the filename together are the file identification key inside a bucket. The FileHub provides further directory 
management, allowing the following operations:

- Create a new directory
- Rename a directory
- Delete a directory
- List the existing files inside the directory, including others directories
- Check if the directory exists


##### Disable directory operations

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
To disable directory operations, it is possible use the no-dir attribute on a trigger as shown in the example below:

````xml
<trigger id="user-auth" action="ALL" no-dir="true">
    <url>http://10.0.0.10:8080/auth</url>
    <header>myheader</header>
    <http-method>GET</http-method>
</trigger>
````
<p align="center"><sub>Example of trigger with disabled directories</sub></p>


<!--------------------------------------------------------------------------------------------------------------------->


### Upload

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
An upload operation allows the sending of files that will be saved in all storages linked with a schema. When the 
FileHub receives the upload request and the file transfer begins, the FileHub can send the file to the storages by 
two ways:

- **Sequential transference:** It is the default transference type. The FileHub will transfer the files to each
  storage in a sequential way, following the storage declaration order from the schema.
- **Parallel transference:** The FileHub transfers the files to the storages at the same time. In this case,
  there isn’t a specific transference order.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Regardless of the transference type, the upload request will only return a response after the file transference has 
ended to all storages from the schema.


### Middle-Storage

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In some cases, where there exists only one storage in the schema and the files are small, the transference operation 
is executed quickly. On the other hand, there are cases where it is necessary to transfer greater files to more than 
one storage, and in these scenarios the request can take a significant amount of time. A way to soften that problem 
is to use the middle-storage concept.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The middle-storage defines which storage from a schema will be the intermediate between the consumer application and 
the rest of the storages. See the following example:

````xml
<filehub>
    <storages>
        <storage id="S3-Test" type="AWS_S3">
            <region>us-east-2</region>
            <secretKeyId>G5HG4G66RDYIYE1</secretKeyId>
            <secretKey>6F51E6f1e6F7A2E4F761F61fd51s1F</secretKey>
            <bucket>test</bucket>
        </storage>
        <storage id="FileSystem-Test" type="FILE_SYSTEM">
            <baseDir>C:\Users\user\filehub</baseDir>
        </storage>
    </storages>
    <schemas>
        <schema name="myschema" middle="FileSystem-Test">
            <storage-id>FileSystem-Test</storage-id>
            <storage-id>S3-Test</storage-id>
        </schema>
    </schemas>
</filehub>
````
<p align="center"><sub>Middle-storage example</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the example above, in an upload operation, the FileSystem-Test storage will receive the file, return the answer 
to the consumer application and will then transfer the file to the S3-Test storage.


### Temporary Middle-Storage

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A storage defined as middle-storage and not included in the one of schema storages will be a temporary storage. 
It will work like a middle-storage, but it will delete all files after the upload operation.

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p align="center"><sub>Temporary middle-storage example</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
As shown in the example above, the FileSystem-Test storage isn’t declared in any storage-id schema element. In 
other words, it is a temporary middle-storage.


<!--------------------------------------------------------------------------------------------------------------------->


### Download

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Different from the upload operation that does the communication among all the schema storages, the download operation 
will use the first schema storage to execute the transfer operation.


### Cache-Storage

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The **cache** attribute usage will affect the download operation. If the file isn’t inside of the first storage, 
the FileHub will check the file’s existence in the next storage. If the file is there, the FileHub will download 
it, leaving the file saved in the first storage as well.

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p align="center"><sub>Cache-storage example</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the previous example, if a file is missing from the FileSystem-Test storage, the FileHub will check if the S3-Test 
has the file. In the case of a positive result, the download operation will be executed, but also transferring the 
file to the FileSystem-Test. On the other hand, the FileHub will return a not found error.

<br>

> **Warning**
> If there is a middle-storage linked with the schema, that storage will be used to do the cache operation,
> in the opposite case, it will be the first storage from the schema.
> 
> It is not allowed to have a cache-storage and a temporary middle-storage configuration at the same time.

<br>

-----

### API Documentation

* Run the service and access: http://localhost:8088/swagger-ui.html
* Apiary Docs: https://filehub.docs.apiary.io

-----
