package com.example.helloworld.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class ConnectorConnectionPoolFactory extends ConnectionPoolFactory {

	private static final String INSTANCE_CONNECTION_NAME = System.getenv("INSTANCE_CONNECTION_NAME");
	private static final String INSTANCE_UNIX_SOCKET = System.getenv("INSTANCE_UNIX_SOCKET");
	private static final String DB_USER = System.getenv("DB_USER");
	private static final String DB_PASS = System.getenv("DB_PASS");
	private static final String DB_NAME = System.getenv("DB_NAME");

	private static DataSource dataSource = null;

	public static synchronized DataSource createConnectionPool() {
		if (dataSource != null) {
			return dataSource;
		}
		HikariConfig config = new HikariConfig();

		config.setJdbcUrl(String.format("jdbc:postgresql:///%s", DB_NAME));
		config.setUsername(DB_USER);
		config.setPassword(DB_PASS);

		config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
		config.addDataSourceProperty("cloudSqlInstance", INSTANCE_CONNECTION_NAME);

		if (INSTANCE_UNIX_SOCKET != null) {
			config.addDataSourceProperty("unixSocketPath", INSTANCE_UNIX_SOCKET);
		}
		config.addDataSourceProperty("ipTypes", "PUBLIC");

		configureConnectionPool(config);

		dataSource = new HikariDataSource(config);
		return dataSource;
	}
}