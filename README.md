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
