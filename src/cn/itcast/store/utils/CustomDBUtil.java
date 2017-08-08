package cn.itcast.store.utils;

import java.sql.Connection;
import java.sql.SQLException;

public class CustomDBUtil {

	private static CustomDataSource dataSource = new CustomDataSource();
	private static ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();

	public static Connection getConnection() throws SQLException {
		Connection connection = threadLocal.get();
		if (connection == null) {
			connection = dataSource.getConnection();
			threadLocal.set(connection);
		}
		return connection;
	}

}
