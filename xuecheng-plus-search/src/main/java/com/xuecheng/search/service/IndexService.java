package com.xuecheng.search.service;

import com.xuecheng.search.po.CourseIndex;

/**
 * @author Swith4Sumin
 * @version 1.0
 * @description 课程索引service
 * @date 2025/2/26 10:30
 */
public interface IndexService {

    /**
     * @param indexName 索引名称
     * @param id 主键
     * @param object 索引对象
     * @return Boolean true表示成功,false失败
     * @description 添加索引
     * @author Swith4Sumin
     * @date 2025/2/26 10:30
     */
    Boolean addCourseIndex(String indexName,String id,Object object);


    /**
     * @description 更新索引
     * @param indexName 索引名称
     * @param id 主键
     * @param object 索引对象
     * @return Boolean true表示成功,false失败
     * @author Swith4Sumin
     * @date 2025/2/26 10:30
    */
    Boolean updateCourseIndex(String indexName,String id,Object object);

    /**
     * @description 删除索引
     * @param indexName 索引名称
     * @param id  主键
     * @return java.lang.Boolean
     * @author Swith4Sumin
     * @date 2025/2/26 10:30
    */
    Boolean deleteCourseIndex(String indexName,String id);

}
