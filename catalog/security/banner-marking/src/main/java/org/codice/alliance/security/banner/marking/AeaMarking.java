/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.security.banner.marking;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Atomic Energy Act marking parser. */
public class AeaMarking implements Serializable {
  private static final Pattern SIGMAS_PATTERN = Pattern.compile(" ");

  private static final Logger LOGGER = LoggerFactory.getLogger(AeaMarking.class);

  private AeaType type;

  private boolean criticalNuclearWeaponDesignInformation;

  private List<Integer> sigmas;

  /**
   * Parses and instantiates an AEA marking class from marking string.
   *
   * @param marking
   */
  public AeaMarking(String marking) {
    type = AeaType.lookupType(marking);

    String[] split = marking.split("[-]");
    if (split.length == 1) {
      criticalNuclearWeaponDesignInformation = false;
      sigmas = ImmutableList.of();
    } else if (split[1].equals("N")) {
      criticalNuclearWeaponDesignInformation = true;
      sigmas = ImmutableList.of();
    } else {
      criticalNuclearWeaponDesignInformation = false;
      String sigmaMarking = "SIGMA";
      if (split[1].contains("SG")) {
        sigmaMarking = "SG";
      }
      sigmas =
          ImmutableList.copyOf(
              SIGMAS_PATTERN
                  .splitAsStream(split[1].substring(sigmaMarking.length()).trim())
                  .map(AeaMarking::parseSigma)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList()));
    }
  }

  /**
   * Gets the AEA type
   *
   * @return
   */
  public AeaType getType() {
    return type;
  }

  /**
   * Returns if the AEA marking is Critical Nuclear Weapon Design Information.
   *
   * @return
   */
  public boolean isCriticalNuclearWeaponDesignInformation() {
    return criticalNuclearWeaponDesignInformation;
  }

  /**
   * Returns the SIGMA marking
   *
   * @return
   */
  public List<Integer> getSigmas() {
    return sigmas;
  }

  private static Integer parseSigma(String sigma) {
    try {
      return Integer.parseInt(sigma);
    } catch (NumberFormatException e) {
      LOGGER.trace("Unable to parse sigma: {}", sigma, e);
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(type.getName());
    if (isCriticalNuclearWeaponDesignInformation()) {
      sb.append("-N");
    }
    if (CollectionUtils.isNotEmpty(sigmas)) {
      sb.append(
          sigmas.stream()
              .map(i -> Integer.toString(i))
              .collect(Collectors.joining(" ", "-SIGMA ", "")));
    }

    return sb.toString();
  }
}
