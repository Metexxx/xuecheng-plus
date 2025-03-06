package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

public interface CoursePublishService {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param courseId  课程id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * @description 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     */
    void publish(Long companyId,Long courseId);

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    File generateCourseHtml(Long courseId);

    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     */
    void uploadCourseHtml(Long courseId,File file);

    /**
     * 保存课程索引
     * @param courseId  课程id
     */
    Boolean saveCourseIndex(Long courseId);

    /**
     * 查询缓存中的课程发布信息
     * @param courseId  课程id
     * @return  课程发布信息
     */
    CoursePublish getCoursePublishCache(Long courseId);
}
