/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nguye
 */
public class QUANLYDIEM extends javax.swing.JFrame {

    private DefaultTableModel model;
    private Connection conn;
    private ArrayList<GRADE> list = new ArrayList<>();
    private boolean check = false;
    private int current = 0;

    /**
     * Creates new form QUANLYDIEM
     */
    public QUANLYDIEM() throws SQLException {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Quản lý điểm sinh viên");
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        }
        conn = KETNOISQL.getConnection("sa", "nguyentuakina", "QLDA_SINHVIEN");
        loadTable();
//        display(current);
        lblReocrd.setText(layThongTinBanGhi());
        lblTop3.setVisible(false);
    }

    public void loadTable() throws SQLException {
        model = (DefaultTableModel) tblDiem.getModel();
        model.setRowCount(0);
        tblDiem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        String[] a = {"ID","MASV","TIENGANH","TINHOC","GDTC"};
        String sql = "select GRADE.*,STUDENTS.HOTEN"
                + " from GRADE join STUDENTS on GRADE.MASV = STUDENTS.MASV";
//        PreparedStatement pst = conn.prepareStatement(sql);
//        pst.setInt(1, 1);
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        list.clear();
        while (rs.next()) {
            int iD = rs.getInt("ID");
            String maSv = rs.getString("MASV");
            String hoTen = rs.getString("HOTEN");
            double tiengAnh = rs.getDouble("TIENGANH");
            double tinHoc = rs.getDouble("TINHOC");
            double gdTC = rs.getDouble("GDTC");
            double trungBinh = (tiengAnh + tinHoc + gdTC) / 3;
            GRADE gr = new GRADE(iD, maSv, hoTen, tiengAnh, tinHoc, gdTC, trungBinh);
            list.add(gr);
        }
        for (GRADE gr : list) {
            model.addRow(new Object[]{
                gr.getMaSV(), gr.getHoTen(), gr.getTiengAnh(), gr.getTinHoc(),
                gr.getGdTC(), gr.getDiemTb()
            });
        }
        rs.close();
    }

    public boolean checkNull() {
        String formatMaSV = "SV[0-9]{3}";
        if (txtMasv.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập mã sinh viên!");
            return true;
        } else if (!txtMasv.getText().matches(formatMaSV)) {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không đúng định dạng!");
            return true;
        }
        for (GRADE grade : list) {
            if (grade.getMaSV().equalsIgnoreCase(txtMasv.getText())) {
                check = true;
            }
        }
        return false;
    }

    public boolean checkDiem() {
        double diem;
        if (txtTienganh.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập điểm tiếng anh!");
            return true;
        }
        try {
            diem = Double.parseDouble(txtTienganh.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số!");
            return true;
        }
        if (Double.parseDouble(txtTienganh.getText()) > 10 || Double.parseDouble(txtTienganh.getText()) < 0) {
            JOptionPane.showMessageDialog(this, "Điểm chỉ được nhập từ 1 đến 10!");
            return true;
        }
        if (txtTinhoc.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập điểm tin học!");
            return true;
        }
        try {
            diem = Double.parseDouble(txtTinhoc.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số!");
            return true;
        }
        if (Double.parseDouble(txtTinhoc.getText()) > 10 || Double.parseDouble(txtTinhoc.getText()) < 0) {
            JOptionPane.showMessageDialog(this, "Điểm chỉ được nhập từ 1 đến 10!");
            return true;
        }
        if (txtGiaoducTC.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập điểm GDTC!");
            return true;
        }
        try {
            diem = Double.parseDouble(txtGiaoducTC.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số!");
            return true;
        }
        if (Double.parseDouble(txtGiaoducTC.getText()) > 10 || Double.parseDouble(txtGiaoducTC.getText()) < 0) {
            JOptionPane.showMessageDialog(this, "Điểm chỉ được nhập từ 1 đến 10!");
            return true;
        }
        return false;
    }

    public boolean checkMaSV() {
        String selectSQL = "select MASV from STUDENTS";
        Statement st;
        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(selectSQL);
            while (rs.next()) {
                if (rs.getString("MASV").equalsIgnoreCase(txtMasv.getText())) {
                    return true;
                }
            }
            loadTable();
        } catch (SQLException ex) {
            Logger.getLogger(QUANLYDIEM.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void btnSave() throws SQLException {
        if (checkNull()) {
            return;
        } else if (check) {
            check = false;
            JOptionPane.showMessageDialog(this, "Mã sinh viên có điểm!");
            return;
        } else if (checkDiem()) {
            return;
        }
        if (checkMaSV()) {
            int id = 0;
            boolean checkID = true;
            while (checkID) {
                Random random = new Random();
                id = random.nextInt(1000); // Thay số trong ngoặc bằng giới hạn trên của giá trị ID
                String checkSQL = "select count(*) from GRADE where ID = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
//                if (list.size() > 5) {
//                    JOptionPane.showMessageDialog(this, "Max ID");
//                    checkID = false;
//                }
                if (rs.next() && rs.getInt(1) == 0) {
//                JOptionPane.showMessageDialog(this, rs.getInt(1));
                    checkID = false; // Không trùng, thoát khỏi vòng lặp
                }

            }
            Statement st = conn.createStatement();
            String insertSQL = "insert into GRADE values"
                    + "('" + id + "','" + txtMasv.getText() + "',"
                    + "'" + txtTienganh.getText() + "','" + txtTinhoc.getText() + "',"
                    + "'" + txtGiaoducTC.getText() + "')";
            int soDong = st.executeUpdate(insertSQL);
            if (soDong > 0) {
                JOptionPane.showMessageDialog(this, "Đã thêm thành công!");
            }
            loadTable();
        } else {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không tồn tại!");
        }
    }

    public void updateDiem() {
        String updateSQL = "update GRADE set TIENGANH = ?, TINHOC = ?, GDTC = ? where MASV = '" + txtMasv.getText() + "'";
        PreparedStatement pst;
        if (checkNull()) {
            return;
        } else if (checkDiem()) {
            return;
        } else if (check) {
            check = false;
            try {
                pst = conn.prepareStatement(updateSQL);
                pst.setDouble(1, Double.parseDouble(txtTienganh.getText()));
                pst.setDouble(2, Double.parseDouble(txtTinhoc.getText()));
                pst.setDouble(3, Double.parseDouble(txtGiaoducTC.getText()));
                int soDong = pst.executeUpdate();
                if (soDong > 0) {
                    JOptionPane.showMessageDialog(this, "Update thành công");
                    loadTable();
//                    txtSearchMasv.setText("");
//                    txtHoten.setText("");
//                    txtMasv.setText("");
//                    txtTienganh.setText("");
//                    txtTinhoc.setText("");
//                    txtGiaoducTC.setText("");
//                    lblTB.setText("0");
//                    model.setRowCount(0);
//                    try {
//                        loadTable();
//                    } catch (SQLException ex) {
//                        Logger.getLogger(QUANLYDIEM.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(QUANLYDIEM.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không tồn tại!");
        }

    }

    public String layThongTinBanGhi() {
        return "Record: " + (current + 1) + " of " + list.size();
    }

    public void display(int so) {
        GRADE gr = list.get(so);
        txtHoten.setText(gr.getHoTen());
        txtMasv.setText(gr.getMaSV());
        txtTienganh.setText(String.valueOf(gr.getTiengAnh()));
        txtTinhoc.setText(String.valueOf(gr.getTinHoc()));
        txtGiaoducTC.setText(String.valueOf(gr.getGdTC()));
        lblTB.setText(String.valueOf(gr.getDiemTb()));
        tblDiem.setRowSelectionInterval(so, so);
        current = so;
        lblReocrd.setText(layThongTinBanGhi());
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
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtSearchMasv = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtTienganh = new javax.swing.JTextField();
        txtMasv = new javax.swing.JTextField();
        txtHoten = new javax.swing.JTextField();
        txtTinhoc = new javax.swing.JTextField();
        txtGiaoducTC = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        lblTB = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnNew = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        lblTop3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDiem = new javax.swing.JTable();
        btnTop3 = new javax.swing.JButton();
        btnAll = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        lblReocrd = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("QUẢN LÝ ĐIỂM SINH VIÊN");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("TÌm kiếm");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel3.setText("Mã sinh viên:");

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/search.png"))); // NOI18N
        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addGap(15, 15, 15)
                .addComponent(txtSearchMasv)
                .addGap(18, 18, 18)
                .addComponent(btnSearch)
                .addGap(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSearchMasv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Họ tên:");

        jLabel6.setText("Mã sinh viên:");

        jLabel7.setText("Tiếng anh:");

        jLabel8.setText("Tin học:");

        jLabel9.setText("Giáo dục TC:");

        txtHoten.setEditable(false);

        txtTinhoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTinhocActionPerformed(evt);
            }
        });

        txtGiaoducTC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGiaoducTCActionPerformed(evt);
            }
        });

        lblTB.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTB.setForeground(new java.awt.Color(255, 0, 51));
        lblTB.setText("0");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("ĐIỂM TB");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblTB, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtHoten, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                        .addGap(15, 15, 15))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtMasv)
                            .addComponent(txtTienganh)
                            .addComponent(txtTinhoc)
                            .addComponent(txtGiaoducTC, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtHoten, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtMasv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtTienganh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(txtTinhoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(txtGiaoducTC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/add.png"))); // NOI18N
        btnNew.setText("New");
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Save.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/update.png"))); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        jPanel4.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/first.png"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton4);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/pri.png"))); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton5);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/next.png"))); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton3);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/last.png"))); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton2);

        lblTop3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTop3.setForeground(new java.awt.Color(51, 102, 255));
        lblTop3.setText("3 Sinh viên có điểm cao nhất");

        tblDiem.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã sinh viên", "Họ và tên", "Tiếng anh", "Tin học", "Giáo dục TC", "Điểm TB"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblDiem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblDiemMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tblDiem);

        btnTop3.setText("TOP 3");
        btnTop3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTop3ActionPerformed(evt);
            }
        });

        btnAll.setText("ALL");
        btnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAllActionPerformed(evt);
            }
        });

        btnLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/exit.png"))); // NOI18N
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        lblReocrd.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblReocrd.setForeground(new java.awt.Color(255, 0, 51));
        lblReocrd.setText("Record: 0 of 10");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnTop3, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAll, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblTop3)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblReocrd)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(btnUpdate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                        .addComponent(btnDelete, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnSave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1))))
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(96, 96, 96)
                .addComponent(btnLogout)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(btnLogout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(lblReocrd)))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTop3)
                    .addComponent(btnAll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTop3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtGiaoducTCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGiaoducTCActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtGiaoducTCActionPerformed

    private void txtTinhocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTinhocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTinhocActionPerformed

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        txtSearchMasv.setText("");
        txtHoten.setText("");
        txtMasv.setText("");
        txtTienganh.setText("");
        txtTinhoc.setText("");
        txtGiaoducTC.setText("");
        lblTB.setText("0");
        model.setRowCount(0);
        try {
            loadTable();
        } catch (SQLException ex) {
            Logger.getLogger(QUANLYDIEM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            btnSave();
        } catch (SQLException ex) {
            Logger.getLogger(QUANLYDIEM.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        String deleteSQL = "delete from GRADE where MASV = '" + txtMasv.getText() + "'";
        try {
            Statement acTionDL = conn.createStatement();
            int soDong = acTionDL.executeUpdate(deleteSQL);
            if (soDong > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                lblReocrd.setText(layThongTinBanGhi());
                loadTable();
            }
            btnNewActionPerformed(evt);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại\n" + ex);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateDiem();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        current = list.size() - 1;
        display(current);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAllActionPerformed
        try {
            loadTable();
            lblTop3.setVisible(false);
        } catch (SQLException ex) {
            Logger.getLogger(QUANLYDIEM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAllActionPerformed

    private void btnTop3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTop3ActionPerformed
        String selectTOP3 = "select top 3 GRADE.ID, GRADE.MASV, STUDENTS.HOTEN, GRADE.TIENGANH, GRADE.TINHOC, GRADE.GDTC "
                + "from GRADE, STUDENTS where GRADE.MASV = STUDENTS.MASV order by (TIENGANH+TINHOC+GDTC)/3 desc";
        try {
            btnNewActionPerformed(evt);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(selectTOP3);
            model.setRowCount(0);
            list.clear();
            current = 0;
            while (rs.next()) {
                int iD = rs.getInt("ID");
                String maSv = rs.getString("MASV");
                String hoTen = rs.getString("HOTEN");
                double tiengAnh = rs.getDouble("TIENGANH");
                double tinHoc = rs.getDouble("TINHOC");
                double gdTC = rs.getDouble("GDTC");
                double trungBinh = (tiengAnh + tinHoc + gdTC) / 3;
                GRADE gr = new GRADE(iD, maSv, hoTen, tiengAnh, tinHoc, gdTC, trungBinh);
                list.add(gr);
            }
            for (GRADE gr : list) {
                model.addRow(new Object[]{
                    gr.getMaSV(), gr.getHoTen(), gr.getTiengAnh(), gr.getTinHoc(),
                    gr.getGdTC(), gr.getDiemTb()
                });
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        lblTop3.setVisible(true);
    }//GEN-LAST:event_btnTop3ActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        LOGIN lg;
        int a = JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất",
                "Thông báo", JOptionPane.YES_NO_OPTION);
        if (a == 0) {
            try {
                lg = new LOGIN();
                lg.setVisible(true);

            } catch (SQLException ex) {
                Logger.getLogger(QUANLYSINHVIEN.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            setVisible(false);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void tblDiemMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDiemMousePressed
        int index = tblDiem.getSelectedRow();
        current = index;
        txtHoten.setText(list.get(index).getHoTen());
        txtMasv.setText(list.get(index).getMaSV());
        txtTienganh.setText(String.valueOf(list.get(index).getTiengAnh()));
        txtTinhoc.setText(String.valueOf(list.get(index).getTinHoc()));
        txtGiaoducTC.setText(String.valueOf(list.get(index).getGdTC()));
        lblTB.setText(String.valueOf(list.get(index).getDiemTb()));
    }//GEN-LAST:event_tblDiemMousePressed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        boolean checkMASV = true;
        for (int i = 0; i < list.size(); i++) {
            if (txtSearchMasv.getText().equals(list.get(i).getMaSV())) {
                display(i);
                checkMASV = false;
            }
        }
        if (checkMASV) {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không tồn tại!");
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        current = 0;
        display(current);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        current++;
        if (current >= list.size()) {
            JOptionPane.showMessageDialog(this, "Bạn đang ở cuối danh sách!");
            current = list.size() - 1;
            return;
        }
        display(current);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        current--;
        if (current < 0) {
            JOptionPane.showMessageDialog(this, "Bạn đang ở đầu danh sách!");
            current = 0;
            return;
        }
        display(current);
    }//GEN-LAST:event_jButton5ActionPerformed

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
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(QUANLYDIEM.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(QUANLYDIEM.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(QUANLYDIEM.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(QUANLYDIEM.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new QUANLYDIEM().setVisible(true);

                } catch (SQLException ex) {
                    Logger.getLogger(QUANLYDIEM.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAll;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnTop3;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblReocrd;
    private javax.swing.JLabel lblTB;
    private javax.swing.JLabel lblTop3;
    private javax.swing.JTable tblDiem;
    private javax.swing.JTextField txtGiaoducTC;
    private javax.swing.JTextField txtHoten;
    private javax.swing.JTextField txtMasv;
    private javax.swing.JTextField txtSearchMasv;
    private javax.swing.JTextField txtTienganh;
    private javax.swing.JTextField txtTinhoc;
    // End of variables declaration//GEN-END:variables
}
