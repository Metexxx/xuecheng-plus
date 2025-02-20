package com.xuecheng.media.api;

import com.alibaba.nacos.common.model.RestResult;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @description 媒资文件管理接口
 * @author Swith4Sumin
 * @date 2025/2/18 11:29
 * @version 1.0
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

     @Autowired
     MediaFileService mediaFileService;

     @ApiOperation("媒资列表查询接口")
     @PostMapping("/files")
     public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
         Long companyId = 1232141425L;
         return mediaFileService.queryMediaFiles(companyId,pageParams,queryMediaParamsDto);
     }
     @ApiOperation("上传文件")
     @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
     public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata,
                                       @RequestParam(value = "folder", required = false) String folder,
                                       @RequestParam(value = "objectName", required = false) String objectName) {
         UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
         String contentType = filedata.getContentType();
         assert contentType != null;
         if (contentType.contains("image")) {
             uploadFileParamsDto.setFileType("001001"); // 图片
         } else {
             uploadFileParamsDto.setFileType("001003"); // 视频
         }
         // 设置文件名、大小、备注
         uploadFileParamsDto.setRemark("");
         uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
         uploadFileParamsDto.setFileSize(filedata.getSize());

         Long companyId = 1232141425L;
         // 上传文件并返回结果
         try {
             // 创建临时文件，并将上传的文件拷贝到临时文件
             File tempFile = File.createTempFile("minio", ".temp");
             filedata.transferTo(tempFile);
             // 文件路径
             String absolutePath = tempFile.getAbsolutePath();
             return mediaFileService.uploadFile(companyId, uploadFileParamsDto, absolutePath);
         } catch (IOException e) {
             XueChengPlusException.cast("上传文件过程出错:" + e.getMessage());
         }
         return new UploadFileResultDto();
     }

//     @ApiOperation("预览文件")
//     @GetMapping("/preview/{mediaId}")
//     public RestResult<String> preview(@PathVariable String mediaId){
//         mediaFileService.getFileId(mediaId);
//         return RestResponse.success()
//     }
}