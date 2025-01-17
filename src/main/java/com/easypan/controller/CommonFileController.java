package com.easypan.controller;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileFolderTypeEnums;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FolderVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.FileInfoService;
import com.easypan.utils.CopyTools;
import com.easypan.utils.StringTools;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文件操作
 */
public class CommonFileController extends ABaseController {

    @Resource
    protected FileInfoService fileInfoService;

    @Resource
    protected AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取当前目录
     * @param path
     * @param userId
     * @return
     */
    public ResponseVO getFolderInfo(String path, String userId) {
        String[] pathArray = path.split("/");
        FileInfoQuery infoQuery = FileInfoQuery
                .builder().userId(userId)
                .folderType(FileFolderTypeEnums.FOLDER.getType())
                .fileIdArray(pathArray)
                .build();
        String orderBy = "field(file_id,\"" + StringUtils.join(pathArray, "\",\"") + "\")";

        //TODO 父类属性，单独获取，反射使用暂时不太方便
        infoQuery.setOrderBy(orderBy);
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(infoQuery);

        return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FolderVO.class));
    }

    /**
     * 获取图片
     * @param response
     * @param imageFolder
     * @param imageName
     */
    public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (StringTools.isEmpty(imageFolder) || StringUtils.isBlank(imageName)) {
            return;
        }
        String imageSuffix = StringTools.getFileSuffix(imageName);
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
        imageSuffix = imageSuffix.replace(".", "");
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, filePath);
    }

    /**
     * 读取文件
     * @param response
     * @param fileId
     * @param userId
     */
    @SuppressWarnings("checkstyle:ReturnCount")
    protected void getFile(HttpServletResponse response, String fileId, String userId) {
        String filePath = null;
        // 读取index.ts文件，如果有的话
        if (fileId.endsWith(".ts")) {
            String[] tsAarray = fileId.split("_");
            String realFileId = tsAarray[0];
            //根据原文件的id查询出一个文件集合
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(realFileId, userId);
            if (fileInfo == null) {
                //分享的视频，ts路径记录的是原视频的id,这里通过id直接取出原视频

                List<FileInfo> fileInfoList = fileInfoService.findListByParam(FileInfoQuery
                        .builder()
                        .fileId(realFileId)
                        .build());
                fileInfo = fileInfoList.get(0);
                if (fileInfo == null) {
                    return;
                }

                //更具当前用户id和路径去查询当前用户是否有该文件，如果没有直接返回
                Integer count = fileInfoService.findCountByParam(FileInfoQuery
                        .builder()
                        .fileId(fileInfo.getFileId())
                        .userId(userId)
                        .build());
                if (count == 0) {
                    return;
                }
            }
            // 获取文件名字
            String fileName = fileInfo.getFilePath();
            fileName = StringTools.getFileNameNoSuffix(fileName) + "/" + fileId;
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileName;
        } else {
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                return;
            }
            //视频文件读取.m3u8文件
            if (FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                //重新设置文件路径
                String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
            } else {
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
            }
        }
        // file是用来判断是否该路径下有对应的文件，如果有才传该路劲给前端
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        // 除了视频文件需要进行处理，其他类型的则不需要进行改动，只需要进行简单的判断，如果没有问题则把路劲传给前端即可
        readFile(response, filePath);
    }

    /**
     * 生成下载链接
     * @param fileId
     * @param userId
     * @return
     */
    protected ResponseVO createDownloadUrl(String fileId, String userId) {
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String code = StringTools.getRandomString(Constants.LENGTH_50);

        redisComponent.saveDownloadCode(code, DownloadFileDto
                  .builder()
                  .downloadCode(code)
                  .filePath(fileInfo.getFilePath())
                  .fileName(fileInfo.getFileName())
                  .build());
        return getSuccessResponseVO(code);
    }

    /**
     * 下载文件
     * @param request
     * @param response
     * @param code
     * @throws Exception
     */
    @SuppressWarnings("checkstyle:WhitespaceAround")
    protected void download(HttpServletRequest request, HttpServletResponse response, String code) throws Exception {
        DownloadFileDto downloadFileDto = redisComponent.getDownloadCode(code);
        if (null == downloadFileDto) {
            return;
        }
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + downloadFileDto.getFilePath();
        String fileName = downloadFileDto.getFileName();
        response.setContentType("application/x-msdownload; charset=UTF-8");
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) { //IE浏览器
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        readFile(response, filePath);
    }
}
