CitizensBooks
===========

## Building
CitizensBooks uses Maven to handle dependencies.

#### Requirements
* Java 8 JDK or newer
* Maven 3.3.x or newer
* Git

#### Compiling From Source
```sh
git clone https://github.com/nicuch/CitizensBooks.git
mvn clean package
```

## Pikacraft Maven Repository & Dependency
```xml
<repositories>
  <repository>
    <id>pikacraft-repo</id>
    <url>https://repo.pikacraft.ro/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>ro.nicuch</groupId>
    <artifactId>CitizensBooks</artifactId>
    <version>LATEST</version>
  </dependency>
</dependencies>
```

## Jitpack Maven Repository & Dependency
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.nicuch</groupId>
    <artifactId>CitizensBooks</artifactId>
    <version>master-SNAPSHOT</version>
  </dependency>
</dependencies>
```
