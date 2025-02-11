package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
    /**
     * @description 增加课程计划
     * @param teachplanDto  课程计划信息
     * @return void
     * @author Mr.M
     * @date 2022/9/9 13:39
     */
    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //课程计划id
        Long id = teachplanDto.getId();
        //修改课程计划
        if(id != null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count + 1);
            BeanUtils.copyProperties(teachplanDto, teachplanNew);

            teachplanMapper.insert(teachplanNew);
        }
    }
    /**
     * @description 获取最新的排序号
     * @param courseId  课程id
     * @param parentId  父课程计划id
     * @return int 最新排序号
     * @author Mr.M
     * @date 2022/9/9 13:43
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }

    /**
     * 删除章、节
     * @param id
     */
    @Override
    @Transactional
    public void deleteTeachplan(Long id) {
        // 1. 参数校验
        judgeId(id);
        // 2. 对象存在性校验
        Teachplan teachplan = teachplanMapper.selectById(id);
        judgeTeachplanObject(teachplan);
        // 3. 子节点检查逻辑
        boolean isRootNode = judgeRootNode(teachplan.getParentid(), teachplan.getGrade());
        if(isRootNode && !teachplanMapper.selectChildNodes(id).isEmpty()) {
            throw new XueChengPlusException("请先删除所有子章节");
        }
        teachplanMapper.deleteById(id);
        if(isRootNode){
//            QueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new QueryWrapper<>();
//            teachplanMediaQueryWrapper
//                    .eq("teachplan_id", id);
            teachplanMediaMapper.delete(
                    new QueryWrapper<TeachplanMedia>()
                    .eq("teachplan_id", id));
//            Map<String, Object> map = new HashMap<>();
//            map.put("teachplan_id", teachplanId);
//            teachplanMediaMapper.deleteByMap(map);
        }
    }
    private void judgeId(Long id) {
        if(id == null || id <= 0){
            throw new XueChengPlusException("请输入有效章节ID");
        }
    }
    private void judgeTeachplanObject(Teachplan teachplan) {
        if(teachplan == null) {
            throw new XueChengPlusException("章节不存在或已被删除");
        }
    }
    private boolean judgeRootNode(Long parentId, Integer grade) {
        return (parentId == 0 && grade == 1);
    }

    @Override
    @Transactional
    public void moveUp(Long id) {
        // 1. 参数校验
        judgeId(id);
        // 2. 对象存在性校验
        Teachplan current = teachplanMapper.selectById(id);
        judgeTeachplanObject(current);
        // 3. 边界检查
        if (current.getOrderby() == 1) {
            throw new XueChengPlusException("已是首位不可上移");
        }
        // 4. 构建查询条件
        boolean isRootNode = judgeRootNode(current.getParentid(), current.getGrade());
        QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentid", isRootNode? 0 : current.getParentid())
                    .eq("grade", isRootNode? 1 : 2)
                    .eq("orderby", current.getOrderby() - 1)
                    .eq("course_id", current.getCourseId());     // 补充课程ID条件 限制查询范围在同一课程内

        // 5. 获取相邻节点 , 空指针防护优化
        log.debug("查询条件: {}", queryWrapper.getTargetSql());
        Teachplan previous  = Optional.ofNullable(teachplanMapper.selectOne(queryWrapper))
                .orElseThrow(() -> new XueChengPlusException("已达排序边界"));
        // 6. 交换排序序号
        int currentNewOrder = current.getOrderby() - 1;
        int previousNewOrder = previous.getOrderby() + 1;
        current.setOrderby(currentNewOrder);
        previous.setOrderby(previousNewOrder);
        // 7. 批量更新
        teachplanMapper.updateBatchById(Arrays.asList(current, previous));
    }

    @Override
    @Transactional
    public void moveDown(Long id) {
        // 1. 参数校验
        judgeId(id);
        // 2. 对象存在性校验
        Teachplan current = teachplanMapper.selectById(id);
        judgeTeachplanObject(current);
        // 3. 构建查询条件
        boolean isRootNode = judgeRootNode(current.getParentid(), current.getGrade());
        QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentid", isRootNode ? 0 : current.getParentid())
                    .eq("grade", isRootNode ? 1 : 2)
                    .eq("orderby", current.getOrderby() + 1) // 查找orderby+1的下个节点
                    .eq("course_id", current.getCourseId());     // 补充课程ID条件 限制查询范围在同一课程内

        // 4. 获取相邻节点
        log.debug("查询条件: {}", queryWrapper.getTargetSql());
        Teachplan next = Optional.ofNullable(teachplanMapper.selectOne(queryWrapper))
                .orElseThrow(() -> new XueChengPlusException("已是末尾不可下移"));

        // 5. 交换排序序号
        int currentNewOrder = current.getOrderby() + 1;
        int nextNewOrder = next.getOrderby() - 1;
        current.setOrderby(currentNewOrder);
        next.setOrderby(nextNewOrder);

        // 6. 批量更新
        teachplanMapper.updateBatchById(Arrays.asList(current, next));
    }
}
