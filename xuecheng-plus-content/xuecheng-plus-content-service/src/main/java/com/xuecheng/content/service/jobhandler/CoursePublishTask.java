package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;

    // 任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    //课程发布任务处理
    @Override
    public boolean execute(MqMessage mqMessage) {
        //  //获取消息相关的业务信息 修正后代码（提取纯数字部分）
        String businessKey1 = mqMessage.getBusinessKey1();
        if (businessKey1 != null && businessKey1.startsWith("course_")) {
            String numberPart = businessKey1.substring(7); // 截取"course_"之后的部分
            int courseId = Integer.parseInt(numberPart);
            //课程静态化
            generateCourseHtml(mqMessage, courseId);
            //课程索引
            saveCourseIndex(mqMessage, courseId);
            //课程缓存
            saveCourseCache(mqMessage, courseId);
        } else {
            // 处理非法格式
            XueChengPlusException.cast("非法的课程ID格式：" + businessKey1);
        }
//        long courseId = Integer.parseInt(businessKey1);
//        //课程静态化
//        generateCourseHtml(mqMessage, courseId);
//        //课程索引
//        saveCourseIndex(mqMessage, courseId);
//        //课程缓存
//        saveCourseCache(mqMessage, courseId);
        return true;
    }

    //生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.debug("开始课程静态化，课程id：{}", courseId);
        // 1. 幂等性判断
        // 1.1 获取消息id
        Long id = mqMessage.getId();
        // 1.2 获取小任务阶段状态
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        // 1.3 判断小任务阶段是否完成
        if (stageOne == 1) {
            log.debug("当前阶段为静态化课程信息任务，已完成，无需再次处理，任务信息：{}", mqMessage);
            return;
        }
        // 2. 生成静态页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast("课程静态化异常");
        }
        // 3. 将静态页面上传至MinIO
        coursePublishService.uploadCourseHtml(courseId, file);
        // 4. 保存第一阶段状态
        mqMessageService.completedStageOne(id);
    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("正在保存课程信息索引，课程id:{}", courseId);
        // 1. 获取消息id
        Long id = mqMessage.getId();
        // 2. 获取小任务阶段状态
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(id);
        // 3. 当前小任务完成，无需再次处理
        if (stageTwo == 1) {
            log.debug("当前阶段为创建课程索引任务，已完成，无需再次处理，任务信息：{}", mqMessage);
            return;
        }
        // 4. 远程调用保存课程索引接口，将课程信息上传至ElasticSearch
        Boolean result = coursePublishService.saveCourseIndex(courseId);
        if (result) {
            mqMessageService.completedStageTwo(id);
        }
    }
}

