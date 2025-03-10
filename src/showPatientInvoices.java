
import databaseConnection.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author shahd
 */
public class showPatientInvoices extends javax.swing.JFrame {

    int patientID;

    /**
     * Creates new form showPatientInvoices
     */
    public showPatientInvoices() throws SQLException {
        initComponents();
        fetchPatientInvoices();

    }

    public showPatientInvoices(int PatientID) throws SQLException {
        this.patientID = PatientID;

        initComponents();
        fetchPatientInvoices();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        invoicesTable = new javax.swing.JTable();
        patientName = new javax.swing.JLabel();
        patientName1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        invoicesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "invoiceID", "doctor", "nurse", "dischargeDate", "status", "amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(invoicesTable);

        patientName.setText("name");

        patientName1.setText("Patient:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 619, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(patientName1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patientName, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(patientName)
                    .addComponent(patientName1))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
  public void fetchPatientInvoices() throws SQLException {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        // Use DatabaseUtil to get the connection
        conn = DatabaseUtil.getConnection();

        // Fetch the patient's name securely using PreparedStatement
        String fetchName = "SELECT NAME FROM PATIENTS WHERE PATIENT_ID = ?";
        pstmt = conn.prepareStatement(fetchName);
        pstmt.setInt(1, patientID); // Bind parameter to avoid SQL injection
        rs = pstmt.executeQuery();

        if (rs.next()) {  // Ensure a result is returned
            patientName.setText(rs.getString("NAME"));
        } else {
            JOptionPane.showMessageDialog(null,
                    "No patient found with the provided ID: " + patientID, 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit the method if no patient is found
        }

        String fetchNurseData = "SELECT"
                + "    i.INVOICE_ID AS InvoiceID,"
                + "    e.NAME AS INVOICE_DOCTOR_NAME,     "
                + "    n.NAME AS NURSE_NAME,  "
                + "    pd.DISCHARGE_DATE AS FormattedDate,"
                + "    CASE pd.DISCHARGE_STATUS "
                + "        WHEN 0 THEN 'Dead' "
                + "        WHEN 1 THEN 'Review' "
                + "        WHEN 2 THEN 'Recovered' "
                + "        ELSE 'Unknown' "
                + "    END AS DischargeStatus,"
                + "    i.AMOUNT AS InvoiceAmount "
                + "FROM  "
                + "    invoices i "
                + "LEFT JOIN "
                + "    (  "
                + "        SELECT "
                + "            PATIENT_ID,"
                + "            INVOICE_ID, "
                + "            NURSE_ID, "
                + "             DOCTOR_ID,  "
                + "            DISCHARGE_DATE, "
                + "            DISCHARGE_STATUS, "
                + "            ROW_NUMBER() OVER"
                + " (PARTITION BY INVOICE_ID ORDER BY DISCHARGE_DATE DESC)"
                + " AS rn "
                + "        FROM "
                + "            patient_discharges "
                + "    ) pd ON i.INVOICE_ID = pd.INVOICE_ID AND pd.rn = 1 "
                + "LEFT JOIN "
                + "    doctors d ON pd.DOCTOR_ID = d.EMPLOYEE_ID  "
                + "LEFT JOIN "
                + "    employees e ON d.EMPLOYEE_ID = e.EMPLOYEE_ID  "
                + "LEFT JOIN "
                + "    employees n ON pd.NURSE_ID = n.EMPLOYEE_ID "
                + "WHERE "
                + "    pd.PATIENT_ID = ? "
                + "ORDER BY i.INVOICE_ID";

     
        DefaultTableModel model = (DefaultTableModel) invoicesTable.getModel();
        model.setRowCount(0); 

        pstmt = conn.prepareStatement(fetchNurseData);
        pstmt.setInt(1, patientID);
        rs = pstmt.executeQuery();

        // Check if invoices exist
        boolean hasInvoices = false;

        // Fetching data from ResultSet and adding to JTable
        while (rs.next()) {
            hasInvoices = true; // Set flag if data exists
            int invoiceID = rs.getInt("InvoiceID");
            String formattedDate = rs.getString("FormattedDate");
            String dischargeStatus = rs.getString("DischargeStatus");
            String doctorName = rs.getString("INVOICE_DOCTOR_NAME");
            String nurseName = rs.getString("NURSE_NAME");
            double invoiceAmount = rs.getDouble("InvoiceAmount");

            // Adding row to table model
            model.addRow(new Object[]{invoiceID, doctorName, nurseName, formattedDate, dischargeStatus, invoiceAmount});
        }

        // If no invoices are found
        if (!hasInvoices) {
            JOptionPane.showMessageDialog(null, "No invoices found for the patient ID: " + patientID, 
                                          "Information", JOptionPane.INFORMATION_MESSAGE);
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        // Close resources
        DatabaseUtil.close(conn, pstmt, rs);
    }
}

public void showName(){
    
}
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
            java.util.logging.Logger.getLogger(showPatientInvoices.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(showPatientInvoices.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(showPatientInvoices.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(showPatientInvoices.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new showPatientInvoices().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(showPatientInvoices.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable invoicesTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel patientName;
    private javax.swing.JLabel patientName1;
    // End of variables declaration//GEN-END:variables
}
