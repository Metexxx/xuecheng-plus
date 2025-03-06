package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;

public interface MyCourseTablesService {

    /**
     * @description 添加选课
     * @param userId 用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    XcChooseCourse addCourse(String userId, CoursePublish coursePublish, String CourseType, String status);

    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    PageResult<XcCourseTables> myCourseTables(MyCourseTableParams params);

    boolean saveChooseCourseStatus(String chooseCourseId);
}
