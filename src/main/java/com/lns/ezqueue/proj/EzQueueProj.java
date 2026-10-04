/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.lns.ezqueue.proj;

import com.formdev.flatlaf.*;
import com.lns.ezqueue.proj.ui.HomePage;
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
            
            FlatDarculaLaf.setup();
            new HomePage().setVisible(true);
        });
    }
}
