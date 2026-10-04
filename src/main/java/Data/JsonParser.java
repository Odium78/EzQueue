/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author lans
 */
public class JsonParser {
    private final ObjectMapper mapper = new ObjectMapper();
    
    public String parseName() {
        try {
            // 1. Map the JSON file directly to the Company object
            Company company = mapper.readValue(new File("data.json"), Company.class);
            
            // 2. Return the string value
            return company.getcompanyName();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Company JSON", e);
        }
    }
}
