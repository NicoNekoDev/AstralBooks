AstralBooks
===========

## Building
AstralBooks uses Maven to handle dependencies.

#### Requirements
* Java 17 JDK or newer
* Maven 3.3.x or newer
* Git

#### Compiling From Source
```sh
git clone https://github.com/niconeko/AstralBooks.git
mvn clean package
```

## Repsy Maven Repository & Dependency
```xml
<repositories>
  <repository>
      <id>repsy-release</id>
      <url>https://repo.repsy.io/mvn/niconeko/release</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>ro.niconeko</groupId>
    <artifactId>astralbooks-api</artifactId>
    <version>LATEST</version>
  </dependency>
</dependencies>
```
