package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {
    List<CourseCategoryTreeDto> queryTreeNodesFinal();
    List<CourseCategoryTreeDto> queryTreeNodesFull();
    List<CourseCategoryTreeDto> queryTreeNodes();
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
