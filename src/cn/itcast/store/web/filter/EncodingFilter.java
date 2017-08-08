package cn.itcast.store.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import cn.itcast.store.utils.RequestDecorator;

/**
 * 编码过滤器
 */
public class EncodingFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		// 如果请求方法为GET,则使用装饰器模式处理中文乱码
		if (req.getMethod().equalsIgnoreCase("get")) {
			req = new RequestDecorator(req);
		}
		chain.doFilter(req, response);
	}

	@Override
	public void destroy() {
	}

}
