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

import static org.codice.alliance.security.banner.marking.AeaType.FRD;
import static org.codice.alliance.security.banner.marking.AeaType.RD;
import static org.codice.alliance.security.banner.marking.ClassificationLevel.CONFIDENTIAL;
import static org.codice.alliance.security.banner.marking.ClassificationLevel.RESTRICTED;
import static org.codice.alliance.security.banner.marking.ClassificationLevel.SECRET;
import static org.codice.alliance.security.banner.marking.ClassificationLevel.TOP_SECRET;
import static org.codice.alliance.security.banner.marking.ClassificationLevel.UNCLASSIFIED;
import static org.codice.alliance.security.banner.marking.DissemControl.FOUO;
import static org.codice.alliance.security.banner.marking.DissemControl.IMCON;
import static org.codice.alliance.security.banner.marking.DissemControl.NOFORN;
import static org.codice.alliance.security.banner.marking.DissemControl.ORCON;
import static org.codice.alliance.security.banner.marking.DissemControl.PROPIN;
import static org.codice.alliance.security.banner.marking.DissemControl.RELIDO;
import static org.codice.alliance.security.banner.marking.DissemControl.WAIVED;
import static org.codice.alliance.security.banner.marking.MarkingType.FGI;
import static org.codice.alliance.security.banner.marking.MarkingType.JOINT;
import static org.codice.alliance.security.banner.marking.OtherDissemControl.EXDIS;
import static org.codice.alliance.security.banner.marking.OtherDissemControl.NODIS;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BannerValidator {

  protected static final Comparator<String> COUNTRY_CODE_COMPARATOR =
      (o1, o2) -> {
        if (o1.length() == o2.length()) {
          return o1.compareTo(o2);
        }
        return Integer.compare(o1.length(), o2.length());
      };

  protected static final Comparator<String> USA_FIRST_COUNTRY_CODE_COMPARATOR =
      (o1, o2) -> {
        if (o1.equals("USA")) {
          return -1;
        }
        if (o2.equals("USA")) {
          return 1;
        }
        if (o1.length() == o2.length()) {
          return o1.compareTo(o2);
        }
        return Integer.compare(o1.length(), o2.length());
      };

  protected static void validate(BannerMarkings bannerMarkings) throws MarkingsValidationException {
    Set<ValidationError> errors = new HashSet<>();

    errors.addAll(validateFgiJoint(bannerMarkings));
    errors.addAll(validateSciControls(bannerMarkings));
    errors.addAll(validateSapControls(bannerMarkings));
    errors.addAll(validateAeaMarkings(bannerMarkings));
    errors.addAll(validateFgi(bannerMarkings));
    errors.addAll(validateDisseminationControls(bannerMarkings));
    errors.addAll(validateRelToDisplayOnly(bannerMarkings));
    errors.addAll(validateOtherDissemControls(bannerMarkings));

    if (!errors.isEmpty()) {
      throw new MarkingsValidationException(
          "Errors found in processing", bannerMarkings.getInputMarkings(), errors);
    }
  }

  protected static Set<ValidationError> validateFgiJoint(BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (bannerMarkings.getType() == FGI) {
      if (bannerMarkings.getFgiAuthority().equals("COSMIC")
          && bannerMarkings.getClassification() != TOP_SECRET) {
        errors.add(
            new ValidationError(
                "COSMIC is applied only to TOP SECRET material that belongs to NATO.", "4.b.2.a."));
      }
      if (bannerMarkings.getFgiAuthority().equals("NATO")
          && bannerMarkings.getClassification() == TOP_SECRET) {
        errors.add(
            new ValidationError(
                "NATO is applied only to SECRET, CONFIDENTIAL, RESTRICTED, and UNCLASSIFIED material belonging to NATO",
                "4.b.2.a."));
      }

      if (bannerMarkings.getFgiAuthority().equals("NATO")
          || bannerMarkings.getFgiAuthority().equals("COSMIC")) {
        if (bannerMarkings.getDisseminationControls().contains(NOFORN)) {
          errors.add(new ValidationError("No use of NOFORN with NATO documents", "4.b.3."));
        }
      }
    }

    if (bannerMarkings.getNatoQualifier() != null) {
      if (bannerMarkings.getType() != FGI) {
        errors.add(
            new ValidationError(
                String.format(
                    "%s classification qualifier only valid for NATO markings",
                    bannerMarkings.getNatoQualifier()),
                "4.b."));
      } else {
        if (bannerMarkings.getNatoQualifier().equals("ATOMAL")) {
          if (!(bannerMarkings.getFgiAuthority().equals("NATO")
              || bannerMarkings.getFgiAuthority().equals("COSMIC"))) {
            errors.add(
                new ValidationError(
                    "ATOMAL classification qualifier only valid for NATO markings", "4.b."));
          }
        } else {
          if (!bannerMarkings.getFgiAuthority().equals("COSMIC")) {
            errors.add(
                new ValidationError(
                    String.format(
                        "%s classification qualifier only valid for NATO TOP SECRET SIGINT material",
                        bannerMarkings.getNatoQualifier()),
                    "4.b.2.c."));
          }
        }
      }
    }

    if (bannerMarkings.getType() == JOINT && bannerMarkings.getJointAuthorities().contains("USA")) {
      if (bannerMarkings.getClassification() == RESTRICTED) {
        errors.add(new ValidationError("RESTRICTED not valid for US JOINT documents", "5.d."));
      }
    }

    return errors;
  }

  protected static Set<ValidationError> validateSciControls(BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (bannerMarkings.getSciControls().stream()
        .map(SciControl::getControl)
        .anyMatch(c -> c.equals("HCS") || c.equals("KLONDIKE"))) {
      if (!bannerMarkings.getDisseminationControls().contains(NOFORN)) {
        errors.add(new ValidationError("HCS/KLONDIKE require NOFORN", "6.f."));
      }
    } else {
      if (!bannerMarkings.getSciControls().isEmpty()) {
        // an explicit foreign disclosure or release marking as
        // required by Reference (k) for classified intelligence information
        // under the purview of the DNI.
        boolean hasRelease = !bannerMarkings.getRelTo().isEmpty();
        if (!hasRelease) {
          hasRelease = !bannerMarkings.getDisplayOnly().isEmpty();
        }
        if (!hasRelease) {
          hasRelease =
              bannerMarkings.getDisseminationControls().stream()
                  .anyMatch(s -> s == NOFORN || s == ORCON || s == RELIDO);
        }
        if (!hasRelease) {
          errors.add(
              new ValidationError(
                  "SCI Markings require explicit foreign disclosure or release marking", "6.c."));
        }
      }
    }

    return errors;
  }

  protected static Set<ValidationError> validateSapControls(BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (bannerMarkings.getSapControl() != null) {
      if (!bannerMarkings.getSapControl().isMultiple()) {
        if (bannerMarkings.getSapControl().getPrograms().size() > 4) {
          errors.add(new ValidationError("More than four SAPs included in document ", "7.e."));
        }
      }
    }
    if (bannerMarkings.getDisseminationControls() != null
        && bannerMarkings.getDisseminationControls().contains(WAIVED)) {
      if (bannerMarkings.getSapControl() == null) {
        errors.add(
            new ValidationError("WAIVED dissemination is only appropriate for SAPs", "7.f."));
      }
    }

    return errors;
  }

  protected static Set<ValidationError> validateAeaMarkings(BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (bannerMarkings.getAeaMarking() != null) {
      if (bannerMarkings.getAeaMarking().getType() == RD
          && bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
        errors.add(new ValidationError("RD data must be marked at least CONFIDENTIAL", "8.a.4."));
      }
      if (bannerMarkings.getAeaMarking().getType() == FRD
          && bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
        errors.add(new ValidationError("FRD data must be marked at least CONFIDENTIAL", "8.b.2."));
      }
      if (bannerMarkings.getAeaMarking().isCriticalNuclearWeaponDesignInformation()
          && bannerMarkings.getAeaMarking().getType() == FRD) {
        errors.add(
            new ValidationError(
                "CNWDI is a subset of RD and not applicable to FRD documents", "8.c.3."));
      }
      if (bannerMarkings.getAeaMarking().getSigmas().stream().anyMatch(i -> i < 1 || i > 99)) {
        errors.add(new ValidationError("Valid SIGMA values are 1 to 99 inclusive", "8.d.3."));
      }

      if ((bannerMarkings.getAeaMarking().getType() == AeaType.DOE_UCNI)
          && bannerMarkings.getClassification() != UNCLASSIFIED) {
        errors.add(new ValidationError("UCNI Data must be marked UNCLASSIFIED", "8.f.3."));
      }
    }

    return errors;
  }

  protected static Set<ValidationError> validateFgi(BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (!bannerMarkings.getUsFgiCountryCodes().isEmpty()) {
      if (bannerMarkings.getType() != MarkingType.US) {
        errors.add(new ValidationError("FGI markings only valid in US products", "9.a."));
      }
      if (bannerMarkings.getUsFgiCountryCodes().contains("USA")) {
        errors.add(new ValidationError("USA invalid as FGI source country"));
      }
      if (bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
        errors.add(
            new ValidationError(
                "FGI data must be classified at a level no less than CONFIDENTIAL", "9.b."));
      }

      // The correct ordering for FGI entries is alpha trigraphs followed by alpha tetragraphs
      List<String> sortedFgi = new ArrayList<>(bannerMarkings.getUsFgiCountryCodes());
      sortedFgi.sort(COUNTRY_CODE_COMPARATOR);

      if (!ImmutableList.copyOf(sortedFgi).equals(bannerMarkings.getUsFgiCountryCodes())) {
        errors.add(
            new ValidationError(
                "FGI country codes must have alpha trigraphs followed by alpha tetragraphs",
                "9.d."));
      }
    }

    return errors;
  }

  protected static Set<ValidationError> validateDisseminationControls(
      BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (bannerMarkings.getDisseminationControls().isEmpty()) {
      return errors;
    }

    if (bannerMarkings.getDisseminationControls().contains(ORCON)
        && bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
      errors.add(
          new ValidationError(
              "ORCON dissemination only valid with classifications at a level no less than CONFIDENTIAL",
              "10.d.3."));
    }
    if (bannerMarkings.getDisseminationControls().contains(IMCON)) {
      if (bannerMarkings.getClassification().compareTo(SECRET) < 0) {
        errors.add(new ValidationError("IMCON may only be applied at SECRET", "2", "1.b."));
      }
      if (bannerMarkings.getRelTo().isEmpty()
          && !bannerMarkings.getDisseminationControls().contains(NOFORN)
          && !bannerMarkings.getDisseminationControls().contains(RELIDO)) {
        errors.add(new ValidationError("IMCON requires a dissemination notice", "2", "1.c."));
      }
    }
    if (bannerMarkings.getDisseminationControls().contains(NOFORN)) {
      if (bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
        errors.add(
            new ValidationError(
                "NOFORN dissemination only valid with classifications at a level no less than CONFIDENTIAL",
                "2",
                "2.c."));
      }
      if (!bannerMarkings.getRelTo().isEmpty()
          || bannerMarkings.getDisseminationControls().contains(RELIDO)) {
        errors.add(
            new ValidationError(
                "NOFORN dissemination may not be mixed with REL TO or RELIDO markings",
                "2",
                "2.d."));
      }
    }
    if (bannerMarkings.getDisseminationControls().contains(PROPIN)
        && bannerMarkings.getClassification() == RESTRICTED) {
      errors.add(
          new ValidationError(
              "PROPIN marking not valid with RESTRICTED classification level", "2", "3.b."));
    }
    if (bannerMarkings.getDisseminationControls().contains(RELIDO)
        && bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
      errors.add(
          new ValidationError(
              "RELIDO dissemination only valid with classifications at a level no less than CONFIDENTIAL",
              "2",
              "4.c."));
    }
    if (bannerMarkings.getDisseminationControls().contains(FOUO)
        && bannerMarkings.getClassification() != UNCLASSIFIED) {
      errors.add(
          new ValidationError(
              "FOUO dissemination is only valid with classification of UNCLASSIFIED", "10.b.1."));
    }

    return errors;
  }

  protected static Set<ValidationError> validateRelToDisplayOnly(BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (!bannerMarkings.getRelTo().isEmpty()) {
      if (bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
        errors.add(
            new ValidationError(
                "REL TO dissemination only valid with classifications at a level no less than CONFIDENTIAL",
                "10.e.3."));
      }
      if (bannerMarkings.getRelTo().size() == 1 && bannerMarkings.getRelTo().contains("USA")) {
        errors.add(
            new ValidationError(
                "REL TO USA without any other countries is not a valid marking", "10.e.5."));
      }
      if (bannerMarkings.getDisseminationControls().contains(NOFORN)) {
        errors.add(
            new ValidationError("REL TO and NOFORN not valid in a banner marking", "10.e.7."));
      }

      // The correct ordering for REL TO entries is USA first followed by alpha trigraphs
      // then by alpha tetragraphs
      List<String> sortedRelTo = new ArrayList<>(bannerMarkings.getRelTo());
      sortedRelTo.sort(USA_FIRST_COUNTRY_CODE_COMPARATOR);

      if (!ImmutableList.copyOf(sortedRelTo).equals(bannerMarkings.getRelTo())) {
        errors.add(
            new ValidationError(
                "REL TO country codes must have USA first followed by alpha trigraphs then alpha tetragraphs",
                "10.e.4."));
      }
    }

    if (!bannerMarkings.getDisplayOnly().isEmpty()) {
      if (bannerMarkings.getClassification().compareTo(CONFIDENTIAL) < 0) {
        errors.add(
            new ValidationError(
                "DISPLAY ONLY dissemination only valid with classifications at a level no less than CONFIDENTIAL",
                "10.g.3."));
      }
      if (bannerMarkings.getDisseminationControls() != null) {
        if (bannerMarkings.getDisseminationControls().contains(RELIDO)
            || bannerMarkings.getDisseminationControls().contains(NOFORN)) {
          errors.add(
              new ValidationError(
                  "DISPLAY ONLY dissemination not valid with NOFORN or RELIDO", "10.g.4."));
        }
      }

      // The correct ordering for DISPLAY ONLY entries is alpha trigraphs followed by alpha
      // tetragraphs
      List<String> sortedDisplayOnly = new ArrayList<>(bannerMarkings.getDisplayOnly());
      sortedDisplayOnly.sort(COUNTRY_CODE_COMPARATOR);

      if (!ImmutableList.copyOf(sortedDisplayOnly).equals(bannerMarkings.getDisplayOnly())) {
        errors.add(
            new ValidationError(
                "DISPLAY ONLY country codes must have alpha trigraphs followed by alpha tetragraphs",
                "10.g.5."));
      }
    }

    return errors;
  }

  protected static Set<ValidationError> validateOtherDissemControls(BannerMarkings bannerMarkings) {
    Set<ValidationError> errors = new HashSet<>();

    if (bannerMarkings.getOtherDissemControl().contains(EXDIS)) {
      if (bannerMarkings.getOtherDissemControl().contains(NODIS)) {
        errors.add(new ValidationError("EXDIS and NODIS markings cannot be combined", "3", "1.d."));
      }
      if (!bannerMarkings.getRelTo().isEmpty()) {
        errors.add(
            new ValidationError(
                "Documents bearing the EXDIS marking cannot be released to foreign "
                    + "goverments or international organizations",
                "3",
                "1.c."));
      }
    }

    if (bannerMarkings.getOtherDissemControl().contains(NODIS)
        && !bannerMarkings.getRelTo().isEmpty()) {
      errors.add(
          new ValidationError(
              "Documents bearing the NODIS marking cannot be released to foreign "
                  + "goverments or international organizations",
              "3",
              "2.d."));
    }

    return errors;
  }
}
