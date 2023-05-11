package com.example.helloworld.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helloworld.datasource.ConnectorConnectionPoolFactory;

@RestController
public class PostgresController {
	
	@Value("${NAME:World}")
	String name;
	
	@GetMapping("/")
	String hello() {
		return "Hello " + name + "!";
	}

	@GetMapping("/increase")
	String hello2() throws SQLException {
		int count = 10;
		DataSource ds = ConnectorConnectionPoolFactory.createConnectionPool();
		try (Connection conn = ds.getConnection()) {
			String newInsert = "insert into test values(100)";
			
			try (PreparedStatement createTableStatement = conn.prepareStatement(newInsert);) {
				createTableStatement.executeUpdate();
				createTableStatement.close();
			}
			
			String stmt = "select * from test";
			try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {					
				ResultSet rs = createTableStatement.executeQuery();
				while(rs.next()) {
					count++;
				}
			}
		}

		return "test table count is " + count;
	}
}
