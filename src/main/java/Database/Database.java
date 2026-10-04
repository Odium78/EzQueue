/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    public String getDate(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedString = now.format(formatter);
        
        return formattedString;
    }
    
    public boolean authUser(String username, String password){
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
    
    public boolean addUser(String username, String password){
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
    
    public boolean deleteUser(String username){
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
    
    public boolean updateUsername(String oldUsername, String newUsername){
        String query = "UPDATE users SET username = ? WHERE username = ?";
        try (PreparedStatement exec = database.prepareStatement(query)) {
            exec.setString(1, newUsername);
            exec.setString(2, oldUsername);
            return exec.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "SQL ERROR " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean updatePassword(String username, String newPassword){
        String query = "UPDATE users SET password = ? WHERE username = ?";
        try (PreparedStatement exec = database.prepareStatement(query)) {
            exec.setString(1, newPassword);
            exec.setString(2, username);
            return exec.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "SQL ERROR " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean log(String message){
        String query = "INSERT INTO logs(date, message) VALUES(?,?)";
        try (PreparedStatement exec = database.prepareStatement(query)) {
            exec.setString(1, getDate());
            exec.setString(2, message);
            exec.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace(),
                    "SQL ERROR " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public List<String[]> getLogs(){
        List<String[]> logs = new ArrayList<>();
        String query = "SELECT date, message FROM logs ORDER BY date DESC, rowid DESC";
        try (PreparedStatement exec = database.prepareStatement(query);
             ResultSet set = exec.executeQuery()) {
            while (set.next()) {
                logs.add(new String[]{set.getString("date"), set.getString("message")});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "SQL ERROR " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
        }
        return logs;
    }
}
