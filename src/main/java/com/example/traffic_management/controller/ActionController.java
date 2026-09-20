package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Action;
import com.example.traffic_management.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/actions")
public class ActionController {

    @Autowired
    private ActionService actionService;

    @GetMapping
    public List<Action> list() {
        return actionService.selectAll();
    }

    @GetMapping("/{id}")
    public Action getById(@PathVariable Integer id) {
        return actionService.selectById(id);
    }

    @PostMapping
    public String add(@RequestBody Action action) {
        actionService.insert(action);
        return "新增成功";
    }

    @PutMapping
    public String update(@RequestBody Action action) {
        actionService.update(action);
        return "修改成功";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        actionService.deleteById(id);
        return "删除成功";
    }
}