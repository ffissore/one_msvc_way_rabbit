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
package it.jugtorino.one.msvc.way.rabbit.geoip.reference.service;

import java.util.Map;
import java.util.function.Predicate;

import it.jugtorino.one.msvc.way.rabbit.geoip.reference.service.dao.GeoIPDao;
import org.apache.commons.net.util.SubnetUtils;

public class IPLocationTemplate {

  private final GeoIPDao geoIPDao;

  public IPLocationTemplate(GeoIPDao geoIPDao) {
    this.geoIPDao = geoIPDao;
  }

  private Map<String, Object> addGoogleMapsLink(Map<String, Object> location) {
    String googleMapsLink = "http://www.google.com/maps/place/" + location.getOrDefault("latitude", 0) + "," + location.getOrDefault("longitude", 0);
    location.put("google_map", googleMapsLink);
    return location;
  }

  private Predicate<Map<String, Object>> filterOutNonInRange(String ipAddress) {
    return network -> new SubnetUtils(network.get("network").toString()).getInfo().isInRange(ipAddress);
  }

}
