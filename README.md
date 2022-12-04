Read this in other languages: [English](README.md), [Portuguese](README.pt-br.md)

<div style="text-align: center; background-color: white; padding: 20px; border-radius: 15px;">
    <img src="https://user-images.githubusercontent.com/32067860/205196790-95818243-2378-4761-b3b6-c44eacdbf5f8.png" 
    alt="drawing" width="200"/>
</div>

<p>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    The FileHub is a service that standardizes file management, independent of the storage platform used. 
    Moreover, it makes file persistence easier when we think about multiple storage places, serving as requests 
    gateway, using a safe and easy way.
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h2>Configuration</h2>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The FileHub uses an XML configuration file where the properties and some rules are defined. That file can be created locally where the service is executed or remotely using a Git repository.
The following table shows the environment variables used to define where the configuration file is:
</p>

<table>
    <thead>
      <tr style="background-color: #E1E1E1; color: black">
        <th colspan="2" style="text-align: left; font-size: small">
            <i style="color: red">*</i> Required
        </th>
      </tr>
    </thead>
    <tbody>
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
<h3>Concepts</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Before executing the service it is necessary to define which storage platforms will be used, in addition to configuring 
the access parameters of each one independently. To do that the FileHub uses an XML file that will be read when the 
service starts. The file contains some elements that will process the requests. Each element will be explained next:
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Storage</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
It is used to represent a storage platform. A storage has an ID to identify it inside the service and a type. Each type 
corresponds to a service or a storage platform, for example, a FTP server, a cloud service like the AWS S3 or a local 
directory where the FileHub is running. In other words, each type has their own properties for access and specifications.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the configuration file the storages are defined inside of tag <b>storages</b> like shown in the example below:
</p>

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
<p style="font-size: smaller; text-align: center">Storage declaration example<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
All storage elements have an <b>ID</b> and a <b>type</b>. The ID will identify the storage and the type will define which 
configuration properties the storage has. The storage types are listed next:
</p>

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
            <div><b>Type:</b> FILE_SYSTEM</div>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td colspan="2">
            <div style="color: blue"><b>Properties:</b></div>
            <li><b>baseDir:</b> root directory</li>
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
            <div style="color: blue"><b>Properties:</b></div>
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


<h3>Schema</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A schema represents a storage set. When any operation is performed on FileHub, either upload or download, it will be 
necessary to inform the system what the schema is that will be considered. The FileHub service doesn’t perform 
operations directly on the storage element. It uses a schema that represents one or more storages.
</p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The schemas are declared inside the <b>schemas</b> tag, where possible the declaration of more than one schema. All schema 
records have a <b>name</b> that will be the identifier on the request used in FileHub. It is possible to link the storages 
to a specific schema using the <b>storage-id</b> tag. The example below shows how to get a schema configuration with two 
storages linked.
</p>

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
<p style="font-size: smaller; text-align: center">Schema declaration example<p>

<h3>Auto Schemas</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
It is not necessary to declare a schema for each storage to perform storage operations individually. It is possible to 
inform FileHub to perform the file reading, creating a schema for each existing storage. To do that, it uses the 
<b>generate-schema</b> attribute, filling as value, the schema’s name that will be created. See the example below:
</p>

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
<p style="font-size: smaller; text-align: center">Example of schema creation directly on storage<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
It is also possible to use the attribute <b>generate-schema</b> on the <b>storages</b> element to create a schema 
with all existing storages. See the example below:
</p>

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
<p style="font-size: smaller; text-align: center">Example of schema creation with all existing storages<p>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th colspan="2" style="text-align: center">
            Alert
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            If an auto schema was created without a configured default trigger, the schema won’t have any kind of security.
        </td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Trigger</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Triggers are used to guarantee security on operations. They work as <b>web hooks</b> that will validate if an operation is 
authorized or not by another service/application.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The trigger element has an <b>ID</b> to the identification and a <b>action</b> attribute that can assume two possible values:
</p>

<ol style="margin-left: 50px">
    <li>
        <b>ALL:</b> it will consider the trigger to any kind of operation, be from writing 
        (creation/updating/exclusion) or reading (download);
    </li>
    <li>
        <b>UPDATE:</b> the trigger just will be applied to writing operations (creation/updating/exclusion).
    </li>
</ol>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th style="text-align: center">
            Alert
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            The <b>default</b> term is a special value and cannot be used as ID to a trigger.
        </td>
      </tr>
    </tbody>
</table>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
When a trigger is configured it is necessary to inform three properties:
</p>

<ol style="margin-left: 50px">
    <li>
        <b>header:</b> it is a header name that should be sent to the authorization service.
    </li>
    <li>
        <b>url:</b> it is the service endpoint that will validate if the request is valid or not. The request goal 
        is to check if the header value is valid. If the response of that request does not return a 200 (OK) 
        code, the operation will be canceled.
    </li>
    <li>
        <b>http-method (optional):</b> define which HTTP method type will be used on the request (GET, HEAD, POST, PUT, 
        PATCH, DELETE, OPTIONS). The default value is GET.
    </li>
</ol>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the XML configuration file, the triggers are defined inside of the <b>triggers</b> tag. A trigger should be linked 
to a schema. That bond is created through the <b>trigger</b> attribute used in the schema tag. All storages inside the 
schema consider the trigger on its operations.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
For clarification, see the following configuration example:
</p>

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
<p style="font-size: smaller; text-align: center">Exemplo de declaração de trigger<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
We can observe the trigger <b>user-auth</b> was created and the schema <b>test</b> uses it. In the other words, each operation from 
the storage <b>example</b> will call the trigger to check the authorization.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The flowchart below shows the process considering the upload operation to the previous configuration.
</p>

<div style="text-align: center">
    <img src="https://user-images.githubusercontent.com/32067860/205391431-3c1d8d56-2bd8-48d1-9737-5469f3564cbb.png" alt="drawing" width="80%"/>
    <p style="font-size: smaller">Flowchart of file uploading with trigger<p>
</div>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The application that uses the FileHub service should send the trigger configured header with a value. When the 
FileHub receives the request, it will call the trigger configured endpoint, transferring the header to the authorization 
service to check the validation. A JWT token is a good example of using that process.
</p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Another purpose of the triggers is to allow the creation of customized paths for the files. To explain that, imagine a 
system where each user has a directory to store their images. We will have URLs similar to the following list:
</p>

<ul style="margin-left: 50px">
    <li>/schema/example/user/<b>paul</b>/photo01</li>
    <li>/schema/example/user/<b>paul</b>/photo02</li>
    <li>/schema/example/user/<b>john</b>/photo01</li>
    <li>/schema/example/user/<b>john</b>/photo02</li>
    <li>/schema/example/user/<b>john</b>/photo03</li>
</ul>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
You can see that to perform an upload or download operation, the consumer application should use the FileHub to manage 
the user logged identifiers. However, if the consumer application is a web interface, it will be possible to change that 
identifier, implicating the security of file accesses that are managed for FileHub. To deal with this problem, it is 
possible the trigger endpoint returns a parameter list that should be used to replace parts of the URL before completing 
an operation. The following sequence diagram shows that process:
</p>

<div style="text-align: center">
    <img src="https://user-images.githubusercontent.com/32067860/205391630-604078ed-1b27-4772-918a-6477f924f4e9.png" alt="drawing" width="80%"/>
    <p style="font-size: smaller">Sequence diagram of trigger communication<p>
</div>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The parameter returned from the Authorization Service response should have the same name as the parameter used in 
operation URL ($user = user).
</p>

<table>
    <thead>
      <tr style="background-color: #FFF2CC; color: black">
        <th style="text-align: center">
            Tip
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            The <b>file name</b> can be also modified by the Authorization Service response. 
            Use the <b style="color: blue">filename</b> parameter to do that.
        </td>
      </tr>
    </tbody>
</table>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th style="text-align: center">
            Alert
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            If a trigger has the action attribute configured as the value UPDATE and the authorization header is filled 
            on the request, the trigger will call the configured endpoint even though.
        </td>
      </tr>
    </tbody>
</table>

<h4>Default Trigger</h4>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
There is the possibility to create a trigger that will be called on all schemas without an explicit filled trigger. 
To do that, use the <b>default</b> attribute on the trigger as shown in the example below:
</p>

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
<p style="font-size: smaller; text-align: center">Default trigger example<p>


<!--------------------------------------------------------------------------------------------------------------------->


<h2>Operations</h2>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
After the understanding of the main FileHub concepts, the next step is to know which operations you can execute by the service.
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h5>Directories</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The directories are used as a way to group and organize the files. The major part of storage deals with the directory 
structure as a special file type, but there are cases such the AWS S3 that uses it as prefixes. In this case, the prefix 
and the filename together are the file identification key inside a bucket. The FileHub provides further directory 
management, allowing the following operations:
</p>

<ul style="margin-left: 50px">
    <li>Create a new directory</li>
    <li>Rename a directory</li>
    <li>Delete a directory</li>
    <li>List the existing files inside the directory, including others directories</li>
    <li>Check if the directory exists</li>
</ul>

<h5>Disable directory operations</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
To disable directory operations, it is possible use the no-dir attribute on a trigger as shown in the example below:
</p>

````xml
<trigger id="user-auth" action="ALL" no-dir="true">
    <url>http://10.0.0.10:8080/auth</url>
    <header>myheader</header>
    <http-method>GET</http-method>
</trigger>
````
<p style="font-size: smaller; text-align: center">Example of trigger with disabled directories<p>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Upload</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
An upload operation allows the sending of files that will be saved in all storages linked with a schema. When the 
FileHub receives the upload request and the file transfer begins, the FileHub can send the file to the storages by 
two ways:
</p>

<ul style="margin-left: 50px">
    <li>
        <b>Sequential transference:</b> It is the default transference type. The FileHub will transfer the files to each 
        storage in a sequential way, following the storage declaration order from the schema.
    </li>
    <li>
        <b>Parallel transference:</b> The FileHub transfers the files to the storages at the same time. In this case, 
        there isn’t a specific transference order.
    </li>
</ul>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Regardless of the transference type, the upload request will only return a response after the file transference has 
ended to all storages from the schema.
</p>

<h3>Middle-Storage</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In some cases, where there exists only one storage in the schema and the files are small, the transference operation 
is executed quickly. On the other hand, there are cases where it is necessary to transfer greater files to more than 
one storage, and in these scenarios the request can take a significant amount of time. A way to soften that problem 
is to use the middle-storage concept.
</p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The middle-storage defines which storage from a schema will be the intermediate between the consumer application and 
the rest of the storages. See the following example:
</p>

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
<p style="font-size: smaller; text-align: center">Middle-storage example<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the example above, in an upload operation, the FileSystem-Test storage will receive the file, return the answer 
to the consumer application and will then transfer the file to the S3-Test storage.
</p>

<h3>Temporary Middle-Storage</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A storage defined as middle-storage and not included in the one of schema storages will be a temporary storage. 
It will work like a middle-storage, but it will delete all files after the upload operation.
</p>

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p style="font-size: smaller; text-align: center">Temporary middle-storage example<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
As shown in the example above, the FileSystem-Test storage isn’t declared in any storage-id schema element. In 
other words, it is a temporary middle-storage.
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Download</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Different from the upload operation that does the communication among all the schema storages, the download operation 
will use the first schema storage to execute the transfer operation.
</p>

<h3>Cache-Storage</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
The <b>cache</b> attribute usage will affect the download operation. If the file isn’t inside of the first storage, 
the FileHub will check the file’s existence in the next storage. If the file is there, the FileHub will download 
it, leaving the file saved in the first storage as well.
</p>

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p style="font-size: smaller; text-align: center">Cache-storage example<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
In the previous example, if a file is missing from the FileSystem-Test storage, the FileHub will check if the S3-Test 
has the file. In the case of a positive result, the download operation will be executed, but also transferring the 
file to the FileSystem-Test. On the other hand, the FileHub will return a not found error.
</p>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th style="text-align: center">
            Alert
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            If there is a middle-storage linked with the schema, that storage will be used to do the cache operation, 
            in the opposite case, it will be the first storage from the schema.
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>
            It is not allowed to have a cache-storage and a temporary middle-storage configuration at the same time.
        </td>
      </tr>
    </tbody>
</table>
<br>

-----

<h3>API Documentation</h3>

* Run the service and access: http://localhost:8088/swagger-ui.html
* Apiary Docs: https://filehub.docs.apiary.io

-----


