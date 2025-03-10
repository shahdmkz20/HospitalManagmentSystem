
import databaseConnection.DatabaseUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author shahd
 */
public class doctorsIncome extends javax.swing.JFrame {

    /**
     * Creates new form doctorsIncome
     */
    public doctorsIncome() {
        initComponents();
        try {
            doctorIncome();
        } catch (SQLException ex) {
            Logger.getLogger(doctorsIncome.class.getName()).log(Level.SEVERE, null, ex);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        doctorIncome = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        doctorIncome.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DoctorID", "DoctorName", "yearly salary", "bonus", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(doctorIncome);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 619, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
       public void doctorIncome() throws SQLException {
    java.sql.Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        // Use DatabaseUtil to get the connection
        conn = DatabaseUtil.getConnection();
       
        String fetchDoctorIncomeData = 
             "SELECT " +
        "    e.EMPLOYEE_ID AS doctorID, " +
        "    e.NAME AS doctorName, " +
        "    e.SALARY * 12 AS totalSalaryForYear, " +
        "    COALESCE(div.doctor_income, 0) AS bonus " +
        "FROM " +
        "    employees e " +
        "LEFT JOIN " +
        "    doctor_income_view div ON e.EMPLOYEE_ID = div.doctor_id " +
        "WHERE " +
        "    e.ROLE = 0";
        // Creating a DefaultTableModel to hold the table data
            DefaultTableModel model = (DefaultTableModel) doctorIncome.getModel();
        model.setRowCount(0); // Clear existing rows if any

        pstmt = conn.prepareStatement(fetchDoctorIncomeData);
        rs = pstmt.executeQuery();
        
        // Fetching data from ResultSet and adding to JTable
         while (rs.next()) {
            int doctorID = rs.getInt("doctorID");
            String doctorName = rs.getString("doctorName");
            int totalSalaryForYear = rs.getInt("totalSalaryForYear");
            double bonus = rs.getDouble("bonus");
            double total  = totalSalaryForYear + bonus;
           

            // Adding row to table model
            model.addRow(new Object[]{doctorID, doctorName, totalSalaryForYear,
                bonus , total});
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        // Close resources
        DatabaseUtil.close(conn, pstmt, rs);
    }
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
            java.util.logging.Logger.getLogger(doctorsIncome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(doctorsIncome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(doctorsIncome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(doctorsIncome.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new doctorsIncome().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable doctorIncome;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
