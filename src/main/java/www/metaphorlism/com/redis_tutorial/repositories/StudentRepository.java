package www.metaphorlism.com.redis_tutorial.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import www.metaphorlism.com.redis_tutorial.models.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student,String> {
}
