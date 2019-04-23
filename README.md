CitizensBooks
===========

## Building
CitizensBooks uses Maven to handle dependencies.

#### Requirements
* Java 8 JDK or newer
* Maven 3.3.x or newer
* Git

#### Compiling from source
```sh
git clone https://github.com/nicuch/CitizensBooks.git
mvn clean install
```
## Maven repository

```xml
<repositories>
  <repository>
    <id>pikacraft-repo</id>
    <url>http://repo.pikacraft.ro/</url>
    </repository>
  </repositories>

<dependencies>
  <dependency>
    <groupId>ro.nicuch</groupId>
    <artifactId>CitizensBooks</artifactId>
    <version>2.4.8</version>
  </dependency>
</dependencies>
```
