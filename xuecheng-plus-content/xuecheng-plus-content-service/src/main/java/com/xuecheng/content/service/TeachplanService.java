package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {
    List<TeachplanDto> findTeachplanTree(long courseId);
    
    void saveTeachplan(SaveTeachplanDto teachplanDto);
    
    void deleteTeachplan(Long id);

    void moveUp(Long id);

    void moveDown(Long id);

    /**
     * 教学计划绑定媒资信息
     * @param bindTeachplanMediaDto 绑定信息dto
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /** 解绑教学计划与媒资信息
     * @param teachPlanId       教学计划id
     * @param mediaId           媒资信息id
     */
    void unassociationMedia(Long teachPlanId, String mediaId);
}
