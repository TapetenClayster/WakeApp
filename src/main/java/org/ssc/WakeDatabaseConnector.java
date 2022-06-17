package org.ssc;

import org.ssc.model.Location;
import org.ssc.model.WakeTime;

import java.sql.*;
import java.time.LocalTime;

public class WakeDatabaseConnector {

    private static final String DATABASE_URL = "jdbc:mariadb://localhost/";
    private static final String USERNAME = "root";
    private static final String PASSWORD = null;

    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private void connect() {
        try {
            // create connection for a server installed in localhost, with a user "root" with no password
            connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WakeTime getWaketime() {
        WakeTime wakeTime;

        try {
            connect();
            preparedStatement = connection.prepareStatement("SELECT * FROM wakeapp_db.T_Waketimes " +
                    "JOIN wakeapp_db.T_Locations " +
                    "ON wakeapp_db.T_Waketimes.f_location_id1 = wakeapp_db.T_Locations.p_location_id " +
                    "JOIN wakeapp_db.T_Locations l " +
                    "ON wakeapp_db.T_Waketimes.f_location_id2 = l.p_location_id;");

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Location start;
                Location end;
                if (resultSet.getString(16) == null) {
                    start = new Location(resultSet.getString(8),
                            resultSet.getString(9),
                            resultSet.getString(10),
                            resultSet.getString(11),
                            resultSet.getString(12),
                            resultSet.getString(13),
                            resultSet.getString(14),
                            resultSet.getString(15));
                } else {
                    start = new Location(resultSet.getString(8),
                            resultSet.getString(14),
                            resultSet.getString(15),
                            resultSet.getString(16));
                }

                if (resultSet.getString(26) == null) {
                    end = new Location(resultSet.getString(18),
                            resultSet.getString(19),
                            resultSet.getString(20),
                            resultSet.getString(21),
                            resultSet.getString(22),
                            resultSet.getString(23),
                            resultSet.getString(24),
                            resultSet.getString(25));
                } else {
                    end = new Location(resultSet.getString(18),
                            resultSet.getString(24),
                            resultSet.getString(25),
                            resultSet.getString(26));
                }

                wakeTime = new WakeTime(resultSet.getTime(2).toLocalTime(),
                        resultSet.getInt(3),
                        WakeTime.TransportType.valueOf(resultSet.getString(4)),
                        start,
                        end);
            } else {
                wakeTime = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            disconnect();
        }

        return wakeTime;
    }

    public boolean insertWaketime(WakeTime wakeTime) {
        try {
            connect();
            Location startLocation = wakeTime.getStartLocation();
            Location endLocation = wakeTime.getEndLocation();

            preparedStatement = connection.prepareStatement(
                    "INSERT INTO wakeapp_db.T_Locations (p_location_id, name, street, housenumber, postalcode, region, country, longitude, latitude, bvgId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, startLocation.name);
            preparedStatement.setString(3, startLocation.street);
            preparedStatement.setString(4, startLocation.housenumber);
            preparedStatement.setString(5, startLocation.postalcode);
            preparedStatement.setString(6, startLocation.region);
            preparedStatement.setString(7, startLocation.country);
            preparedStatement.setString(8, startLocation.longitude);
            preparedStatement.setString(9, startLocation.latitude);
            preparedStatement.setString(10, startLocation.bvgId);

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(
                    "INSERT INTO wakeapp_db.T_Locations (p_location_id, name, street, housenumber, postalcode, region, country, longitude, latitude, bvgId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

            preparedStatement.setInt(1, 2);
            preparedStatement.setString(2, endLocation.name);
            preparedStatement.setString(3, endLocation.street);
            preparedStatement.setString(4, endLocation.housenumber);
            preparedStatement.setString(5, endLocation.postalcode);
            preparedStatement.setString(6, endLocation.region);
            preparedStatement.setString(7, endLocation.country);
            preparedStatement.setString(8, endLocation.longitude);
            preparedStatement.setString(9, endLocation.latitude);
            preparedStatement.setString(10, endLocation.bvgId);

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(
                    "INSERT INTO wakeapp_db.T_Waketimes (p_waketime_id, arrivaltime, prepduration, transporttype, f_location_id1, f_location_id2) VALUES (?, ?, ?, ?, ?, ?);");

            preparedStatement.setInt(1, 1);
            preparedStatement.setTime(2, Time.valueOf(wakeTime.getArrival()));
            preparedStatement.setInt(3, wakeTime.getPreparation());
            preparedStatement.setString(4, wakeTime.getTransType().name());
            preparedStatement.setInt(5, 1);
            preparedStatement.setInt(6, 2);

            preparedStatement.executeUpdate();


            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            disconnect();
        }
    }

    public boolean insertOrUpdateWaketime(WakeTime wakeTime) {
        try {
            connect();
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM wakeapp_db.T_Waketimes WHERE p_waketime_id = ?;");
            preparedStatement.setInt(1, 1);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return updateWaketime(wakeTime);
            }

            return insertWaketime(wakeTime);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            disconnect();
        }
    }

    public boolean updateWaketime(WakeTime wakeTime) {
        try {
            Location startLocation = wakeTime.getStartLocation();
            Location endLocation = wakeTime.getEndLocation();

            preparedStatement = connection.prepareStatement(
                    "UPDATE wakeapp_db.T_Locations SET name = ?, street = ?, housenumber = ?, postalcode = ?, region = ?, country = ?, longitude = ?, latitude = ?, bvgId = ? WHERE p_location_id = ?;");

            preparedStatement.setString(1, startLocation.name);
            preparedStatement.setString(2, startLocation.street);
            preparedStatement.setString(3, startLocation.housenumber);
            preparedStatement.setString(4, startLocation.postalcode);
            preparedStatement.setString(5, startLocation.region);
            preparedStatement.setString(6, startLocation.country);
            preparedStatement.setString(7, startLocation.longitude);
            preparedStatement.setString(8, startLocation.latitude);
            preparedStatement.setString(9, startLocation.bvgId);
            preparedStatement.setInt(10, 1);

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(
                    "UPDATE wakeapp_db.T_Locations SET name = ?, street = ?, housenumber = ?, postalcode = ?, region = ?, country = ?, longitude = ?, latitude = ?, bvgId = ? WHERE p_location_id = ?;");

            preparedStatement.setString(1, endLocation.name);
            preparedStatement.setString(2, endLocation.street);
            preparedStatement.setString(3, endLocation.housenumber);
            preparedStatement.setString(4, endLocation.postalcode);
            preparedStatement.setString(5, endLocation.region);
            preparedStatement.setString(6, endLocation.country);
            preparedStatement.setString(7, endLocation.longitude);
            preparedStatement.setString(8, endLocation.latitude);
            preparedStatement.setString(9, endLocation.bvgId);
            preparedStatement.setInt(10, 2);

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(
                    "UPDATE wakeapp_db.T_Waketimes SET arrivaltime = ?, prepduration = ?, transporttype = ? WHERE p_waketime_id = ?;");

            preparedStatement.setTime(1, Time.valueOf(wakeTime.getArrival()));
            preparedStatement.setInt(2, wakeTime.getPreparation());
            preparedStatement.setString(3, wakeTime.getTransType().name());
            preparedStatement.setInt(4, 1);

            preparedStatement.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            disconnect();
        }
    }

    public boolean removeWaketime() {
        try {
            connect();
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM wakeapp_db.T_Locations WHERE p_location_id = ?;");
            preparedStatement.setInt(1, 1);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return false;
            } else {
                preparedStatement = connection.prepareStatement(
                        "DELETE FROM wakeapp_db.T_Locations WHERE p_location_id = ?;");
                preparedStatement.setInt(1, 1);
                preparedStatement.executeUpdate();
            }

            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM wakeapp_db.T_Locations WHERE p_location_id = ?;");
            preparedStatement.setInt(1, 2);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return false;
            } else {
                preparedStatement = connection.prepareStatement(
                        "DELETE FROM wakeapp_db.T_Locations WHERE p_location_id = ?;");
                preparedStatement.setInt(1, 2);
                preparedStatement.executeUpdate();
            }

            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM wakeapp_db.T_Waketimes WHERE p_waketime_id = ?;");
            preparedStatement.setInt(1, 1);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return false;
            } else {
                preparedStatement = connection.prepareStatement(
                        "DELETE FROM wakeapp_db.T_Waketimes WHERE p_waketime_id = ?;");
                preparedStatement.setInt(1, 1);
                preparedStatement.executeUpdate();
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
        // Database test: Adds Waketime, then updates and removes it.
        WakeDatabaseConnector dbConnector = new WakeDatabaseConnector();
        Location l1 = new Location("Home", "", "", "");
        Location l2 = new Location("School", "", "", "");
        WakeTime wt1 = new WakeTime(LocalTime.now().plusHours(2), 10, WakeTime.TransportType.BVG, l1, l2);
        WakeTime wt2 = new WakeTime(LocalTime.now().plusHours(4), 20, WakeTime.TransportType.CAR, l2, l1);
        System.out.println((dbConnector.getWaketime() != null ? "True" : "False") + " Waketime in database.");
        dbConnector.insertOrUpdateWaketime(wt1);
        System.out.println((dbConnector.getWaketime() != null ? "True" : "False") + " Waketime in database.");
        dbConnector.insertOrUpdateWaketime(wt2);
        System.out.println((dbConnector.getWaketime() != null ? "True" : "False") + " Waketime in database.");
        dbConnector.removeWaketime();
        System.out.println((dbConnector.getWaketime() != null ? "True" : "False") + " Waketime in database.");
    }
}
