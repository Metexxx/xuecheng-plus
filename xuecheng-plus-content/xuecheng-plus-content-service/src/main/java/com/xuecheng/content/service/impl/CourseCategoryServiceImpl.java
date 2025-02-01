package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodesFinal() {
        // 1. 一次性查询所有节点
        List<CourseCategory> allNodes = courseCategoryMapper.selectAllNodes();
        if (CollectionUtils.isEmpty(allNodes)) {
            return Collections.emptyList();
        }

        // 2. 转换为DTO对象并构建Id到DTO的映射表
        Map<String, CourseCategoryTreeDto> nodeMap = new HashMap<>();
        List<CourseCategoryTreeDto> allDtos = allNodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        allDtos.forEach(dot -> nodeMap.put(dot.getId(), dot));

        // 3. 构建树形结构
        List<CourseCategoryTreeDto> roots = new ArrayList<>();
        for (CourseCategoryTreeDto dto : allDtos) {
            String parentId = dto.getParentid();
            if (isRootNode(parentId)) {
                roots.add(dto);
            } else {
                CourseCategoryTreeDto parentDto = nodeMap.get(parentId);
                if (parentDto != null) {
                    parentDto.getChildrenTreeNodes().add(dto);
                } else {
                    // 处理孤节点：根据业务逻辑决定是否加入根列表或忽略
                    log.warn("发现孤立节点：{}，其父节点{}不存在", dto.getId(), parentId);
                }
            }
        }
        return roots;
    }
    // 判断是否为根节点（根据实际业务规则调整）
    private boolean isRootNode(String parentId) {
        return "1".equals(parentId); // 假设根节点的parentid为1
    }
    /**
     * 森林
     * @return
     */
    @Autowired
    public List<CourseCategoryTreeDto> queryTreeNodesFull() {
        // 1. 查询所有根节点（根据实际业务规则定义根节点条件）
        List<CourseCategory> rootNodes = courseCategoryMapper.selectRootNodes();
        if (CollectionUtils.isEmpty(rootNodes)) {
           return Collections.emptyList();
        }

        // 2. 为每个根节点构建树结构
        return rootNodes.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }
    /**
     * 一棵树
     * @return
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        // 获取根节点（）
        CourseCategory root = courseCategoryMapper.selectById("1");
        if(root == null) {
            return Collections.emptyList();
        }
        // 递归构建树结构，并返回包含根节点的列表
        CourseCategoryTreeDto rootTree = buildTree(root);
        return Collections.singletonList(rootTree);
    }
    private CourseCategoryTreeDto buildTree(CourseCategory node) {
        // 转换为DTO对象
        CourseCategoryTreeDto dto = convertToDTO(node);
        // 若当前节点不是叶子节点，递归处理子节点
        if (node.getIsLeaf() != 1) { // 假设1表示叶子节点
            // 查询以当前节点为父节点的子节点 （parentId = 当前节点id)
            List<CourseCategory> children = courseCategoryMapper.selectChildNodeList(node.getId());
            // 递归构建子树并收集子节点DTO
            List<CourseCategoryTreeDto> childDtos = children.stream()
                    .map(this::buildTree)
                    .collect(Collectors.toList());

            dto.setChildrenTreeNodes(childDtos);
        }
        return dto;
    }
    private CourseCategoryTreeDto convertToDTO(CourseCategory courseCategory) {
        CourseCategoryTreeDto dto = new CourseCategoryTreeDto();
        BeanUtils.copyProperties(courseCategory, dto);
        dto.setChildrenTreeNodes(new ArrayList<>());    // 初始化子节点列表
        return dto;
    }

    /**
     * 方法重载：教程的方法
     * @param id
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //将list转map,以备使用,排除根节点
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //最终返回的list
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        //依次遍历每个元素,排除根节点
        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item->{
            if(item.getParentid().equals(id)){
                categoryTreeDtos.add(item);
            }
            //找到当前节点的父节点
            CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
            if(courseCategoryTreeDto!=null){
                if(courseCategoryTreeDto.getChildrenTreeNodes() ==null){
                    courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //下边开始往ChildrenTreeNodes属性中放子节点
                courseCategoryTreeDto.getChildrenTreeNodes().add(item);
            }
        });
        return categoryTreeDtos;
    }

}
