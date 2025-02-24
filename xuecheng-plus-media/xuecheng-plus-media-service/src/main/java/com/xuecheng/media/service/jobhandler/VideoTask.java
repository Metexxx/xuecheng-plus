package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegPath;

    @XxlJob("videoJobHandler0ld")
    public void videoJobHandler0ld() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        try {
            //取出cpu核心数作为一次处理数据的条数
            int processors = Runtime.getRuntime().availableProcessors();
            //一次处理视频数量不要超过cpu核心数
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条", size);
            if (size < 0) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //启动size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //将处理任务加入线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                try {
                    //任务id
                    Long taskId = mediaProcess.getId();
                    //抢占任务
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        return;
                    }
                    log.debug("开始执行任务:{}", mediaProcess);
                    //下边是处理逻辑
                    //桶
                    String bucket = mediaProcess.getBucket();
                    //存储路径
                    String filePath = mediaProcess.getFilePath();
                    //原始视频的md5值
                    String fileId = mediaProcess.getFileId();
                    //原始文件名称
                    String filename = mediaProcess.getFilename();
                    //将要处理的文件下载到服务器上
                    File originalFile = mediaFileService.downloadFileFromMinio(File.createTempFile("original", null), mediaProcess.getBucket(), mediaProcess.getFilePath());
                    if (originalFile == null) {
                        log.debug("下载待处理文件失败,originalFile:{}", mediaProcess.getBucket().concat(mediaProcess.getFilePath()));
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "下载待处理文件失败");
                        return;
                    }
                    //处理结束的视频文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("mp4", ".mp4");
                    } catch (IOException e) {
                        log.error("创建mp4临时文件失败");
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "创建mp4临时文件失败");
                        return;
                    }
                    //视频处理结果
                    String result = "";
                    try {
                        //开始处理视频
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                        //开始视频转换，成功将返回success
                        result = videoUtil.generateMp4();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                    }
                    if (!result.equals("success")) {
                        //记录错误信息
                        log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath, result);
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                        return;
                    }

                    //将mp4上传至minio
                    //mp4在minio的存储路径
                    String objectName = getFilePath(fileId, ".mp4");
                    //访问url
                    String url = "/" + bucket + "/" + objectName;
                    try {
                        mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), bucket, objectName);
                        //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, null);
                    } catch (Exception e) {
                        log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                        //最终还是失败了
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5,String fileExt){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws InterruptedException {
        // 分片序号
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("========== shardIndex:{}, shardTotal:{} =========", shardIndex, shardTotal);
        // 查询待处理任务，一次处理的任务数与cpu核心数相同
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 12);
        CountDownLatch countDownLatch = new CountDownLatch(mediaProcessList.size());
        // 未查询到待处理任务，结束方法
        if (mediaProcessList == null || mediaProcessList.size() == 0) {
            log.debug("查询到的待处理任务数为0");
            return;
        }
        // 要处理的任务数
        int size = mediaProcessList.size();
        // 查询到任务，创建size个线程去处理
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        mediaProcessList.forEach(mediaProcess -> threadPool.execute(() -> {
            String status = mediaProcess.getStatus();
            if ("2".equals(status)) {
                log.debug("该视频已经被处理，无需再次处理。视频信息：{}", mediaProcess);
                countDownLatch.countDown();
                return;
            }
            // 桶
            String bucket = mediaProcess.getBucket();
            // 文件路径
            String filePath = mediaProcess.getFilePath();
            // 原始文件的md5
            String fileId = mediaProcess.getFileId();
            File originalFile = null;
            File mp4File = null;
            try {
                // 将原始视频下载到本地，创建临时文件
                originalFile = File.createTempFile("original", null);
                // 处理完成后的文件
                mp4File = File.createTempFile("mp4", ".mp4");
            } catch (IOException e) {
                log.error("处理视频前创建临时文件失败");
                countDownLatch.countDown();
                XueChengPlusException.cast("处理视频前创建临时文件失败");
            }
            try {
                mediaFileService.downloadFileFromMinio(originalFile, bucket, filePath);
            } catch (Exception e) {
                log.error("下载原始文件过程中出错：{}，文件信息：{}", e.getMessage(), mediaProcess);
                countDownLatch.countDown();
                XueChengPlusException.cast("下载原始文件过程出错");
            }
            // 调用工具类将avi转为mp4
            String result = null;
            try {
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                result = videoUtil.generateMp4();
            } catch (Exception e) {
                log.error("处理视频失败，视频地址：{}，错误信息：{}", originalFile.getAbsolutePath(), e.getMessage());
                countDownLatch.countDown();
                XueChengPlusException.cast("处理视频失败");
            }
            // 转换成功，上传到MinIO
            status = "3";
            String url = null;
            if ("success".equals(result)) {
                // 根据文件md5，生成objectName
                String objectName = mediaFileService.getFilePathByMd5(fileId, ".mp4");
                try {
                    mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), bucket, objectName);
                } catch (Exception e) {
                    log.error("上传文件失败：{}", e.getMessage());
                    XueChengPlusException.cast("上传文件失败");
                }
                status = "2";    // 处理成功
                url = "/" + bucket + "/" + objectName;
            }
            // 记录任务处理结果url
            mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), status, fileId, url, result);
            countDownLatch.countDown();
        }));
        countDownLatch.await(30, TimeUnit.MINUTES);
    }
}

