package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Poi;
import com.example.traffic_management.service.PoiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pois")
public class PoiController {
    private final PoiService poiService;

    public PoiController(PoiService poiService) { this.poiService = poiService; }

    @GetMapping
    public List<Poi> list() { return poiService.findAllActive(); }

    @GetMapping("/{id}")
    public Poi getById(@PathVariable Integer id) { return poiService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Poi create(@Valid @RequestBody Poi poi) { return poiService.create(poi); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Integer id) { poiService.deactivate(id); }
}
