package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Poi;
import com.example.traffic_management.mapper.PoiMapper;
import com.example.traffic_management.service.PoiService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PoiServiceImpl implements PoiService {
    private final PoiMapper poiMapper;

    public PoiServiceImpl(PoiMapper poiMapper) { this.poiMapper = poiMapper; }

    @Override
    public Poi findById(Integer id) {
        Poi poi = poiMapper.selectActiveById(id);
        if (poi == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "POI 不存在或已失效");
        return poi;
    }

    @Override
    public List<Poi> findAllActive() { return poiMapper.selectAllActive(); }

    @Override
    @Transactional
    public Poi create(Poi poi) {
        if (poi == null || poi.getPoiName() == null || poi.getPoiName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "poiName 不能为空");
        }
        if (poi.getPoiLatitude() == null || poi.getPoiLongitude() == null
                || !Double.isFinite(poi.getPoiLatitude()) || !Double.isFinite(poi.getPoiLongitude())
                || poi.getPoiLatitude() < -90 || poi.getPoiLatitude() > 90
                || poi.getPoiLongitude() < -180 || poi.getPoiLongitude() > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "经纬度超出有效范围");
        }
        poiMapper.insert(poi);
        return findById(poi.getId());
    }

    @Override
    @Transactional
    public void deactivate(Integer id) {
        if (poiMapper.selectActiveById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "POI 不存在或已失效");
        if (poiMapper.countRouteReferences(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该 POI 已被路线引用，不能失效；请先调整路线关系");
        }
        if (poiMapper.softDeleteById(id) != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "POI 状态已变化，请刷新后重试");
    }
}
