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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BannerMarkings implements Serializable {
  private static final List<String> NATO_CLASS_QUALIFIERS =
      ImmutableList.of("ATOMAL", "BALK", "BOHEMIA");

  public enum ClassificationLevel {
    UNCLASSIFIED("UNCLASSIFIED", "U"),
    RESTRICTED("RESTRICTED", "R"),
    CONFIDENTIAL("CONFIDENTIAL", "C"),
    SECRET("SECRET", "S"),
    TOP_SECRET("TOP SECRET", "TS");

    private String name;

    private String shortName;

    private static final Map<String, ClassificationLevel> LOOKUP_MAP =
        Arrays.stream(ClassificationLevel.values())
            .collect(Collectors.toMap(cl -> cl.name, cl -> cl));

    private static final Map<String, ClassificationLevel> SHORTNAME_LOOKUP =
        Arrays.stream(ClassificationLevel.values())
            .collect(Collectors.toMap(cl -> cl.shortName, cl -> cl));

    ClassificationLevel(String name, String shortName) {
      this.name = name;
      this.shortName = shortName;
    }

    public String getName() {
      return name;
    }

    public String getShortName() {
      return shortName;
    }

    public static ClassificationLevel lookup(String name) {
      return LOOKUP_MAP.get(name);
    }
  }

  enum MarkingType {
    US,
    FGI,
    JOINT
  }

  public enum DissemControl {
    IMCON("IMCON", "CONTROLLED IMAGERY"),
    NOFORN("NOFORN", "NOT RELEASABLE TO FOREIGN NATIONALS"),
    PROPIN("PROPIN", "CAUTION-PROPRIETARY INFORMATION INVOLVED"),
    RELIDO("RELIDO", "RELEASABLE BY INFORMATION DISCLOSURE OFFICIAL"),
    FISA("FISA", "FOREIGN INTELLIGENCE SURVEILLANCE ACT"),
    ORCON("ORCON", "ORIGINATOR CONTROLLED"),
    DEA_SENSITIVE("DEA SENSITIVE"),
    WAIVED("WAIVED");

    private String name;

    private List<String> lookupNames;

    DissemControl(String... lookupNames) {
      this.lookupNames = ImmutableList.copyOf(lookupNames);
      name = lookupNames[0];
    }

    public String getName() {
      return name;
    }

    public static DissemControl lookup(String name) {
      return Arrays.stream(DissemControl.values())
          .filter(dc -> dc.lookupNames.contains(name))
          .findFirst()
          .orElse(null);
    }
  }

  enum OtherDissemControl {
    ACCM("ACCM"),
    EXDIS("EXDIS", "EXCLUSIVE DISTRIBUTION"),
    LIMDIS("LIMDIS", "LIMITED DISTRIBUTION"),
    NODIS("NODIS", "NO DISTRIBUTION"),
    SBU("SBU", "SENSITIVE BUT UNCLASSIFIED"),
    SBU_NOFORN("SBU NOFORN", "SENSITIVE BUT UNCLASSIFIED NOFORN");

    private String name;

    private List<String> lookupNames;

    OtherDissemControl(String... lookupNames) {
      this.lookupNames = ImmutableList.copyOf(lookupNames);
      name = lookupNames[0];
    }

    public String getName() {
      return name;
    }

    public static OtherDissemControl lookup(String name) {
      return Arrays.stream(OtherDissemControl.values())
          .filter(dc -> dc.lookupNames.contains(name))
          .findFirst()
          .orElse(null);
    }

    public static boolean prefixMatch(String value) {
      return Arrays.stream(OtherDissemControl.values())
          .flatMap(odc -> odc.lookupNames.stream())
          .anyMatch(value::startsWith);
    }
  }

  enum AeaType {
    RD("RESTRICTED DATA"),
    FRD("FORMERLY RESTRICTED DATA");

    private final String name;

    AeaType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static class SciControl implements Serializable {
    private final String control;

    private final Map<String, List<String>> compartments;

    private SciControl(String marking) {
      String[] split = marking.split("[-]");
      control = split[0];

      if (split.length == 1) {
        compartments = ImmutableMap.of();
        return;
      }

      Map<String, List<String>> tempCompartments = new HashMap<>();
      for (int i = 1; i < split.length; i++) {
        String[] compartment = split[i].split(" ");
        List<String> subComps;
        if (compartment.length > 1) {
          subComps =
              ImmutableList.copyOf(
                  Arrays.asList(Arrays.copyOfRange(compartment, 1, compartment.length)));
        } else {
          subComps = ImmutableList.of();
        }
        tempCompartments.put(compartment[0], subComps);
      }

      compartments = ImmutableSortedMap.copyOf(tempCompartments);
    }

    public String getControl() {
      return control;
    }

    public Map<String, List<String>> getCompartments() {
      return compartments;
    }
  }

  static class SapControl implements Serializable {
    private boolean multiple;

    private boolean hvsaco;

    private List<String> programs;

    private SapControl(String programString) {
      programs =
          ImmutableList.copyOf(
              Pattern.compile("[/]").splitAsStream(programString).collect(Collectors.toList()));

      multiple = (programs.size() == 1 && programs.contains("MULTIPLE PROGRAMS"));
      if (multiple) {
        programs = ImmutableList.of();
      }

      hvsaco = false;
    }

    private SapControl() {
      programs = ImmutableList.of();
      multiple = false;
      hvsaco = true;
    }

    public boolean isMultiple() {
      return multiple;
    }

    public List<String> getPrograms() {
      return programs;
    }

    public boolean isHvsaco() {
      return hvsaco;
    }

    @Override
    public String toString() {
      if (hvsaco) {
        return "HVSACO";
      }

      StringBuilder sb = new StringBuilder("SAR");
      if (multiple) {
        sb.append("-MULTIPLE PROGRAMS");
        return sb.toString();
      }
      sb.append(programs.stream().collect(Collectors.joining("/", "-", "")));

      return sb.toString();
    }
  }

  static class AeaMarking implements Serializable {
    private AeaType type;

    private boolean cnwdi;

    private List<Integer> sigmas;

    private AeaMarking(String marking) {
      if (marking.startsWith("RD") || marking.startsWith("RESTRICTED DATA")) {
        type = AeaType.RD;
      } else {
        type = AeaType.FRD;
      }

      String[] split = marking.split("[-]");
      if (split.length == 1) {
        cnwdi = false;
        sigmas = ImmutableList.of();
      } else if (split[1].equals("N")) {
        cnwdi = true;
        sigmas = ImmutableList.of();
      } else {
        cnwdi = false;
        sigmas =
            ImmutableList.copyOf(
                Pattern.compile(" ")
                    .splitAsStream(split[1].substring("SIGMA".length()).trim())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList()));
      }
    }

    public AeaType getType() {
      return type;
    }

    public boolean isCnwdi() {
      return cnwdi;
    }

    public List<Integer> getSigmas() {
      return sigmas;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(type.getName());
      if (isCnwdi()) {
        sb.append("-N");
      }
      if (CollectionUtils.isNotEmpty(sigmas)) {
        sb.append(
            sigmas
                .stream()
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(" ", "-SIGMA ", "")));
      }

      return sb.toString();
    }
  }

  private String inputMarkings;

  private ClassificationLevel classification;

  private final MarkingType type;

  private String fgiAuthority;

  private String natoQualifier;

  private List<String> jointAuthorities;

  private List<String> usFgiCountryCodes;

  private List<SciControl> sciControls;

  private SapControl sapControl = null;

  private AeaMarking aeaMarking = null;

  private Boolean dodUcni = null;

  private Boolean doeUcni = null;

  private List<DissemControl> disseminationControls;

  // 10.e.
  private List<String> relTo;

  // 10.g.
  private List<String> displayOnly;

  private List<OtherDissemControl> otherDissemControl;

  // ACCS
  // 11.b.
  private List<String> accm;

  private BannerMarkings(MarkingType type, String classificationSegment, String inputMarkings)
      throws MarkingsValidationException {
    this.type = type;
    this.inputMarkings = inputMarkings;

    switch (type) {
      case US:
        classification = ClassificationLevel.lookup(classificationSegment);
        if (classification == null) {
          throw new MarkingsValidationException("Unknown classification marking", inputMarkings);
        }
        break;
      case FGI:
        fgiAuthority = classificationSegment.split(" ")[0];
        classification =
            ClassificationLevel.lookup(
                classificationSegment.substring(fgiAuthority.length()).trim());
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
        if (classification == null) {
          throw new MarkingsValidationException(
              "Unknown JOINT classification marking", inputMarkings);
        }
        suffix = suffix.substring(classification.getName().length());
        jointAuthorities =
            ImmutableList.copyOf(
                Pattern.compile(" ").splitAsStream(suffix).collect(Collectors.toList()));
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
    return dodUcni == null ? Boolean.FALSE : dodUcni;
  }

  public Boolean getDoeUcni() {
    return doeUcni == null ? Boolean.FALSE : doeUcni;
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
        && ("COSMIC".equals(fgiAuthority) || "NATO".equals(fgiAuthority)));
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
    processors.add(bannerMarkings::processDodUcni);
    processors.add(bannerMarkings::processDoeUcni);
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

  private static <T> List<T> ensureCollectionInitialized(List<T> collection) {
    return collection == null ? ImmutableList.of() : collection;
  }

  private boolean processSap(String segment) {
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

  private boolean processAea(String segment) {
    if ((aeaMarking != null)
        || (!segment.startsWith("RD")
            && !segment.startsWith("FRD")
            && !segment.startsWith("RESTRICTED DATA")
            && !segment.startsWith("FORMERLY RESTRICTED DATA"))) {
      return false;
    }

    aeaMarking = new AeaMarking(segment);
    return true;
  }

  private boolean processDodUcni(String segment) {
    if (dodUcni != null || !segment.startsWith("DOD U")) {
      return false;
    }

    dodUcni = true;
    return true;
  }

  private boolean processDoeUcni(String segment) {
    if (doeUcni != null || !segment.startsWith("DOE U")) {
      return false;
    }

    doeUcni = true;
    return true;
  }

  private boolean processUsFgi(String segment) {
    if (usFgiCountryCodes != null || !segment.startsWith("FGI")) {
      return false;
    }

    String suffix = segment.substring("FGI".length()).trim();
    if (suffix.isEmpty()) {
      usFgiCountryCodes = ImmutableList.of();
    } else {
      usFgiCountryCodes =
          ImmutableList.copyOf(
              Pattern.compile(" ").splitAsStream(suffix).collect(Collectors.toList()));
    }
    return true;
  }

  private boolean processNato(String segment) {
    if (natoQualifier != null || !NATO_CLASS_QUALIFIERS.contains(segment)) {
      return false;
    }

    natoQualifier = segment;
    return true;
  }

  private boolean processOtherDissem(String segment) {
    if (otherDissemControl != null || !OtherDissemControl.prefixMatch(segment)) {
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
      if (tok.startsWith("ACCM-")) {
        processingAccm = true;
      } else if (OtherDissemControl.lookup(tok) != null) {
        processingAccm = false;
      }

      if (processingAccm) {
        if (tok.startsWith("ACCM-")) {
          tempAccm.add(tok.substring("ACCM-".length()));
        } else {
          tempAccm.add(tok);
        }
      } else {
        tempOther.add(OtherDissemControl.lookup(tok));
      }
    }

    otherDissemControl = ImmutableList.copyOf(tempOther);
    accm = ImmutableList.copyOf(tempAccm);
    return true;
  }

  private boolean processDisseminationControls(String segment) {
    String[] split = segment.split("[/]");

    if (disseminationControls != null
        || !(split[0].startsWith("REL TO")
            || split[0].startsWith("DISPLAY ONLY")
            || Arrays.stream(DissemControl.values())
                .map(DissemControl::getName)
                .anyMatch(split[0]::equals))) {
      return false;
    }

    Set<DissemControl> tempDissem = new HashSet<>();

    for (String s : split) {
      if (s.startsWith("REL TO")) {
        String suffix = s.substring("REL TO".length());
        relTo =
            ImmutableList.copyOf(
                Pattern.compile(",")
                    .splitAsStream(suffix)
                    .map(String::trim)
                    .collect(Collectors.toList()));
      } else if (s.startsWith("DISPLAY ONLY")) {
        String suffix = s.substring("DISPLAY ONLY".length());
        displayOnly =
            ImmutableList.copyOf(
                Pattern.compile(",")
                    .splitAsStream(suffix)
                    .map(String::trim)
                    .collect(Collectors.toList()));
      } else {
        tempDissem.add(DissemControl.lookup(s.trim()));
      }
    }
    disseminationControls = ImmutableList.copyOf(tempDissem);
    return true;
  }

  private boolean processSciControls(String segment) {
    String[] split = segment.split("[/]");

    List<SciControl> tempSci = new ArrayList<>();
    for (String s : split) {
      tempSci.add(new SciControl(s));
    }

    sciControls = ImmutableList.copyOf(tempSci);
    return true;
  }
}
