CitizensBooks [![Build Status](https://travis-ci.org/nicuch/CitizensBooks.svg?branch=master)](https://travis-ci.org/nicuch/CitizensBooks)
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

```
    <repositories>
        <repository>
	        <id>citizensbooks_repo</id>
	        <url>https://raw.github.com/nicuch/maven_repo/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>ro.nicuch</groupId>
            <artifactId>CitizensBooks</artifactId>
            <version>2.3.7</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
```
