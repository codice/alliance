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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

public class BannerMarkings implements Serializable {
  protected static final String NATO_FGI = "NATO";

  protected static final String COSMIC_FGI = "COSMIC";

  protected static final List<String> NATO_CLASS_QUALIFIERS =
      ImmutableList.of("ATOMAL", "BALK", "BOHEMIA");

  protected static final Pattern SPACE_PATTERN = Pattern.compile(" ");

  protected static final Pattern COMMA_PATTERN = Pattern.compile(",");

  protected static final String FOREIGN_GOVERNMENT_INFORMATION = "FOREIGN GOVERNMENT INFORMATION";

  protected static final String ACCM = "ACCM-";

  protected static final String REL_TO = "REL TO";

  protected static final String DISPLAY_ONLY = "DISPLAY ONLY";

  protected static final String FGI = "FGI";

  protected String inputMarkings;

  protected ClassificationLevel classification;

  protected MarkingType type;

  protected String fgiAuthority;

  protected String natoQualifier;

  protected List<String> jointAuthorities;

  protected List<String> usFgiCountryCodes;

  protected List<SciControl> sciControls;

  protected SapControl sapControl = null;

  protected AeaMarking aeaMarking = null;

  protected List<DissemControl> disseminationControls;

  // 10.e.
  protected List<String> relTo;

  // 10.g.
  protected List<String> displayOnly;

  protected List<OtherDissemControl> otherDissemControl;

  // ACCS
  // 11.b.
  protected List<String> accm;

  protected BannerMarkings(MarkingType type, String classificationSegment, String inputMarkings)
      throws MarkingsValidationException {
    this.type = type;
    this.inputMarkings = inputMarkings;

    switch (type) {
      case US:
        classification = ClassificationLevel.lookup(classificationSegment);
        if (classification == null) {
          classification = ClassificationLevel.lookupByShortname(classificationSegment);
          if (classification == null) {
            throw new MarkingsValidationException("Unknown classification marking", inputMarkings);
          }
        }
        break;
      case FGI:
        fgiAuthority = classificationSegment.split(" ")[0];
        classification =
            ClassificationLevel.lookup(
                classificationSegment.substring(fgiAuthority.length()).trim());
        if (classification == null) {
          switch (classificationSegment) {
            case "NU":
              classification = ClassificationLevel.UNCLASSIFIED;
              fgiAuthority = NATO_FGI;
              break;
            case "NR":
              classification = ClassificationLevel.RESTRICTED;
              fgiAuthority = NATO_FGI;
              break;
            case "NC":
              classification = ClassificationLevel.CONFIDENTIAL;
              fgiAuthority = NATO_FGI;
              break;
            case "NS":
              classification = ClassificationLevel.SECRET;
              fgiAuthority = NATO_FGI;
              break;
            case "CTS":
              classification = ClassificationLevel.TOP_SECRET;
              fgiAuthority = COSMIC_FGI;
              break;
            default:
              break;
          }
        }
        if (classification == null) {
          classification =
              ClassificationLevel.lookupByShortname(
                  classificationSegment.substring(fgiAuthority.length()).trim());
        }
        if (classification == null) {
          throw new MarkingsValidationException("Unknown classification marking", inputMarkings);
        }

        break;
      case JOINT:
        String suffix = classificationSegment.substring("JOINT".length()).trim();

        classification =
            ClassificationLevel.lookup(
                Arrays.stream(ClassificationLevel.values())
                    .map(ClassificationLevel::getName)
                    .filter(suffix::startsWith)
                    .findFirst()
                    .orElse(null));
        if (classification != null) {
          suffix = suffix.substring(classification.getName().length());
        } else {
          classification =
              ClassificationLevel.lookupByShortname(
                  Arrays.stream(ClassificationLevel.values())
                      .map(ClassificationLevel::getShortName)
                      .filter(suffix::startsWith)
                      .findFirst()
                      .orElse(null));
          if (classification != null && classification.getShortName() != null) {
            suffix = suffix.substring(classification.getShortName().length());
          }
        }
        if (classification == null) {
          throw new MarkingsValidationException(
              "Unknown JOINT classification marking", inputMarkings);
        }

        List<String> allJointAuthorities =
            SPACE_PATTERN
                .splitAsStream(suffix)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Collections.sort(allJointAuthorities);
        jointAuthorities = ImmutableList.copyOf(allJointAuthorities);
        break;
      default:
        break;
    }
  }

  public String getInputMarkings() {
    return inputMarkings;
  }

  public ClassificationLevel getClassification() {
    return classification;
  }

  public MarkingType getType() {
    return type;
  }

  public String getFgiAuthority() {
    return fgiAuthority;
  }

  public String getNatoQualifier() {
    return natoQualifier;
  }

  public List<String> getJointAuthorities() {
    return jointAuthorities;
  }

  public List<String> getUsFgiCountryCodes() {
    return usFgiCountryCodes;
  }

  public List<SciControl> getSciControls() {
    return sciControls;
  }

  public SapControl getSapControl() {
    return sapControl;
  }

  public AeaMarking getAeaMarking() {
    return aeaMarking;
  }

  public Boolean getDodUcni() {
    if (aeaMarking == null) {
      return false;
    }
    return aeaMarking.getType() == AeaType.DOD_UCNI;
  }

  public Boolean getDoeUcni() {
    if (aeaMarking == null) {
      return false;
    }
    return aeaMarking.getType() == AeaType.DOE_UCNI;
  }

  public List<DissemControl> getDisseminationControls() {
    return disseminationControls;
  }

  public List<String> getRelTo() {
    return relTo;
  }

  public List<String> getDisplayOnly() {
    return displayOnly;
  }

  public List<OtherDissemControl> getOtherDissemControl() {
    return otherDissemControl;
  }

  public List<String> getAccm() {
    return accm;
  }

  public boolean isNato() {
    return (type == MarkingType.FGI
        && (COSMIC_FGI.equals(fgiAuthority) || NATO_FGI.equals(fgiAuthority)));
  }

  public static BannerMarkings parseMarkings(String markings) throws MarkingsValidationException {
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
    BannerMarkings bannerMarkings = new BannerMarkings(type, split[0], markings);

    List<Function<String, Boolean>> processors = new ArrayList<>();
    if (type == MarkingType.FGI) {
      processors.add(bannerMarkings::processNato);
    }

    processors.add(bannerMarkings::processUsFgi);
    processors.add(bannerMarkings::processSap);
    processors.add(bannerMarkings::processAea);
    processors.add(bannerMarkings::processOtherDissem);
    processors.add(bannerMarkings::processDisseminationControls);
    processors.add(bannerMarkings::processSciControls);

    for (int i = 1; i < split.length; i++) {
      for (Function<String, Boolean> processor : processors) {
        if (processor.apply(split[i])) {
          break;
        }
      }
    }

    // Ensure that all Collection types have been initialized
    bannerMarkings.jointAuthorities = ensureCollectionInitialized(bannerMarkings.jointAuthorities);
    bannerMarkings.usFgiCountryCodes =
        ensureCollectionInitialized(bannerMarkings.usFgiCountryCodes);
    bannerMarkings.sciControls = ensureCollectionInitialized(bannerMarkings.sciControls);
    bannerMarkings.disseminationControls =
        ensureCollectionInitialized(bannerMarkings.disseminationControls);
    bannerMarkings.relTo = ensureCollectionInitialized(bannerMarkings.relTo);
    bannerMarkings.displayOnly = ensureCollectionInitialized(bannerMarkings.displayOnly);
    bannerMarkings.otherDissemControl =
        ensureCollectionInitialized(bannerMarkings.otherDissemControl);
    bannerMarkings.accm = ensureCollectionInitialized(bannerMarkings.accm);

    BannerValidator.validate(bannerMarkings);
    return bannerMarkings;
  }

  protected static <T> List<T> ensureCollectionInitialized(List<T> collection) {
    return collection == null ? ImmutableList.of() : collection;
  }

  protected boolean processSap(String segment) {
    if ((sapControl != null)
        || (!segment.startsWith("SAR-")
            && !segment.startsWith("SPECIAL ACCESS REQUIRED-")
            && !segment.equals("HVSACO"))) {
      return false;
    }

    if (segment.equals("HVSACO")) {
      sapControl = new SapControl();
    } else {
      sapControl = new SapControl(segment.split("[-]")[1]);
    }
    return true;
  }

  protected boolean processAea(String segment) {
    if (aeaMarking != null || AeaType.lookupType(segment) == null) {
      return false;
    }

    aeaMarking = new AeaMarking(segment);
    return true;
  }

  protected boolean processUsFgi(String segment) {
    if (usFgiCountryCodes != null
        || (!segment.startsWith(FGI) && !segment.startsWith(FOREIGN_GOVERNMENT_INFORMATION))) {
      return false;
    }

    String suffix = null;
    if (segment.startsWith(FGI)) {
      suffix = segment.substring(FGI.length()).trim();

    } else if (segment.startsWith(FOREIGN_GOVERNMENT_INFORMATION)) {
      suffix = segment.substring(FOREIGN_GOVERNMENT_INFORMATION.length()).trim();
    }

    if (suffix == null || suffix.isEmpty()) {
      usFgiCountryCodes = ImmutableList.of();
    } else {
      usFgiCountryCodes =
          ImmutableList.copyOf(SPACE_PATTERN.splitAsStream(suffix).collect(Collectors.toList()));
    }

    return true;
  }

  protected boolean processNato(String segment) {
    if (natoQualifier != null || !NATO_CLASS_QUALIFIERS.contains(segment)) {
      return false;
    }

    natoQualifier = segment;
    return true;
  }

  protected boolean processOtherDissem(String segment) {
    if (otherDissemControl != null || !OtherDissemControl.prefixBannerMatch(segment)) {
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
      if (tok.startsWith(ACCM)) {
        processingAccm = true;
      } else if (OtherDissemControl.lookupBannerName(tok) != null) {
        processingAccm = false;
      }

      if (processingAccm) {
        if (tok.startsWith(ACCM)) {
          tempAccm.add(tok.substring(ACCM.length()));
        } else {
          tempAccm.add(tok);
        }
      } else {
        tempOther.add(OtherDissemControl.lookupBannerName(tok));
      }
    }

    otherDissemControl = ImmutableList.copyOf(tempOther);
    accm = ImmutableList.copyOf(tempAccm);
    return true;
  }

  protected boolean processDisseminationControls(String segment) {
    String[] split = segment.split("[/]");

    if (!(split[0].startsWith(REL_TO)
        || split[0].startsWith(DISPLAY_ONLY)
        || DissemControl.lookupBannerName(split[0]) != null)) {
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
        tempDissem.add(DissemControl.lookupBannerName(s.trim()));
      }
    }
    disseminationControls = ImmutableList.copyOf(tempDissem);
    return true;
  }

  protected boolean processSciControls(String segment) {
    String[] split = segment.split("[/]");

    List<SciControl> tempSci = new ArrayList<>();
    for (String s : split) {
      tempSci.add(new SciControl(s));
    }

    sciControls = ImmutableList.copyOf(tempSci);
    return true;
  }
}
