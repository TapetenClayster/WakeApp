package org.ssc;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;

public class WakeDatabaseConnector {

    private static final String DATABASE_URL = "jdbc:mariadb://localhost/";
    private static final String USERNAME = "root";
    private static final String PASSWORD = null;

    private Connection connection = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private void connect() {
        try {
            // create connection for a server installed in localhost, with a user "root" with no password
            connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
            // create a Statement
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<WakeTime> getWaketimes() {
        ArrayList<WakeTime> waketimeList = new ArrayList<>();

        try {
            connect();
            preparedStatement = connection.prepareStatement("SELECT * FROM wakeapp_db.T_Waketimes;");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                WakeTime wakeTime = new WakeTime(resultSet.getTime(2).toLocalTime(),
                        resultSet.getInt(3),
                        resultSet.getInt(4),
                        WakeTime.TransportType.valueOf(resultSet.getString(5)));
                waketimeList.add(wakeTime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return waketimeList;
    }

    public int addWaketime(WakeTime wakeTime) {
        try {
            connect();
            preparedStatement = connection.prepareStatement(
                    "INSERT INTO wakeapp_db.T_Waketimes (arrivaltime, travelduration, prepduration, transporttype) VALUES (?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setTime(1, Time.valueOf(wakeTime.getArrival()));
            preparedStatement.setInt(2, wakeTime.getDrive());
            preparedStatement.setInt(3, wakeTime.getPreparation());
            preparedStatement.setString(4, wakeTime.getTransType().name());

            preparedStatement.executeUpdate();

            ResultSet tableKeys = preparedStatement.getGeneratedKeys();
            if (tableKeys.next()) {
                return tableKeys.getInt(1);
            }

            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            disconnect();
        }
    }

    public boolean removeWaketimeById(int waketimeId) {
        try {
            connect();
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM wakeapp_db.T_Waketimes WHERE p_waketime_id = ?;");
            preparedStatement.setInt(1, waketimeId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                preparedStatement = connection.prepareStatement(
                        "DELETE FROM wakeapp_db.T_Waketimes WHERE p_waketime_id = ?;");
                preparedStatement.setInt(1, waketimeId);
                preparedStatement.executeUpdate();

                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            disconnect();
        }
    }

    public void test() {
        // Database test: Adds Waketime, then removes it.
        WakeDatabaseConnector dbConnector = new WakeDatabaseConnector();
        WakeTime wt = new WakeTime(LocalTime.now().plusHours(2), 30, 10, WakeTime.TransportType.OVPN);
        System.out.println(dbConnector.getWaketimes().size() + " Waketimes in database.");
        int autoIncrementId = dbConnector.addWaketime(wt);
        System.out.println(dbConnector.getWaketimes().size() + " Waketimes in database.");
        // IMPORTANT: deleting the row will not reset the auto increment counter.
        dbConnector.removeWaketimeById(autoIncrementId);
        System.out.println(dbConnector.getWaketimes().size() + " Waketimes in database.");
    }
}
