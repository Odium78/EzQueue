/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package Main;

import Data.Company;
import Data.JsonParser;
import Data.SkinLoader;
import com.formdev.flatlaf.*;
import Ui.StaffPage;
import Database.*;
import Ui.QueuePage;
import java.net.SocketOptions;

/**
 *
 * @author lans
 */
public class EzQueueProj {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            ConnectDB connection = new ConnectDB();
            Database db = new Database(connection);
            
            JsonParser parser = new JsonParser();
            Company company = parser.parseCompany();
            
            try {
//                switch (company.getTheme().toLowerCase()){
//                    case "darcula" -> { FlatDarculaLaf.setup(); }
//                    case "light" -> { FlatLightLaf.setup(); }
//                    default -> { FlatLightLaf.setup(); }
//                }

                SkinLoader.apply(company.getTheme());
            } catch (Exception ex) {
                System.getLogger(EzQueueProj.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

            new StaffPage(db).setVisible(true);
            new QueuePage().setVisible(true);
            
            db.log("Program Started");
        });
    }
}
