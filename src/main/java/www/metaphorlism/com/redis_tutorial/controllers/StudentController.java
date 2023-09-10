package www.metaphorlism.com.redis_tutorial.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import www.metaphorlism.com.redis_tutorial.models.Response;
import www.metaphorlism.com.redis_tutorial.models.Student;
import www.metaphorlism.com.redis_tutorial.services.StudentService;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class StudentController {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private StudentService studentService;
    private final String REDIS_KEY = "STUDENT";
    
    @PostMapping("/students")
    public ResponseEntity<Object> createStudent(@RequestBody Student student) {
        student.setId(UUID.randomUUID().toString());
        studentService.createStudent(student);
    
        return new ResponseEntity<Object>(new Response("success","create successfully",student), HttpStatus.CREATED);
    }

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
}
