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
