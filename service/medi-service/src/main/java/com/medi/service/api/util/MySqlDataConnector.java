package com.medi.service.api.util;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;

import java.sql.*;

/**
 * Created by nan on 10/2/2016.
 */
@Slf4j
public class MySqlDataConnector {
    private final ObjectPool<Connection> connPool;

    @Inject
    public MySqlDataConnector(final MySqlConnectorFactory connFactory) {
        this.connPool = new StackObjectPool<>(connFactory);
    }

    @FunctionalInterface
    public interface DbFunction<T, R> {
        R apply(T t) throws SQLException, MySqlException, DataNotFoundException;
    }

    @FunctionalInterface
    public interface DbConsumer<T> {
        void accept(T t) throws SQLException, MySqlException, DataNotFoundException;
    }

    /*
    * Sql execution wrap
    * */
    protected <T> T executeQuery(String query, DbConsumer<PreparedStatement> setInputFn, DbFunction<ResultSet, T> procResultFn) throws SQLException, MySqlException, DataNotFoundException {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            try {
                conn = connPool.borrowObject();
            } catch (Exception e) {
                throw new SQLException("Failed to borrow connection from the pool", e);
            }
            st = conn.prepareStatement(query);
            if (setInputFn != null) {
                setInputFn.accept(st);
            }
            res = st.executeQuery();
            return procResultFn.apply(res);
        } finally {
            safeClose(res);
            safeClose(st);
            safeClose(conn);
        }
    }

    /*
    * Sql execution update wrap
    * */
    protected int executeUpdate(String query, DbConsumer<PreparedStatement> setInputFn) throws SQLException, MySqlException, DataNotFoundException {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            try {
                conn = connPool.borrowObject();
            } catch (Exception e) {
                throw new MySqlException("Failed to borrow connection from the pool", e);
            }
            st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            setInputFn.accept(st);
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new DataNotFoundException("Update failed, no rows affected.");
            }
            try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1;
                }
            }
        } finally {
            safeClose(res);
            safeClose(st);
            safeClose(conn);
        }
    }

    private void safeClose(Connection conn) {
        if (conn != null) {
            try {
                connPool.returnObject(conn);
            } catch (Exception e) {
                log.warn("Failed to return the connection to the pool", e);
            }
        }
    }

    private void safeClose(ResultSet res) {
        if (res != null) {
            try {
                res.close();
            } catch (SQLException e) {
                log.warn("Failed to close databse resultset", e);
            }
        }
    }

    private void safeClose(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                log.warn("Failed to close databse statment", e);
            }
        }
    }

    public class DataNotFoundException extends Exception {
        public DataNotFoundException(String e) {
            super(e);
        }
    }

    public class MySqlException extends Exception {
        public MySqlException(String err) {
            super(err);
        }

        public MySqlException(String err, Exception e) {
            super(err, e);
        }
    }
}
