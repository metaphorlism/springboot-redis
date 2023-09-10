By: Yim Sotharoth

===

## Table of Contents

[TOC]

## Architecture
![](https://hackmd.io/_uploads/SyHxOAc03.png)

## Redis Docker Compose

___you can run `docker compose up -d` to create the docker image/container of the redis server___

```yaml=
version: "3.8"
services:
  cache:
    image: redis:6.2-alpine
    restart: always
    ports:
      - "6379:6379"
    command: redis-server --save 20 1 --loglevel warning --requirepass eYVX7EwVmmxKPCDmwMtyKVge8oLd2t81
    volumes:
      - cache:/data
volumes:
  cache:
    driver: local
```

## Dependencies

___Prerequisites___

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20151123</version>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.9.0</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```


## application.yaml configurations

```yaml
server:
  port: 8090

spring:
  application:
    name: Redis-Tutorial
  data:
    redis:
      host: localhost
      port: 6379
      password: YOUR_PASSWORD
  datasource:
    url: jdbc:postgresql://YOUR_DATABASE_IP_ADDRESS:5432/YOUR_DB
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Model
_Create a `models` folder_

### Student Model
_Create a `Student.java` file in `models`_

```java=
package www.metaphorlism.com.redis_tutorial.models;

import com.google.gson.Gson;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Student {
    @Id
    private String id;
    private String name;
    private String major;
    private String contact;
    
    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
```

### Response Model

___a model to get response in the below JSON sample___

```json
{
    "status": "success",
    "message": "student from cache",
    "student": {
        "id": "900851e1-4baa-4946-841d-daa8f8357643",
        "name": "Student One",
        "major": "Computer Science",
        "contact": "yimsotharoth999@gmail.com"
    }
}
```

_Create a `Response.java` file in `models`_

```java=
package www.metaphorlism.com.redis_tutorial.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    private String status;
    private String message;
    private Student student;
    
    public Response(String status,String message,Student student){
        this.status = status;
        this.message = message;
        this.student = student;
    }
}
```

## Repository

_Create a `repositories` folder and a `StudentRepository` interface_

```java=
package www.metaphorlism.com.redis_tutorial.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import www.metaphorlism.com.redis_tutorial.models.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student,String> {
}
```

## Service

_Create a `services` folder and a `StudentService.java` file_

```java=
package www.metaphorlism.com.redis_tutorial.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import www.metaphorlism.com.redis_tutorial.models.Student;
import www.metaphorlism.com.redis_tutorial.repositories.StudentRepository;

import java.util.Optional;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;
    
    public void createStudent(Student student) {
        studentRepository.save(student);
    }
    
    public Optional<Student> getStudentById(String student_id) {
        return studentRepository.findById(student_id);
    }
}
```

## Redis Configuration

_Create a `config` folder and a `RedisConfig.java` file_

```java=
package www.metaphorlism.com.redis_tutorial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
public class RedisConfig{
    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        return template;
    }
}
```

## Controller

_Create a `controllers` folder and a `StudentController.java` file_

### DECLARATION AND GLOBAL VARIABLE

```java=
@Autowired
private RedisTemplate<String,String> redisTemplate;

@Autowired
private StudentService studentService;

private final String REDIS_KEY = "STUDENT";
```

### CREATE STUDENT CONTROLLER

___a POST method controller to create new student to the database___

```java=
@PostMapping("/students")
public ResponseEntity<Object> createStudent(@RequestBody Student student) {
    student.setId(UUID.randomUUID().toString());
    studentService.createStudent(student);
    
    return new ResponseEntity<Object>(new Response("success","create successfully",student), HttpStatus.CREATED);
}
```

### GET STUDENT BY ID CONTROLLER

___a get method that get student by ID from redis/database___

```java=
@GetMapping("/students/{student_id}")
public ResponseEntity<Object> getStudentById(@PathVariable String student_id) throws JsonProcessingException {
        try{
            String key = REDIS_KEY.concat(":").concat(student_id);
    
            var cachedStudent = redisTemplate.opsForValue().get(key);
    
            if(cachedStudent == null) {
                Optional<Student> student = studentService.getStudentById(student_id);
                
                if(student.isPresent()){
                   
                    redisTemplate.opsForValue().set(key,student.get().toJson());
                    
                    if(redisTemplate.getExpire(key) < 0){
                        redisTemplate.expire(key,2,TimeUnit.MINUTES);
                    }
                    
                    return new ResponseEntity<Object>(new Response("success","student from db",student.get()), HttpStatus.OK);
                }
                
                return new ResponseEntity<Object>(new Response("success","student from db not found",null), HttpStatus.NOT_FOUND);
            }
    
            ObjectMapper objectMapper = new ObjectMapper();
            Student student = objectMapper.readValue(cachedStudent,Student.class);
    
            return new ResponseEntity<Object>(new Response("success","student from cache",student), HttpStatus.OK);
            
        }catch (Exception e){
            log.info(e.getMessage());
    
            return new ResponseEntity<Object>(new Response("fail",e.getMessage(),null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
```

Project Repository: https://github.com/metaphorlism/springboot-redis

## Contact Us

- :mailbox: yimsotharoth999@gmail.com
- [GitHub](https://github.com/metaphorlism) 
- [Facebook Page](https://www.facebook.com/Metaphorlism)
- [Instagram: Metaphorlism](https://www.instagram.com/metaphorlism/)
