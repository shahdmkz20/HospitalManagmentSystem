
import databaseConnection.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JOptionPane;
import java.sql.*;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author shahd
 */
public class makePatientDischarge extends javax.swing.JFrame {

    private int patientID;

    /**
     * Creates new form patientInfo
     */
      public makePatientDischarge() {
        initComponents();
    }

    // Constructor with Patient ID
 
    public makePatientDischarge(int patientID) {
         this.patientID = patientID;
        initComponents();
        fetchNurses();
        fetchDoctors();
        fetchStatus();
        fetchPatientData();
    }

   private void fetchNurses() {
   String query = "SELECT e.Employee_ID AS nurse_id, "
             + "e.name AS nurse_name "
             + "FROM Employees e "
             + "WHERE role = '1'";
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        conn = DatabaseUtil.getConnection();
        pstmt = conn.prepareStatement(query);
        rs = pstmt.executeQuery();

        DefaultComboBoxModel nurseComboBox = new DefaultComboBoxModel(); 

        // Loop through the result set and add each clinic to the ComboBox
        while (rs.next()) {
            Integer id = rs.getInt("nurse_id");
            String name = rs.getString("nurse_name");

            // Add a Clinic object to the model
            nurseComboBox.addElement(new Employee(id, name));
        }

        // Set the ComboBox model to the populated model
        nurseBox.setModel(nurseComboBox);

    } catch (SQLException e) {
        e.printStackTrace(); // Handle database connection errors
    } finally {
        DatabaseUtil.close(conn, pstmt, rs); // Ensure proper resource cleanup
    }
}
  private void fetchDoctors() {
   String query = "SELECT e.Employee_ID AS doctor_id, "
             + "e.name AS doctor_name "
             + "FROM Employees e "
             + "WHERE role = '0'";


    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        conn = DatabaseUtil.getConnection();
        pstmt = conn.prepareStatement(query); 
        rs = pstmt.executeQuery(); 

        DefaultComboBoxModel doctorComboBox = new DefaultComboBoxModel(); 

        // Loop through the result set and add each clinic to the ComboBox
        while (rs.next()) {
            Integer id = rs.getInt("doctor_id");
            String name = rs.getString("doctor_name");
            doctorComboBox.addElement(new Employee(id, name));
        }

        // Set the ComboBox model to the populated model
        doctorBox.setModel(doctorComboBox);

    } catch (SQLException e) {
        e.printStackTrace(); // Handle database connection errors
    } finally {
        DatabaseUtil.close(conn, pstmt, rs); // Ensure proper resource cleanup
    }
}
 private void fetchStatus() {
   String query = "select status_id , status_name From discharge_status";

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        conn = DatabaseUtil.getConnection();
        pstmt = conn.prepareStatement(query); 
        rs = pstmt.executeQuery(); 

        DefaultComboBoxModel statusComboBox = new DefaultComboBoxModel(); 
        while (rs.next()) {
            Integer id = rs.getInt("status_id");
            String name = rs.getString("status_name");

            statusComboBox.addElement(new dischargeModel(id, name));
        }

        // Set the ComboBox model to the populated model
        status.setModel(statusComboBox);

    } catch (SQLException e) {
        e.printStackTrace(); // Handle database connection errors
    } finally {
        DatabaseUtil.close(conn, pstmt, rs); // Ensure proper resource cleanup
    }
}
 private void fetchPatientData() {
   String query = "select NAME  From Patients WHERE PATIENT_ID = " + patientID ;
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        conn = DatabaseUtil.getConnection();
        pstmt = conn.prepareStatement(query); 
        rs = pstmt.executeQuery(); 

            if (rs.next()) {
                String patientName = rs.getString("NAME");
                name.setText(patientName);
            } else {
                name.setText("Patient not found"); 
            }

        } catch (SQLException e) {
            e.printStackTrace(); 
        } finally {
            DatabaseUtil.close(conn, pstmt, rs); 
        }
    }

    public void insertPatientDetails() {
        int patientId = patientID;
        String diseaseSql = disease.getText();
        Employee selectedDoctor = (Employee) doctorBox.getSelectedItem();
        Employee selectedNurse = (Employee) nurseBox.getSelectedItem();
        dischargeModel selectedDischargeStatus = 
                (dischargeModel) status.getSelectedItem();
        int doctorId = selectedDoctor.getId();
        int nurseId = selectedNurse.getId();
        int dischargeStatus = selectedDischargeStatus.getId();
        double invoiceAmount = Double.parseDouble(bill_ammount.getText());
        java.util.Date dischargeDateSql = dischargeDate.getDate();
        String notesSql = notes.getText();

        Connection conn = null;
        PreparedStatement invoiceStmt = null;
        PreparedStatement dischargeStmt = null;

        try {
            conn = DatabaseUtil.getConnection();

            conn.setAutoCommit(false);

            // Insert into INVOICES table
       
            String insertInvoiceSQL = "INSERT INTO INVOICES "
                    + "(INVOICE_ID, INVOICE_DATE, AMOUNT) "
                    + "VALUES (INVOICE_SEQ.NEXTVAL, ?, ?)";

            invoiceStmt = conn.prepareStatement(insertInvoiceSQL,
                    Statement.RETURN_GENERATED_KEYS);

            invoiceStmt.setDate(1,
                    new java.sql.Date(dischargeDateSql.getTime()));
            invoiceStmt.setDouble(2, invoiceAmount); 
            invoiceStmt.executeUpdate();

            ResultSet generatedKeys = invoiceStmt.getGeneratedKeys();
            int generatedInvoiceId = 0;
            if (generatedKeys.next()) {
                generatedInvoiceId = generatedKeys.getInt(1);
                System.out.println("Generated Invoice ID: " 
                        + generatedInvoiceId);
            } else {
    System.out.println("No generated keys found.");
}


            String insertDischargeSQL = "INSERT INTO PATIENT_DISCHARGES "
                    + "(DISCHARGE_ID, PATIENT_ID, DISCHARGE_DATE,"
                    + " DISCHARGE_STATUS, NURSE_ID, DOCTOR_ID, NOTES,"
                    + " INVOICE_ID) "
                    + "VALUES (DISCHARGE_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

            dischargeStmt = conn.prepareStatement(insertDischargeSQL);
            dischargeStmt.setInt(1, patientId);
            dischargeStmt.setDate(2, 
                    new java.sql.Date(dischargeDateSql.getTime()));
            dischargeStmt.setInt(3, dischargeStatus);
            dischargeStmt.setInt(4, nurseId);
            dischargeStmt.setInt(5, doctorId);
            dischargeStmt.setString(6, notesSql);
            dischargeStmt.setInt(7, generatedInvoiceId);

            dischargeStmt.executeUpdate();
            conn.commit(); 


    } catch (SQLException ex) {
        // Rollback the transaction in case of an error
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
        ex.printStackTrace();
    } finally {
        // Close resources
        DatabaseUtil.close(conn, invoiceStmt, null);
        DatabaseUtil.close(null, dischargeStmt, null);
    }
}

private void clearFields() {
    disease.setText("");
    bill_ammount.setText("");
    notes.setText("");
    dischargeDate.setDate(null);
    nurseBox.setSelectedIndex(-1); // Reset selection
    doctorBox.setSelectedIndex(-1); // Reset selection
    status.setSelectedIndex(-1); // Reset selection
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        bill_ammount = new javax.swing.JTextField();
        nurseBox = new javax.swing.JComboBox<>();
        doctorBox = new javax.swing.JComboBox<>();
        status = new javax.swing.JComboBox<>();
        dischargeDate = new com.toedter.calendar.JCalendar();
        name = new javax.swing.JLabel();
        disease = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        notes = new javax.swing.JTextField();
        addDischarge = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("name:");

        jLabel2.setText("disease:");

        jLabel3.setText("nurse:");

        jLabel4.setText("doctor:");

        jLabel6.setText("discharge date:");

        jLabel7.setText("discharge status:");

        jLabel8.setText("bill ammount:");

        bill_ammount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bill_ammountActionPerformed(evt);
            }
        });

        nurseBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        doctorBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        name.setText("jLabel5");

        disease.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diseaseActionPerformed(evt);
            }
        });

        jLabel9.setText("notes:");

        notes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notesActionPerformed(evt);
            }
        });

        addDischarge.setText("add discharge");
        addDischarge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDischargeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel6)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel1))
                .addGap(52, 52, 52)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addDischarge, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(nurseBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(doctorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dischargeDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(name)
                        .addComponent(notes, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                        .addComponent(disease)
                        .addComponent(bill_ammount)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(name))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(disease, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(nurseBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(doctorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(44, 44, 44)
                        .addComponent(jLabel7))
                    .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(dischargeDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bill_ammount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(jLabel6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(notes, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addDischarge)
                .addGap(16, 16, 16))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bill_ammountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bill_ammountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bill_ammountActionPerformed

    private void diseaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diseaseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_diseaseActionPerformed

    private void notesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_notesActionPerformed

    private void addDischargeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDischargeActionPerformed
insertPatientDetails();        
    }//GEN-LAST:event_addDischargeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(makePatientDischarge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(makePatientDischarge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(makePatientDischarge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(makePatientDischarge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new makePatientDischarge().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDischarge;
    private javax.swing.JTextField bill_ammount;
    private com.toedter.calendar.JCalendar dischargeDate;
    private javax.swing.JTextField disease;
    private javax.swing.JComboBox<String> doctorBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel name;
    private javax.swing.JTextField notes;
    private javax.swing.JComboBox<String> nurseBox;
    private javax.swing.JComboBox<String> status;
    // End of variables declaration//GEN-END:variables
}
