package com.xuecheng.search.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.search.dto.SearchCourseParamDto;
import com.xuecheng.search.dto.SearchPageResultDto;
import com.xuecheng.search.po.CourseIndex;

/**
 * @description 课程搜索service
 * @author Swith4Sumin
 * @date 2025/2/26 10:33
 * @version 1.0
 */
public interface CourseSearchService {

    /**
     * @description 搜索课程列表
     * @param pageParams 分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.search.po.CourseIndex> 课程列表
     * @author Swith4Sumin
     * @date 2025/2/26 10:33
    */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

 }
