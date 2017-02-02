/*
 * Copyright (C) 2017 Federico Fissore
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.jugtorino.one.msvc.way.rabbit.geoip.reference.service.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

public class GeoIPDao {

  private final Connection connection;

  public GeoIPDao(String pathToDB) throws ClassNotFoundException, SQLException {
    Class.forName("org.sqlite.JDBC");

    connection = DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
  }

  public Stream<Map<String, Object>> findNetworks(String ipAddress) {
    String firstTwoOctects = ipAddressToFirstTwoOctects(ipAddress);

    try (PreparedStatement stmt = connection.prepareStatement("select * from networks where first_2_octects = ?")) {
      stmt.setString(1, firstTwoOctects);

      try (ResultSet rs = stmt.executeQuery()) {
        List<String> columns = columnsOf(rs);
        List<Map<String, Object>> rows = new LinkedList<>();
        while (rs.next()) {
          rows.add(resultSetRowToMap(rs, columns));
        }
        return rows.stream();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<Map<String, Object>> findLocation(int geonameID) {
    try (PreparedStatement stmt = connection.prepareStatement("select * from locations where geoname_id = ?")) {
      stmt.setInt(1, geonameID);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        List<String> columns = columnsOf(rs);
        return Optional.of(resultSetRowToMap(rs, columns));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Object> resultSetRowToMap(ResultSet rs, List<String> columns) throws SQLException {
    Map<String, Object> row = new HashMap<>();
    for (String col : columns) {
      row.put(col, rs.getObject(col));
    }
    return row;
  }

  private List<String> columnsOf(ResultSet rs) throws SQLException {
    ResultSetMetaData metaData = rs.getMetaData();

    List<String> columns = new LinkedList<>();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      columns.add(metaData.getColumnName(i));
    }

    return columns;
  }

  private String ipAddressToFirstTwoOctects(String ipAddress) {
    String[] ipAddressParts = ipAddress.split("\\.");
    StringJoiner joiner = new StringJoiner(".");
    for (int i = 0; i < 2; i++) {
      joiner.add(ipAddressParts[i]);
    }
    return joiner.toString();
  }

}
