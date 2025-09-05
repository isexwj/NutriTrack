package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myproject.dto.UserLoginDTO;
import com.myproject.dto.UserRegisterDTO;
import com.myproject.dto.ForgetPasswordDTO;
import com.myproject.vo.UserLoginVO;
import com.myproject.vo.UserInfoVO;
import com.myproject.entity.User;
import com.myproject.enums.UserStatus; // 引入用户状态枚举
import com.myproject.mapper.UserMapper;
import com.myproject.service.UserService;
import com.myproject.result.ResponseResult;
import com.myproject.utils.JwtUtil;
import com.myproject.vo.CaptchaVO;
import com.myproject.dto.RegisterStep1DTO;
import com.myproject.dto.RegisterEmailSendDTO;
import com.myproject.dto.RegisterStep2DTO;
import com.myproject.dto.ForgetPasswordSendDTO;
import com.myproject.dto.ForgetPasswordResetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import com.myproject.service.MailService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.Base64;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private MailService mailService;

    // 兜底：当未配置 Redis 时使用内存 Map（本地联调用）
    private final Map<String, String> memRegisterSessions = new ConcurrentHashMap<>();
    private final Map<String, String> memEmailVerifyCodes = new ConcurrentHashMap<>();
    private final Map<String, String> memEmailResetCodes = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public ResponseResult<String> register(UserRegisterDTO registerDTO) {
        try {
            // 检查用户名是否已存在 (无论状态如何)
            if (findByUsername(registerDTO.getUsername()) != null) {
                return ResponseResult.fail("用户名已存在");
            }

            // 创建User实体，适配新的表结构
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.setEmail(registerDTO.getEmail());
            // 核心改动：不再设置role，而是设置默认状态为 ACTIVE
            user.setStatus(UserStatus.ACTIVE);
            user.setNickname(registerDTO.getUsername()); // 默认昵称为用户名

            userMapper.insert(user);

            return ResponseResult.success("注册成功");
        } catch (Exception e) {
            // 建议使用全局异常处理器，但此处保留原结构
            return ResponseResult.fail("注册失败：" + e.getMessage());
        }
    }

    // 注册步骤1：缓存注册数据（不入库）
    @Override
    public ResponseResult<String> registerStep1(RegisterStep1DTO dto) {
        if (dto.getUsername() == null || dto.getPassword() == null || dto.getConfirmPassword() == null) {
            return ResponseResult.fail("参数不完整");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return ResponseResult.fail("两次密码不一致");
        }
        if (findByUsername(dto.getUsername()) != null) {
            return ResponseResult.fail("用户名已存在");
        }
        String regToken = "rt_" + UUID.randomUUID();
        String payload = dto.getUsername() + "|" + passwordEncoder.encode(dto.getPassword()) + "|" + (dto.getNickname() == null ? dto.getUsername() : dto.getNickname());
        boolean stored = false;
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set("reg:session:" + regToken, payload, 30, TimeUnit.MINUTES);
                stored = true;
            } catch (Exception ignore) {
                // fallback to memory map
            }
        }
        if (!stored) {
            memRegisterSessions.put(regToken, payload);
        }
        return ResponseResult.success(regToken);
    }

    // 注册：发送邮箱验证码
    @Override
    public ResponseResult<String> registerEmailSend(RegisterEmailSendDTO dto) {
        if (dto.getRegToken() == null || dto.getEmail() == null) {
            return ResponseResult.fail("参数不完整");
        }
        String payload = null;
        if (stringRedisTemplate != null) {
            try {
                payload = stringRedisTemplate.opsForValue().get("reg:session:" + dto.getRegToken());
            } catch (Exception ignore) { }
        }
        if (payload == null) {
            payload = memRegisterSessions.get(dto.getRegToken());
        }
        if (payload == null) {
            return ResponseResult.fail("注册会话已失效，请重试");
        }
        // 检查邮箱是否占用
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", dto.getEmail());
        User exists = userMapper.selectOne(qw);
        if (exists != null) {
            return ResponseResult.fail("邮箱已被使用");
        }
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        boolean codeStored = false;
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set("email:verify:" + dto.getEmail(), code, 10, TimeUnit.MINUTES);
                codeStored = true;
            } catch (Exception ignore) { }
        }
        if (!codeStored) {
            memEmailVerifyCodes.put(dto.getEmail(), code);
        }
        // 发送邮件（如未配置，将不抛错但也不发送）
        if (mailService != null) {
            String subject = "【NutriTrack】注册验证码";
            String content = "您好，\n\n"
                    + "您正在进行注册邮箱验证，请在页面输入以下验证码：\n\n"
                    + code + "\n\n"
                    + "验证码有效期为10分钟，为保障账号安全，请勿将验证码泄露给他人。"
                    + "如非本人操作，请忽略本邮件。\n\n"
                    + "— NutriTrack 安全团队";
            mailService.sendTextMail(dto.getEmail(), subject, content);
        }
        return ResponseResult.success("验证码已发送，请查收邮箱");
    }

    // 注册步骤2：校验邮箱验证码并入库
    @Override
    @Transactional
    public ResponseResult<String> registerStep2(RegisterStep2DTO dto) {
        if (dto.getRegToken() == null || dto.getEmail() == null || dto.getEmailCode() == null) {
            return ResponseResult.fail("参数不完整");
        }
        String payload2 = null;
        if (stringRedisTemplate != null) {
            try {
                payload2 = stringRedisTemplate.opsForValue().get("reg:session:" + dto.getRegToken());
            } catch (Exception ignore) { }
        }
        if (payload2 == null) {
            payload2 = memRegisterSessions.get(dto.getRegToken());
        }
        if (payload2 == null) {
            return ResponseResult.fail("注册会话已失效，请从步骤一重试");
        }
        String code = null;
        if (stringRedisTemplate != null) {
            try {
                code = stringRedisTemplate.opsForValue().get("email:verify:" + dto.getEmail());
            } catch (Exception ignore) { }
        }
        if (code == null) {
            code = memEmailVerifyCodes.get(dto.getEmail());
        }
        if (code == null || !code.equals(dto.getEmailCode())) {
            return ResponseResult.fail("邮箱验证码错误或已失效");
        }
        // 再次检查用户名/邮箱是否被占用
        String[] parts = payload2.split("\\|");
        String username = parts[0];
        String passwordHash = parts[1];
        String nickname = parts[2];
        if (findByUsername(username) != null) {
            return ResponseResult.fail("用户名已存在");
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", dto.getEmail());
        if (userMapper.selectOne(qw) != null) {
            return ResponseResult.fail("邮箱已被使用");
        }
        // 入库
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordHash);
        user.setEmail(dto.getEmail());
        user.setNickname(nickname);
        user.setStatus(UserStatus.ACTIVE);
        userMapper.insert(user);
        // 清理
        boolean cleaned = false;
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete("reg:session:" + dto.getRegToken());
                stringRedisTemplate.delete("email:verify:" + dto.getEmail());
                cleaned = true;
            } catch (Exception ignore) { }
        }
        if (!cleaned) {
            memRegisterSessions.remove(dto.getRegToken());
            memEmailVerifyCodes.remove(dto.getEmail());
        }
        return ResponseResult.success("注册成功");
    }

    @Override
    public ResponseResult<UserLoginVO> login(UserLoginDTO loginDTO) {
        try {
            // 校验图形验证码（如前端传了则校验）
            if (loginDTO.getCaptchaId() != null && loginDTO.getCaptchaCode() != null) {
                try {
                    if (stringRedisTemplate != null) {
                        String key = "captcha:login:" + loginDTO.getCaptchaId();
                        String expect = stringRedisTemplate.opsForValue().get(key);
                        if (expect == null || !expect.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
                            return ResponseResult.fail("验证码错误或已失效");
                        }
                        stringRedisTemplate.delete(key);
                    }
                    // 如果没有 Redis，验证码校验跳过（开发模式）。
                } catch (Exception ignore) { /* 跳过校验 */ }
            }

            String identifier = loginDTO.getIdentifier() != null && !loginDTO.getIdentifier().isEmpty()
                    ? loginDTO.getIdentifier()
                    : loginDTO.getUsername();
            if (identifier == null || identifier.isEmpty()) {
                return ResponseResult.fail("请输入用户名或邮箱");
            }
            User dbUser;
            if (identifier.contains("@")) {
                QueryWrapper<User> qw = new QueryWrapper<>();
                qw.eq("email", identifier);
                dbUser = userMapper.selectOne(qw);
            } else {
                dbUser = findByUsername(identifier);
            }
            if (dbUser == null) {
                return ResponseResult.fail("用户名或密码错误");
            }

            if (!passwordEncoder.matches(loginDTO.getPassword(), dbUser.getPassword())) {
                return ResponseResult.fail("用户名或密码错误");
            }

            // 关键：检查用户状态
            if (dbUser.getStatus() != UserStatus.ACTIVE) {
                return ResponseResult.fail("账号状态异常，无法登录");
            }

            String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername());

            // 构建登录响应VO，保持旧结构
            UserLoginVO loginVO = UserLoginVO.builder()
                    .token(token)
                    .username(dbUser.getUsername())
                    // 核心适配：根据status模拟旧的role字段
                    .role("USER") // 假设所有活跃用户角色都是"USER"
                    .userId(dbUser.getId())
                    .build();

            return ResponseResult.success(loginVO);
        } catch (Exception e) {
            return ResponseResult.fail("登录失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult<String> forgetPassword(ForgetPasswordDTO forgetPasswordDTO) {
        // 为避免绕过邮箱验证码的旧接口，统一禁用该模板接口
        return ResponseResult.fail("请使用邮箱验证码重置接口 /user/forget-password/reset");
    }

    // 忘记密码：发送邮箱验证码
    @Override
    public ResponseResult<String> forgetPasswordSend(ForgetPasswordSendDTO dto) {
        if (dto.getEmail() == null) {
            return ResponseResult.fail("参数不完整");
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", dto.getEmail());
        User user = userMapper.selectOne(qw);
        if (user == null) {
            return ResponseResult.fail("邮箱未注册");
        }
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        boolean stored = false;
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set("email:reset:" + dto.getEmail(), code, 10, TimeUnit.MINUTES);
                stored = true;
            } catch (Exception ignore) { }
        }
        if (!stored) {
            memEmailResetCodes.put(dto.getEmail(), code);
        }
        if (mailService != null) {
            String subject = "【NutriTrack】重置密码验证码";
            String content = "您好，\n\n"
                    + "您正在进行密码重置操作，请在页面输入以下验证码：\n\n"
                    + code + "\n\n"
                    + "验证码有效期为10分钟，为保障账号安全，请勿将验证码泄露给他人。"
                    + "如非本人操作，请忽略本邮件。\n\n"
                    + "— NutriTrack 安全团队";
            mailService.sendTextMail(dto.getEmail(), subject, content);
        }
        return ResponseResult.success("验证码已发送，请查收邮箱");
    }

    // 忘记密码：校验验证码并重置
    @Override
    @Transactional
    public ResponseResult<String> forgetPasswordReset(ForgetPasswordResetDTO dto) {
        if (dto.getEmail() == null || dto.getEmailCode() == null || dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            return ResponseResult.fail("参数不完整");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseResult.fail("两次密码不一致");
        }
        String code = null;
        if (stringRedisTemplate != null) {
            try {
                code = stringRedisTemplate.opsForValue().get("email:reset:" + dto.getEmail());
            } catch (Exception ignore) { }
        }
        if (code == null) {
            code = memEmailResetCodes.get(dto.getEmail());
        }
        if (code == null || !code.equals(dto.getEmailCode())) {
            return ResponseResult.fail("邮箱验证码错误或已失效");
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", dto.getEmail());
        User user = userMapper.selectOne(qw);
        if (user == null) {
            return ResponseResult.fail("邮箱未注册");
        }
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id", user.getId());
        User upd = new User();
        upd.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.update(upd, uw);
        boolean cleaned = false;
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete("email:reset:" + dto.getEmail());
                cleaned = true;
            } catch (Exception ignore) { }
        }
        if (!cleaned) {
            memEmailResetCodes.remove(dto.getEmail());
        }
        return ResponseResult.success("密码重置成功");
    }

    @Override
    public ResponseResult<UserInfoVO> getUserInfo(String username) {
        try {
            User user = findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            // 构建用户信息VO，保持旧结构
            UserInfoVO userInfoVO = UserInfoVO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    // 核心适配：根据status模拟旧的role字段
                    .role("USER") // 假设所有用户角色都是"USER"
                    // 核心适配：将新的createdAt字段值赋给旧的createTime字段
                    .createTime(user.getCreatedAt())
                    .updateTime(user.getUpdatedAt())
                    .phoneNumber(user.getPhoneNumber())
                    .nickname(user.getNickname())
                    .build();

            return ResponseResult.success(userInfoVO);
        } catch (Exception e) {
            return ResponseResult.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult<CaptchaVO> generateCaptcha(String scene) {
        try {
            String captchaId = UUID.randomUUID().toString();
            // 4 位数字验证码 PNG
            com.wf.captcha.SpecCaptcha captcha = new com.wf.captcha.SpecCaptcha(120, 40, 4);
            captcha.setCharType(com.wf.captcha.base.Captcha.TYPE_ONLY_NUMBER);
            String code = captcha.text();
            String base64 = "data:image/png;base64," + captcha.toBase64();

            // 严格使用 Redis 进行存储与校验
            String key = "captcha:" + (scene == null ? "login" : scene) + ":" + captchaId;
            stringRedisTemplate.opsForValue().set(key, code, 3, TimeUnit.MINUTES);

            return ResponseResult.success(new CaptchaVO(captchaId, base64));
        } catch (Exception e) {
            return ResponseResult.fail("生成验证码失败，请稍后重试");
        }
    }

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public ResponseResult<String> updateUserInfo(String username, UserInfoVO userInfoVO) {
        try {
            User user = findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            // 更新用户信息
            user.setNickname(userInfoVO.getNickname());
            user.setEmail(userInfoVO.getEmail());
            user.setPhoneNumber(userInfoVO.getPhoneNumber());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            return ResponseResult.success("用户信息更新成功");
        } catch (Exception e) {
            return ResponseResult.fail("更新用户信息失败，请稍后重试");
        }
    }

    @Override
    public ResponseResult<String> deactivateAccount(String username) {
        try {
            User user = findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            // 检查用户状态
            if (user.getStatus().equals(UserStatus.DELETED)) {
                return ResponseResult.fail("账户已被注销");
            }

            // 更新用户状态为已注销
            user.setStatus(UserStatus.DELETED);
            user.setDeletedAt(LocalDateTime.now());
            userMapper.updateById(user);

            return ResponseResult.success("账户注销成功");
        } catch (Exception e) {
            return ResponseResult.fail("注销账户失败，请稍后重试");
        }
    }


    @Override
    @Transactional
    public boolean resetPassword(String username, String newPassword) {
        User user = findByUsername(username);
        // 关键：只允许活跃用户重置密码
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", username);

        User updateUser = new User();
        updateUser.setPassword(passwordEncoder.encode(newPassword));

        return userMapper.update(updateUser, updateWrapper) > 0;
    }
}

