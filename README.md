# Mantis-Indexer-API

Mantis-Conseil is a fork of [Conseil](https://github.com/Cryptonomic/Conseil) to include support for mantis.

Conseil is an indexer and API for blockchains and consists of two processes:
- Lorre: it indexes the blockchain by downloading data from a blockchain node and storing it in a database.
- Conseil: it provides an API into the blockchain data indexed by Lorre.

Information on using, running, configuring and developing Conseil can be found on the [Conseil Wiki](https://github.com/Cryptonomic/Conseil/wiki).

# Conseil for Mantis

The following steps were taken from the [Conseil Wiki](https://github.com/Cryptonomic/Conseil/wiki), so you can got there for more detailed information.

Both Lorre and the api can be run separately or simultaneouly, since they're independent processes.

**1. Building Conseil (both lorre and the api):**

**1.1 Prerequisites:**

 Java Development Kit (> 8.x)

 Scala (> 2.12.x)

 SBT (> 1.2.6)

```sh
$ sudo apt-get install -y openjdk-8-jdk-headless
$ echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
$ sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
$ sudo apt-get update
$ sudo apt-get install -y sbt
```
A database supported by Typesafe Slick, e.g. Postgres
```sh
$ sudo apt update
$ sudo apt install postgresql postgresql-contrib
```

**1.2 Build proyect using sbt**
```sh
 $ sbt clean assembly -J-Xss32m
```
**1.3 Copy `.jar` files to another directory**
*Note: this is to run conseil using the jar files, but more running options can be found in [Running Conseil](https://github.com/Cryptonomic/Conseil/wiki/Running-Conseil)*

```sh
$ cp /tmp/conseil-lorre.jar ~/path/to/your/directory/mantis-lorre.jar
$ cp /tmp/conseil-api.jar ~/path/to/your/directory/mantis-api.jar
```

**1.4 Generate db schema (necessary to run both Lorre and conseil-api)**

Create DB and user and grant db privileges on that db to said user
```sh
$ sudo -u postgres psql

postgres=# create database conseil;
postgres=# create user foo with encrypted password 'bar';
postgres=# grant all privileges on database conseil to foo;
```
Connect to the db and generate the schema from `mantis.sql` in the repository
```sh
postgres=# \c conseil
You are now connected to database "conseil" as user "postgres".
conseil=# \i ~/mantis-indexer-api/sql/mantis.sql
```
 After success output, the db is ready to be used by lorre and conseil

**1.5 Configuration Files**

When running lore and conseil-api, we will need a custom configuration file.
There is a reference file in the respository -> `~/mantis-conseil/mantis.conf`

*Improtant configurations:*
1. Platforms: These represent the client and network that lorre and conseil will be indexing and querying. Note that `node` configurations are the one to most likely need changes (if the user has a different protocol/ip/port)

     This is an array of platforms (in this case, only with mantis-testnet platform)
    ```sh
    platforms: [                              
        {
          name: "mantis"
          network: "testnet"
          enabled: true
          node: "http://localhost:8546"

          retry {
            max-wait: 2s
            max-retry: 5
          }

          batching {
            indexer-threads-count: 8
            http-fetch-threads-count: 8
            blocks-batch-size: 100
            transactions-batch-size: 100
            contracts-batch-size: 100
            tokens-batch-size: 100
          }
        }
    ]          
    ```
2. Database connection in conseil and lorre sections (since they are independent processes, they could have different db configuration, so _both_ need to be modified)              

    DB configurations: User and password should be the ones from section [1.4], the same with the name of the db at the end of the url. Postgres runs by default in port 5432. This configuration should be updated both in conseil and in lorre sections.
    ```sh
    db {
        dataSourceClass: "org.postgresql.ds.PGSimpleDataSource"
        properties {
          user: "foo"
          password: "bar"
          url: "jdbc:postgresql://localhost:5432/conseil"
        }
        numThreads: 20
        maxConnections: 20
    }
    ```
3. Security Settings in Conseil section: this is only por de coneil-api, therefore it's its section.

    ApiKey in the requests headers use Conseil-api: The can be set to empty keys and allow-blank: true to avoid any kind of authorization.     
    ```sh
     security.api-keys {        
       # List of authorized API 
       keys: ["conseil"]        
                                
       allow-blank: false       
     }                          
    ```


**2. Running Conseil**

**2.1 Prerequisites**

1. Have Postrges service up and running. Check status:
    ```sh
    $ service postgresql status
    ```
    If the service isn't running, start it:
    ```sh
    $ sudo service postgresql start
    ```
2.  To run the indexer (Lorre) a running **mantis node is needed**. It must match the JSON-RPC configurations in section 1.5 (Platforms).
    
**2.2 Running lorre (platform to index should be running)**

The configuration file is the one from section [1.5]. The arguments are tha platform and the network, both should be previously configured in `platforms` as described in section [1.5]

**Using sbt**
```sh
sbt "runLorre mantis testnet" -Dconfig.file='<platform> <network>'
```
For example, using the configuration file in section [1.5] to run mantis/testnet indexer:
```sh
sbt "runLorre mantis testnet" -Dconfig.file='/home/<username>/mantis-indexer-api/mantis.conf'
```
**Using the `.jar` files**

```sh
$ java -Dconfig.file='<path/to/config/file.conf>' -jar conseil-lorre.jar <platform> <network>
```

For example, using the configuration file in section [1.5] and the `.jar` files from [1.3] to run mantis/testnet indexer:
```sh
$ java -Dconfig.file='/home/<username>/mantis-indexer-api/mantis.conf' -jar mantis-lorre.jar mantis testnet
```
**2.3 Running conseil-api**

The configuration file is the one from section [1.5]. No mantis node is needed.

**Using sbt**
```sh
sbt "runApi" -Dconfig.file='<platform> <network>'
```
For example, using the configuration file in section [1.5] to run mantis/testnet indexer:
```sh
sbt "runApi" -Dconfig.file='/home/<username>/mantis-indexer-api/mantis.conf'
```
**Using the `.jar` files**
```sh
$ java -Dconfig.file='<path/to/config/file.conf>' -jar conseil-lorre.jar
```
For example, using the configuration file in section [1.5] and the `.jar` files from [1.3] to query mantis/testnet data: 
```sh
$ java -Dconfig.file='/home/<username>/mantis-indexer-api/mantis.conf' -jar mantis-lorre.jar
```

# Run tests (no running platform is needed)

```sh
$ sbt test
```

# To format code

```sh
$ sbt scalafmt
```
