/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author lans
 */
public class LogoManager {

    // saved logo, always PNG, lives in the working folder like settings.json
    private static final File LOGO_FILE = new File("custom_logo.png");

    private LogoManager() {}

    public static boolean hasCustomLogo() {
        return LOGO_FILE.isFile();
    }

    // Reads any image ImageIO understands. Returns null if the file is not an image.
    public static BufferedImage read(File file) throws IOException {
        return ImageIO.read(file);
    }

    // Saves the image as the custom logo (PNG). Writes a temp file first so a failed
    // write can never leave a half-written logo behind.
    public static void save(BufferedImage image) throws IOException {
        BufferedImage argb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        File temp = new File(LOGO_FILE.getPath() + ".tmp");
        if (!ImageIO.write(argb, "png", temp)) {
            throw new IOException("No PNG writer available");
        }
        Files.move(temp.toPath(), LOGO_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    // Loads the saved logo scaled to fit inside maxWidth x maxHeight, or null if there is none.
    public static ImageIcon loadIcon(int maxWidth, int maxHeight) {
        if (!hasCustomLogo()) return null;
        try {
            BufferedImage image = ImageIO.read(LOGO_FILE);
            return image == null ? null : scaleToFit(image, maxWidth, maxHeight);
        } catch (IOException e) {
            System.getLogger(LogoManager.class.getName()).log(System.Logger.Level.ERROR, "Could not load logo", e);
            return null;
        }
    }

    // Scales to fit inside maxWidth x maxHeight keeping the aspect ratio (never enlarges).
    public static ImageIcon scaleToFit(BufferedImage image, int maxWidth, int maxHeight) {
        double scale = Math.min(1.0, Math.min((double) maxWidth / image.getWidth(),
                                              (double) maxHeight / image.getHeight()));
        int w = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(image.getHeight() * scale));
        if (w == image.getWidth() && h == image.getHeight()) return new ImageIcon(image);

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();
        return new ImageIcon(out);
    }
}
