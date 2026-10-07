/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author lans
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonParser {
    private final ObjectMapper mapper = new ObjectMapper();
    
    public Company parseCompany() {
        try {
            return mapper.readValue(new File("settings.json"), Company.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Company JSON", e);
        }
    }
}
