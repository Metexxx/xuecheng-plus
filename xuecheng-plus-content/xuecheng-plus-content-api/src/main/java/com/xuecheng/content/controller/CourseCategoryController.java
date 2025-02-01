package com.xuecheng.content.controller;

import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseBaseInfoService;
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
    @ApiOperation(value = "课程分类查询", tags = "课程分类查询")
    @GetMapping("/course-category/tree-nodesFinal")
    public List<CourseCategoryTreeDto> queryTreeNodesFinal() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList = courseCategoryService.queryTreeNodesFinal();
        return courseCategoryTreeDtoList;
    }
    @ApiOperation(value = "课程分类查询", tags = "课程分类查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList = courseCategoryService.queryTreeNodes("1");
        return courseCategoryTreeDtoList;
    }
    @ApiOperation(value = "课程分类查询", tags = "课程分类查询")
    @GetMapping("/course-category/tree-nodesFull")
    public List<CourseCategoryTreeDto> queryTreeNodesFull() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList = courseCategoryService.queryTreeNodesFull();
        return courseCategoryTreeDtoList;
    }
}
