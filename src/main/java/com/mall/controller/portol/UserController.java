package com.mall.controller.portol;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.CooKieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.RedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by Administrator on 2017/12/31.
 */
@Controller
@RequestMapping("/user/")
@Slf4j
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登陆
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value="login.do")
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()){
//            session.setAttribute(Const.CURRENT_USER, response.getData());
            CooKieUtil.writeLoginToken(httpServletResponse, session.getId());
            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()), Const.RedisCacheExTime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    @RequestMapping(value="logout.do")
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest request, HttpServletResponse response){
        String loginToken = CooKieUtil.readLoginCookie(request);
        CooKieUtil.delLoginCookie(request, response);
        RedisPoolUtil.del(loginToken);

//        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "register.do")
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    @RequestMapping(value = "check_valid.do")
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){
        return iUserService.checkValid(str, type);
    }

    @RequestMapping(value = "get_user_info.do")
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
    }

    @RequestMapping(value = "forget_get_question.do")
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do")
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){
        return iUserService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "forget_reset_password.do")
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken){
        return iUserService.forgetResetPassword(username, newPassword, forgetToken);
    }

    @RequestMapping(value = "reset_password.do")
    @ResponseBody
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userJsonStr, User.class);

        if (user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    @RequestMapping(value = "update_information.do")
    @ResponseBody
    public ServerResponse<User> update_information(HttpServletRequest request, User user){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

        if (currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()){
            String updateUserJsonStr = JsonUtil.obj2String(response.getData());
            RedisPoolUtil.setEx(loginToken, updateUserJsonStr, Const.RedisCacheExTime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    @RequestMapping(value = "get_information.do")
    @ResponseBody
    public ServerResponse<User> get_information(HttpServletRequest request){
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);

        String loginToken = CooKieUtil.readLoginCookie(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.str2Obj(userJsonStr, User.class);

        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    "未登录，需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
