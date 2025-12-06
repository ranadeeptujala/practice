# Java Practice Project

A simple Java project using Maven.

## Requirements

- Java 17 or higher
- Maven 3.6+

## Project Structure

```
practice/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── practice/
    │               └── Main.java
    └── test/
        └── java/
            └── com/
                └── practice/
                    └── MainTest.java
```

## Build & Run

### Compile the project
```bash
mvn compile
```

### Run tests
```bash
mvn test
```

### Package as JAR
```bash
mvn package
```

### Run the application
```bash
java -jar target/java-practice-1.0-SNAPSHOT.jar
```

Or run directly with Maven:
```bash
mvn exec:java -Dexec.mainClass="com.practice.Main"
```

