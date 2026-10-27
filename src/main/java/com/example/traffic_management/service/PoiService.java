package com.example.traffic_management.service;

import com.example.traffic_management.entity.Poi;
import java.util.List;

public interface PoiService {
    Poi findById(Integer id);
    List<Poi> findAllActive();
    Poi create(Poi poi);
    void deactivate(Integer id);
}
