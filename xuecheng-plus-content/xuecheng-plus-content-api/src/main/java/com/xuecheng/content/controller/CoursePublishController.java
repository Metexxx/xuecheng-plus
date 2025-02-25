package com.xuecheng.content.controller;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class CoursePublishController {
    @Autowired
    CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }
    /**
     * 提交课程审核
     *
     * @param courseId 课程id
     */
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);

//        SecurityUtil.XcUser user = SecurityUtil.getUser();
//        Long companyId = null;
//        if (StringUtils.isNotEmpty(user.getCompanyId())) {
//            companyId = Long.parseLong(user.getCompanyId());
//        }
//        coursePublishService.commitAudit(companyId, courseId);
    }
}
