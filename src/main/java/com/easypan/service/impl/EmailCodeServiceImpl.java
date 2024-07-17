package com.easypan.service.impl;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.enums.PageSize;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.EmailCodeQuery;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.EmailCodeMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;


/**
 * 邮箱验证码 业务接口实现
 */
@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);

    @Resource
    private EmailCodeMapper<EmailCode, EmailCodeQuery> emailCodeMapper;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private AppConfig appConfig;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<EmailCode> findListByParam(EmailCodeQuery param) {
        return this.emailCodeMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(EmailCodeQuery param) {
        return this.emailCodeMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<EmailCode> list = this.findListByParam(param);
        PaginationResultVO<EmailCode> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(EmailCode bean) {
        return this.emailCodeMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.emailCodeMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.emailCodeMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据EmailAndCode获取对象
     */
    @Override
    public EmailCode getEmailCodeByEmailAndCode(String email, String code) {
        return this.emailCodeMapper.selectByEmailAndCode(email, code);
    }

    /**
     * 根据EmailAndCode修改
     */
    @Override
    public Integer updateEmailCodeByEmailAndCode(EmailCode bean, String email, String code) {
        return this.emailCodeMapper.updateByEmailAndCode(bean, email, code);
    }

    /**
     * 根据EmailAndCode删除
     */
    @Override
    public Integer deleteEmailCodeByEmailAndCode(String email, String code) {
        return this.emailCodeMapper.deleteByEmailAndCode(email, code);
    }

    // 重载
    private void sendEmailCode(String toEmail, String code) {
        try {

            // multipart message类型邮件包含多个正文、附件以及内嵌资源，邮件表现形式更加丰富
            MimeMessage message = javaMailSender.createMimeMessage();
            // 创建helper对象，辅助配置邮箱的相关信息
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            //配置邮件发件人 1661323614@qq.com
            helper.setFrom(appConfig.getSendUserName());
            //配置邮件收件人 1或多个  18319720112@163.com
            helper.setTo(toEmail);

            // 在redis中获取要发送到邮箱的模板内容
            SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();

            //配置邮件主题
            helper.setSubject(sysSettingsDto.getRegisterEmailTitle());
            //配置邮件内容
            helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(), code));
            //配置邮件发送时间
            helper.setSentDate(new Date());
            //发送！！
            javaMailSender.send(message);
        } catch (Exception e) {
            logger.error("邮件发送失败", e);
            throw new BusinessException("邮件发送失败");
        }
    }

    /**
     * 发送邮箱验证码
     * @param toEmail
     * @param type
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String toEmail, Integer type) {
        //如果是注册，校验当前邮箱是否已被用户注册了
        if (type == Constants.ZERO) {
            UserInfo userInfo = userInfoMapper.selectByEmail(toEmail);
            if (null != userInfo) {
                throw new BusinessException("邮箱已经存在");
            }
        }

        // 调用工具类，随机生成五位数字的验证码，然后存到数据库中
        String code = StringTools.getRandomNumber(Constants.LENGTH_5);
        // 方法重载 -》 140行
        sendEmailCode(toEmail, code);

        // 发送后让当前的邮箱验证码（code）失效，说白了内部只是改变当前邮箱的状态，标记为已经注册，但是感觉这个单词用的有点。。
        // 感觉方法的名字为signEmailAlreadySignup更好点
        // 方法里面先是把状态改为一，然后下面又改为0，感觉是为了防止多人同时用这个邮箱注册吧
        emailCodeMapper.disableEmailCode(toEmail);

        // 把数据封装一下，写入到数据表中去
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(code);
        emailCode.setEmail(toEmail);
        emailCode.setStatus(Constants.ZERO);
        emailCode.setCreateTime(new Date());
        emailCodeMapper.insert(emailCode);
    }

    /**
     * 检验用户输入的邮箱
     * @param email
     * @param code
     */
    @Override
    public void checkCode(String email, String code) {
        EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email, code);
        if (null == emailCode) {
            throw new BusinessException("邮箱验证码不正确");
        }
        // 输入时间大于15后，自动失效
        if (emailCode.getStatus() == 1 || System.currentTimeMillis() - emailCode.getCreateTime().getTime() > Constants.LENGTH_15 * 1000 * 60) {
            throw new BusinessException("邮箱验证码已失效");
        }
        // 如若没有问题，通过并且让上次的验证码失效
        emailCodeMapper.disableEmailCode(email);
    }
}