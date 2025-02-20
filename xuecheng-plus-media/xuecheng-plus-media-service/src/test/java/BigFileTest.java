import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BigFileTest {
    static MinioClient minioClient = MinioClient.builder()
            .credentials("minioadmin", "minioadmin")
            .endpoint("http://192.168.45.129:9000")
            .build();

    @Test
    public void testChunkKimi() throws IOException {
        // 源文件路径
        File sourceFile = new File("d:/develop_tools/upload/bigfile_test/lalala.mkv");
        // 分块文件存储路径
        String chunkPath = "d:/develop_tools/upload/bigfile_test/chunk/";
        File chunkFolder = new File(chunkPath);
        // 如果分块目录不存在，则创建
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        // 分块大小，设置为5MB
        long chunkSize = 1024 * 1024 * 5;
        // 计算分块数量，使用Math.ceil确保向上取整
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数：" + chunkNum);
        // 缓冲区大小，设置为1KB
        byte[] buffer = new byte[1024];
        // 使用RandomAccessFile读取源文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");

        // 遍历每个分块
        for (int i = 0; i < chunkNum; i++) {
            // 创建分块文件，文件名为分块编号
            File file = new File(chunkPath + i);
            // 如果分块文件已存在，则删除
            if (file.exists()) {
                file.delete();
            }
            // 创建分块文件
            boolean newFile = file.createNewFile();
            if (newFile) {
                // 使用RandomAccessFile写入分块文件
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                // 从源文件中读取数据并写入分块文件
                while ((len = raf_read.read(buffer)) != -1) {
                    raf_write.write(buffer, 0, len);
                    // 如果分块文件大小达到分块大小，则停止写入
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                raf_write.close(); // 关闭分块文件的RandomAccessFile
                System.out.println("完成分块" + i);

                // 检查是否为最后一块且小于5MB
                if (i == chunkNum - 1 && file.length() < chunkSize) {
                    // 合并到前一块
                    File prevFile = new File(chunkPath + (i - 1));
                    if (prevFile.exists()) {
                        RandomAccessFile raf_prev = new RandomAccessFile(prevFile, "rw");
                        raf_prev.seek(prevFile.length()); // 移动到前一块文件末尾
                        RandomAccessFile raf_last = new RandomAccessFile(file, "r");
                        byte[] lastBuffer = new byte[1024];
                        int lastLen;
                        while ((lastLen = raf_last.read(lastBuffer)) != -1) {
                            raf_prev.write(lastBuffer, 0, lastLen);
                        }
                        raf_last.close();
                        raf_prev.close();
                        file.delete(); // 删除最后一块文件
                        System.out.println("最后一块合并到前一块");
                    }
                }
            }
        }
        raf_read.close(); // 关闭源文件的RandomAccessFile
    }

    // 测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        // 源文件路径
        File sourceFile = new File("d:/develop_tools/upload/bigfile_test/lalala.mkv");
        // 分块文件存储路径
        String chunkPath = "d:/develop_tools/upload/bigfile_test/chunk/";
        File chunkFolder = new File(chunkPath);
        // 如果分块目录不存在，则创建
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        // 分块大小，这里设置为1MB = 1024Byte（=1KB） * 1024 = 1024KB = 1MB
        long chunkSize = 1024 * 1024 * 5;
        // 计算分块数量，使用Math.ceil确保向上取整
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数：" + chunkNum);
        // 缓冲区大小，这里设置为1KB
        byte[] buffer = new byte[1024];
        // 使用RandomAccessFile读取源文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");

        // 遍历每个分块
        for (int i = 0; i < chunkNum; i++) {
            // 创建分块文件，文件名为分块编号
            File file = new File(chunkPath + i);
            // 如果分块文件已存在，则删除
            if (file.exists()) {
                file.delete();
            }
            // 创建分块文件
            boolean newFile = file.createNewFile();
            if (newFile) {
                // 使用RandomAccessFile写入分块文件
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                // 从源文件中读取数据并写入分块文件
                while ((len = raf_read.read(buffer)) != -1) {
                    raf_write.write(buffer, 0, len);
                    // 如果分块文件大小达到分块大小，则停止写入
                    if (file.length() >= chunkSize) break;
                }
                raf_write.close(); // 关闭分块文件的RandomAccessFile
                System.out.println("完成分块" + i);
            }
        }
        raf_read.close(); // 关闭源文件的RandomAccessFile
    }


    // 测试文件合并
    @Test
    public void testMerge() throws IOException {
        // 块文件目录
        File chunkFolder = new File("d:/develop_tools/upload/bigfile_test/chunk/");
        // 原始文件
        File originalFile = new File("d:/develop_tools/upload/bigfile_test/lalala.mkv");
        // 合并文件
        File mergeFile = new File("d:/develop_tools/upload/bigfile_test/lalala_merge.mkv");
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        // 创建新的合并文件
        mergeFile.createNewFile();
        // 用于写文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        // 指针指向文件顶端
        raf_write.seek(0);
        // 缓冲区
        byte[] buffer = new byte[1024];
        // 分块列表
        File[] fileArray = chunkFolder.listFiles();
        // 转成集合，便于排序
        List<File> fileList = Arrays.asList(fileArray);
        // 按文件名升序排序
        fileList.sort( (f1, f2) -> Integer.parseInt(f1.getName()) - Integer.parseInt(f2.getName()));

        // 合并文件
        for (File chunkFile : fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len = raf_read.read(buffer)) != -1) {
                raf_write.write(buffer, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();

        // 校验文件
        try (FileInputStream fileInputStream = new FileInputStream(originalFile);
             FileInputStream mergeFileInputStream = new FileInputStream(mergeFile)) {
            // 取出原始文件的md5
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            String mergeMd5 = DigestUtils.md5Hex(mergeFileInputStream);
            System.out.println("originalMd5:" + originalMd5);
            System.out.println("mergeMd5:" + mergeMd5);
            if (originalMd5.equals(mergeMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }
        }
    }

    //将分块文件上传至minio
    @Test
    public void uploadChunk(){
        String chunkFolderPath = "d:/develop_tools/upload/bigfile_test/chunk/";
        File chunkFolder = new File(chunkFolderPath);
        //分块文件
        File[] files = chunkFolder.listFiles();
        //将分块文件上传至minio
        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .filename(files[i].getAbsolutePath())
                        .build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块成功" + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //合并文件，要求分块文件最小5M
    @Test
    public void test_merge() throws Exception {
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(12)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mkv")
                .sources(sources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }
    @Test
    public void test_merge_kimi() throws Exception {
        // 假设分块文件总数为12
        int totalChunks = 12;

        // 构建分块文件的列表
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(totalChunks) // 确保分块数量正确
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i))) // 分块文件路径
                        .build())
                .collect(Collectors.toList());

        // 构建合并参数
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mkv") // 合并后的文件名
                .sources(sources) // 引用所有分块文件
                .build();

        // 调用MinIO客户端的composeObject方法合并文件
        minioClient.composeObject(composeObjectArgs);

        System.out.println("文件合并完成！");
    }
    //清除分块文件
    @Test
    public void test_removeObjects(){
        //合并分块完成将分块文件清除
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(6)
                .map(i -> new DeleteObject("chunk/".concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("testbucket").objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }






    // 分块测试
    @Test
    public void testChunkNew() throws IOException {
        File sourceFile = new File("d:/develop_tools/upload/bigfile_test/lalala.mkv");
        String chunkPath = "d:/develop_tools/upload/bigfile_test/chunk/";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) chunkFolder.mkdirs();

        long fileSize = sourceFile.length();
        long minChunkSize = 5 * 1024 * 1024; // 5MB
        long chunkSize = minChunkSize;
        long chunkNum = (long) Math.ceil(fileSize * 1.0 / chunkSize);
        long lastChunkSize = fileSize % chunkSize;

        // 调整分块数及大小，确保每块≥5MB
        if (lastChunkSize > 0 && lastChunkSize < minChunkSize) {
            chunkNum -= 1;
            chunkSize = (long) Math.ceil(fileSize * 1.0 / chunkNum);
        }

        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        byte[] buffer = new byte[1024];
        System.out.println("分块总数：" + chunkNum + "，每块大小：" + chunkSize + "字节");

        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkPath + i);
            if (chunkFile.exists()) chunkFile.delete();
            chunkFile.createNewFile();

            RandomAccessFile raf_write = new RandomAccessFile(chunkFile, "rw");
            long bytesToWrite = (i == chunkNum - 1) ? fileSize - (chunkSize * i) : chunkSize;
            long bytesWritten = 0;

            while (bytesWritten < bytesToWrite) {
                int len = raf_read.read(buffer);
                if (len == -1) break;
                if (bytesWritten + len > bytesToWrite)
                    len = (int) (bytesToWrite - bytesWritten);
                raf_write.write(buffer, 0, len);
                bytesWritten += len;
            }
            raf_write.close();
            System.out.println("完成分块" + i);
        }
        raf_read.close();
    }
    @Test
    public void testMergeNew() throws IOException {
        File chunkFolder = new File("d:/develop_tools/upload/bigfile_test/chunk/");
        File mergeFile = new File("d:/develop_tools/upload/bigfile_test/lalala_merge.mkv");

        try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw")) {
            raf_write.setLength(0); // 原子化清空文件内容

            Arrays.stream(Objects.requireNonNull(chunkFolder.listFiles()))
                    .sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName())))
                    .forEach(chunk -> {
                        try (RandomAccessFile raf_read = new RandomAccessFile(chunk, "r")) {
                            // 动态获取当前分块实际尺寸
                            long remaining = raf_read.length();
                            FileChannel source = raf_read.getChannel();
                            FileChannel target = raf_write.getChannel();

                            // 使用NIO零拷贝传输优化大文件性能
                            long position = raf_write.getFilePointer();
                            while (remaining > 0) {
                                long transferred = source.transferTo(
                                        source.position(), remaining, target);
                                remaining -= transferred;
                            }
//                            raf_write.seek(position + raf_read.length());

                            // 分块合规性断言
                            if (raf_read.length() < 5 * 1024 * 1024) {
                                throw new IllegalStateException("分块" + chunk.getName()
                                        + "尺寸违规: " + raf_read.length() + "字节");
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

        }
        // MD5校验逻辑保持不变
        // 校验文件
        // 原始文件
        File originalFile = new File("d:/develop_tools/upload/bigfile_test/lalala.mkv");
        if (originalFile.length() != mergeFile.length()) {
            throw new IllegalStateException("文件长度不一致");
        }
        try (FileInputStream fileInputStream = new FileInputStream(originalFile);
             FileInputStream mergeFileInputStream = new FileInputStream(mergeFile)) {
            // 取出原始文件的md5
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            String mergeMd5 = DigestUtils.md5Hex(mergeFileInputStream);
            System.out.println("originalMd5:" + originalMd5);
            System.out.println("mergeMd5:" + mergeMd5);
            if (originalMd5.equals(mergeMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }
        }
    }
}
