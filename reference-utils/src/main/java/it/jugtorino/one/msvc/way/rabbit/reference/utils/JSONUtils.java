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
package it.jugtorino.one.msvc.way.rabbit.reference.utils;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {

  public static Map<String, Object> toMap(byte[] bytes) {
    ObjectMapper mapper = new ObjectMapper();

    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
    };

    try {
      return mapper.readValue(bytes, typeRef);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] toBytes(Map<String, Object> map) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsBytes(map);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
