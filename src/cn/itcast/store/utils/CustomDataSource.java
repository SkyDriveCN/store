package cn.itcast.store.utils;

import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 自定义连接池
 */
public class CustomDataSource implements DataSource {
	// 连接池属性集合
	private static Map<String, String> properties;
	// 使用链表数据结构来存放连接对象
	private static LinkedList<Connection> pool = (LinkedList<Connection>) Collections
			.synchronizedList(new LinkedList<Connection>());
	// 统计连接数
	private static int connectionCount;
	static {
		// 获取资源文件路径
		URL resource = CustomDataSource.class.getClassLoader().getResource("DataSource.xml");
		// 判断是否为空
		if (resource != null) {
			// 使用DOM4J解析技术
			SAXReader saxReader = new SAXReader();
			// 初始化连接池属性集合
			properties = new HashMap<String, String>();
			try {
				// 读取连接池属性配置文件
				Document document = saxReader.read(resource.getPath());
				// 获取根节点元素
				Element rootElement = document.getRootElement();
				// 获取根节点下所有节点元素
				@SuppressWarnings("unchecked")
				List<Element> elements = rootElement.elements();
				// 遍历
				for (Element element : elements) {
					// 获取节点name属性值
					String name = element.attributeValue("name");
					// 获取节点值
					String value = element.getText();
					// 将属性name与属性值放入连接池属性集合中
					properties.put(name, value);
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		// 获取数据库驱动类名
		String driverClass = properties.get("driverClass");
		// 获取数据库用户名
		String user = properties.get("user");
		// 获取数据库用户密码
		String password = properties.get("password");
		// 获取数据库连接地址
		String url = properties.get("url");
		// 如果没设置初始化连接数就给一个默认值
		String initialPoolSize = properties.get("initialPoolSize");
		if (initialPoolSize == null) {
			initialPoolSize = "10";
			properties.put("initialPoolSize", "10");
		}
		// 如果没设置最小连接数就给一个默认值
		if (properties.get("minPoolSize") == null) {
			properties.put("minPoolSize", "5");
		}
		// 如果没设置最大连接数就给一个默认值
		if (properties.get("maxPoolSize") == null) {
			properties.put("maxPoolSize", "20");
		}
		// 如果没设置增长连接数就给一个默认值
		if (properties.get("acquireIncrement") == null) {
			properties.put("acquireIncrement", "5");
		}
		// 如果没设置超时时间就给一个默认值
		if (properties.get("checkoutTimeout") == null) {
			properties.put("checkoutTimeout", "10000");
		}
		// 获取初始化连接数
		int initialSize = Integer.parseInt(initialPoolSize);
		try {
			// 加载驱动
			Class.forName(driverClass);
			// 初始化连接
			for (int i = 0; i < initialSize; i++) {
				// 获取连接
				Connection connection = DriverManager.getConnection(url, user, password);
				// 将连接对象放入连接池中
				pool.add(connection);
				// 给连接数自增
				connectionCount++;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/**
	 * 获取连接
	 */
	@Override
	public Connection getConnection() throws SQLException {
		// 判断池中的连接对象数量是否大于最小连接数
		if (pool.size() > Integer.parseInt(properties.get("minPoolSize"))) {
			// 从池中取出一个连接对象并返回
			Connection connection = new CustomConnection(pool.removeFirst(), pool);
			return connection;
		} else {
			int maxPoolSize = Integer.parseInt(properties.get("maxPoolSize"));
			// 为了保持连接对象的最小连接数,需要根据增长连接属性值来新建连接
			int acquireIncrement = Integer.parseInt(properties.get("acquireIncrement"));
			// 判断已经存在的连接数与自增连接数之和是否小于最大连接数
			if ((connectionCount + acquireIncrement) < maxPoolSize) {
				// 如果小于最大连接数,那么就可以创建连接
				for (int i = 0; i < acquireIncrement; i++) {
					Connection connection = DriverManager.getConnection(properties.get("url"), properties.get("user"),
							properties.get("password"));
					pool.add(connection);
					connectionCount++;
				}
				Connection connection = new CustomConnection(pool.removeFirst(), pool);
				return connection;
			} else {
				try {
					// 如果已经存在的连接数与自增连接数之和大于或等于最大连接数,超时等待其他连接对象被释放回池中
					Thread.sleep(Long.parseLong(properties.get("checkoutTimeout")));
					if (pool.size() > Integer.parseInt(properties.get("minPoolSize"))) {
						// 从池中取出一个连接对象并返回
						Connection connection = new CustomConnection(pool.removeFirst(), pool);
						return connection;
					} else {
						throw new SQLTimeoutException("数据库连接超时");
					}
				} catch (NumberFormatException | InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {

		return null;
	}

}
