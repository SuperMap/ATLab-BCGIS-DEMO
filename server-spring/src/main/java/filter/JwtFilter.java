package filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JwtFilter implements Filter {

    private final String authHeaderKey = "Authorization";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String authHeader = httpServletRequest.getHeader(authHeaderKey);
        if (authHeader!= null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

        } else {

        }
    }
}
