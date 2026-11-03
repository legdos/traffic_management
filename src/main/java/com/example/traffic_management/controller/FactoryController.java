package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Factory;
import com.example.traffic_management.service.FactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/factories")
public class FactoryController {

    @Autowired
    private FactoryService factoryService;

    // 根据ID查询工厂
    @GetMapping("/{id}")
    public Factory getById(@PathVariable Integer id) {
        return factoryService.findById(id);
    }

    // 查询所有工厂
    @GetMapping
    public List<Factory> list() {
        return factoryService.findAll();
    }

    // 新增工厂
    @PostMapping
    public String add(@RequestBody Factory factory) {
        factoryService.save(factory);
        return "工厂添加成功，ID：" + factory.getId();
    }

    // 更新工厂
    @PutMapping
    public String update(@RequestBody Factory factory) {
        factoryService.update(factory);
        return "工厂更新成功";
    }

    // 删除工厂（物理删除）
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        factoryService.deleteById(id);
        return "工厂删除成功";
    }

    // 生成需求并回写need_id
    @PostMapping("/{id}/generate-need")
    public String generateNeed(@PathVariable Integer id, @RequestParam String material) {
        Integer needId = factoryService.generateNeed(id, material);
        return "需求[" + material + "]生成成功，需求ID：" + needId;
    }

    // 批量导入工厂（前端用高德 POI 检索得到真实坐标后提交），同名同坐标自动跳过
    @PostMapping("/import")
    public String importFactories(@RequestBody List<Factory> factories) {
        int added = factoryService.importFactories(factories);
        return "导入完成，新增 " + added + " 家工厂";
    }
}