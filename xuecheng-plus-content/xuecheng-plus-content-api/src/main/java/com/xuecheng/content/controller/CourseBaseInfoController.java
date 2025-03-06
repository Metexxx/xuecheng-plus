package com.xuecheng.content.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * @author Swith4Sumin
 * @version 1.0
 * @description 课程信息编辑接口
 * @date 2025/2/10 11:29
 */
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
@RequestMapping
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAnyAuthority('xc_teachmanager_course_list')")   // 拥有课程列表查询的权限才可访问
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
//        CourseBase courseBase = new CourseBase();
//        courseBase.setName("测试名称");
//        courseBase.setCreateDate(LocalDateTime.now());
//        List<CourseBase> courseBaseList = new ArrayList<>();
//        courseBaseList.add(courseBase);
//        return new PageResult<>(courseBaseList, 10, 1, 10);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        assert user != null;
        if (StringUtils.isNotEmpty(user.getCompanyId())) {
            companyId = Long.parseLong(user.getCompanyId());
        }
        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParams);
    }

    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
        //取出当前用户身份
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(principal);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("删除课程及关联信息")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        // todo 删除前校验权限
        courseBaseInfoService.deleteCourse(courseId);
    }
}

