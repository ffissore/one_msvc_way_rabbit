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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import it.jugtorino.one.msvc.way.rabbit.geoip.reference.service.dao.GeoIPDao;
import org.apache.commons.net.util.SubnetUtils;

public class IPLocation {

  private final GeoIPDao geoIPDao;

  public IPLocation(GeoIPDao geoIPDao) {
    this.geoIPDao = geoIPDao;
  }

  public Optional<Map<String, Object>> locate(String ipAddress) {
    return geoIPDao.findNetworks(ipAddress)
        .filter(filterOutNonInRange(ipAddress))
        .flatMap(this::networkToLocation)
        .map(this::addGoogleMapsLink)
        .findFirst();
  }

  private Map<String, Object> addGoogleMapsLink(Map<String, Object> location) {
    String googleMapsLink = "http://www.google.com/maps/place/" + location.getOrDefault("latitude", 0) + "," + location.getOrDefault("longitude", 0);
    location.put("google_map", googleMapsLink);
    return location;
  }

  private Stream<Map<String, Object>> networkToLocation(Map<String, Object> network) {
    return geoIPDao.findLocation((Integer) network.get("geoname_id"))
        .map(location -> {
          location.put("postalcode", network.get("postalcode"));
          location.put("latitude", network.get("latitude"));
          location.put("longitude", network.get("longitude"));
          location.put("accurancy", network.get("accurancy"));
          return location;
        })
        .map(Stream::of)
        .orElse(Stream.empty());
  }

  private Predicate<Map<String, Object>> filterOutNonInRange(String ipAddress) {
    return network -> new SubnetUtils(network.get("network").toString()).getInfo().isInRange(ipAddress);
  }

}
