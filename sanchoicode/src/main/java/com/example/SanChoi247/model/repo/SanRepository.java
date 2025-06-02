package com.example.SanChoi247.model.repo;

import java.util.List;

public class SanRepository {
    List<San> findByUser_Uid(int uid);
    San findByTenSan(String tenSan);    
}
