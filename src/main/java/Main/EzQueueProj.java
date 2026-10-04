/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package Main;

import com.formdev.flatlaf.*;
import Ui.HomePage;
import Database.*;

/**
 *
 * @author lans
 */
public class EzQueueProj {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            ConnectDB connection = new ConnectDB();
            Database db = new Database(connection);
            
            FlatLightLaf.setup();
            new HomePage().setVisible(true);
        });
    }
}
