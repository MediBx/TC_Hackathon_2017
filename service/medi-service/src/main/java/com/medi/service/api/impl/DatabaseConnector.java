package com.medi.service.api.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.medi.service.api.util.MySqlConnectorFactory;
import com.medi.service.api.util.MySqlDataConnector;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Created by nan on 10/2/2016.
 */
@Slf4j
public class DatabaseConnector extends MySqlDataConnector {
    // Queries
    // User
    private static final String SQL_SELECT_USER_ID = "SELECT user_id FROM users WHERE email = ?";
    private static final String SQL_SELECT_USER_DATA = "SELECT * FROM users WHERE email = ?";
    private static final String SQL_INSERT_USER_INFO = "INSERT INTO users (email, user_data, login_type) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_USER_INFO = "UPDATE users SET user_data = ?, login_type = ? WHERE user_id = ?";
    private static final String SQL_INSERT_USER_SETTING = "INSERT INTO user_settings (user_id, user_setting) VALUES (?, ?)";
    private static final String SQL_SELECT_USER_SETTING = "SELECT * FROM user_settings WHERE user_id = ?";
    private static final String SQL_UPDATE_USER_SETTING = "UPDATE user_settings SET user_setting = ? WHERE user_id = ?";
    // App
    private static final String SQL_SELECT_ALL_AVAILABLE_APPS = "SELECT app_info FROM apps WHERE app_status = 0";
    private static final String SQL_SELECT_USER_APPS = "SELECT app_id FROM user_app_list WHERE user_id = ?";
    private static final String SQL_ADD_APP_TO_USER = "INSERT INTO user_app_list (user_id, app_id) VALUES (?, ?)";
    private static final String SQL_SELECT_APP_INFO = "SELECT app_info FROM apps WHERE app_id = ?";
    private static final String SQL_SELECT_APP_TEMPLATE = "SELECT app_template FROM apps WHERE app_id = ?";
    // CustomApp
    private static final String SQL_SELECT_CUSTOM_APP_BY_ID = "SELECT * FROM custom_apps WHERE capp_id = ?";
    private static final String SQL_SELECT_CUSTOM_APP_BY_USER_AND_APP_ID = "SELECT * FROM custom_apps WHERE user_id = ? and app_id = ?";
    private static final String SQL_UPDATE_CUSTOM_APP_BY_ID = "UPDATE custom_apps SET custom_app_data = ? WHERE capp_id = ?";
    private static final String SQL_NEW_CUSTOM_APP = "INSERT INTO custom_apps (user_id, app_id, custom_app_data) VALUES (?, ?, ?)";
    // Schedule
    private static final String SQL_INSERT_SCHEDULE = "INSERT INTO schedules (user_id, app_id, schedule_type, schedule_data, is_active, utc) VALUES (?,?,?,?,?,?)";
    private static final String SQL_UPDATE_SCHEDULE = "UPDATE schedules SET schedule_type = ?, schedule_data = ?, is_active = ?, utc = ? WHERE schedule_id = ?";
    private static final String SQL_SELECT_SCHEDULE = "SELECT * FROM schedules WHERE schedule_id = ?";
    private static final String SQL_SELECT_USER_SCHEDULES = "SELECT * FROM schedules WHERE user_id = ?";
    private static final String SQL_SELECT_USER_SCHEDULES_BY_TYPE = "SELECT * FROM schedules WHERE user_id = ? and schedule_type = ?";
    private static final String SQL_SET_SCHEDULE_INACTIVE = "UPDATE schedules SET is_active = 0 WHERE schedule_id = ?";
    // CheckIn Schedule
    private static final String SQL_INSERT_CHECKIN_SCHEDULE = "INSERT INTO schedules_checkin (schedule_id, schedule_type, schedule_data, is_active, utc) VALUES (?,?,?,?,?)";
    private static final String SQL_UPDATE_CHECKIN_SCHEDULE = "UPDATE schedules_checkin SET schedule_data = ?, is_active = ?, utc = ? WHERE schedule_id = ? and schedule_type = ?";
    private static final String SQL_SELECT_CHECKIN_OF_SCHEDULE = "SELECT * FROM schedules_checkin WHERE schedule_id = ?";
    // Survey
    private static final String SQL_SELECT_SURVEY = "SELECT * FROM surveys WHERE survey_id = ?";
    private static final String SQL_SAVE_SURVEY_RESULT = "INSERT INTO survey_results (user_id, survey_id, survey_result_data) VALUES (?,?,?) ON DUPLICATE KEY UPDATE survey_result_data = ?";
    // Resource
    private static final String SQL_SELECT_RESOURCE = "SELECT * FROM resources WHERE type = ? and field = ?";

    // Parser
    private Gson gson;

    @Inject
    public DatabaseConnector(final MySqlConnectorFactory connFactory) {
        super(connFactory);
        gson = new GsonBuilder().create();
    }

    public Integer findUserID(String email) throws MySqlException, SQLException, DataNotFoundException {
        return executeQuery(SQL_SELECT_USER_ID,
                st -> st.setString(1, email),
                res -> {
                    if (res.next()) {
                        int userId = res.getInt("user_id");
                        log.trace("User ID [{}] found for email [{}]", userId, email);
                        return userId;
                    } else {
                        return -1;
                    }
                });
    }

    // Helper
    private java.sql.Timestamp convertDateTimeToTimestamp(LocalDateTime ldt) {
        long tm = ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
        return new java.sql.Timestamp(tm);
    }
}
