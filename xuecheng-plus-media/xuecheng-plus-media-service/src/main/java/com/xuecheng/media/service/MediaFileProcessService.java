package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {
    /**
     * @description 获取待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return 待处理任务集合
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);
    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

    void saveProcessFinishStatusNew(Long taskId, String status, String fileId, String url, String errorMsg);

    boolean startTask(long id);
}
