package com.xuecheng.messagesdk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * @author Swith4Sumin
 * @version 1.0
 * @description test
 * @date 2025/2/25 14:15
 */
@SpringBootTest
public class MessageProcessClassTest {

    @Autowired
    MessageProcessClass messageProcessClass;

    @Test
    public void test() throws InterruptedException {
        System.err.println("开始执行-----》" + LocalDateTime.now());
        messageProcessClass.process(0, 1, "media_test", 5, 30);
        System.err.println("结束执行-----》" + LocalDateTime.now());
        Thread.sleep(9000000);
    }
}

