package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        if(courseId == null || courseId < 1) {
            throw new XueChengPlusException("请输入有效章节ID");
        }
        return courseTeacherMapper.selectList(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
    }

    @Override
    @Transactional
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getCourseId();
        if (id == null) {
            // id为空, 添加教师
            courseTeacher.setCreateDate(LocalDateTime.now());
            int flag = courseTeacherMapper.insert(courseTeacher);
            if (flag <= 0) {
                XueChengPlusException.cast("新增失败");
            }
            return getCourseTeacher(courseTeacher);
        } else {
            // id不为空, 编辑教师
            CourseTeacher teacher = getCourseTeacher(courseTeacher);
            BeanUtils.copyProperties(courseTeacher, teacher);
            int flag = courseTeacherMapper.updateById(teacher);
            if (flag <= 0) {
                XueChengPlusException.cast("修改失败");
            }
            return getCourseTeacher(teacher);
        }
    }
    public CourseTeacher getCourseTeacher(CourseTeacher courseTeacher) {
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Override
    @Transactional
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        int flag = courseTeacherMapper.delete(queryWrapper);
        if(flag < 0) {
            XueChengPlusException.cast("删除失败");
        }
    }
}
