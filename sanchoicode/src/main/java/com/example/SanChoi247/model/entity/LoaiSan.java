package com.example.SanChoi247.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class LoaiSan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    // loai_san_id int auto_increment primary key,
    // loai_san_type varchar(50),
    private int loai_san_id;
    private String loai_san_type;
    public LoaiSan(int loai_san_id) {
        this.loai_san_id = loai_san_id;
    }
}
