package com.xuecheng.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SampleJob {
    private static final String[] DATA_ARRAY = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    @XxlJob("shardingJobHandlerArray")
    public void shardingJobHandlerArray() {
        // 获取分片参数（当前节点分片序号、总分片数）
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        // 根据分片参数计算处理范围
        int totalItems = DATA_ARRAY.length;
        int itemsPerShard = (int) Math.ceil((double) totalItems / shardTotal);
        int startIndex = shardIndex * itemsPerShard;
        int endIndex = Math.min(startIndex + itemsPerShard, totalItems);

        // 执行当前分片任务
        log.info("分片[{}]处理数据范围: {}-{}", shardIndex, startIndex, endIndex-1);
        for (int i = startIndex; i < endIndex; i++) {
            String item = DATA_ARRAY[i];
            log.info("分片[{}]处理数据: {}", shardIndex, item);
        }
    }
    /**
     * 2025-02-21 16:43:40,066 INFO [xxl-job, EmbedServer bizThreadPool-625532085][XxlJobExecutor.java:246] - >>>>>>>>>>> xxl-job regist JobThread success, jobId:4, handler:com.xxl.job.core.handler.impl.MethodJobHandler@4e50ae56[class com.xuecheng.media.service.jobhandler.SampleJob#shardingJobHandlerArray]
     * 2025-02-21 16:43:40,068 INFO [xxl-job, JobThread-4-1740127420066][SampleJob.java:26] - 分片[0]处理数据范围: 0-4
     * 2025-02-21 16:43:40,068 INFO [xxl-job, JobThread-4-1740127420066][SampleJob.java:29] - 分片[0]处理数据: A
     * 2025-02-21 16:43:40,068 INFO [xxl-job, JobThread-4-1740127420066][SampleJob.java:29] - 分片[0]处理数据: B
     * 2025-02-21 16:43:40,068 INFO [xxl-job, JobThread-4-1740127420066][SampleJob.java:29] - 分片[0]处理数据: C
     * 2025-02-21 16:43:40,068 INFO [xxl-job, JobThread-4-1740127420066][SampleJob.java:29] - 分片[0]处理数据: D
     * 2025-02-21 16:43:40,068 INFO [xxl-job, JobThread-4-1740127420066][SampleJob.java:29] - 分片[0]处理数据: E
     * 2025-02-21 16:43:40,135 INFO [xxl-job, EmbedServer bizThreadPool-898671655][XxlJobExecutor.java:246] - >>>>>>>>>>> xxl-job regist JobThread success, jobId:4, handler:com.xxl.job.core.handler.impl.MethodJobHandler@4e50ae56[class com.xuecheng.media.service.jobhandler.SampleJob#shardingJobHandlerArray]
     * 2025-02-21 16:43:40,138 INFO [xxl-job, JobThread-4-1740127420135][SampleJob.java:26] - 分片[1]处理数据范围: 5-9
     * 2025-02-21 16:43:40,138 INFO [xxl-job, JobThread-4-1740127420135][SampleJob.java:29] - 分片[1]处理数据: F
     * 2025-02-21 16:43:40,138 INFO [xxl-job, JobThread-4-1740127420135][SampleJob.java:29] - 分片[1]处理数据: G
     * 2025-02-21 16:43:40,138 INFO [xxl-job, JobThread-4-1740127420135][SampleJob.java:29] - 分片[1]处理数据: H
     * 2025-02-21 16:43:40,138 INFO [xxl-job, JobThread-4-1740127420135][SampleJob.java:29] - 分片[1]处理数据: I
     * 2025-02-21 16:43:40,138 INFO [xxl-job, JobThread-4-1740127420135][SampleJob.java:29] - 分片[1]处理数据: J
     */

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("testJob")
    public void testJob() {
        log.info("testJob开始执行...");
    }

    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        log.info("开始执行第"+shardIndex+"批任务");

    }

}
