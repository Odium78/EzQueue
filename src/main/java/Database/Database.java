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
    
    public boolean authUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement exec = database.prepareStatement(query)) {
            exec.setString(1, username);
            exec.setString(2, password);
            ResultSet set = exec.executeQuery();
            return set.next();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace(),
                    "SQL ERROR " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean addUser(String username, String password) {
        String query = "INSERT INTO users(username, password) VALUES(?,?)";
        try (PreparedStatement exec = database.prepareStatement(query)) {
            exec.setString(1, username);
            exec.setString(2, password);
            exec.executeUpdate();
            JOptionPane.showMessageDialog(null, username + " Created Successfully",
                    "User Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace(),
                    "SQL ERROR " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean deleteUser(String username) {
        String query = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement exec = database.prepareStatement(query)) {
            exec.setString(1, username);
            int rowsAffected = exec.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, username + " Deleted Successfully",
                        "User Success", JOptionPane.OK_OPTION);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, username + " Not Found",
                        "User ERROR", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace(),
                    "SQL ERROR " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
