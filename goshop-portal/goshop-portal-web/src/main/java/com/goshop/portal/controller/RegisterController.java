package com.goshop.portal.controller;

import com.goshop.common.context.ValidationCodeServlet;
import com.goshop.common.exception.PageException;
import com.goshop.common.pojo.ResponseStatus;
import com.goshop.common.utils.ResponseMessageUtils;
import com.goshop.common.utils.TokenUtils;
import com.goshop.manager.pojo.Member;
import com.goshop.manager.pojo.User;
import com.goshop.portal.i.RegisterService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {

    @Autowired
    RegisterService registerService;


    @RequestMapping
    public String register(HttpServletRequest request) {
        TokenUtils.getInstance().saveToken(request);
        return "register";
    }

    @RequestMapping("save")
    @ResponseBody
    public Object save(Member member,
                       User user,
                       String password_confirm,
                       HttpServletRequest request,
                       HttpServletResponse resopnse) {
        String url = request.getContextPath() + "/register.html";
        if (!TokenUtils.getInstance().verifyToken(request)) {
            ResponseMessageUtils.xmlCDataOut(resopnse, "你已提交了用户数据！", url);
        }

        if (!(password_confirm != null && password_confirm.equals(user.getPassword()))) {
            ResponseMessageUtils.xmlCDataOut(resopnse, "两次密码不同！", url);
        }

        if (!ValidationCodeServlet.isCaptcha(request)) {
            ResponseMessageUtils.xmlCDataOut(resopnse, "验证码错误！", url);
        }

        String password = user.getPassword();
        try {
            user=registerService.saveMember(member,user);
        }catch (Exception e){
            e.printStackTrace();
            ResponseMessageUtils.xmlCDataOut(resopnse, "保存用户错误请联系管理员", url);
        }



        //注册用户自动登录
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(user.getLoginName(),password,user.getSalt());
        token.setRememberMe(true);
        subject.login(token);

        url = request.getContextPath() + "/home.html";
        ResponseMessageUtils.xmlCDataOut(resopnse, "欢迎来到电商测试建议您尽快完善资料，祝您购物愉快！", url);
        return null;
    }

    @RequestMapping("/check/captcha")
    @ResponseBody
    public Object captcha(HttpServletRequest request) {
        return ResponseStatus.get(ValidationCodeServlet.isCaptcha(request));
    }


}
