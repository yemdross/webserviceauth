import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final String XML_FILE_PATH = "users.xml";
    private final XmlMapper xmlMapper = new XmlMapper();

    // Helper method to load users from the local XML
    private List<User> loadUsers() {
        try {
            File file = new File(XML_FILE_PATH);
            if (!file.exists()) return new ArrayList<>();
            // Reads XML root <Users> and maps to a List of User objects
            return xmlMapper.readValue(file, xmlMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // 1. Login Service
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User credentials) {
        List<User> users = loadUsers();
        
        boolean isValid = users.stream()
                .anyMatch(u -> u.getUsername().equals(credentials.getUsername()) && 
                               u.getPassword().equals(credentials.getPassword()));

        if (isValid) {
            return ResponseEntity.ok("{\"status\": \"Success\", \"message\": \"Login successful\"}");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body("{\"status\": \"Failure\", \"message\": \"Invalid credentials\"}");
    }

    // 2. Registration Service
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User newUser) {
        List<User> users = loadUsers();

        // Check for duplicates
        if (users.stream().anyMatch(u -> u.getUsername().equals(newUser.getUsername()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("{\"status\": \"Error\", \"message\": \"User already exists\"}");
        }

        users.add(newUser);

        try {
            // Persist the updated list back to the local XML file
            xmlMapper.writeValue(new File(XML_FILE_PATH), users);
            return ResponseEntity.ok("{\"status\": \"Success\", \"message\": \"User registered\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("{\"status\": \"Error\", \"message\": \"Could not save user\"}");
        }
    }
}