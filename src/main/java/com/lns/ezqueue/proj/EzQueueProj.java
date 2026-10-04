/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.lns.ezqueue.proj;

import com.formdev.flatlaf.*;
import com.lns.ezqueue.proj.ui.HomePage;

/**
 *
 * @author lans
 */
public class EzQueueProj {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            FlatDarculaLaf.setup();
            new HomePage().setVisible(true);
        });
    }
}
