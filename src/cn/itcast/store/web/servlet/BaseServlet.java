package cn.itcast.store.web.servlet;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import cn.itcast.store.utils.StoreConstant;

/**
 * 通用Servlet
 */
public class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = -6174638477485564433L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 获取方法参数
		String param = req.getParameter(StoreConstant.method.name());
		// 判断是否为空
		if (StringUtils.isNotEmpty(param)) {
			// 获取当前类的Class对象
			Class<? extends BaseServlet> clazz = this.getClass();
			try {
				// 查询类中对应的方法
				Method method = clazz.getMethod(param, HttpServletRequest.class, HttpServletResponse.class);
				// 执行方法并获取路径返回值
				String path = (String) method.invoke(clazz, req, resp);
				// 获取响应提交状态
				boolean committed = resp.isCommitted();
				// 判断路径返回值是否为空并且响应是否提交
				if (StringUtils.isNotEmpty(path) && !committed) {
					// 执行转发操作
					req.getRequestDispatcher(path).forward(req, resp);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().write("请指定执行方法!");
		}
	}

}
