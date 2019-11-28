package com.supermap.atlab.blockchain;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CorsFilter implements Filter {
    public CorsFilter() {

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse
                .setHeader(
                        "Access-Control-Allow-Headers",
                        "User-Agent,Origin,Cache-Control,Content-type,Date,Server,withCredentials,AccessToken");
        httpServletResponse.setHeader("Access-Control-Allow-Credentials",
                "true");
        httpServletResponse.setHeader("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        httpServletResponse.setHeader("Access-Control-Max-Age", "1209600");
        httpServletResponse.setHeader("Access-Control-Expose-Headers",
                "accesstoken");
        httpServletResponse.setHeader("Access-Control-Request-Headers",
                "accesstoken");
        httpServletResponse.setHeader("Expires", "-1");
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.setHeader("pragma", "no-cache");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        List<String> authorizeList = new ArrayList<String>();
        if (!"OPTIONS".equals(httpServletRequest.getMethod())) {// OPTIONS方法不要拦截，不然跨域设置不成功
            if (!authorizeList.contains(request.getParameter("key"))) { //当请求的key不在我的授权列表 key值可以自己设定 用来识别是否为授权
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }
}