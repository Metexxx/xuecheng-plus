package com.xuecheng.content.controller;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(value = "课程分类控制器")
@RestController
public class CourseCategoryController {
    @Autowired
    private CourseCategoryService courseCategoryService;

    /**
     * 教程使用的方法：在SQL里面递归查询
     * @return
     */
    @ApiOperation(value = "课程分类查询", tags = "课程分类查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }
    @ApiOperation(value = "课程分类查询", tags = "课程分类查询")
    @GetMapping("/course-category/tree-nodesFinal")
    public List<CourseCategoryTreeDto> queryTreeNodesFinal() {
        return  courseCategoryService.queryTreeNodesFinal();
    }
    @ApiOperation(value = "课程分类查询", tags = "课程分类查询")
    @GetMapping("/course-category/tree-nodesFull")
    public List<CourseCategoryTreeDto> queryTreeNodesFull() {
        return courseCategoryService.queryTreeNodesFull();
    }

}
