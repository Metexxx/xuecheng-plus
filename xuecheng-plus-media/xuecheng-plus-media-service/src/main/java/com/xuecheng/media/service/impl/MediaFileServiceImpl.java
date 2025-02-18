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
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.Digest;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
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
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;
    @Value("${minio.bucket.files}")
    private String bucket_Files;

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
     * @description         将文件写入minIO
     * @param localFilePath 文件地址
     * @param mimeType      mime类型
     * @param bucket        桶
     * @param objectName    对象名称
     * @return  boolean
     */
    public boolean addMediaFilesToMinIo(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testbucket);
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
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return mediaFiles文件信息
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
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
        return mediaFiles;
    }
    @Override
    @Transactional
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        // 文件名称
        String filename = uploadFileParamsDto.getFilename();
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 文件mimeType
        String mimeType = getMimeType(extension);
        // 文件md5值
        String fileMd5 = getFileMd5(file);
        // 文件的默认目录
        String defaultFolderPath = getDefaultFolderPath();
        // 存储到minio中的对象名（带目录）
        String objectName = defaultFolderPath + fileMd5 + extension;
        // 将文件上传到minio
        boolean b = addMediaFilesToMinIo(localFilePath, mimeType, bucket_Files, objectName);
        if (!b) {
            XueChengPlusException.cast("上传文件失败");
        }
        // 设置文件大小
        uploadFileParamsDto.setFileSize(file.length());
        // 将文件信息存储到数据库
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);
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
}
