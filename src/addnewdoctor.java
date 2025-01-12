
import databaseConnection.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.DefaultComboBoxModel;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author shahd
 */
public class addnewdoctor extends javax.swing.JFrame {

    /**
     * Creates new form AddNewDoctor
     */
    public addnewdoctor() {
        initComponents();
         fetchSpecializations();
           fetchClinics(); 
    }
    
    private void fetchSpecializations() {
    String query = "SELECT SpecializationID , name FROM specialization";  

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        conn = DatabaseUtil.getConnection(); // Get connection to DB
        pstmt = conn.prepareStatement(query); // Prepare the statement
        rs = pstmt.executeQuery(); // Execute query to fetch data

        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

        while (rs.next()) {
            String specialization = rs.getString("name");
            Integer id = rs.getInt("SpecializationID");

            // Add a Specialization object to the model
            comboBoxModel.addElement(new Specialization(id, specialization));
        }
      
        specializationBox.setModel(comboBoxModel);

    } catch (SQLException e) {
        e.printStackTrace(); // Handle database connection errors
    } finally {
        DatabaseUtil.close(conn, pstmt, rs); // Ensure proper resource cleanup
    }
}
   private void fetchClinics() {
    String query = "SELECT Clinic_ID, name FROM clinics";

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        conn = DatabaseUtil.getConnection(); 
        pstmt = conn.prepareStatement(query);
        rs = pstmt.executeQuery();

        DefaultComboBoxModel clinicComboBox = new DefaultComboBoxModel(); 

        // Loop through the result set and add each clinic to the ComboBox
        while (rs.next()) {
            Integer id = rs.getInt("Clinic_ID");
            String name = rs.getString("name");

            clinicComboBox.addElement(new Clinic(id, name));
        }

        clinicBox.setModel(clinicComboBox);

    } catch (SQLException e) {
        e.printStackTrace(); // Handle database connection errors
    } finally {
        DatabaseUtil.close(conn, pstmt, rs); // Ensure proper resource cleanup
    }
}

private void addDoctor() {
    int doctorID = Integer.parseInt(doctorId.getText());
    String doctorName = doctorname.getText();
    String doctorPhone = doctorphone.getText();
    int doctorSalary = Integer.parseInt(doctorsalary.getText());
    String doctorAddress = doctoraddress.getText();
    java.util.Date joinDate = jCalendar1.getDate();

    Specialization selectedSpecialization = 
            (Specialization) specializationBox.getSelectedItem();
    int specializationId = selectedSpecialization.getId();

    Clinic selectedClinic = (Clinic) clinicBox.getSelectedItem();
    int clinicID = selectedClinic.getId();

    Connection conn = null;
    PreparedStatement pstmtCheckEmployee = null;
    PreparedStatement pstmtEmployee = null;
    PreparedStatement pstmtDoctor = null;
    PreparedStatement pstmtClinicEmployee = null;
    ResultSet rsCheck = null;
    ResultSet rs = null;

    try {
        conn = DatabaseUtil.getConnection();

        //Check if the EMPLOYEE_ID already exists
        String checkEmployeeQuery = 
                "SELECT COUNT(*) FROM employees WHERE EMPLOYEE_ID = ?";
        pstmtCheckEmployee = conn.prepareStatement(checkEmployeeQuery);
        pstmtCheckEmployee.setInt(1, doctorID);
        rsCheck = pstmtCheckEmployee.executeQuery();

        if (rsCheck.next() && rsCheck.getInt(1) > 0) {
            // EMPLOYEE_ID already exists
            JOptionPane.showMessageDialog
        (this, "Error: Employee ID already exists in the database!", "Error",
                JOptionPane.ERROR_MESSAGE);
            return; // Exit the method
        }

        //Insert into the employees table
        String insertEmployeeQuery = 
                "INSERT INTO employees (EMPLOYEE_ID, NAME, SALARY,"
                + " Address, phone, role) VALUES (?, ?, ?, ?, ?, ?)";
        pstmtEmployee = conn.prepareStatement(insertEmployeeQuery, 
                PreparedStatement.RETURN_GENERATED_KEYS);
        pstmtEmployee.setInt(1, doctorID);
        pstmtEmployee.setString(2, doctorName);
        pstmtEmployee.setInt(3, doctorSalary);
        pstmtEmployee.setString(4, doctorAddress);
        pstmtEmployee.setString(5, doctorPhone);
        pstmtEmployee.setInt(6, 0); 

        int rowsAffected = pstmtEmployee.executeUpdate();

        if (rowsAffected > 0) {
            //Insert into the doctors table
            String insertDoctorQuery = 
                    "INSERT INTO doctors (EMPLOYEE_ID,"
                    + " WORK_HISTORY, SPECIALIZATIONID) VALUES (?, ?, ?)";
            pstmtDoctor = conn.prepareStatement(insertDoctorQuery);
            pstmtDoctor.setInt(1, doctorID);
            pstmtDoctor.setDate(2, new java.sql.Date(joinDate.getTime()));
            pstmtDoctor.setInt(3, specializationId);
            rowsAffected = pstmtDoctor.executeUpdate();

            if (rowsAffected > 0) {
                // Step 4: Add the doctor to the clinic_employees table
                String insertClinicEmployeeQuery =
                        "INSERT INTO clinic_employees (CLINIC_ID, EMPLOYEE_ID)"
                        + " VALUES (?, ?)";
                pstmtClinicEmployee = conn.prepareStatement(insertClinicEmployeeQuery);
                pstmtClinicEmployee.setInt(1, clinicID); // Insert the clinic ID
                pstmtClinicEmployee.setInt(2, doctorID); // Insert the employee ID
                pstmtClinicEmployee.executeUpdate();

                // Show success dialog and clear fields
                JOptionPane.showMessageDialog(this,
                        "Doctor added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } finally {
        DatabaseUtil.close(null, pstmtCheckEmployee, rsCheck);
        DatabaseUtil.close(conn, pstmtEmployee, rs);
        DatabaseUtil.close(null, pstmtDoctor, null);
        DatabaseUtil.close(null, pstmtClinicEmployee, null);
    }
}


// Method to clear all fields
private void clearFields() {
    doctorId.setText("");
    doctorname.setText("");
    doctorphone.setText("");
    doctorsalary.setText("");
    doctoraddress.setText("");
    jCalendar1.setDate(new java.util.Date()); // Reset to the current date
    specializationBox.setSelectedIndex(0); // Reset to the first item
    clinicBox.setSelectedIndex(0); // Reset to the first item
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        doctorname = new javax.swing.JTextField();
        doctorphone = new javax.swing.JTextField();
        doctorsalary = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jCalendar1 = new com.toedter.calendar.JCalendar();
        specializationBox = new javax.swing.JComboBox<>();
        clinicBox = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        doctorId = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        doctoraddress = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();

        jLabel5.setText("doctor name:");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("doctor name:");

        jLabel2.setText("Specialization");

        jLabel3.setText("doctor salary:");

        jLabel4.setText("doctor phone:");

        jLabel6.setText("Clinic:");

        jLabel7.setText("Join Date:");

        specializationBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        specializationBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specializationBoxActionPerformed(evt);
            }
        });

        clinicBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton1.setText("add doctor");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel8.setText("doctor ID:");

        jLabel9.setText("doctor address:");

        jButton2.setText("Show all doctors");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel9)
                            .addComponent(jLabel4)
                            .addComponent(jLabel8)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel6))
                        .addGap(94, 94, 94))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(124, 124, 124)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCalendar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(doctorname, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(doctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(doctorphone, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(doctoraddress, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(doctorsalary, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(specializationBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(clinicBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(doctorId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(doctorname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(doctorphone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(doctoraddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(doctorsalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(specializationBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(clinicBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCalendar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2)
                .addContainerGap(55, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void specializationBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specializationBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_specializationBoxActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
addDoctor();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DoctorsInfo().setVisible(true);
            }
        }); 
    }//GEN-LAST:event_jButton2ActionPerformed

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
            java.util.logging.Logger.getLogger(addnewdoctor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(addnewdoctor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(addnewdoctor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(addnewdoctor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new addnewdoctor().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> clinicBox;
    private javax.swing.JTextField doctorId;
    private javax.swing.JTextField doctoraddress;
    private javax.swing.JTextField doctorname;
    private javax.swing.JTextField doctorphone;
    private javax.swing.JTextField doctorsalary;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private com.toedter.calendar.JCalendar jCalendar1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JComboBox<String> specializationBox;
    // End of variables declaration//GEN-END:variables
}
