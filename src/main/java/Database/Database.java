/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author lans
 */
public class Database {
    private ConnectDB connectDB;
    public Connection database;
    
    public Database(ConnectDB connectionDB){
        this.connectDB = connectionDB;
        this.database  = connectionDB.getConnection();
    }
}
