package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    /**
     * 根据分片参数获取待处理任务
     * @param shardTotal    分片总数
     * @param shardIndex    分片序号
     * @param count         任务数
     * @return
     */
    @Select("SELECT * FROM media_process WHERE id % #{shardTotal} = #{shardIndex} AND status = '1' LIMIT #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    @Update("update media_process m set m.status = '4' where (m.status = '1' or m.status = '3') and m.fail_count < 3 and m.id = #{id}")
    int startTask(@Param("id") long id);

}
