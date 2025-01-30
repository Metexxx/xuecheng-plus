package com.xuecheng.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 课程信息编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {
    @ApiOperation("课程查询接口")
    @RequestMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParams){

        return null;
    }
}

