package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Need;
import com.example.traffic_management.service.NeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/needs")
public class NeedController {

    @Autowired
    private NeedService needService;

    // 根据ID查询需求
    @GetMapping("/{id}")
    public Need getById(@PathVariable Integer id) {
        return needService.findById(id);
    }

    // 查询所有未删除的需求
    @GetMapping
    public List<Need> list() {
        return needService.findAllActive();
    }

    // 新增需求
    @PostMapping
    public String add(@RequestBody Need need) {
        needService.save(need);
        return "需求添加成功，ID：" + need.getId();
    }

    // 软删除
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        needService.softDeleteById(id);
        return "需求删除成功";
    }
}