Leia este documento em outro idioma: [English](README.md), [Portuguese](README.pt-br.md)

<div style="text-align: center; background-color: white; padding: 20px; border-radius: 15px;">
    <img src="https://user-images.githubusercontent.com/32067860/205196790-95818243-2378-4761-b3b6-c44eacdbf5f8.png" 
    alt="drawing" width="200"/>
</div>

<p>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    O FileHub é um serviço que padroniza o gerenciamento de arquivos, independente da plataforma de armazenamento 
    utilizada. Além disso, ele facilita a persistência de arquivos em mais de uma plataforma de armazenamento, 
    servindo como gateway de requisições, de forma segura e prática.
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h2>Configuração</h2>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O FileHub utiliza um arquivo de configuração XML, onde são definidas as propriedades e como o serviço irá se comportar. 
Este arquivo poderá ser criado localmente onde o serviço será executado ou remotamente em um repositório Git.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Para informar ao serviço onde está o arquivo de configuração, utiliza-se as seguintes variáveis de ambiente:
</p>

<table>
    <thead>
      <tr style="background-color: #E1E1E1; color: black">
        <th colspan="2" style="text-align: left; font-size: small">
            <i style="color: red">*</i> Informação obrigatória
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: #DAE8FC; color: black">
        <th>Nome da variável</th>
        <th>Descrição</th>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_TYPE <i style="color: red">*</i></td>
        <td>Define se o arquivo está localizado localmente ou remotamente.<br>
            Valor Padrão: <b>LOCAL_FILE</b><br>
            Valores possíveis:<br>
            <li>LOCAL_FILE</li>
            <li>GIT_FILE</li>
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>LOCAL_FILE_PATH</td>
        <td>Caminho do arquivo no Sistema Operacional<br>Exemplo: C:/filehub/example.xml</td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_GIT_FILE_PATH</td>
        <td>
            Endereço do arquivo no repositório Git
            Obs: Use a URL <b>raw</b> do arquivo no repositório (texto plano)
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>CONFIG_GIT_FILE_TOKEN</td>
        <td>Token de autenticação do repositório Git</td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>MAX_FILE_SIZE</td>
        <td>Tamanho máximo do arquivo. Valor Padrão: <b>7000000000</b></td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>MAX_REQUEST_SIZE</td>
        <td>Tamanho máximo da requisição. Valor Padrão: <b>7000000000</b></td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Conceitos</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Antes de executar o serviço é necessário definir quais plataformas de armazenamento serão utilizadas, além de configurar 
os parâmetros de acesso de cada plataforma de forma independente. Para isso o FileHub utiliza um arquivo XML que será 
lido quando o serviço iniciar. Nele existem elementos que irão determinar como o FileHub irá processar as requisições. 
Cada elemento é descrito a seguir:
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Storage</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
É o elemento que representa uma plataforma de armazenamento. Um storage possui um ID para identificá-lo no sistema e 
um tipo. Cada tipo corresponde a um serviço ou plataforma de armazenamento, como por exemplo, um servidor FTP, um 
serviço em cloud como o S3 da AWS ou um diretório do servidor onde o FileHub está sendo executado, ou seja, cada 
tipo de storage possui suas propriedades para acesso e especificações.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No arquivo de configuração os storages são definidos dentro da tag <b>storages</b>, como no exemplo abaixo:
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
<p style="font-size: smaller; text-align: center">Exemplo de declaração de storage<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Todo elemento storage possui um <b>ID</b> e um <b>type</b>. O ID irá identificar o storage e o type irá definir quais as propriedades 
de configuração o storage possui. Os tipos de storages são listados a seguir:
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
            <div>Define um diretório do servidor onde o Filehub está sendo executado como storage.</div>
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
            <img src="https://user-images.githubusercontent.com/32067860/205457114-e7f44363-a144-4b14-938b-8fcb216546d1.png" alt="drawing" width="80"/>
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
            <li><b>region:</b> região onde o S3 está localizado (e.g.: sa-east-1)</li>
            <li><b>secretKeyId:</b> ID do usuário no IAM</li>
            <li><b>secretKey:</b> código do secret do usuário no IAM</li>
            <li><b>bucket:</b> nome do bucket do S3</li>
            <li><b>baseDir:</b> diretório raiz</li>
        </td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Schema</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Um schema representa um conjunto de storages. Ao realizar qualquer operação no FileHub, seja de upload ou download, 
será necessário informar qual o schema que deverá ser considerado. Em outras palavras, o serviço FileHub não realiza 
operações diretamente em um elemento Storage, mas sim em um schema que representa um ou mais storages.
</p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Os schemas são declarados dentro da tag <b>schemas</b>, sendo possível a declaração de mais de um schema. Todo registro de 
schema possui um <b>name</b> que será o identificador do mesmo nas requisições realizadas no FileHub. Para vincular os 
storages a determinado schema utilizamos a tag <b>storage-id</b>. O exemplo abaixo mostra como fica uma configuração de 
um schema que possui dois storages vinculados.
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
<p style="font-size: smaller; text-align: center">Exemplo de declaração de schema<p>

<h3>Schemas Auto Gerados</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Não é necessário declarar um schema para cada storage caso exista a necessidade de realizar operações nos storages 
de forma individual. É possível fazer com que o FileHub realize a leitura do arquivo de configuração, criando um 
schema para cada storage existente. Para isso, utilize o atributo <b>generate-schema</b>, informando como valor, o nome 
do schema que deverá ser criado. Veja o exemplo abaixo:
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
<p style="font-size: smaller; text-align: center">Exemplo de geração de schema diretamente no storage<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Utilize o atributo <b>generate-schema</b> no elemento <b>storages</b> caso seja necessário criar um schema com todos os 
storages existentes. Veja o exemplo abaixo:
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
<p style="font-size: smaller; text-align: center">Exemplo de geração de schema com todos os storages existentes<p>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th colspan="2" style="text-align: center">
            Alerta
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            Se um schema auto gerado foi criado sem uma trigger default configurada, o schema não terá nenhum tipo de segurança.
        </td>
      </tr>
    </tbody>
</table>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Trigger</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Triggers são utilizadas para garantir a segurança das operações. Funcionam como web hooks que irão validar se 
determinada operação está autorizada ou não por sua aplicação. 
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O elemento trigger possui um <b>ID</b> para identificação e também um atributo <b>action</b>, que poderá assumir dois 
valores possíveis:
</p>

<ol style="margin-left: 50px">
    <li>
        <b>ALL:</b> irá considerar a trigger para qualquer tipo de operação, seja de escrita 
        (upload/criação/exclusão) ou leitura (download);
    </li>
    <li>
        <b>UPDATE:</b> a trigger só será aplicada para operações de escrita (upload/criação/exclusão).
    </li>
</ol>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th style="text-align: center">
            Alerta
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            O termo <b>default</b> é um valor especial e não pode ser utilizado como ID para uma trigger.
        </td>
      </tr>
    </tbody>
</table>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Ao configurar uma trigger três propriedades deverão ser informadas:
</p>

<ol style="margin-left: 50px">
    <li>
        <b>header:</b> é o nome do header que deverá ser enviado ao serviço de autorização.
    </li>
    <li>
        <b>url:</b> é o endpoint do serviço que irá validar se a requisição é válida ou não. Seu objetivo é verificar se 
        o valor do header é válido. Caso a requisição enviada para este endpoint retornar um código HTTP diferente de 200 (OK) 
        a operação é cancelada.
    </li>
    <li>
        <b>http-method (opcional):</b>  define qual o tipo do método HTTP utilizado na requisição 
        (GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS). O valor padrão é GET.
    </li>
</ol>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No XML de configuração as triggers são definidas dentro da tag <b>triggers</b>. Uma trigger deverá ser vinculada a um schema. 
Esse vínculo é criado através do atributo <b>trigger</b> do schema, isso faz com que todos os storages do schema passem a 
considerar a trigger em suas operações.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Para deixar mais claro, considere o seguinte exemplo de configuração:
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
Observe que a trigger <b>user-auth</b> foi criada e o schema <b>test</b> faz o uso da mesma, ou seja, cada operação realizada 
para o storage <b>example</b> irá chamar a trigger para verificação de autorização.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O fluxograma abaixo apresenta o processo considerando a operação de <b>upload</b> para a configuração anterior.
</p>

<div style="text-align: center">
    <img src="https://user-images.githubusercontent.com/32067860/205391431-3c1d8d56-2bd8-48d1-9737-5469f3564cbb.png" alt="drawing" width="80%"/>
    <p style="font-size: smaller">Fluxograma de upload de arquivo com trigger<p>
</div>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A aplicação que consome o serviço FileHub deverá enviar o header configurado na trigger com um valor, ao receber a 
requisição, o FileHub irá chamar o endpoint configurado na trigger repassando o header para que o serviço de autorização 
faça a devida validação. Um token JWT é um bom exemplo do uso desse processo. 
</p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Outra função das triggers é permitir a criação de caminhos customizáveis para os arquivos. Para deixar mais claro essa 
função da trigger, imagine um sistema onde cada usuário possui um diretório para armazenar suas imagens, teríamos URLs 
semelhantes a seguinte lista:
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
Observe que para realizar uma operação de upload ou download, a aplicação que irá consumir o FileHub deverá gerenciar 
os identificadores dos usuários logados. Porém, se essa aplicação consumidora for uma interface web, seria possível 
alterar esse identificador, comprometendo a segurança no acesso aos arquivos gerenciados pelo FileHub. Para contornar 
esse problema existe a possibilidade do endpoint configurado na trigger, retornar uma lista de parâmetros que deverão 
ser utilizados para substituir partes da URL nas operações. O diagrama de sequência a seguir mostra esse processo:
</p>

<div style="text-align: center">
    <img src="https://user-images.githubusercontent.com/32067860/205391630-604078ed-1b27-4772-918a-6477f924f4e9.png" alt="drawing" width="80%"/>
    <p style="font-size: smaller">Diagrama de sequência do processo de comunicação com trigger<p>
</div>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Note que o parâmetro retornado da resposta do Authorization Service deverá ter o mesmo nome que o parâmetro 
informado na URL da operação ($user = user).
</p>

<table>
    <thead>
      <tr style="background-color: #FFF2CC; color: black">
        <th style="text-align: center">
            Dica
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            O <b>nome do arquivo</b> também pode ser alterado pelo retorno da requisição através do parâmetro chamado <b style="color: blue">filename</b>.
        </td>
      </tr>
    </tbody>
</table>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th style="text-align: center">
            Alerta
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            Caso uma trigger esteja configurada com o atributo action como UPDATE e o header de autorização 
            configurado, seja enviado na requisição, a trigger irá chamar o endpoint configurado.
        </td>
      </tr>
    </tbody>
</table>

<h4>Trigger Padrão</h4>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Existe a possibilidade de criar uma trigger que será chamada em todos os schemas que não definirem uma trigger de 
forma explícita. Para isso, utiliza-se o atributo <b>default</b> na trigger como mostrado no exemplo abaixo:
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
<p style="font-size: smaller; text-align: center">Exemplo de trigger padrão<p>


<!--------------------------------------------------------------------------------------------------------------------->


<h2>Operações</h2>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Após o entendimento dos principais conceitos do FileHub, o próximo passo é saber quais as possíveis operações que 
podem ser executadas pelo serviço.
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h5>Diretórios</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Os diretórios podem ser vistos como uma forma de agrupar e organizar os arquivos. A maioria dos storages tratam os 
diretórios como um tipo especial de arquivo, porém existem casos como o S3 da AWS que tratam os diretórios como 
prefixos, que juntamente com o nome do arquivo, compõem a chave de identificação do arquivo dentro de um bucket. 
O FileHub facilita o gerenciamento de diretórios, permitindo as seguintes operações:
</p>

<ul style="margin-left: 50px">
    <li>Criar um novo diretório</li>
    <li>Renomear um diretório</li>
    <li>Deletar um diretório</li>
    <li>Listar os arquivos existentes dentro do diretório, incluindo outros diretórios</li>
    <li>Verificar se o diretório existe</li>
</ul>

<h5>Desativar operações em diretórios</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Caso exista a necessidade de desativar as operações em diretórios, pode-se utilizar o atributo <b>no-dir</b> em uma 
trigger como mostrado no exemplo abaixo.
</p>

````xml
<trigger id="user-auth" action="ALL" no-dir="true">
    <url>http://10.0.0.10:8080/auth</url>
    <header>myheader</header>
    <http-method>GET</http-method>
</trigger>
````
<p style="font-size: smaller; text-align: center">Exemplo de trigger com desativação de diretórios<p>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Upload</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
A operação de upload permite que sejam enviados arquivos que serão salvos em todos os storages vinculados a determinado 
schema. Quando o FileHub recebe a requisição de upload e a transferência dos arquivos inicia, o FileHub pode enviar o 
arquivo para os storages de duas maneiras:
</p>

<ul style="margin-left: 50px">
    <li>
        <b>Transferência sequencial:</b> É o tipo de transferência padrão. O FileHub irá transferir os arquivos para 
        cada um dos storages de forma sequencial, obedecendo a ordem de declaração dos storages no schema.
    </li>
    <li>
        <b>Transferência paralela:</b> O FileHub transfere os arquivos para os storages ao mesmo tempo. Nesse caso 
        não existe uma ordem de transferência.
    </li>
</ul>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Independente do tipo de transferência realizada, a requisição de upload só irá retornar uma resposta após o término 
da transferência dos arquivos para todos os storages do schema.
</p>

<h3>Middle-Storage</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Em alguns cenários, onde se tem apenas 1 storage no schema e os arquivos são pequenos, a operação de transferência 
é executada rapidamente. Porém, existem casos onde é necessário transferir arquivos maiores para mais de 1 storage, 
e nesses casos a requisição pode levar um tempo considerável. Para amenizar este problema, utiliza-se o conceito de 
middle-storage.
</p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O middle-storage define qual dos storages do schema irá servir como intermediário entre a aplicação consumidora e o 
restante dos storages. Veja o exemplo a seguir:
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
<p style="font-size: smaller; text-align: center">Exemplo de middle-storage<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No exemplo acima, em uma operação de upload, o storage FileSystem-Test irá receber o arquivo, retornar a resposta 
para a aplicação consumidora e depois irá transferir o arquivo para o storage S3-Test.
</p>

<h3>Middle-Storage Temporário</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Um storage definido como middle-storage e não incluído como um dos storage do schema será considerado um storage 
temporário. Esse storage irá funcionar igual ao middle-storage, porém irá deletar os arquivos após a operação de upload.
</p>

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p style="font-size: smaller; text-align: center">Exemplo de middle-storage temporário<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Como mostrado no exemplo acima, o storage FileSystem-Test não está declarado em nenhum elemento storage-id dentro 
do schema, ou seja, ele é um middle-storage temporário.
</p>


<!--------------------------------------------------------------------------------------------------------------------->


<h3>Download</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Diferente do upload que faz a comunicação com todos os storages de um schema, o download irá utilizar o primeiro 
storage declarado para fazer a operação de download.
</p>

<h3>Cache-Storage</h3>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
O uso do atributo <b>cache</b> no schema irá afetar a operação de download. Caso o arquivo não exista no primeiro storage 
do schema, o FileHub irá verificar a existência do arquivo no próximo storage. Se o arquivo existir, o FileHub fará 
o download do mesmo, porém deixando o arquivo salvo no primeiro storage também.
</p>

````xml
<schemas>
    <schema name="myschema" middle="FileSystem-Test">
        <storage-id>S3-Test</storage-id>
    </schema>
</schemas>
````
<p style="font-size: smaller; text-align: center">Exemplo de schema com cache<p>

<p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
No exemplo anterior, caso seja realizado o download de um arquivo que não exista no FileSystem-Test, o FileHub irá 
verificar se o S3-Test possui o arquivo. Em caso positivo o download será executado, porém transferindo o arquivo 
também para o FileSystem-Test, o primeiro storage consultado.
</p>

<table>
    <thead>
      <tr style="background-color: #F4CCCC; color: black">
        <th style="text-align: center">
            Alerta
        </th>
      </tr>
    </thead>
    <tbody>
      <tr style="background-color: white; color: black">
        <td>
            Caso exista um middle-storage associado ao schema, o mesmo será utilizado para o cache, caso contrário, 
            será o primeiro storage do schema.
        </td>
      </tr>
      <tr style="background-color: white; color: black">
        <td>
            Não é possível ter um cache-storage atuando como middle-storage temporário.
        </td>
      </tr>
    </tbody>
</table>
<br>

-----

<h3>Documentação da API</h3>

* Execute o serviço e acesse: http://localhost:8088/swagger-ui.html
* Apiary Docs: https://filehub.docs.apiary.io

-----


