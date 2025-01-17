package com.easypan.controller;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.utils.CopyTools;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ABaseController {

    // 获取当前的日志
    @SuppressWarnings("checkstyle:ConstantName")
    private static final Logger logger = LoggerFactory.getLogger(ABaseController.class);

    // 成功状态
    protected static final String STATUC_SUCCESS = "success";

    //失败状态
    protected static final String STATUC_ERROR = "error";

    // VO
    protected <T> ResponseVO getSuccessResponseVO(T t) {

        return  ResponseVO.<T>builder()
                .status(STATUC_SUCCESS)
                .code(ResponseCodeEnum.CODE_200.getCode())
                .info(ResponseCodeEnum.CODE_200.getMsg())
                .data(t)
               .build();
    }

    // 分页Vo
    protected <S, T> PaginationResultVO<T> convert2PaginationVO(PaginationResultVO<S> result, Class<T> classz) {
        return PaginationResultVO.<T>builder()
                .list(CopyTools.copyList(result.getList(), classz))
                .pageNo(result.getPageNo())
                .pageSize(result.getPageSize())
                .pageTotal(result.getPageTotal())
                .totalCount(result.getTotalCount())
                .build();
    }

    // 获取封装用户信息的实体
    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        return sessionWebUserDto;
    }


    // 获取分享的表单信息
    protected SessionShareDto getSessionShareFromSession(HttpSession session, String shareId) {
        SessionShareDto sessionShareDto = (SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY + shareId);
        return sessionShareDto;
    }


    // 阅读文件
    @SuppressWarnings({"checkstyle:ReturnCount", "checkstyle:MagicNumber"})
    protected void readFile(HttpServletResponse response, String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }
        // 标准的写入文件操作
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            logger.error("读取文件异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }

            }
        }
    }
}
