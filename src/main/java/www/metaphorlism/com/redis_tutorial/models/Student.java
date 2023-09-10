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
