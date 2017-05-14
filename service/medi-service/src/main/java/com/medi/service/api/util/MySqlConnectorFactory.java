package com.medi.service.api.util;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.BasePoolableObjectFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by nan on 10/2/2016.
 */
@Slf4j
public class MySqlConnectorFactory extends BasePoolableObjectFactory<Connection> {
    public static final String MYSQL_HOST = "mysql-host";
    public static final String MYSQL_PORT = "mysql-port";
    public static final String MYSQL_SCHEMA = "mysql-schema";
    public static final String MYSQL_USER = "mysql-user";
    public static final String MYSQL_PW = "mysql-pw";

    private String host;
    private int port;
    private String schema;
    private String user;
    private String password;

    @Inject
    public MySqlConnectorFactory(@Named(MYSQL_HOST) String host, @Named(MYSQL_PORT) int port, @Named(MYSQL_SCHEMA) String schema,
                                 @Named(MYSQL_USER) String user, @Named(MYSQL_PW) String password) {
        this.host = host;
        this.port = port;
        this.schema = schema;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection makeObject() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        String url = "jdbc:mysql://" + host + ":" + port + "/"
                + schema + "?allowMultiQueries=true&autoReconnectForPools=true";
        log.debug("Connecting to MySql: {}", url);
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public boolean validateObject(Connection conn) {
        try {
            if (conn.isValid(0))
                return true;
        } catch (SQLException e) {
            // ignore
        }
        return false;
    }
}
