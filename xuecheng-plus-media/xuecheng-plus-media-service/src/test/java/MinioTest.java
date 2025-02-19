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

    /**
     * 初始化并配置Minio客户端静态实例
     *
     * 使用建造者模式创建MinioClient实例，配置以下参数：
     * @implNote credentials - 设置MinIO服务认证凭证
     *           (参数1) accessKey: 用户名，默认开发环境凭证为"minioadmin"
     *           (参数2) secretKey: 密码，默认开发环境凭证为"minioadmin"
     * @implNote endpoint - 设置MinIO服务端点地址
     *           格式为协议://IP地址:端口，示例中使用的是本地开发环境地址9000端口
     *
     * 最终通过build()方法完成客户端实例的构建，该实例将用于后续对象存储操作
     */
    static MinioClient minioClient = MinioClient.builder()
            .credentials("minioadmin", "minioadmin")
            .endpoint("http://192.168.45.129:9000")
            .build();

    /**
     * minio测试文件上传功能
     *
     * 测试场景：
     * 1. 根据文件扩展名获取对应的mime类型
     * 2. 构建MinIO上传参数并执行文件上传
     * 3. 处理上传成功/失败的结果输出
     */
    @Test
    public void upload() {
        /* 根据.mp4扩展名获取对应的(mimeType)MIME类型，若未匹配则使用默认二进制流类型 */
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".png");
        // 通用mimeType:字节流; 先默认使用通用二进制流类型
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        try {
            /* 构建MinIO上传参数：存储桶名称/对象名称/本地文件路径/MIME类型 */
            UploadObjectArgs testBucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("1.png")
                    .filename("D:\\develop_tools\\upload\\1.png")
                    .contentType(mimeType)
                    .build();

            /* 执行MinIO客户端上传操作并输出结果 */
            minioClient.uploadObject(testBucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            /* 异常处理：打印堆栈轨迹并输出上传失败信息 */
            e.printStackTrace();
            System.out.println("上传失败");
            fail("对象上传失败: " + e.getMessage());
        }
    }



    /**
     * 删除MinIO存储桶中的指定对象
     *
     * 本函数用于从"testbucket"存储桶中永久删除名为"test001.mp4"的对象文件。
     * 操作结果将通过控制台输出提示信息，成功时打印"删除成功"，失败时打印异常堆栈信息及"删除失败"。
     *
     * @throws Exception 当连接MinIO服务异常或对象删除失败时抛出，包含具体错误信息
     */
    @Test
    public void delete() {
        try {
            // 构建删除请求并执行MinIO对象删除操作：存储桶名称/对象名称
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("testbucket")
                            .object("test001.mp4")
                            .build());

            System.out.println("删除成功");
        } catch (Exception e) {
            // 异常处理：打印异常堆栈并输出失败提示
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