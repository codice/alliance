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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Handles parsing the classification in the short form portion marking syntax. */
public class PortionMarkings extends BannerMarkings {

  private static final String ACCM1 = "ACCM-";

  private static final String REL_TO = "REL TO";

  private static final String DISPLAY_ONLY = "DISPLAY ONLY";

  protected PortionMarkings(MarkingType type, String classificationSegment, String inputMarkings)
      throws MarkingsValidationException {
    super(type, classificationSegment, inputMarkings);
  }

  public static PortionMarkings parseMarkings(String markings) throws MarkingsValidationException {
    MarkingType type;
    String trimmedMarkings;
    if (markings.startsWith("//JOINT")) {
      type = MarkingType.JOINT;
      trimmedMarkings = markings.substring(2);
    } else if (markings.startsWith("//")) {
      type = MarkingType.FGI;
      trimmedMarkings = markings.substring(2);
    } else {
      type = MarkingType.US;
      trimmedMarkings = markings;
    }

    String[] split = trimmedMarkings.split("[/][/]");
    PortionMarkings portionMarkings = new PortionMarkings(type, split[0], markings);

    List<Function<String, Boolean>> processors = new ArrayList<>();
    if (type == MarkingType.FGI) {
      processors.add(portionMarkings::processNato);
    }

    processors.add(portionMarkings::processUsFgi);
    processors.add(portionMarkings::processSap);
    processors.add(portionMarkings::processAea);
    processors.add(portionMarkings::processOtherDissem);
    processors.add(portionMarkings::processDisseminationControls);
    processors.add(portionMarkings::processSciControls);

    for (int i = 1; i < split.length; i++) {
      for (Function<String, Boolean> processor : processors) {
        if (processor.apply(split[i])) {
          break;
        }
      }
    }

    // Ensure that all Collection types have been initialized
    portionMarkings.jointAuthorities =
        ensureCollectionInitialized(portionMarkings.jointAuthorities);
    portionMarkings.usFgiCountryCodes =
        ensureCollectionInitialized(portionMarkings.usFgiCountryCodes);
    portionMarkings.sciControls = ensureCollectionInitialized(portionMarkings.sciControls);
    portionMarkings.disseminationControls =
        ensureCollectionInitialized(portionMarkings.disseminationControls);
    portionMarkings.relTo = ensureCollectionInitialized(portionMarkings.relTo);
    portionMarkings.displayOnly = ensureCollectionInitialized(portionMarkings.displayOnly);
    portionMarkings.otherDissemControl =
        ensureCollectionInitialized(portionMarkings.otherDissemControl);
    portionMarkings.accm = ensureCollectionInitialized(portionMarkings.accm);

    BannerValidator.validate(portionMarkings);
    return portionMarkings;
  }

  @Override
  protected boolean processOtherDissem(String segment) {
    if (otherDissemControl != null || !OtherDissemControl.prefixPortionMatch(segment)) {
      return false;
    }

    // Process each OtherDissem control sequentially. If ACCM- is found, attempt to process
    // the next tokens as ACCM markers unless they are in the OTHER_DISSEM set
    String[] tokens = segment.split("[/]");
    HashSet<OtherDissemControl> tempOther = new HashSet<>();
    HashSet<String> tempAccm = new HashSet<>();
    boolean processingAccm = false;
    for (String tok : tokens) {
      // This if/elif will leave the processingAcm as true once ACCM processing has started
      // until a non-ACCM control is encountered
      if (tok.startsWith(ACCM1)) {
        processingAccm = true;
      } else if (OtherDissemControl.lookupPortionName(tok) != null) {
        processingAccm = false;
      }

      if (processingAccm) {
        if (tok.startsWith(ACCM1)) {
          tempAccm.add(tok.substring(ACCM1.length()));
        } else {
          tempAccm.add(tok);
        }
      } else {
        OtherDissemControl control = OtherDissemControl.lookupPortionName(tok);
        if (control != null) {
          tempOther.add(control);
        } else {
          return false;
        }
      }
    }

    otherDissemControl = ImmutableList.copyOf(tempOther);
    accm = ImmutableList.copyOf(tempAccm);
    return true;
  }

  @Override
  protected boolean processDisseminationControls(String segment) {
    String[] split = segment.split("[/]");

    if (!(split[0].startsWith(REL_TO)
        || split[0].startsWith(DISPLAY_ONLY)
        || DissemControl.lookupPortionName(split[0]) != null)) {
      return false;
    }

    Set<DissemControl> tempDissem = new HashSet<>();

    for (String s : split) {
      if (s.startsWith(REL_TO)) {
        String suffix = s.substring(REL_TO.length());
        relTo =
            ImmutableList.copyOf(
                COMMA_PATTERN.splitAsStream(suffix).map(String::trim).collect(Collectors.toList()));
      } else if (s.startsWith(DISPLAY_ONLY)) {
        String suffix = s.substring(DISPLAY_ONLY.length());
        displayOnly =
            ImmutableList.copyOf(
                COMMA_PATTERN.splitAsStream(suffix).map(String::trim).collect(Collectors.toList()));
      } else {
        DissemControl dissemControl = DissemControl.lookupPortionName(s.trim());
        if (dissemControl != null) {
          tempDissem.add(dissemControl);
        } else {
          return false;
        }
      }
    }
    disseminationControls = ImmutableList.copyOf(tempDissem);
    return true;
  }
}
