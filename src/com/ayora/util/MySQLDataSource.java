package com.ayora.util;

public class MySQLDataSource extends DataSource {
	// public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver"; // MySQL 5
	public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver"; // MySQL 8
	public static final String MYSQL_BRIDGE = "jdbc:mysql:";

	public MySQLDataSource(String host, String source, String username, String password) {
		super(MYSQL_DRIVER, MYSQL_BRIDGE + "//" + host + "/" + source, username, password);
	}

	public MySQLDataSource(String source, String username, String password) {
		super(MYSQL_DRIVER, MYSQL_BRIDGE + "//localhost/" + source, username, password);
	}

	public MySQLDataSource(String source, String username) {
		super(MYSQL_DRIVER, MYSQL_BRIDGE + "//localhost/" + source, username, "");
	}

	public MySQLDataSource(String source) {
		super(MYSQL_DRIVER, MYSQL_BRIDGE + "//localhost/" + source, "root", "");
	}
}
