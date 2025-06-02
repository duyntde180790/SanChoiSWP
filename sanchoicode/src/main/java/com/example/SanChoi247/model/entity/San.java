package com.example.SanChoi247.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class San {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int san_id;

    public San(LoaiSan loaiSan, String vi_tri_san, Size size, String img) {
        this.loaiSan = loaiSan;
        this.vi_tri_san = vi_tri_san;
        this.size = size;
        this.img = img;
    }

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;
    @ManyToOne
    @JoinColumn(name = "loai_san_id") // tên cột FK trong bảng San
    private LoaiSan loaiSan;
    @Column(name = "vi_tri_san")
    private String vi_tri_san;
    @ManyToOne
    @JoinColumn(name = "size_id") // tên cột FK trong bảng San
    private Size size;
    private String img;
    private int is_approve;// 0. pending 1. Approved 2. Hide 3. Rejected 4.Request PayMent 5. Resolved\
    private int eyeview;
    private double averageRating; // Thêm thuộc tính này

    public San(int san_id, User user, LoaiSan loaiSan, String vi_tri_san, Size size, String img, int is_approve,
            int eyeview) {
        this.san_id = san_id;
        this.user = user;
        this.loaiSan = loaiSan;
        this.vi_tri_san = vi_tri_san;
        this.size = size;
        this.img = img;
        this.is_approve = is_approve;
        this.eyeview = eyeview;
    }
    // Getter và Setter cho averageRating

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

}
