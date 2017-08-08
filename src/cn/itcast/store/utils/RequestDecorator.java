package cn.itcast.store.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 使用装饰器模式处理GET请求中文乱码
 */
public class RequestDecorator extends HttpServletRequestWrapper {
	private HttpServletRequest request;
	private boolean encoding = false;

	public RequestDecorator(HttpServletRequest request) {
		super(request);
		this.request = request;
		// 若请求方式为GET,则需要处理乱码
		if (request.getMethod().equalsIgnoreCase("get")) {
			encoding = true;
		}
	}

	@Override
	public String getParameter(String name) {
		if (encoding) {
			String[] parameterValues = getParameterValues(name);
			return parameterValues[0];
		} else {
			return super.getParameter(name);
		}
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> map = null;
		if (encoding) {
			map = new HashMap<String, String[]>();
			Map<String, String[]> parameterMap = request.getParameterMap();
			if (parameterMap != null) {
				Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
				for (Entry<String, String[]> entry : entrySet) {
					String key = entry.getKey();
					String[] values = entry.getValue();
					String[] array = new String[values.length];
					for (int i = 0, length = values.length; i < length; i++) {
						try {
							String value = new String(values[i].getBytes("iso-8859-1"), "utf-8");
							array[i] = value;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					map.put(key, array);
				}
				return map;
			}
		}
		return super.getParameterMap();
	}

	@Override
	public String[] getParameterValues(String name) {
		if (encoding) {
			return getParameterMap().get(name);
		} else {
			return super.getParameterValues(name);
		}
	}

}
