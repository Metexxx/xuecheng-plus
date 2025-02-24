package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.errorprone.annotations.Var;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {
    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket_Files;

    @Value("${minio.bucket.videofiles}")
    private String video_files;

    /**
     *  获取文件默认存储目录路径， 目录名：年/月/日
     *
    */
    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folder = simpleDateFormat.format(new Date()).replace("-","/") + "/";
        return folder;
    }

    /**
     * 获取文件的 md5
     * @param file 文件
     * @return md5值
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 根据MD5和文件扩展名，生成文件路径.
     * 例 fileMd5 =2f6451sdg;  fileMd5.charAt(0) = 2, fileMd5.charAt(1) = f
     * fileMd5 + extension = 2f6451sdg.mp4
     * 拼接路径: /2/f/2f6451sdg/2f6451sdg.mp4
     * @param fileMd5   文件MD5
     * @param extension 文件扩展名
     * @return string 文件路径
     */
    public String getFilePathByMd5(String fileMd5, String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }

    // 得到分块文件的目录：在文件的同级目录的chunk目录下，分块名为编号chunkIndex
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 获取文件的 mimeType
     * @param extension 扩展名
     * @return mimeType
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        // 通用mimeType字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 根据objectName获取对应的MimeType
     * @param objectName 对象名称
     * @return contentType 内容类型
     */
    private static String getContentType(String objectName) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认content-type为未知二进制流
        if (objectName.contains(".")) { // 判断对象名是否包含 .
            // 有"." 则划分出扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            // 根据扩展名得到content-type，如果为未知扩展名，例如 .abc之类的东西，则会返回null
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            // 如果得到了正常的content-type，则重新赋值，覆盖默认类型
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }

    /**
     * @description         将文件写入minIO
     * @param localFilePath 文件地址
     * @param mimeType      mime类型
     * @param bucket        桶
     * @param objectName    对象名称
     * @return  boolean
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build());
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            System.out.println("上传成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio失败,bucket:{},objectName:{},失败原因:{}",bucket,objectName,e.getMessage());
            System.out.println("上传失败");
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
    }
    /**
     * 将本地文件上传到minio-重载方法
     * @param filePath   本地文件路径
     * @param bucket     桶
     * @param objectName 对象名称
     */
    public void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
        String contentType = getContentType(objectName);
        try {
            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filePath)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            XueChengPlusException.cast("上传到文件系统出错:" + e.getMessage());
        }
    }
    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return mediaFiles文件信息
     */
    @Override
    @Transactional
    public MediaFiles addMediaFilesToDB(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        // 根据文件名获取Content-Type
        String contentType = getContentType(objectName);
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            // 拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            // 保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败, {}", mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功, {}", mediaFiles.toString());
        }
        // 如果是avi视频，则额外添加至视频待处理表
        if ("video/x-msvideo".equals(contentType)) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1"); // 未处理
            int processInsert = mediaProcessMapper.insert(mediaProcess);
            if (processInsert <= 0) {
                XueChengPlusException.cast("保存avi视频到待处理表失败");
            }
        }
        return mediaFiles;
    }
    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件信息
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        // 文件名称,文件扩展名,文件mimeType,文件md5值,文件的默认目录,存储到minio中的对象名（带目录）
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        String fileMd5 = getFileMd5(file);
        String defaultFolderPath = getDefaultFolderPath();
        String objectName = defaultFolderPath + fileMd5 + extension;

        // 将文件上传到minio
        boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_Files, objectName);
        if (!b) {
            XueChengPlusException.cast("上传文件失败");
        }
        // 设置文件大小
        uploadFileParamsDto.setFileSize(file.length());
        // 将文件信息存储到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);
        // 准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }
    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!org.springframework.util.StringUtils.isEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        queryWrapper.eq(!StringUtils.isEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集返回
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 当数据库和minio中文件都存在，则返回true
     * @param fileMd5 文件的md5
     * @return 封装的boolean
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //查询文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        // 数据库中不存在，则直接返回false 表示不存在
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }
        // 若数据库中存在，根据数据库中的文件信息，则继续判断bucket中是否存在
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(mediaFiles.getBucket())
                            .object(mediaFiles.getFilePath())
                            .build());
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(true);
    }

    /**
     * 检查分块文件是否存在-避免重复上传文件块: 分块文件只临时存储在minio中
     * @param fileMd5    文件MD5
     * @param chunkIndex 分块序号
     * @return RestResponse<Boolean>
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        //文件流
        InputStream fileInputStream = null;
        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(video_files)
                            .object(chunkFilePath)
                            .build());

            if (fileInputStream != null) {
                //分块已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //分块未存在
        return RestResponse.success(false);
    }

    /**
     * 上传分块
     * @param fileMd5 文件MD5
     * @param chunk   分块序号
     * @param bytes   文件字节数组
     * @return RestResponse
     */
    @Override
    public RestResponse<?> uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        // 分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        try {
            addMediaFilesToMinIO(bytes, video_files, chunkFilePath);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.debug("上传分块文件：{}失败：{}", chunkFilePath, e.getMessage());
        }
        return RestResponse.validfail(false,"上传分块失败");
    }

    /**
     * 本类方法(非接口实现)：将文件字节数组上传到minio中
     * @param bytes      文件字节数组-用于传输分块
     * @param bucket     桶
     * @param objectName 对象名称objectName.extension
     */
    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        String contentType = getContentType(objectName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.debug("上传到文件系统出错:{}", e.getMessage());
            throw new XueChengPlusException("上传到文件系统出错");
        }
    }

    @Override
    public RestResponse<?> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 下载分块文件
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
        // 获取源文件名
        String fileName = uploadFileParamsDto.getFilename();
        // 获取源文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        // 创建出临时文件，准备合并
        File mergeFile = null;
        try {
            mergeFile = File.createTempFile(fileName, extension);
        } catch (IOException e) {
            XueChengPlusException.cast("创建合并临时文件出错");
        }
        try {
            // 缓冲区
            byte[] buffer = new byte[1024];
            // 写入流，向临时文件写入
            try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw")) {
                // 遍历分块文件数组
                for (File chunkFile : chunkFiles) {
                    // 读取流，读分块文件
                    try (RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r")) {
                        int len;
                        while ((len = raf_read.read(buffer)) != -1) {
                            raf_write.write(buffer, 0, len);
                        }
                    }
                }
            } catch (Exception e) {
                XueChengPlusException.cast("合并文件过程中出错");
            }
            uploadFileParamsDto.setFileSize(mergeFile.length());
            // 对文件进行校验，通过MD5值比较
            try (FileInputStream mergeInputStream = new FileInputStream(mergeFile)) {
                String mergeMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(mergeInputStream);
                if (!fileMd5.equals(mergeMd5)) {
                    XueChengPlusException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过：{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                XueChengPlusException.cast("合并文件校验异常");
            }
            String mergeFilePath = getFilePathByMd5(fileMd5, extension);
            // 将本地合并好的文件，上传到minio中，这里重载了一个方法
            addMediaFilesToMinIO(mergeFile.getAbsolutePath(), video_files, mergeFilePath);
            log.debug("合并文件上传至MinIO完成{}", mergeFile.getAbsolutePath());
            // 将文件信息写入数据库
            MediaFiles mediaFiles = addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, video_files, mergeFilePath);
            if (mediaFiles == null) {
                XueChengPlusException.cast("媒资文件入库出错");
            }
            log.debug("媒资文件入库完成");
            return RestResponse.success();
        } finally {
            for (File chunkFile : chunkFiles) {
                try {
                    chunkFile.delete();
                } catch (Exception e) {
                    log.debug("临时分块文件删除错误：{}", e.getMessage());
                }
            }
            try {
                mergeFile.delete();
            } catch (Exception e) {
                log.debug("临时合并文件删除错误：{}", e.getMessage());
            }
        }
    }

    /**
     * 下载分块文件
     * @param fileMd5    文件的MD5
     * @param chunkTotal 总块数
     * @return 分块文件数组
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        // 作为结果返回
        File[] files = new File[chunkTotal];
        // 获取分块文件目录
        String chunkFileFolder = getChunkFileFolderPath(fileMd5);
        for (int i = 0; i < chunkTotal; i++) {
            // 获取分块文件路径
            String chunkFilePath = chunkFileFolder + i;
            File chunkFile = null;
            try {
                // 创建临时的分块文件
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (Exception e) {
                XueChengPlusException.cast("创建临时分块文件出错：" + e.getMessage());
            }

            chunkFile = downloadFileFromMinio(chunkFile, video_files, chunkFilePath);
            files[i] = chunkFile;
        }
        return files;
    }

    /**
     * 从Minio中下载文件
     * @param file       目标文件
     * @param bucket     桶
     * @param objectName 桶内文件路径
     * @return file
     */
    public File downloadFileFromMinio(File file, String bucket, String objectName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             InputStream inputStream = minioClient.getObject(GetObjectArgs
                     .builder()
                     .bucket(bucket)
                     .object(objectName)
                     .build())) {
            IOUtils.copy(inputStream, fileOutputStream);
            return file;
        } catch (Exception e) {
            XueChengPlusException.cast("查询文件分块出错");
        }
        return null;
    }
}
