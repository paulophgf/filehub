<div align="center">
<img src="https://user-images.githubusercontent.com/32067860/205514546-468d61e5-28a4-4e6f-a172-857330620b79.png" alt="drawing" width="200"/>

![REST API](https://img.shields.io/badge/Config_Type-XML-red)
[![Java Version](https://img.shields.io/badge/Java-19-blue)](https://www.oracle.com/br/java/technologies/downloads/)
[![Spring Boot Version](https://img.shields.io/badge/Spring_Boot-2.7.6-darkgreen)](https://www.oracle.com/br/java/technologies/downloads/)
[![License](https://shields.io/badge/License-MIT%2FApache--2.0-blue)](https://github.com/burn-rs/burn/blob/master/LICENSE)

</div>

Leia esse documento em outro idioma: [Inglês](README.md), [Português](README.pt-br.md)

> O FileHub é um serviço que padroniza o gerenciamento de arquivos, independente da plataforma de armazenamento 
> utilizada. Além disso, ele facilita a persistência de arquivos em mais de uma plataforma de armazenamento, 
> servindo como gateway de requisições, de forma segura e prática.


__Seções__

* [Configuração](#configuração)
* [Conceitos](#conceitos)
  * [Storage](#storage)
  * [Schema](#schema)
  * [Trigger](#trigger)
* [Operações](#operações)
  * [Diretórios](#diretórios)
  * [Upload](#upload)
  * [Middle-Storage](#middle-storage)
  * [Download](#download)
  * [Cache-Storage](#cache-storage)
* [Documentação da API](#documentação-da-api)
* [Configuração Docker](#configuração-docker)

<!--------------------------------------------------------------------------------------------------------------------->


## Configuração


&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O FileHub utiliza um arquivo de configuração XML, onde são definidas as propriedades e como o serviço irá se comportar. 
Este arquivo poderá ser criado localmente onde o serviço será executado ou remotamente em um repositório Git.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Para informar ao serviço onde está o arquivo de configuração, utiliza-se as seguintes variáveis de ambiente:

<table>
    <tbody>
      <tr>
        <td colspan="2" style="text-align: left">
            * <sub>Informação Obrigatória</sub>
        </td>
      </tr>
      <tr style="background-color: #DAE8FC; color: black">
        <th>Nome da variável</th>
        <th>Descrição</th>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_TYPE <i style="color: red">*</i></td>
        <td>Define se o arquivo está localizado localmente ou remotamente.<br>
            Valor padrão: <b>LOCAL_FILE</b><br>
            Valores possíveis:<br>
            <li>LOCAL_FILE</li>
            <li>GIT_FILE</li>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>LOCAL_FILE_PATH</td>
        <td>Caminho do arquivo no Sistema Operacional <br>Exemplo: C:/filehub/example.xml</td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_GIT_FILE_PATH</td>
        <td>
            Endereço do arquivo no repositório Git<br>
            Obs: Use a URL <b>raw</b> do arquivo no repositório (texto plano) (plain text)
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_GIT_FILE_TOKEN</td>
        <td>Token de autenticação do repositório Git</td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>MAX_FILE_SIZE</td>
        <td>Tamanho máximo do arquivo.<br>Valor padrão: <b>7000000000</b></td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>MAX_REQUEST_SIZE</td>
        <td>Tamanho máximo da requisição.<br>Valor padrão: <b>7000000000</b></td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


## Conceitos

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Antes de executar o serviço é necessário definir quais plataformas de armazenamento serão utilizadas, além de 
configurar os parâmetros de acesso de cada plataforma de forma independente. Para isso o FileHub utiliza um 
arquivo XML que será lido quando o serviço iniciar. Nele existem elementos que irão determinar como o FileHub 
irá processar as requisições. Cada elemento é descrito a seguir:


<!--------------------------------------------------------------------------------------------------------------------->


### Storage

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
É o elemento que representa uma plataforma de armazenamento. Um storage possui um ID para identificá-lo no sistema e 
um tipo. Cada tipo corresponde a um serviço ou plataforma de armazenamento, como por exemplo, um servidor FTP, um 
serviço em cloud como o S3 da AWS ou um diretório do servidor onde o FileHub está sendo executado, ou seja, cada 
tipo de storage possui suas propriedades para acesso e especificações.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No arquivo de configuração os storages são definidos dentro da tag **storages**, como no exemplo abaixo:

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
<p align="center"><sub>Exemplo de declaração de storage</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Todo elemento storage possui um **ID** e um **type**. O ID irá identificar o storage e o type irá definir quais 
as propriedades de configuração o storage possui. Os tipos de storages são listados a seguir:


<!--------------------------------------------------------------------------------------------------------------------->


<table>
    <tbody>
      <tr style="background-color: white; color: black">
        <th colspan="1" style="text-align: center">
            <img src="https://user-images.githubusercontent.com/32067860/205519206-bf7fe5be-d8d6-4853-8119-d61c7c11a8f8.png" alt="drawing" width="80"/>
            <div>Local File System</div>
        </th>
        <td colspan="1">
            <div>Define um diretório do servidor onde o FileHub está sendo executado como storage.</div>
            <div><b>Type:</b> FILE_SYSTEM</div>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td colspan="2">
            <div style="color: blue"><b>Propriedades:</b></div>
            <li><b>baseDir:</b> diretório raiz</li>
        </td>
      </tr>
      <!--######################################################################################-->  
      <tr style="background-color: transparent; border-width: 0px;"><td colspan="2"></td></tr>
      <tr style="background-color: white; color: black">
        <th colspan="1" style="text-align: center">
            <img src="https://user-images.githubusercontent.com/32067860/205519209-92f81d0b-b38a-416b-923e-cb2d2cab8ee4.png" alt="drawing" width="80"/>
            <div>Amazon S3</div>
        </th>
        <td colspan="1">
            <div>Define um bucket do serviço S3 da AWS como storage.</div>
            <div><b>Type:</b> AWS_S3</div>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td colspan="2">
            <div style="color: blue"><b>Propriedades:</b></div>
            <li><b>region:</b> região onde o S3 está localizado (e.g.: sa-east-1) </li>
            <li><b>secretKeyId:</b> ID do usuário no IAM</li>
            <li><b>secretKey:</b>  código do secret do usuário no IAM</li>
            <li><b>bucket:</b> nome do bucket do S3</li>
            <li><b>baseDir:</b> diretório raiz</li>
        </td>
      </tr>
      <!--######################################################################################-->  
      <tr style="background-color: transparent; border-width: 0px;"><td colspan="2"></td></tr>
      <tr style="background-color: white; color: black">
        <th colspan="1" style="text-align: center">
            <img src="https://user-images.githubusercontent.com/32067860/209043609-a6bacd02-19b8-4b01-9887-98a0f96ca1c1.png" alt="drawing" width="80"/>
            <div>Google Cloud Storage</div>
        </th>
        <td colspan="1">
            <div>Cria um link com um bucket do google cloud storage</div>
            <div><b>Type:</b> GOOGLE_CLOUD</div>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td colspan="2">
            <div style="color: blue"><b>Propriedades:</b></div>
            <li>
                <b>jsonCredentials:</b> Objeto JSON gerado por uma conta de serviço 
                <br/>(APIs e serviços > Credenciais > Contas de serviço > Chaves) 
                <br/>Faça o download do arquivo da chave, copie o conteúdo do arquivo (objeto JSON) e cole dentro da tag jsonCredentials
            </li>
            <li><b>bucket:</b> nome do bucket do storage</li>            
            <li><b>baseDir:</b> diretório raiz</li>
        </td>
      </tr>
      <!--######################################################################################-->  
      <tr style="background-color: transparent; border-width: 0px;"><td colspan="2"></td></tr>
      <tr style="background-color: white; color: black">
        <th colspan="1" style="text-align: center">
            <img src="https://user-images.githubusercontent.com/32067860/207856945-47f8929e-8292-42fe-91d4-0a6495c264e0.png" alt="drawing" width="80"/>
            <div>Dropbox</div>
        </th>
        <td colspan="1">
            <div>Cria um link com uma conta do dropbox</div>
            <div><b>Type:</b> DROPBOX</div>
            <div><b>Limitação da Integração: </b>
               A operação de atualização do token não foi implementada.
               É necessário gerar um token toda vez que for utilizar esse tipo de storage.
            </div>
            <div><b>Limitação da Integração:</b>
                <li><b>Access Token:</b> A operação de atualização do token não foi implementada.
                É necessário gerar um token toda vez que for utilizar esse tipo de storage.</li>
                <li><b>Tamanho dos arquivosFile Size:</b> O tamanho máximo do arquivo é de 150 Mb. 
                Operações com arquivos maiores são irão funcionar.</li>
            </div>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td colspan="2">
            <div style="color: blue"><b>Propriedades:</b></div>
            <li><b>accessToken:</b> token de acesso</li>
            <li><b>baseDir:</b> diretório raiz</li>
        </td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


### Schema


&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Um schema representa um conjunto de storages. Ao realizar qualquer operação no FileHub, seja de upload ou download, 
será necessário informar qual o schema que deverá ser considerado. Em outras palavras, o serviço FileHub não realiza 
operações diretamente em um elemento Storage, mas sim em um schema que representa um ou mais storages.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Os schemas são declarados dentro da tag **schemas**, sendo possível a declaração de mais de um schema. Todo registro de 
schema possui um **name** que será o identificador do mesmo nas requisições realizadas no FileHub. Para vincular os 
storages a determinado schema utilizamos a tag **storage-id**. O exemplo abaixo mostra como fica uma configuração de 
um schema que possui dois storages vinculados.

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
<p align="center"><sub>Exemplo de declaração de schema</sub></p> <br>

### Schemas Auto Gerados

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Não é necessário declarar um schema para cada storage caso exista a necessidade de realizar operações nos storages de 
forma individual. É possível fazer com que o FileHub realize a leitura do arquivo de configuração, criando um schema 
para cada storage existente. Para isso, utilize o atributo **generate-schema**, informando como valor, o nome do schema 
que deverá ser criado. Veja o exemplo abaixo:


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
<p align="center"><sub>Exemplo de geração de schema diretamente no storage</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Utilize o atributo **generate-schema** no elemento storages caso seja necessário criar um schema com todos os 
storages existentes. Veja o exemplo abaixo:

````xml
<filehub>
    <storages generate-schema="all">
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
<p align="center"><sub>Exemplo de geração de schema com todos os storages existentes</sub></p> <br>

> **Warning**
> Se um schema auto gerado foi criado sem uma trigger default configurada, o schema não terá nenhum tipo de segurança.
<br>


<!--------------------------------------------------------------------------------------------------------------------->


### Trigger

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Triggers são utilizadas para garantir a segurança das operações. Funcionam como **web hooks** que irão validar se 
determinada operação está autorizada ou não por sua aplicação.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O elemento trigger possui um **ID** para identificação e também um atributo **action**, que poderá assumir dois valores possíveis:


1. **ALL:** irá considerar a trigger para qualquer tipo de operação, seja de escrita (upload/criação/exclusão) 
ou leitura (download);
2. **UPDATE:** a trigger só será aplicada para operações de escrita (upload/criação/exclusão).

<br>

> **Warning**
> O termo **default** é um valor especial e não pode ser utilizado como ID para uma trigger.

<br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Ao configurar uma trigger três propriedades deverão ser informadas:

1. **header:** é o nome do header que deverá ser enviado ao serviço de autorização.
2. **url:** é o endpoint do serviço que irá validar se a requisição é válida ou não. Seu objetivo é verificar 
se o valor do header é válido. Caso a requisição enviada para este endpoint retornar um código HTTP diferente 
de 200 (OK) a operação é cancelada.
3. **http-method (optional):** define qual o tipo do método HTTP utilizado na requisição (GET, HEAD, POST, PUT, 
PATCH, DELETE, OPTIONS). O valor padrão é **GET**.

<br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No XML de configuração as triggers são definidas dentro da tag **triggers**. Uma trigger deverá ser vinculada a um schema. 
Esse vínculo é criado através do atributo **trigger** do schema, isso faz com que todos os storages do schema passem a 
considerar a trigger em suas operações.


&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Para deixar mais claro, considere o seguinte exemplo de configuração:


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
<p align="center"><sub>Exemplo de declaração de trigger</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Observe que a trigger **user-auth** foi criada e o schema **test** faz o uso da mesma, ou seja, cada operação realizada 
para o storage **example** irá chamar a trigger para verificação de autorização.


&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O fluxograma abaixo apresenta o processo considerando a operação de **upload** para a configuração anterior.

<p align="center">
    <img src="https://user-images.githubusercontent.com/32067860/209050352-160bf404-1552-4547-88a7-3dc5a5537257.png" alt="drawing" width="80%"/>
</p>
<p align="center">
    <sub>Fluxograma de upload de arquivo com trigger</sub>
</p>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A aplicação que consome o serviço FileHub deverá enviar o header configurado na trigger com um valor, ao receber 
a requisição, o FileHub irá chamar o endpoint configurado na trigger repassando o header para que o serviço de 
autorização faça a devida validação. Um token JWT é um bom exemplo do uso desse processo.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Quando uma trigger realiza uma request para a URL configurada, ela enviará as seguintes informações no corpo da requisição (*request body*):
- **schema:** o nome do schema selecionado na operação
- **operation:** o tipo de operação está sendo executada (CREATE_DIRECTORY, RENAME_DIRECTORY, DELETE_DIRECTORY, LIST_FILES, EXIST_DIRECTORY, UPLOAD_MULTIPART_FILE, UPLOAD_BASE64_FILE, DOWNLOAD_FILE, DELETE_FILE, EXIST_FILE, GET_FILE_DETAILS)
- **path:** o caminho informado
- **filenames:** uma lista com os nomes dos arquivos que estão sendo manipulados na operação

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O seguinte JSON mostra um exemplo desse *request body*:
````json
{
        "schema": "test", 
        "operation": "UPLOAD_MULTIPART_FILE", 
        "path": "/accounts/users/avatar/", 
        "filenames": [ "MyAvatar.jpeg" ]
}
````

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Outra função das triggers é permitir a criação de caminhos customizáveis para os arquivos. Para deixar mais 
claro essa função da trigger, imagine um sistema onde cada usuário possui um diretório para armazenar suas 
imagens, teríamos URLs semelhantes a seguinte lista:

- /schema/example/user/**paul**/photo01
- /schema/example/user/**paul**/photo02
- /schema/example/user/**john**/photo01
- /schema/example/user/**john**/photo02
- /schema/example/user/**john**/photo03
  <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Observe que para realizar uma operação de upload ou download, a aplicação que irá consumir o FileHub deverá 
gerenciar os identificadores dos usuários logados. Porém, se essa aplicação consumidora for uma interface web, 
seria possível alterar esse identificador, comprometendo a segurança no acesso aos arquivos gerenciados pelo 
FileHub. Para contornar esse problema existe a possibilidade do endpoint configurado na trigger, retornar uma 
lista de parâmetros que deverão ser utilizados para substituir partes da URL nas operações. O diagrama de 
sequência a seguir mostra esse processo:

<p align="center">
    <img src="https://user-images.githubusercontent.com/32067860/209051058-65996664-5e72-4a16-bb7b-fe8eb65d1352.png" alt="drawing" width="80%"/>
</p>
<p align="center">
    <sub>Diagrama de sequência do processo de comunicação com trigger</sub>
</p>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Note que o parâmetro retornado da resposta do Authorization Service deverá ter o mesmo nome que o parâmetro 
informado na URL da operação ($user = user).

<br>

> **Note**
> O **nome do arquivo** também pode ser alterado pelo retorno da requisição através do parâmetro chamado **filename**.

> **Warning**
> Caso uma trigger esteja configurada com o atributo action como UPDATE e o header de autorização configurado, 
> seja enviado na requisição, a trigger irá chamar o endpoint configurado.

<br>

#### Trigger Padrão

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Existe a possibilidade de criar uma trigger que será chamada em todos os schemas que não definirem uma trigger de 
forma explícita. Para isso, utiliza-se o atributo **default** na trigger como mostrado no exemplo abaixo:

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
<p align="center"><sub>Exemplo de trigger padrão</sub></p> <br>


<!--------------------------------------------------------------------------------------------------------------------->


## Operações

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Após o entendimento dos principais conceitos do FileHub, o próximo passo é saber quais as possíveis operações 
que podem ser executadas pelo serviço.


<!--------------------------------------------------------------------------------------------------------------------->


### Diretórios

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Os diretórios podem ser vistos como uma forma de agrupar e organizar os arquivos. A maioria dos storages tratam 
os diretórios como um tipo especial de arquivo, porém existem casos como o S3 da AWS que tratam os diretórios 
como prefixos, que juntamente com o nome do arquivo, compõem a chave de identificação do arquivo dentro de um 
bucket. O FileHub facilita o gerenciamento de diretórios, permitindo as seguintes operações:

- Criar um novo diretório
- Renomear um diretório
- Deletar um diretório
- Listar os arquivos existentes dentro do diretório, incluindo outros diretórios
- Verificar se o diretório existe


##### Desativar operações em diretórios

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Caso exista a necessidade de desativar as operações em diretórios, pode-se utilizar o atributo **no-dir**
em uma trigger como mostrado no exemplo abaixo.

````xml
<trigger id="user-auth" action="ALL" no-dir="true">
    <url>http://10.0.0.10:8080/auth</url>
    <header>myheader</header>
    <http-method>GET</http-method>
</trigger>
````
<p align="center"><sub>Exemplo de trigger com desativação de diretórios</sub></p>


<!--------------------------------------------------------------------------------------------------------------------->


### Upload

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A operação de upload permite que sejam enviados arquivos que serão salvos em todos os storages vinculados a 
determinado schema. Quando o FileHub recebe a requisição de upload e a transferência dos arquivos inicia, o 
FileHub pode enviar o arquivo para os storages de duas maneiras:

- **Transferência sequencial:** É o tipo de transferência padrão. O FileHub irá transferir os arquivos para cada 
  um dos storages de forma sequencial, obedecendo a ordem de declaração dos storages no schema.
- **Transferência paralela:** O FileHub transfere os arquivos para os storages ao mesmo tempo. Nesse caso não 
  existe uma ordem de transferência. Para utilizar essa configuração é necessário colocar o atributo
  **parallel-upload** na tag schema com o valor **true**.

````xml
<schemas>
    <schema name="test-parallel" parallel-upload="true">
        <storage-id>example</storage-id>
    </schema>
</schemas>
````
<p align="center"><sub>Exemplo de configuração de tranferência paralela</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Independente do tipo de transferência realizada, a requisição de upload só irá retornar uma resposta após o 
término da transferência dos arquivos para todos os storages do schema.


### Middle-Storage

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Em alguns cenários, onde se tem apenas 1 storage no schema e os arquivos são pequenos, a operação de transferência 
é executada rapidamente. Porém, existem casos onde é necessário transferir arquivos maiores para mais de 1 storage, 
e nesses casos a requisição pode levar um tempo considerável. Para amenizar este problema, utiliza-se o conceito 
de **middle-storage**.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O middle-storage define qual dos storages do schema irá servir como intermediário entre a aplicação consumidora 
e o restante dos storages. Veja o exemplo a seguir:

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
<p align="center"><sub>Exemplo de middle-storage</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No exemplo acima, em uma operação de upload, o storage FileSystem-Test irá receber o arquivo, retornar a resposta 
para a aplicação consumidora e depois irá transferir o arquivo para o storage S3-Test.


### Middle-Storage Temporário

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Um storage definido como middle-storage e não incluído como um dos storage do schema será considerado um storage 
temporário. Esse storage irá funcionar igual ao middle-storage, porém irá deletar os arquivos após a operação de upload.

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p align="center"><sub>Exemplo de middle-storage temporário</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Como mostrado no exemplo acima, o storage FileSystem-Test não está declarado em nenhum elemento storage-id dentro 
do schema, ou seja, ele é um middle-storage temporário.


<!--------------------------------------------------------------------------------------------------------------------->


### Download

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Diferente do upload que faz a comunicação com todos os storages de um schema, o download irá utilizar o 
primeiro storage declarado para fazer a operação de download.


### Cache-Storage

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O uso do atributo **cache** no schema irá afetar a operação de download. Caso o arquivo não exista no primeiro 
storage do schema, o FileHub irá verificar a existência do arquivo no próximo storage. Se o arquivo existir, o 
FileHub fará o download do mesmo, porém deixando o arquivo salvo no primeiro storage também.

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test" cache="true">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p align="center"><sub>Exemplo de schema com cache</sub></p> <br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No exemplo anterior, caso seja realizado o download de um arquivo que não exista no FileSystem-Test, o FileHub 
irá verificar se o S3-Test possui o arquivo. Em caso positivo o download será executado, porém transferindo o 
arquivo também para o FileSystem-Test, o primeiro storage consultado.

<br>

> **Warning**
> Caso exista um middle-storage associado ao schema, o mesmo será utilizado para o cache, caso contrário, 
> será o primeiro storage do schema.

> **Warning**
> Não é possível ter um cache-storage atuando como middle-storage temporário.

<br>

-----

### Documentação da API

* Execute o serviço e acesso o seguinte endpoint: http://localhost:8088/swagger-ui/index.html
* Documentação no Apiary: https://filehub.docs.apiary.io

-----


## Configuração Docker


**Link do DockerHub:** https://hub.docker.com/repository/docker/paulophgf/filehub


**Comando Docker Run** 

> docker run -d --name filehub -v {LOCAL_DIR}:/filehub paulophgf/filehub:{FILEHUB_VERSION}

Exemplo:
````shell
docker run -d --name filehub -v //c/Users/user/filehub:/filehub paulophgf/filehub:1.0.0
````

**Compose**
````yaml
version: '3.1'

services:

  filehub:
    image: paulophgf/filehub:1.0.0
    hostname: filehub
    container_name: filehub
    restart: always
    networks:
      - filehub-default
    ports:
      - "8088:8088"
    volumes:
      - /etc/hosts:/etc/hosts:ro
      - {LOCAL_DIR}:/filehub # Substitua o valor da variável {LOCAL_DIR} | Exemplos: Win: C:\Users\%user%\filehub ou Linux: /filehub
    environment:
      CONFIG_TYPE: "LOCAL_FILE" # Escolha umas das opções LOCAL_FILE ou GIT_FILE
      LOCAL_FILE_PATH: "filehub/fh-config.xml"
      CONFIG_GIT_FILE_PATH: "" # Preencha esta variável caso tenha escolhido GIT_FILE como CONFIG_TYPE
      CONFIG_GIT_FILE_TOKEN: "" # Preencha esta variável caso tenha escolhido GIT_FILE como CONFIG_TYPE
      JAVA_OPTS : "-Xms512m -Xmx1024m"

networks:
  filehub-default:
    name: filehub-default
````