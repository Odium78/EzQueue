/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Ui;

import Data.Company;
import Data.JsonParser;
import Data.LogoManager;
import Data.QueueEntry;
import Data.QueueEntry.Lane;
import Data.QueueManager;
import Data.SkinLoader;
import Data.User;
import Database.Database;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author lans
 */
public class StaffPage extends javax.swing.JFrame {
    JsonParser parser = new JsonParser();
    Company company = parser.parseCompany();
    private final QueuePage queuePage; // save memory bro
    Database database;
    
    private static final Color SERVING_COLOR = new Color(43, 94, 40);
    private static final Color PRIORITY_COLOR = new Color(43, 24, 204);
    
    private final QueueManager queue = QueueManager.getInstance();
    private List<QueueEntry> tableRows = new ArrayList<>();
    private Lane lastLane = Lane.REGULAR;
    
    private User user = new User("nil", "nil");
    
    // size of logo
    private static final int LOGO_PREVIEW_WIDTH = 152;
    private static final int LOGO_PREVIEW_HEIGHT = 59;

    private BufferedImage pendingLogo;
    
    private static final String[] ROLES = {"admin", "manager", "staff"};

    private static final int MIN_PASSWORD_LENGTH = 4;
    
    /**
     * Creates new form HomePage
     */
    public StaffPage(Database database, QueuePage queuePage) {
        initComponents();
        this.database = database;
        this.queuePage = queuePage;
        
        setupQueueTable();
        setupLogsTable();
        setupAccountTable();
        refreshAccountTable();
        setupSkinCombo();
        setupCompanySettings();
        queue.addListener(this::refreshQueueTable);
        refreshQueueTable();
    }
    
    private void setupQueueTable() {
        jTable1.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Queue #", "Type", "Status"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jTable1.setSelectionBackground(new Color(214, 226, 245));
        jTable1.setSelectionForeground(Color.BLACK);
        
        jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                QueueEntry entry = (row >= 0 && row < tableRows.size()) ? tableRows.get(row) : null;
 
                if (entry != null && entry.isServing()) {
                    cell.setForeground(SERVING_COLOR);              // serving wins, even for a priority ticket
                } else if (entry != null && entry.getType().getLane() == Lane.PRIORITY) {
                    cell.setForeground(PRIORITY_COLOR);
                } else {
                    cell.setForeground(table.getForeground());
                }
                return cell;
            }
        });
    }
    
    private void refreshQueueTable() {
        QueueEntry previouslySelected = getSelectedEntry();
 
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        tableRows = queue.getAll();
        for (QueueEntry entry : tableRows) {
            model.addRow(new Object[]{entry.getNumberText(), entry.getType().getLabel(), entry.getStatus()});
        }

        int row = tableRows.indexOf(previouslySelected);
        if (previouslySelected != null && row >= 0) {
            jTable1.setRowSelectionInterval(row, row);
        }
    }
    
    private QueueEntry getSelectedEntry() {
        int row = jTable1.getSelectedRow();
        if (row < 0 || row >= tableRows.size()) return null;
        return tableRows.get(row);
    }
    
    private QueueEntry requireSelectedEntry() {
        QueueEntry selected = getSelectedEntry();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a queue in the table first.",
                    "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
        }
        return selected;
    }

    private Lane pickLane() {
        QueueEntry selected = getSelectedEntry();
        if (selected != null) {
            lastLane = selected.getType().getLane();
            return lastLane;
        }
 
        String[] options = {"Regular", "Priority"};
        int choice = JOptionPane.showOptionDialog(this, "Which line?", "Select Line",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[lastLane.ordinal()]);
        if (choice < 0) return null;
        lastLane = (choice == 1) ? Lane.PRIORITY : Lane.REGULAR;
        return lastLane;
    }
 
    private void showEmptyLine(Lane lane) {
        JOptionPane.showMessageDialog(this, "The " + (lane == Lane.PRIORITY ? "priority" : "regular")
                + " line is empty.", "Queue Empty", JOptionPane.INFORMATION_MESSAGE);
    }
 
    private QueueEntry pickHead() {
        Lane lane = pickLane();
        if (lane == null) return null;
        QueueEntry head = queue.getHead(lane);
        if (head == null) showEmptyLine(lane);
        return head;
    }
 
    private QueueEntry pickRear() {
        Lane lane = pickLane();
        if (lane == null) return null;
        QueueEntry rear = queue.getRear(lane);
        if (rear == null) showEmptyLine(lane);
        return rear;
    }
    
    private QueueEntry.Type getSelectedType() {
        String selected = String.valueOf(jComboBox1.getSelectedItem());
        if (selected.contains("PWD")) return QueueEntry.Type.PWD;
        if (selected.equalsIgnoreCase("Senior")) return QueueEntry.Type.SENIOR;
        if (selected.equalsIgnoreCase("Pregnant")) return QueueEntry.Type.PREGNANT;
        return QueueEntry.Type.REGULAR;
    }

    private void setupLogsTable() {
        jTable2.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Date", "Message"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jTable2.getColumnModel().getColumn(0).setPreferredWidth(170);
    }

    private void refreshLogsTable() {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
        for (String[] row : database.getLogs()) {
            model.addRow(row);
        }
    }
    
    private void applyRole(String username) {
        String role = database.getUserType(username);
        role = (role == null) ? "staff" : role.trim().toLowerCase();
        if (!role.equals("admin") && !role.equals("manager")) role = "staff";
        user.setType(role);

        boolean elevated = role.equals("admin") || role.equals("manager");
        jButton2.setVisible(true);// everyone
        jButton4.setVisible(true);// everyone
        jButton5.setVisible(elevated);// manager, admin
        jButton3.setVisible(elevated);//  manager, admin
        jButton18.setEnabled(role.equals("admin")); // skin - admin only
    }
    private void setupSkinCombo() {
        List<String> skins = SkinLoader.listSkins();
        jComboBox2.setModel(new DefaultComboBoxModel<>(skins.toArray(new String[0])));

        String current = company.getTheme();
        for (String skin : skins) {
            if (skin.equalsIgnoreCase(current)) {
                jComboBox2.setSelectedItem(skin);
                break;
            }
        }
    }

    private void setupCompanySettings() {
        String name = company.getcompanyName();
        jTextField3.setText(name == null ? "" : name);

        ImageIcon saved = LogoManager.loadIcon(LOGO_PREVIEW_WIDTH, LOGO_PREVIEW_HEIGHT);
        if (saved != null) {
            jLabel23.setIcon(saved);
            jLabel23.setText("");
        }
    }

    private void showLogoPreview(BufferedImage image) {
        jLabel23.setIcon(LogoManager.scaleToFit(image, LOGO_PREVIEW_WIDTH, LOGO_PREVIEW_HEIGHT));
        jLabel23.setText("");
    }

    private void showPasswordTooShort() {
        JOptionPane.showMessageDialog(this,
                "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.",
                "Invalid Password", JOptionPane.ERROR_MESSAGE);
    }

    private void setupAccountTable() {
        jTable3.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Name", "Type"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jTable3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void refreshAccountTable() {
        String previouslySelected = getSelectedAccountName();

        DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
        model.setRowCount(0);
        for (String[] row : database.getUsers()) {
            model.addRow(row);
        }

        // keep the same row selected if it still exists
        if (previouslySelected != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (previouslySelected.equals(model.getValueAt(i, 0))) {
                    jTable3.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private String getSelectedAccountName() {
        int row = jTable3.getSelectedRow();
        if (row < 0) return null;
        return String.valueOf(jTable3.getValueAt(row, 0));
    }

    private String getSelectedAccountType() {
        int row = jTable3.getSelectedRow();
        if (row < 0) return null;
        Object type = jTable3.getValueAt(row, 1);
        return type == null ? null : String.valueOf(type);
    }

    private String requireSelectedAccount() {
        String name = getSelectedAccountName();
        if (name == null) {
            JOptionPane.showMessageDialog(this, "Select an account in the table first.",
                    "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
        }
        return name;
    }
    
    // quick way
    private String[] showAccountDialog(String title, String presetName, String presetRole, boolean editing) {
        JTextField nameField = new JTextField(presetName == null ? "" : presetName, 20);
        JPasswordField passField = new JPasswordField(20);
        JComboBox<String> roleBox = new JComboBox<>(ROLES);
        if (presetRole != null) {
            for (String role : ROLES) {
                if (role.equalsIgnoreCase(presetRole)) {
                    roleBox.setSelectedItem(role);
                    break;
                }
            }
        } else {
            roleBox.setSelectedItem("staff");
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Username:"));
        form.add(nameField);
        form.add(new JLabel(editing ? "New Password:" : "Password:"));
        form.add(passField);
        form.add(new JLabel("Role:"));
        form.add(roleBox);
        if (editing) {
            form.add(new JLabel(""));
            form.add(new JLabel("<html><i>Leave password blank to keep it.</i></html>"));
        }

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return null;

            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username can't be empty.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!editing && pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password can't be empty.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            // length checks
            if (!pass.isEmpty() && pass.length() < MIN_PASSWORD_LENGTH) {
                showPasswordTooShort();
                continue;
            }
            // check if username is taken b4 query
            boolean nameChanged = !editing || !name.equals(presetName);
            if (nameChanged && database.userExists(name)) {
                JOptionPane.showMessageDialog(this, "The username \"" + name + "\" is already taken.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            return new String[]{name, pass, String.valueOf(roleBox.getSelectedItem())};
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton11 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel17 = new javax.swing.JLabel();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton21 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel2.setLayout(new java.awt.CardLayout());

        jLabel1.setFont(new java.awt.Font("Adwaita Sans", 1, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(company.getcompanyName() + " Staff Panel");
        jLabel1.putClientProperty("FlatLaf.styleClass", "title");

        jLabel3.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("LOGIN");

        jTextField1.putClientProperty("FlatLaf.styleClass", "loginBar");
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jPasswordField1.putClientProperty("FlatLaf.styleClass", "loginBar");
        jPasswordField1.addActionListener(this::jPasswordField1ActionPerformed);

        jLabel4.setText("Username");

        jLabel5.setText("Password");

        jButton1.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Green"));
        jButton1.setText("LOGIN");
        jButton1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.putClientProperty("FlatLaf.styleClass", "loginBut");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(289, 289, 289)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 919, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(347, 347, 347)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 875, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(420, 420, 420)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(817, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(83, 83, 83)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(jButton1)
                .addContainerGap(1282, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel3, "login");

        jLabel2.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Queue Management System");
        jLabel2.putClientProperty("FlatLaf.styleClass", "title");

        jScrollPane1.setAlignmentY(1.0F);
        jScrollPane1.setVerifyInputWhenFocusTarget(false);

        jTable1.setFont(new java.awt.Font("sansserif", 0, 16)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Queue #", "Type", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.putClientProperty("FlatLaf.styleClass", "mgrTab");
        jScrollPane1.setViewportView(jTable1);

        jLabel7.setFont(new java.awt.Font("sansserif", 0, 10)); // NOI18N
        jLabel7.setText("Powered by EzQueue");

        jButton6.setText("Menu");
        jButton6.putClientProperty("FlatLaf.styleClass", "menuBut");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jButton9.setFont(new java.awt.Font("sansserif", 3, 14)); // NOI18N
        jButton9.setText("Delete Selected");
        jButton9.putClientProperty("FlatLaf.styleClass", "mgrDelS");
        jButton9.addActionListener(this::jButton9ActionPerformed);

        jLabel8.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Control Center");

        jButton12.setFont(new java.awt.Font("sansserif", 3, 14)); // NOI18N
        jButton12.setText("Queue in Selected");
        jButton12.putClientProperty("FlatLaf.styleClass", "mgrQS");
        jButton12.addActionListener(this::jButton12ActionPerformed);

        jButton13.setFont(new java.awt.Font("sansserif", 3, 14)); // NOI18N
        jButton13.setText("Add to Queue");
        jButton13.putClientProperty("FlatLaf.styleClass", "mgrAdd");
        jButton13.addActionListener(this::jButton13ActionPerformed);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Regular", "Senior", "Person with Disablity (PWD)", "Pregnant" }));

        jLabel9.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel9.setText("Type:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton6))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(25, 25, 25)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 883, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(jButton13)
                                .addGap(18, 18, 18)
                                .addComponent(jButton12)
                                .addGap(18, 18, 18)
                                .addComponent(jButton9))
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(834, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addGap(298, 298, 298))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 447, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addComponent(jLabel7)
                .addContainerGap(1131, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel5, "mgr");

        jLabel6.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        jLabel6.setText("Welcome, " + user.getUsername() + "!");
        jLabel6.putClientProperty("FlatLaf.styleClass", "title");

        jButton2.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/queue.png"))); // NOI18N
        jButton2.setText("Manage Queue");
        jButton2.putClientProperty("FlatLaf.styleClass", "dashBut");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/settings.png"))); // NOI18N
        jButton3.setText("System Settings");
        jButton3.putClientProperty("FlatLaf.styleClass", "dashBut");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/account.png"))); // NOI18N
        jButton4.setText("Account Dashboard");
        jButton4.putClientProperty("FlatLaf.styleClass", "dashBut");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jLabel10.setFont(new java.awt.Font("sansserif", 0, 10)); // NOI18N
        jLabel10.setText("Powered by EzQueue");

        jButton5.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/log.png"))); // NOI18N
        jButton5.setText("Logs");
        jButton5.putClientProperty("FlatLaf.styleClass", "dashBut");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(64, 64, 64)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(419, 419, 419)
                        .addComponent(jLabel10)))
                .addContainerGap(843, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addContainerGap(1154, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel1, "dash");

        jLabel11.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Account Overview");

        jLabel12.setFont(new java.awt.Font("sansserif", 1, 24)); // NOI18N
        jLabel12.setText("Account Name: NIL");

        jLabel13.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel13.setText("Username");

        jTextField2.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N

        jLabel14.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel14.setText("Password");

        jPasswordField2.addActionListener(this::jPasswordField2ActionPerformed);

        jButton7.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Green"));
        jButton7.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jButton7.setText("Update Account Info");
        jButton7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton7.addActionListener(this::jButton7ActionPerformed);

        jButton8.setText("LOGOUT");
        jButton8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton8.addActionListener(this::jButton8ActionPerformed);

        jButton10.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Red"));
        jButton10.setText("Delete Account");
        jButton10.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton10.addActionListener(this::jButton10ActionPerformed);

        jButton15.setText("Go Back");
        jButton15.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton15.addActionListener(this::jButton15ActionPerformed);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(236, 236, 236)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                    .addGap(161, 161, 161)
                                    .addComponent(jButton10))
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addGap(137, 137, 137)
                                    .addComponent(jButton7))))))
                .addContainerGap(1147, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10))
                .addContainerGap(1259, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel4, "account");

        jLabel15.setFont(new java.awt.Font("sansserif", 1, 48)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Audit Logs");
        jLabel15.putClientProperty("FlatLaf.styleClass", "title");

        jScrollPane2.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane2.putClientProperty("FlatLaf.styleClass", "mgrTab");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Date", "Message"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable2.putClientProperty("FlatLaf.styleClass", "mgrTab");
        jScrollPane2.setViewportView(jTable2);

        jButton11.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jButton11.setText("Refresh");
        jButton11.addActionListener(this::jButton11ActionPerformed);

        jButton14.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jButton14.setText("Go Back");
        jButton14.putClientProperty("FlatLaf.styleClass", "menuBut");
        jButton14.addActionListener(this::jButton14ActionPerformed);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton14))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 884, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(839, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(1144, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel6, "logs");

        jLabel16.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        jLabel16.setText("System Settings");

        jTable3.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable3);

        jLabel17.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel17.setText("System Skin Configuration");

        jButton16.setText("Add Account");
        jButton16.putClientProperty("FlatLaf.styleClass", "mgrAdd");
        jButton16.addActionListener(this::jButton16ActionPerformed);

        jButton17.setText("Delete Account");
        jButton17.putClientProperty("FlatLaf.styleClass", "mgrDelS");
        jButton17.addActionListener(this::jButton17ActionPerformed);

        jLabel18.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel18.setText("Account Manager");

        jLabel19.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Current Skin:");

        jButton18.setText("Apply");
        jButton18.addActionListener(this::jButton18ActionPerformed);

        jButton19.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jButton19.setText("Back");
        jButton19.putClientProperty("FlatLaf.styleClass", "menuBut");
        jButton19.addActionListener(this::jButton19ActionPerformed);

        jButton20.setText("Edit Account");
        jButton20.putClientProperty("FlatLaf.styleClass", "mgrQS");
        jButton20.addActionListener(this::jButton20ActionPerformed);

        jLabel20.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel20.setText("System Description Configuration");

        jLabel21.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel21.setText("Name:");

        jTextField3.addActionListener(this::jTextField3ActionPerformed);

        jButton21.setText("Apply");
        jButton21.addActionListener(this::jButton21ActionPerformed);

        jLabel22.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel22.setText("Logo:");

        jLabel23.setFont(new java.awt.Font("sansserif", 2, 14)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("-- No Preview --");

        jButton22.setText("Select File");
        jButton22.addActionListener(this::jButton22ActionPerformed);

        jButton23.setText("Apply");
        jButton23.addActionListener(this::jButton23ActionPerformed);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton19))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 825, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                        .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton17))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(123, 123, 123))))
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton18)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton22))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton21)
                                    .addComponent(jButton23))))))
                .addContainerGap(867, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton19, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
                .addGap(17, 17, 17)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton17)
                    .addComponent(jButton16)
                    .addComponent(jButton20))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton18)
                    .addComponent(jLabel21)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton21))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton22)
                .addContainerGap(1152, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel7, "settings");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        setSize(new java.awt.Dimension(932, 694));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if(database.authUser(jTextField1.getText(), new String(jPasswordField1.getPassword()))){
            user.setUsername(jTextField1.getText());
            jLabel6.setText("Welcome, " + user.getUsername() + "!");
            user.setPassword(new String(jPasswordField1.getPassword()));
            applyRole(user.getUsername());
            
            CardLayout cl = (CardLayout) jPanel2.getLayout();
            cl.show(jPanel2, "dash");
            
            database.log("User LogOn: " + user.getUsername());
        } else {
            if(jTextField1.getText().trim().isEmpty()){ 
                JOptionPane.showMessageDialog(null, "Invalid Login! Username not found.",
                    "Login Error!", JOptionPane.ERROR_MESSAGE); 
                database.log("Login Denied Entry Invalid Username");
            } else if(new String(jPasswordField1.getPassword()).isEmpty()){ 
                JOptionPane.showMessageDialog(null, "Invalid Login! Invalid Password.",
                    "Login Error!", JOptionPane.ERROR_MESSAGE); 
                database.log("Login Denied Entry Invalid Password");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Login!",
                    "Login Error!", JOptionPane.ERROR_MESSAGE); 
                database.log("Login Denied Entry");
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "mgr");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        jLabel12.setText("Account Name: " + user.getUsername());
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "account");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        refreshAccountTable();
        cl.show(jPanel2, "settings");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "dash");
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        database.log("Deleted Queue: Entry #" + getSelectedEntry().getNumber() + " Type: " + getSelectedEntry().getType());
        queue.remove(requireSelectedEntry());
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        database.log("Served Queue: Entry #" + getSelectedEntry().getNumber() + " Type: " + getSelectedEntry().getType());
        queue.serve(requireSelectedEntry());
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
        queue.add(getSelectedType());
        database.log("Added Queue Entry with Type: " + getSelectedType());
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jPasswordField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jPasswordField2ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        String passString = new String(jPasswordField2.getPassword()).trim();

        if (!passString.isEmpty() && passString.length() < MIN_PASSWORD_LENGTH) {
            showPasswordTooShort();
            return;
        }

        if (!passString.isEmpty() && !jTextField2.getText().trim().isEmpty()){
            database.updatePassword(user.getUsername(), passString);
            database.updateUsername(user.getUsername(), jTextField2.getText());
            user.setUsername(jTextField2.getText());
            
            database.log("User Updated Username and Password: " + user.getUsername());
            JOptionPane.showMessageDialog(null, "Changed Username and Password Successfully",
                        "User Success", JOptionPane.OK_OPTION);
        } else if (!passString.isEmpty()){
            database.updatePassword(user.getUsername(), passString);
            
            database.log("User Updated Password: " + user.getUsername());
            JOptionPane.showMessageDialog(null, "Changed Password Successfully",
                        "User Success", JOptionPane.OK_OPTION);
        } else if (!jTextField2.getText().trim().isEmpty()){
            database.updateUsername(user.getUsername(), jTextField2.getText());
            user.setUsername(jTextField2.getText());
            
            database.log("User Updated Username: " + user.getUsername());
            JOptionPane.showMessageDialog(null, "Changed Username Successfully",
                        "User Success", JOptionPane.OK_OPTION);
        } else {
            JOptionPane.showMessageDialog(null, "No Account Changes Made",
                        "User Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "login");
        
        database.log("User Logout: " + user.getUsername());
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        database.deleteUser(user.getUsername());
        database.log("Deleted User: " + user.getUsername());
        
        JOptionPane.showMessageDialog(null, user.getUsername() + "Deleted Successfully",
                        "User Success", JOptionPane.OK_OPTION);
        
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "login");
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
        refreshLogsTable();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "dash");
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "dash");
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        refreshLogsTable();
        cl.show(jPanel2, "logs");
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        // Add Account
        String[] result = showAccountDialog("Add Account", null, null, false);
        if (result == null) return;

        if (database.addUser(result[0], result[1], result[2])) {
            database.log("Created Account: " + result[0] + " Role: " + result[2]);
            refreshAccountTable();
        }
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        // Delete Account
        String name = requireSelectedAccount();
        if (name == null) return;

        if (name.equals(user.getUsername())) {
            JOptionPane.showMessageDialog(this, "You can't delete the account you are logged in with here.",
                    "Delete Account", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete \"" + name + "\"?\nThis can't be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        if (database.deleteUser(name)) {
            database.log("Deleted Account: " + name);
            refreshAccountTable();
        }
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        // TODO add your handling code here:
         if(database.authUser(jTextField1.getText(), new String(jPasswordField1.getPassword()))){
            user.setUsername(jTextField1.getText());
            jLabel6.setText("Welcome, " + user.getUsername() + "!");
            user.setPassword(new String(jPasswordField1.getPassword()));
            applyRole(user.getUsername());
            
            CardLayout cl = (CardLayout) jPanel2.getLayout();
            cl.show(jPanel2, "dash");
            
            database.log("User LogOn: " + user.getUsername());
        } else {
            if(jTextField1.getText().trim().isEmpty()){ 
                JOptionPane.showMessageDialog(null, "Invalid Login! Username not found.",
                    "Login Error!", JOptionPane.ERROR_MESSAGE); 
                database.log("Login Denied Entry Invalid Username");
            } else if(new String(jPasswordField1.getPassword()).isEmpty()){ 
                JOptionPane.showMessageDialog(null, "Invalid Login! Invalid Password.",
                    "Login Error!", JOptionPane.ERROR_MESSAGE); 
                database.log("Login Denied Entry Invalid Password");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Login!",
                    "Login Error!", JOptionPane.ERROR_MESSAGE); 
                database.log("Login Denied Entry");
            }
        }
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        // Apply skin
        String skin = (String) jComboBox2.getSelectedItem();
        if (skin == null) {
            JOptionPane.showMessageDialog(this, "No skin selected.",
                    "Skin", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            SkinLoader.apply(skin);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not apply skin \"" + skin + "\":\n" + ex.getMessage(),
                    "Skin Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        database.log("Skin Changed: " + skin);

        try {
            parser.saveTheme(skin);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Skin applied, but it could not be saved to settings.json.",
                    "Skin", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {                                          
        CardLayout cl = (CardLayout) jPanel2.getLayout();
        
        cl.show(jPanel2, "dash");
    }       
    
    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
        // Edit Account
        String oldName = requireSelectedAccount();
        if (oldName == null) return;

        String[] result = showAccountDialog("Edit Account", oldName, getSelectedAccountType(), true);
        if (result == null) return;

        if (database.updateUser(oldName, result[0], result[1], result[2])) {
            database.log("Edited Account: " + oldName + " -> " + result[0] + " Role: " + result[2]
                    + (result[1].isEmpty() ? "" : " (password changed)"));

            // if you edited the account you are logged in with, keep the session in sync
            if (oldName.equals(user.getUsername())) {
                user.setUsername(result[0]);
                if (!result[1].isEmpty()) user.setPassword(result[1]);
                jLabel6.setText("Welcome, " + user.getUsername() + "!");
                applyRole(user.getUsername());
                if (!jButton3.isVisible()) {
                    ((CardLayout) jPanel2.getLayout()).show(jPanel2, "dash");
                }
            }
            refreshAccountTable();
            JOptionPane.showMessageDialog(this, "Account updated.",
                    "User Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        // Apply company name
        String name = jTextField3.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Company name cannot be empty.",
                    "Company Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            parser.saveCompanyName(name);
            company = parser.parseCompany(); // reload so the saved value is what we use
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the company name to settings.json.",
                    "Company Name", JOptionPane.ERROR_MESSAGE);
            return;
        }

        jTextField3.setText(name);
        jLabel1.setText(name + " Staff Panel");
        database.log("Company Name Changed: " + name);
        JOptionPane.showMessageDialog(this, "Company name saved.",
                "Company Name", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        // Select logo file and preview it
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Logo");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Images (png, jpg, gif, bmp)", "png", "jpg", "jpeg", "gif", "bmp");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            BufferedImage image = LogoManager.read(file);
            if (image == null) {
                JOptionPane.showMessageDialog(this, "That file is not a supported image.",
                        "Logo", JOptionPane.ERROR_MESSAGE);
                return;
            }
            pendingLogo = image;
            showLogoPreview(image);
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not read the image:\n" + ex.getMessage(),
                    "Logo", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        // Apply logo: save it and use it in the queue display
        if (pendingLogo == null) {
            JOptionPane.showMessageDialog(this, "Select an image first.",
                    "Logo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            LogoManager.save(pendingLogo);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the logo:\n" + ex.getMessage(),
                    "Logo", JOptionPane.ERROR_MESSAGE);
            return;
        }

        queuePage.refreshLogo();
        database.log("Logo Changed");
        JOptionPane.showMessageDialog(this, "Logo saved.",
                "Logo", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton23ActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
}
