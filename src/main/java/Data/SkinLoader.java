/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author lans
 */
public class SkinLoader {
        public static void apply(String skinName) throws Exception {
        String path = "/Skins/" + skinName + ".properties";
        Properties props = new Properties();

        try (InputStream in = SkinLoader.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new FileNotFoundException("Skin not found: " + path);
            }
            props.load(in);
        }

        FlatLaf.setup(new FlatPropertiesLaf(skinName, props));
        FlatLaf.updateUI(); // refresh
    }
}
