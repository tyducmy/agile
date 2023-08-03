/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package qlda_agile;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nguye
 */
public class QUANLYSINHVIEN extends javax.swing.JFrame {

    private DefaultTableModel model;
    private Connection conn;
    private String imageSave = "";
    private ArrayList<STUDENTS> list = new ArrayList<>();
    private int number = 0;
    private File f1, f2, dir = new File("src\\image");
    private boolean deleted = false;
    private int current = 0;

    /**
     * Creates new form QUANLYSINHVIEN
     *
     * @throws java.sql.SQLException
     */
    public QUANLYSINHVIEN() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        }
        conn = KETNOISQL.getConnection("sa", "bachvanchilo", "QLDA_SINHVIEN");
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Quản lý sinh viên");
        loadTable();
//        display(current);
        lblReocrd.setText(layThongTinBanGhi());
    }

    public void display(int index) {
        txtMasv.setText(list.get(index).getMaSV());
        txtHoten.setText(list.get(index).getHoTen());
        txtEmail.setText(list.get(index).geteMail());
        txtSodt.setText(list.get(index).getSoDT());
        if (list.get(index).getGioiTinh().equals("Nam")) {
            rNam.setSelected(true);
        } else {
            rNu.setSelected(true);
        }
        txtDiachi.setText(list.get(index).getDiaChi());
        ImageIcon ic = new ImageIcon(list.get(index).getAnh());
        ic.getImage().getScaledInstance(lblAnh.getWidth(), lblAnh.getHeight(), Image.SCALE_SMOOTH);
        lblAnh.setText("");
        lblAnh.setHorizontalAlignment(lblAnh.CENTER);
        lblAnh.setVerticalAlignment(lblAnh.CENTER);
        lblAnh.setIcon(ic);
        tblSinhVien.setRowSelectionInterval(index, index);
        current = index;
        lblReocrd.setText(layThongTinBanGhi());
    }

    public void loadTable() throws SQLException {
        model = (DefaultTableModel) tblSinhVien.getModel();//Lấy model trên table set cho model
        //Fortmat Table
        model.setRowCount(0);
        tblSinhVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        String[] a = {"Mã sinh viên", "Họ và tên", "Email", "Số điện thoại", "Giới tính", "Địa chỉ"};
        String select = "select * from STUDENTS";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(select);
        list.clear();
        while (rs.next()) {
            String maSV = rs.getString("MASV");
            String hoTen = rs.getString("HOTEN");
            String eMail = rs.getString("EMAIL");
            String soDT = (rs.getString("SODT"));
            String gioiTinh = rs.getString("GIOITINH");
            String diaChi = rs.getString("DIACHI");
            String anh = rs.getString("HINH");
            STUDENTS sv = new STUDENTS(maSV, hoTen, eMail, soDT, gioiTinh, diaChi, anh);
            list.add(sv);
        }
        for (STUDENTS sv : list) {
            model.addRow(new Object[]{
                sv.getMaSV(), sv.getHoTen(), sv.geteMail(), sv.getSoDT(), sv.getGioiTinh(), sv.getDiaChi()
            });
        }
        rs.close();
    }

    public String getGioiTinh() {
        if (rNam.isSelected()) {
            return "Nam";
        } else {
            return "Nữ";
        }
    }

    public void btnSave() throws SQLException {
        if (checkNull()) {
            return;
        } else if (checkMASV()) {
            JOptionPane.showMessageDialog(this, "Sinh viên này đã có rồi!");
            return;
        }

        String insertSql = "insert into STUDENTS values"
                + "('" + txtMasv.getText() + "',N'" + txtHoten.getText() + "',"
                + "'" + txtEmail.getText() + "','" + txtSodt.getText() + "',"
                + "N'" + getGioiTinh() + "',N'" + txtDiachi.getText() + "',N'" + imageSave + "')";
        boolean checkID = true;
        int id = 0;
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
        String insertSQL = "insert into GRADE values"
                + "('" + id + "','" + txtMasv.getText() + "','0','0','0')";
        String insertUSERSQL = "insert into USERS values"
                + "('" + txtEmail.getText() + "','123456','sv')";
        try {
            Statement acTionIS = conn.createStatement();
            int soDong = acTionIS.executeUpdate(insertSql);
            if (soDong > 0) {
                acTionIS.executeUpdate(insertSQL);
                acTionIS.execute(insertUSERSQL);
                JOptionPane.showMessageDialog(this, "Đã thêm thành công!");
            }
            loadTable();
            copyImage();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Thêm thất bại\n" + ex);
        }
    }

    public void copyImage() throws SQLException {
        if (!dir.exists()) {// nếu folder image chưa tồn tại
            dir.mkdirs();
        }
        f2 = new File(imageSave);
        try {
            Files.copy(f1.toPath(), f2.toPath()); // Sao chép tệp tin ảnh
            loadTable();
        } catch (IOException ex) {
            Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(Level.SEVERE, null, ex);
        }
        String Iname = f2.getAbsolutePath();
        ImageIcon ic = new ImageIcon(Iname);
//            JOptionPane.showMessageDialog(this, imageSave);
//            chinh hinh vua voi label
        ic.getImage().getScaledInstance(lblAnh.getWidth(), lblAnh.getHeight(), Image.SCALE_SMOOTH);
        lblAnh.setText("");
        lblAnh.setHorizontalAlignment(lblAnh.CENTER);
        lblAnh.setVerticalAlignment(lblAnh.CENTER);
        lblAnh.setIcon(ic);
    }

    public void deletedImage() throws SQLException {
        if (deleted) {
            if (checkMASV()) {
                File file = new File(list.get(number).getAnh());
                file.delete();
                loadTable();
            }
        }
    }

    public boolean checkNull() {
        String formatMaSV = "SV[0-9]{3}";
        if (txtMasv.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập mã sinh viên");
            return true;
        } else if (!txtMasv.getText().matches(formatMaSV)) {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không đúng định dạng!");
            return true;
        }
        if (txtHoten.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập họ tên");
            return true;
        }
        if (txtEmail.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập Email");
            return true;
        }
        if (txtSodt.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập số điện thoại");
            return true;
        }
        try {
            int phone = Integer.parseInt(txtSodt.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số điện thoại bạn nhập không đúng định dạng!");
            return true;
        }
        if (txtDiachi.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập địa chỉ");
            return true;
        }
        if (imageSave.equals("")) {
            JOptionPane.showMessageDialog(this, "Bạn chưa chọn ảnh");
            return true;
        }
        return false;
    }

    public boolean checkMASV() {
//        JOptionPane.showMessageDialog(this, tblSinhVien.getRowCount());
        for (int i = 0; i < list.size(); i++) {
            if (txtMasv.getText().equals(list.get(i).getMaSV())) {
                number = i;
                return true;
            }
//            JOptionPane.showMessageDialog(this, i);
        }
        return false;
    }

    public String layThongTinBanGhi() {
        return "Record: " + (current + 1) + " of " + list.size();
    }

    public void actionUpdate(String a) throws SQLException {
        Statement actionUP = conn.createStatement();
        actionUP.executeUpdate(a);
    }

    public void updateName(String codeSQL) throws SQLException {
        if (!txtHoten.getText().equalsIgnoreCase(
                list.get(number).getHoTen())) {
            actionUpdate(codeSQL);
        }
    }

    public void updateEmail(String codeSQL) throws SQLException {
        if (!txtEmail.getText().equalsIgnoreCase(
                list.get(number).geteMail())) {
            actionUpdate(codeSQL);
        }
    }

    public void updateTelPhone(String codeSQL) throws SQLException {
        if (!txtSodt.getText().equalsIgnoreCase(
                list.get(number).getSoDT())) {
            actionUpdate(codeSQL);
        }
    }

    public void updateAddRess(String codeSQL) throws SQLException {
        if (!txtDiachi.getText().equalsIgnoreCase(
                list.get(number).getDiaChi())) {
            actionUpdate(codeSQL);
        }
    }

    public void updateSex(String codeSQL) throws SQLException {
        actionUpdate(codeSQL);
    }

    public void updateImage(String codeSQL) throws SQLException {
        deletedImage();
        actionUpdate(codeSQL);
        copyImage();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollBar1 = new javax.swing.JScrollBar();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtMasv = new javax.swing.JTextField();
        txtHoten = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtSodt = new javax.swing.JTextField();
        rNam = new javax.swing.JRadioButton();
        rNu = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDiachi = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        btnNew = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        jpnAnh = new javax.swing.JPanel();
        lblAnh = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblSinhVien = new javax.swing.JTable();
        btnLogout = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        txtSearchMasv = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        lblReocrd = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("QUẢN LÝ SINH VIÊN");

        jLabel2.setText("Mã sinh viên::");

        jLabel3.setText("Họ và tên:");

        jLabel4.setText("Email:");

        jLabel5.setText("Số ĐT:");

        jLabel6.setText("Giới tính:");

        jLabel7.setText("Địa chỉ:");

        txtMasv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMasvActionPerformed(evt);
            }
        });

        txtHoten.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHotenActionPerformed(evt);
            }
        });

        txtSodt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSodtActionPerformed(evt);
            }
        });

        buttonGroup1.add(rNam);
        rNam.setText("Nam");

        buttonGroup1.add(rNu);
        rNu.setText("Nữ");

        txtDiachi.setColumns(20);
        txtDiachi.setRows(5);
        jScrollPane1.setViewportView(txtDiachi);

        jPanel2.setLayout(new java.awt.GridLayout(2, 2, 5, 5));

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/add.png"))); // NOI18N
        btnNew.setText("New");
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });
        jPanel2.add(btnNew);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Save.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jPanel2.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(btnDelete);

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/update.png"))); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        jPanel2.add(btnUpdate);

        jpnAnh.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jpnAnh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jpnAnhMousePressed(evt);
            }
        });

        lblAnh.setMaximumSize(new java.awt.Dimension(63, 27));
        lblAnh.setMinimumSize(new java.awt.Dimension(63, 27));
        lblAnh.setPreferredSize(new java.awt.Dimension(63, 27));

        javax.swing.GroupLayout jpnAnhLayout = new javax.swing.GroupLayout(jpnAnh);
        jpnAnh.setLayout(jpnAnhLayout);
        jpnAnhLayout.setHorizontalGroup(
            jpnAnhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblAnh, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
        );
        jpnAnhLayout.setVerticalGroup(
            jpnAnhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblAnh, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
        );

        tblSinhVien.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã sinh viên", "Họ và tên", "Email", "Số ĐT", "Giới tính", "Địa chỉ"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSinhVien.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblSinhVienMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(tblSinhVien);

        btnLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/exit.png"))); // NOI18N
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jLabel9.setText("Mã sinh viên:");

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
                .addComponent(jLabel9)
                .addGap(15, 15, 15)
                .addComponent(txtSearchMasv, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnSearch)
                .addGap(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtSearchMasv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap(15, Short.MAX_VALUE))
        );

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

        lblReocrd.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblReocrd.setForeground(new java.awt.Color(255, 0, 51));
        lblReocrd.setText("Record: 0 of 10");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                .addComponent(lblReocrd)
                .addGap(16, 16, 16))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblReocrd)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel5))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtHoten)
                                            .addComponent(txtEmail)
                                            .addComponent(txtSodt, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(rNam)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(rNu, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addComponent(txtMasv, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                        .addGap(39, 39, 39)
                                        .addComponent(jpnAnh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(43, 43, 43))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 539, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(34, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(222, 222, 222)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnLogout, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtMasv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtHoten, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtSodt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(rNam)
                            .addComponent(rNu)))
                    .addComponent(jpnAnh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSodtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSodtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSodtActionPerformed

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        txtMasv.setText("");
        txtHoten.setText("");
        txtEmail.setText("");
        txtSodt.setText("");
        txtDiachi.setText("");
        txtSearchMasv.setText("");
        lblAnh.setIcon(null);
    }//GEN-LAST:event_btnNewActionPerformed

    private void txtHotenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHotenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtHotenActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            btnSave();
        } catch (SQLException ex) {
            Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        String deleteSQL = "delete from STUDENTS where MASV = '" + txtMasv.getText() + "'";
        try {
            Statement acTionDL = conn.createStatement();
            int soDong = acTionDL.executeUpdate(deleteSQL);
            if (soDong > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                deleted = true;
            }
            deletedImage();
            loadTable();
            btnNewActionPerformed(evt);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại\n" + ex);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void tblSinhVienMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSinhVienMousePressed
        display(tblSinhVien.getSelectedRow());
//        btnDelete.setEnabled(false);
        layThongTinBanGhi();
    }//GEN-LAST:event_tblSinhVienMousePressed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        String upHoTen = "update STUDENTS set HOTEN = N'" + txtHoten.getText() + "'"
                + " where MASV = '" + txtMasv.getText() + "'";
        String upMail = "update STUDENTS set EMAIL = N'" + txtEmail.getText() + "'"
                + " where MASV = '" + txtMasv.getText() + "'";
        String upPhone = "update STUDENTS set SODT = N'" + txtSodt.getText() + "'"
                + " where MASV = '" + txtMasv.getText() + "'";
        String upDiaChi = "update STUDENTS set DIACHI = N'" + txtDiachi.getText() + "'"
                + " where MASV = '" + txtMasv.getText() + "'";
        String sex = "update STUDENTS set GIOITINH = N'" + getGioiTinh() + "'"
                + " where MASV = '" + txtMasv.getText() + "'";
        String updateImg = "update STUDENTS set HINH = N'" + imageSave + "'"
                + " where MASV = '" + txtMasv.getText() + "'";
        if (checkMASV()) {
            try {
                if (!txtHoten.getText().isEmpty()) {
                    updateName(upHoTen);
                }
                if (!txtEmail.getText().isEmpty()) {
                    updateEmail(upMail);
                }
                if (!txtSodt.getText().isEmpty()) {
                    updateTelPhone(upPhone);
                }
                if (!txtDiachi.getText().isEmpty()) {
                    updateAddRess(upDiaChi);
                }
                updateSex(sex);
                if (!imageSave.equals("")) {
                    deleted = true;
                    updateImage(updateImg);
                }
                JOptionPane.showMessageDialog(this, "Update thành công!");
                loadTable();
//                btnNewActionPerformed(evt);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Update thất bại!\n"
                        + ex);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không tồn tại!");
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void jpnAnhMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jpnAnhMousePressed
        JFileChooser fc = new JFileChooser();//mở file
//        fc.setCurrentDirectory(new File("D:\\"));//set mac dinh
        int result = fc.showOpenDialog(this);//hiển thị hộp thoại
        if (result == fc.APPROVE_OPTION) {
            f1 = fc.getSelectedFile();// lấy file được chọn
            imageSave = dir + "\\" + f1.getName();
            String Iname = f1.getAbsolutePath();
            ImageIcon ic = new ImageIcon(Iname);
            ic.getImage().getScaledInstance(lblAnh.getWidth(), lblAnh.getHeight(), Image.SCALE_SMOOTH);
            lblAnh.setText("");
            lblAnh.setHorizontalAlignment(lblAnh.CENTER);
            lblAnh.setVerticalAlignment(lblAnh.CENTER);
            lblAnh.setIcon(ic);
        }

    }//GEN-LAST:event_jpnAnhMousePressed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        LOGIN lg;
        int a = JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất",
                "Thông báo", JOptionPane.YES_NO_OPTION);
        if (a == 0) {
            try {
                lg = new LOGIN();
                lg.setVisible(true);
            } catch (SQLException ex) {
                Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(Level.SEVERE, null, ex);
            }
            setVisible(false);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void txtMasvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMasvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMasvActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        current = 0;
        display(current);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        current--;
        if (current < 0) {
            JOptionPane.showMessageDialog(this, "Bạn đang ở đầu danh sách!");
            current = 0;
            return;
        }
        display(current);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        current++;
        if (current >= list.size()) {
            JOptionPane.showMessageDialog(this, "Bạn đang ở cuối danh sách!");
            current = list.size() - 1;
            return;
        }
        display(current);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        current = list.size() - 1;
        display(current);
    }//GEN-LAST:event_jButton2ActionPerformed

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
            java.util.logging.Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new QUANLYSINHVIEN().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(QUANLYSINHVIEN.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.ButtonGroup buttonGroup1;
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
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel jpnAnh;
    private javax.swing.JLabel lblAnh;
    private javax.swing.JLabel lblReocrd;
    private javax.swing.JRadioButton rNam;
    private javax.swing.JRadioButton rNu;
    private javax.swing.JTable tblSinhVien;
    private javax.swing.JTextArea txtDiachi;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtHoten;
    private javax.swing.JTextField txtMasv;
    private javax.swing.JTextField txtSearchMasv;
    private javax.swing.JTextField txtSodt;
    // End of variables declaration//GEN-END:variables
}
