package qlda_agile;

public class GRADE {

    private int id;
    private String maSV, hoTen;
    private double tiengAnh, tinHoc, gdTC, diemTb;

    public GRADE() {
    }

    public GRADE(int id, String maSV, String hoTen, double tiengAnh, double tinHoc, double gdTC, double diemTb) {
        this.id = id;
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.tiengAnh = tiengAnh;
        this.tinHoc = tinHoc;
        this.gdTC = gdTC;
        this.diemTb = diemTb;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public double getTiengAnh() {
        return tiengAnh;
    }

    public void setTiengAnh(double tiengAnh) {
        this.tiengAnh = tiengAnh;
    }

    public double getTinHoc() {
        return tinHoc;
    }

    public void setTinHoc(double tinHoc) {
        this.tinHoc = tinHoc;
    }

    public double getGdTC() {
        return gdTC;
    }

    public void setGdTC(double gdTC) {
        this.gdTC = gdTC;
    }

    public double getDiemTb() {
        return diemTb;
    }

    public void setDiemTb(double diemTb) {
        this.diemTb = diemTb;
    }

}
