package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Swith4Sumin
 * @date 2025/2/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

     /**
      * @description 媒资文件查询方法
      * @param pageParams 分页参数
      * @param queryMediaParamsDto 查询条件
      * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
      * @author Swith4Sumin
      * @date 2025/2/10 8:57
     */
     PageResult<MediaFiles> queryMediaFiles(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

     UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

     MediaFiles addMediaFilesToDB(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

     /**
      * @description 检查文件是否存在
      * @param fileMd5 文件的md5
      * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
      * @author Swith4Sumin
      * @date 2025/2/20 15:38
      */
     RestResponse<Boolean> checkFile(String fileMd5);

     /**
      * @description 检查分块是否存在
      * @param fileMd5  文件的md5
      * @param chunkIndex  分块序号
      * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
      * @author Swith4Sumin
      * @date 2025/2/20 15:38
      */
     RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

     /**
      * @description 上传分块
      * @param fileMd5  文件md5
      * @param chunk  分块序号
      * @param bytes  文件字节
      * @return com.xuecheng.base.model.RestResponse
      * @author Swith4Sumin
      * @date 2025/2/20 15:38
      */
     RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes);

     /**
      * 合并分块
      *
      * @param companyId           机构id
      * @param fileMd5             文件MD5
      * @param chunkTotal          分块数量
      * @param uploadFileParamsDto 文件信息
      * @return
      */
     RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) throws IOException;
}
