package com.xuecheng.search.dto;

import com.xuecheng.base.model.PageResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author Swith4Sumin
 * @version 1.0
 * @description TODO
 * @date 2025/2/26 10:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class SearchPageResultDto<T> extends PageResult<T> {

    //大分类列表
    List<String> mtList;
    //小分类列表
    List<String> stList;

    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }
}
