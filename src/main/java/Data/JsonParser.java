/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    // Writes the company name into settings.json (other settings are kept as they are)
    public void saveCompanyName(String companyName) {
        File file = new File("settings.json");
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(file);
            root.put("companyName", companyName);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save company name to settings.json", e);
        }
    }

    // Writes the chosen skin into settings.json (other settings are kept as they are)
    public void saveTheme(String theme) {
        File file = new File("settings.json");
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(file);
            root.put("theme", theme);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save theme to settings.json", e);
        }
    }
}
