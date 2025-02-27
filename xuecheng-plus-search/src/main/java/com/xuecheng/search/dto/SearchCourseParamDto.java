package com.xuecheng.search.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Swith4Sumin
 * @version 1.0
 * @description 搜索课程参数dtl
 * @date 2025/2/26 10:30
 */
@Data
@ToString
public class SearchCourseParamDto {

    //关键字
    private String keywords;

    //大分类
    private String mt;

    //小分类
    private String st;

    //难度等级
    private String grade;

}
