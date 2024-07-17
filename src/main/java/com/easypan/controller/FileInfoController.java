package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.enums.FileFolderTypeEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.utils.CopyTools;
import com.easypan.utils.StringTools;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 文件信息 Controller
 */
@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController extends CommonFileController {

    private String filePid;

    /**
     * 根据条件分页查询【展示文件】
     */
    @RequestMapping("/loadDataList")
    @GlobalInterceptor(checkParams = true)

    public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category) {

        FileCategoryEnums categoryEnum = FileCategoryEnums.getByCode(category);

        if (null != categoryEnum) {
            query.setFileCategory(categoryEnum.getCategory());
        }
        // 获取用户Id，存到query中
        query.setUserId(getUserInfoFromSession(session).getUserId());
        // 展示的排序顺序
        query.setOrderBy("last_update_time desc");
        // 当前的删除标记 【默认是使用中的状态】
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        // 分页查询结果封装在结果集里面
        PaginationResultVO result = fileInfoService.findListByPage(query);

        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    /**
     * 上传文件【图片、文本、视频、音频等】
     * @param session
     * @param fileId
     * @param file
     * @param fileName
     * @param filePid
     * @param fileMd5
     * @param chunkIndex
     * @param chunks
     * @return
     */
    @RequestMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO uploadFile(HttpSession session,
                                 String fileId,
                                 MultipartFile file,
                                 @VerifyParam(required = true) String fileName,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileMd5,
                                 @VerifyParam(required = true) Integer chunkIndex,
                                 @VerifyParam(required = true) Integer chunks) {

        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        // 上传文件 前端传过来的md5值【这个是用来判断文件是否是首次上传】md5的值由前端生成
        UploadResultDto resultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return getSuccessResponseVO(resultDto);
    }

    /**
     * 查看图片
     * @param response
     * @param imageFolder
     * @param imageName
     */
    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName) {
        super.getImage(response, imageFolder, imageName);
    }

    /**
     * 查看视频
     * @param response
     * @param session
     * @param fileId
     */
    @RequestMapping("/ts/getVideoInfo/{fileId}")
    public void getVideoInfo(HttpServletResponse response, HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDto.getUserId());
    }

    /**
     * 查看文件【wps三件套、txt等】
     * @param response
     * @param session
     * @param fileId
     */
    @RequestMapping("/getFile/{fileId}")
    public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDto.getUserId());
    }

    /**
     * 新增文件夹
     * @param session
     * @param filePid
     * @param fileName
     * @return
     */
    @RequestMapping("/newFoloder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO newFoloder(HttpSession session,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileName) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);

        // 获取文件信息
        FileInfo fileInfo = fileInfoService.newFolder(filePid, webUserDto.getUserId(), fileName);
        return getSuccessResponseVO(fileInfo);
    }

    /**
     * 获取当前目录
     * @param session
     * @param path
     * @return
     */
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getFolderInfo(HttpSession session, @VerifyParam(required = true) String path) {
        return super.getFolderInfo(path, getUserInfoFromSession(session).getUserId());
    }

    /**
     * 文件夹修改名字
     * @param session
     * @param fileId
     * @param fileName
     * @return
     */
    @RequestMapping("/rename")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO rename(HttpSession session,
                             @VerifyParam(required = true) String fileId,
                             @VerifyParam(required = true) String fileName) {
        // 获取用户的基本信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        // 对文件夹进行改名
        FileInfo fileInfo = fileInfoService.rename(fileId, webUserDto.getUserId(), fileName);

        return getSuccessResponseVO(CopyTools.copy(fileInfo, FileInfoVO.class));
    }

    /**
     * 当文件需要被移动时，加载当前模块内所有的文件夹【包括目录之间的关系 一层一层的展开，每点击一次就会调用一次这个方法】
     * 找到之间的层级关系
     * @param session
     * @param filePid
     * @param currentFileIds
     * @return
     */
    @RequestMapping("/loadAllFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadAllFolder(HttpSession session, @VerifyParam(required = true) String filePid, String currentFileIds) {
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setFilePid(filePid);
        query.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        if (!StringTools.isEmpty(currentFileIds)) {
            query.setExcludeFileIdArray(currentFileIds.split(","));
        }
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        query.setOrderBy("create_time desc");
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(query);
        return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FileInfoVO.class));
    }

    /**
     * 更改文件的所处的文件夹
     * @param session
     * @param fileIds 需要被移动的文件或者是文件夹
     * @param filePid 移动的文件夹的目的地
     * @return
     */

    @RequestMapping("/changeFileFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO changeFileFolder(HttpSession session,
                                       @VerifyParam(required = true) String fileIds,
                                       @VerifyParam(required = true) String filePid) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.changeFileFolder(fileIds, filePid, webUserDto.getUserId());
        return getSuccessResponseVO(null);
    }

    /**
     * 创建下载的url
     * @param session
     * @param fileId
     * @return
     */
    @RequestMapping("/createDownloadUrl/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO createDownloadUrl(HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        return super.createDownloadUrl(fileId, getUserInfoFromSession(session).getUserId());
    }

    /**
     * 下载文件
     * @param request
     * @param response
     * @param code
     * @throws Exception
     */
    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable("code") @VerifyParam(required = true) String code) throws Exception {
        super.download(request, response, code);
    }

    /**
     * 删除文件
     * @param session
     * @param fileIds
     * @return
     */
    @RequestMapping("/delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(), fileIds);
        return getSuccessResponseVO(null);
    }
}