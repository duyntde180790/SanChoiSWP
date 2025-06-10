package com.example.SanChoi247.model.entity;

import java.time.LocalDate;
import java.time.LocalTime;

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
public class ScheduleBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int booking_id;
    @ManyToOne
    @JoinColumn(name = "san_id") // FK liên kết với bảng San
    private San san;
    private LocalTime start_time; // Use LocalTime instead of LocalDateTime
    private LocalTime end_time; // Use LocalTime instead of LocalDateTime
    private String status; // 'booked', 'available'
    private float price;
    private LocalDate booking_date;

}
