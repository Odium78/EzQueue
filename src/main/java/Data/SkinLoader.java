/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    private static final String FOLDER = "Skins";

    /**
     * Returns the names (without ".properties") of every skin inside the
     * Skins resource folder. Works from the IDE (plain folder) and from a built jar.
     */
    public static List<String> listSkins() {
        List<String> names = new ArrayList<>();
        try {
            URL dir = SkinLoader.class.getResource("/" + FOLDER);
            boolean done = false;

            if (dir != null && "file".equals(dir.getProtocol())) {
                File[] files = new File(dir.toURI()).listFiles();
                if (files != null) {
                    for (File f : files) addIfSkin(names, f.getName());
                }
                done = true;
            } else if (dir != null && "jar".equals(dir.getProtocol())) {
                JarURLConnection conn = (JarURLConnection) dir.openConnection();
                conn.setUseCaches(false); // so closing the jar below is safe
                try (JarFile jar = conn.getJarFile()) {
                    scanJar(jar, names);
                }
                done = true;
            }

            if (!done) { // fallback: open the jar this class came from
                URL loc = SkinLoader.class.getProtectionDomain().getCodeSource().getLocation();
                File jarFile = new File(loc.toURI());
                if (jarFile.isFile()) {
                    try (JarFile jar = new JarFile(jarFile)) {
                        scanJar(jar, names);
                    }
                }
            }
        } catch (Exception ex) {
            System.getLogger(SkinLoader.class.getName()).log(System.Logger.Level.ERROR, "Could not list skins", ex);
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private static void scanJar(JarFile jar, List<String> names) {
        String prefix = FOLDER + "/";
        for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
            String name = e.nextElement().getName();
            // only files directly inside Skins/, not in sub folders
            if (name.startsWith(prefix) && name.indexOf('/', prefix.length()) < 0) {
                addIfSkin(names, name.substring(prefix.length()));
            }
        }
    }

    private static void addIfSkin(List<String> names, String fileName) {
        String ext = ".properties";
        if (fileName.endsWith(ext) && fileName.length() > ext.length()) {
            names.add(fileName.substring(0, fileName.length() - ext.length()));
        }
    }
}
