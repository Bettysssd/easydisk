package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.CreateImageCode;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.VerifyRegexEnum;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController("accountController")
public class AccountController extends ABaseController {
    @SuppressWarnings("checkstyle:ConstantName")
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";


    @Resource
    private UserInfoService userInfoService;

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;




    /**
     * 生成验证码，type：0登陆， 1注册
     *
     * @param response
     * @param session
     * @param type
     * @throws IOException
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    @RequestMapping(value = "/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {


        // 生成图片形式的验证码，不是给用户发的注册的邮箱验证码
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        //获取数据验证码
        String code = vCode.getCode();

        //判断当前操作的验证码的类型， 是登陆的验证码还是申请注册的验证码， 然后分别传入到session中和下面的remove相对应
        if (type == null || type == 0) {
            // CHECK_CODE_KEY 是防止机器人的恶意提交，判断你是不是人类的验证码（就是那个图片）
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            // CHECK_CODE_KEY_EMAIL 是手机收到的验证码
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }

    /**
     * @Description: 发送邮箱验证码
     * @auther: laoluo
     * @date: 20:39 2023/4/1
     * @param: [session, email, checkCode, type]
     * @return: com.easypan.entity.vo.ResponseVO
     */


    @RequestMapping("/sendEmailCode")
    // @GlobalInterceptor不校验登录，只是校验参数请求中是否传过来
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO sendEmailCode(HttpSession session,
                                    @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                                    @VerifyParam(required = true) String checkCode,
                                    @VerifyParam(required = true) Integer type) {
        try {

            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                throw new BusinessException("图片验证码不正确");
            }
            //发送邮箱编码
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO(null);
        } finally {
            //无论执行成功与否，都会清空session中的CHECK_CODE_KEY_EMAIL
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * @Description: 注册
     * @auther: laoluo
     * @date: 20:39 2023/4/1
     * @param: [session, email, nickName, password, checkCode, emailCode]
     * @return: com.easypan.entity.vo.ResponseVO
     */
    @RequestMapping("/register")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    // 对传过来的参数进行基础长度组成来校验（通过aop来做） checkCode[判断是否为机器人的验证码]， emailCode[注册邮箱的验证码]
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                               @VerifyParam(required = true, max = 20) String nickName,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            // 和上面的set相对应 只是记录一次的话，就有可能有人恶意请求去试出来，如果去掉了，每次获得就是重新请求，机率大大降低
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    /**
     * @Description: 登录
     * @auther: laoluo
     * @date: 20:39 2023/4/1
     * @param: [session, request, email, password, checkCode]
     * @return: com.easypan.entity.vo.ResponseVO
     */
    @RequestMapping("/login")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO login(HttpSession session,
                            @VerifyParam(required = true) String email,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
            session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
            return getSuccessResponseVO(sessionWebUserDto);
        } finally {
            // 和上面的set相对应 只是记录一次的话，就有可能有人恶意请求去试出来，如果去掉了，每次获得就是重新请求，机率大大降低
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    /**
     * 重新设置密码
     * @param session
     * @param email
     * @param password
     * @param checkCode
     * @param emailCode
     * @return
     */
    @RequestMapping("/resetPwd")
    // 参数校验，还是老样子--AOP拦截
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO resetPwd(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            // 对当前的用户重新设置密码
            userInfoService.resetPwd(email, password, emailCode);
            // 返回信息给前端
            return getSuccessResponseVO(null);
        } finally {
            // 和上面的set相对应 只是记录一次的话，就有可能有人恶意请求去试出来，如果去掉了，每次获得就是重新请求，机率大大降低
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    /**
     * 获取用户头像 【当用户登录的时候】
     * @param response
     * @param userId
     */
    @RequestMapping("/getAvatar/{userId}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void getAvatar(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId) {
        // 给用户的头像安排名字 -->  /file/avatar/
        String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        // D:\ProgramFiles\yundisk\version2\EasyPan-main\myworkspace-java\easypan-java\file\avatar
        File folder = new File(appConfig.getProjectFolder() + avatarFolderName);
        if (!folder.exists()) { // 不存在才创建该目录
            folder.mkdirs();
        }
        String avatarPath = appConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        // D:/ProgramFiles/yundisk/version2/EasyPan-main/myworkspace-java/easypan-java/file/avatar/3022229766.jpg
        File file = new File(avatarPath);
        // 如果图片文件不存在的话
        if (!file.exists()) {
            // getProjectFolder():  D:/ProgramFiles/yundisk/version2/EasyPan-main/myworkspace-java/easypan-java/
            // avatarFolderName： /file/avatar/  ，Constants.AVATAR_DEFUALT：default_avatar.jpg
            // 如果也没有默认的图片的话
            if (!new File(appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFUALT).exists()) {
                // 提醒要设置默认的图片
                printNoDefaultImage(response);
                return;
            }
            avatarPath = appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFUALT;
        }
        response.setContentType("image/jpg");
        readFile(response, avatarPath);
    }

    /**
     * 提醒前端开发者要放置默认图片，但是我这里会抛出异常
     * @param response
     */
    private void printNoDefaultImage(HttpServletResponse response) {
        // Content-Type: Content-Type  CONTENT_TYPE_VALUE = "application/json;charset=UTF-8" 设置response头部的相关信息
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        // 设置状态
        response.setStatus(HttpStatus.OK.value());
        // 准备一个写入的模块
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
        } catch (Exception e) {
            logger.error("输出无默认图失败", e);
        } finally {
            writer.close();
        }
    }

    /**
     * 获取用户基本信息
     * @param session
     * @return
     */
    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpSession session) {
        // 部分的用户信息保存在session，这里就不需要前端进行传输了，直接自己去拿
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        return getSuccessResponseVO(sessionWebUserDto);
    }

    /** 获取用户存储空间的大小
     *
     * @param session
     * @return
     */
    @RequestMapping("/getUseSpace")
    @GlobalInterceptor
    public ResponseVO getUseSpace(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        //从前端中获取用户的存储空间，然后再封装回显
        return getSuccessResponseVO(redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId()));
    }

    /**
     * 退出登陆
     * @param session
     * @return
     */
    @RequestMapping("/logout")
    public ResponseVO logout(HttpSession session) {
        //让当前的session失效
        session.invalidate();
        return getSuccessResponseVO(null);
    }

    /**
     * 更新用户头像
     * @param session
     * @param avatar
     * @return
     */
    @RequestMapping("/updateUserAvatar")
    @GlobalInterceptor
    public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar) {
        //获取封装用户信息的实体   MultipartFile可以收取来自前端的头像
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        // D:/ProgramFiles/yundisk/version2/EasyPan-main/myworkspace-java/easypan-java//file/
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        // 利用baseFolder生成一个文件
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        // 如果没有当前路径 【新建一个目录】
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        // 根据用户的id，将图片重新命名储存到本地特定的位置中去
        File targetFile = new File(targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX);
        try {
            //将接收到的文件传输到给定的目标文件
            avatar.transferTo(targetFile);
        } catch (Exception e) {
            logger.error("上传头像失败", e);
        }


        // 修改用户信息
        userInfoService.updateUserInfoByUserId(UserInfo
                .builder()
                .qqAvatar("")
                .build(), webUserDto.getUserId());


        webUserDto.setAvatar(null);
        // 根据session_key来修改当前用户的
        session.setAttribute(Constants.SESSION_KEY, webUserDto);
        return getSuccessResponseVO(null);
    }

    /**
     * 更新密码
     * @param session
     * @param password
     * @return
     */
    @RequestMapping("/updatePassword")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updatePassword(HttpSession session,
                                     @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password) {
        // 获取当前用户的基本信息
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        // 更新
        userInfoService.updateUserInfoByUserId(UserInfo
                .builder()
                .password(StringTools.encodeByMD5(password))   // 接受密码，并进行md5的加密
                .build(), sessionWebUserDto.getUserId());

        return getSuccessResponseVO(null);
    }

    /** 不写
     * qq登录
     * @param session
     * @param callbackUrl
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping("qqlogin")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    // callbackUrl 回跳地址，qq登录完会传回来，然后存到session中，等到下次前端回调时再进行使用
    public ResponseVO qqlogin(HttpSession session, String callbackUrl) throws UnsupportedEncodingException {
        // 获取随机的字符串值 带过去还能带回来 （可能是保证是同一个人的效果）
        String state = StringTools.getRandomString(Constants.LENGTH_30);

        if (!StringTools.isEmpty(callbackUrl)) {
            session.setAttribute(state, callbackUrl);
        }
        // 拼接 https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=123456&redirect_uri=http%3A%2F%2Feasypan.wuhancoder
        String url = String.format(appConfig.getQqUrlAuthorization(), appConfig.getQqAppId(), URLEncoder.encode(appConfig.getQqUrlRedirect(), "utf-8"), state);

        return getSuccessResponseVO(url);
    }

    /** 不写
     * 提供给前端的接口
     * @param session
     * @param code
     * @param state
     * @return
     */
    @RequestMapping("qqlogin/callback")
    // 参数校验，防止别人恶意根据url编辑登录
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO qqLoginCallback(HttpSession session,
                                      @VerifyParam(required = true) String code,
                                      @VerifyParam(required = true) String state) {
        // code是回调后qq那边传过来的，登录成功后，返回用户基本的对象
        // 通过Authorization Code获取Access Token
        SessionWebUserDto sessionWebUserDto = userInfoService.qqLogin(code);

        session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);

        // 储存一下url，和相关的用户信息
        Map<String, Object> result = new HashMap<>();

        result.put("callbackUrl", session.getAttribute(state));

        result.put("userInfo", sessionWebUserDto);

        return getSuccessResponseVO(result);
    }
}
