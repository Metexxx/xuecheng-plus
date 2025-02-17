import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

public class MinioTest {
    static MinioClient minioClient = MinioClient.builder()
            .credentials("minioadmin", "minioadmin")
            .endpoint("http://192.168.45.129:9000")
            .build();

    // minio上传文件测试
    @Test
    public void upload() {
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }

        try {
            UploadObjectArgs testBucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test001.mp4")
                    .filename("D:\\develop_tools\\0.mp4")
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testBucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }
    @Test
    public void delete() {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("testbucket")
                            .object("test001.mp4")
                            .build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    // 测试文件下载功能
    @Test
    public void getFile() {
        String localFilePath = "D:\\develop_tools\\download\\test001.mp4";

        try {
            // 创建下载目录（如果不存在）
            new File(localFilePath).getParentFile().mkdirs();

            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test001.mp4")
                    .build();

            // 使用try-with-resources自动关闭流
            try (FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                 FileOutputStream outputStream = new FileOutputStream(localFilePath)) {

                IOUtils.copy(inputStream, outputStream);
                System.out.println("文件下载成功，路径: " + localFilePath);

                // 验证文件存在且非空
                File downloadedFile = new File(localFilePath);
                assertTrue(downloadedFile.exists(), "文件应存在");
                assertTrue(downloadedFile.length() > 0, "文件内容不应为空");
            }
        } catch (Exception e) {
            fail("文件下载失败: " + e.getMessage());
        }
    }
    @Test
    public void download() {
        String originalFilePath = "D:\\develop_tools\\0.mp4"; // 原始文件
        String downloadedFilePath = "D:\\develop_tools\\download\\test001.mp4"; // 下载文件

        try {
            // 先执行下载（确保文件存在）
            getFile(); // 可以直接调用或独立实现下载逻辑

            // 计算原始文件MD5
            String sourceMd5;
            try (InputStream originalStream = Files.newInputStream(new File(originalFilePath).toPath())) {
                sourceMd5 = DigestUtils.md5DigestAsHex(originalStream);
            }

            // 计算下载文件MD5
            String downloadedMd5;
            try (InputStream downloadedStream = Files.newInputStream(new File(downloadedFilePath).toPath())) {
                downloadedMd5 = DigestUtils.md5DigestAsHex(downloadedStream);
            }

            // 比较MD5值
            assertEquals(sourceMd5, downloadedMd5, "文件MD5校验失败");
            System.out.println("文件完整性校验成功，MD5: " + sourceMd5);
        } catch (Exception e) {
            fail("完整性校验失败: " + e.getMessage());
        } finally {
            // 清理测试文件（可选）
            new File(downloadedFilePath).delete();
        }
    }

    // 其他测试方法保持不变...

    @Test
    public void getFileOld() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("test001.mp4")
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            FileOutputStream outputStream = new FileOutputStream("D:\\develop_tools\\0.mp4");
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Test
    public void downloadOld() {
        try {
            // 校验文件的完整性，对文件计算出md5值，比较原始文件的md5和目标文件的md5，一致则说明完整
            FileInputStream fileInputStream = new FileInputStream("D:\\develop_tools\\upload\\0.mp4");
            String source_md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            FileInputStream fileInputStream1 = new FileInputStream("D:\\develop_tools\\upload\\1.mp4");
            String local_md5 = DigestUtils.md5DigestAsHex(fileInputStream1);
            if(source_md5.equals(local_md5))
                System.out.println("下载成功");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}