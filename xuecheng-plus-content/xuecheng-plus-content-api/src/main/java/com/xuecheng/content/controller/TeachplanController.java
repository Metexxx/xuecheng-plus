package com.xuecheng.content.controller;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 课程计划编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {
    @Autowired
    private TeachplanService teachplanService;
    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程基础Id值",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }
    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }
    @ApiOperation("删除课程计划的章节")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan( @PathVariable Long id){
        teachplanService.deleteTeachplan(id);
    }
    @ApiOperation("章节上移")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveUp( @PathVariable Long id){
        teachplanService.moveUp(id);
    }
    @ApiOperation("章节下移")
    @PostMapping("/teachplan/movedown/{id}")
    public void moveDown( @PathVariable Long id){
        teachplanService.moveDown(id);
    }
}
