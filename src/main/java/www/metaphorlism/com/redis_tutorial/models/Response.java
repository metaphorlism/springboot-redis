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
