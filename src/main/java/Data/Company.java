/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author lans
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Company {
    
    private String companyName;
    private String theme;
    
    public Company(){}
    
    public String getcompanyName(){
        return companyName;
    }
    
    public String getTheme() {
        return theme;
    }
}
