/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.security.banner.marking

import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.codice.alliance.security.banner.marking.AeaType.*
import static org.codice.alliance.security.banner.marking.ClassificationLevel.*
import static org.codice.alliance.security.banner.marking.DissemControl.*
import static org.codice.alliance.security.banner.marking.MarkingType.*
import static org.codice.alliance.security.banner.marking.OtherDissemControl.*

@RunWith(JUnitPlatform.class)
class BannerMarkingsSpec extends Specification {
    def 'test type and classification'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.type == type
        bannerMarkings.classification == classification

        where:
        markings                         | type  | classification
        'TOP SECRET'                     | US    | TOP_SECRET
        'SECRET//FGI FRA'                | US    | SECRET
        'CONFIDENTIAL'                   | US    | CONFIDENTIAL
        '//CAN RESTRICTED'               | FGI   | RESTRICTED
        '//COSMIC TOP SECRET//BOHEMIA'   | FGI   | TOP_SECRET
        '//JOINT SECRET CAN USA'         | JOINT | SECRET
        '//JOINT RESTRICTED CAN GBR'     | JOINT | RESTRICTED
        '//JOINT TOP SECRET CAN DEU USA' | JOINT | TOP_SECRET
    }

    def 'test fgi markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.type == FGI
        bannerMarkings.classification == classification
        bannerMarkings.fgiAuthority == fgiAuthority

        where:
        markings                | classification | fgiAuthority
        '//NATO SECRET//ATOMAL' | SECRET         | 'NATO'
        '//DEU RESTRICTED'      | RESTRICTED     | 'DEU'
    }

    def 'test joint markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.type == JOINT
        bannerMarkings.classification == classification
        bannerMarkings.jointAuthorities.containsAll(jointAuthorities)

        where:
        markings                                      | classification | jointAuthorities
        '//JOINT SECRET CAN USA'                      | SECRET         | ['CAN', 'USA']
        '//JOINT TOP SECRET CAN DEU USA'              | TOP_SECRET     | ['CAN', 'DEU', 'USA']
        '//JOINT SECRET CAN DEU USA NATO//TK//RELIDO' | SECRET         | ['CAN', 'DEU', 'USA', 'NATO']
    }

    def 'U//FOUO'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings("U//FOUO")

        then:
        bannerMarkings.classification == UNCLASSIFIED
        bannerMarkings.disseminationControls.contains(FOUO)
    }

    def 'test sci controls only'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.classification == classification
        bannerMarkings.sciControls.size() == sciControl.size()

        for (SciControl control : bannerMarkings.sciControls) {
            def expectedControl = sciControl.stream()
                    .filter({ c -> c.control == control.control })
                    .findFirst().orElse(null)

            // When looping, Spock gets confused without using an explicit 'assert' statement
            assert expectedControl != null
            assert control.compartments.size() == expectedControl.compartments.size()

            for (def compartmentMap : control.compartments.entrySet()) {

                def expectedCompartments = expectedControl.compartments.entrySet().stream()
                        .filter({ c -> c.key == compartmentMap.key })
                        .map({ c -> c.value })
                        .findFirst()
                        .orElse(null)
                assert expectedCompartments != null
                assert compartmentMap.value.size() == expectedCompartments.size()
                assert compartmentMap.value.containsAll(expectedCompartments)
            }
        }

        where:
        markings                                                | classification | sciControl
        'SECRET//HCS//NOFORN'                                   | SECRET         |
                [[control: 'HCS', compartments: [:]]]
        'SECRET//HCS-XXX//NOFORN'                               | SECRET         |
                [[control: 'HCS', compartments: ['XXX': []]]]
        'SECRET//HCS-XXX-GAMMA//NOFORN'                         | SECRET         |
                [[control: 'HCS', compartments: ['XXX': [], 'GAMMA': []]]]
        'SECRET//HCS-XXX/COMINT/TK-ABC-XYZ HELLO WORLD//NOFORN' | SECRET         |
                [[control: 'HCS', compartments: ['XXX': []]], [control: 'COMINT', compartments: [:]],
                 [control: 'TK', compartments: ['ABC': [], 'XYZ': ['HELLO', 'WORLD']]]]
        'TOP SECRET//TK-ABC X Y Z/COMINT//ORCON'                | TOP_SECRET     |
                [[control: 'TK', compartments: ['ABC': ['X', 'Y', 'Z']]], [control: 'COMINT', compartments: [:]]]
    }

    def 'test sap markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.classification == classification
        bannerMarkings.sapControl.multiple == multiple
        bannerMarkings.sapControl.programs.size() == programs.size()
        bannerMarkings.sapControl.programs.containsAll(programs)
        bannerMarkings.sapControl.hvsaco == hvsaco

        where:
        markings                                               | classification |
                multiple | programs             | hvsaco
        'TOP SECRET//SPECIAL ACCESS REQUIRED-BUTTERED POPCORN' | TOP_SECRET     |
                false    | ['BUTTERED POPCORN'] | false
        'TOP SECRET//SPECIAL ACCESS REQUIRED-BP'               | TOP_SECRET     |
                false    | ['BP']               | false
        'SECRET//SAR-BP'                                       | SECRET         |
                false    | ['BP']               | false
        'SECRET//TK//SAR-BP/GB/TC//NOFORN'                     | SECRET         |
                false    | ['BP', 'GB', 'TC']   | false
        'SECRET//SAR-MULTIPLE PROGRAMS'                        | SECRET         |
                true     | []                   | false
        'SECRET//HVSACO'                                       | SECRET         |
                false    | []                   | true
    }

    def 'test sap waived'() {
        setup:
        def markings = 'TOP SECRET//SAR-BP//WAIVED'

        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.classification == TOP_SECRET
        !bannerMarkings.sapControl.multiple
        bannerMarkings.sapControl.programs.size() == 1
        bannerMarkings.sapControl.programs.containsAll(['BP'])
        bannerMarkings.disseminationControls.size() == 1
        bannerMarkings.disseminationControls.containsAll([WAIVED])
    }

    def 'test atomic energy markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.aeaMarking.type == type
        bannerMarkings.aeaMarking.criticalNuclearWeaponDesignInformation == cnwdi
        bannerMarkings.aeaMarking.sigmas.size() == sigmas.size()
        bannerMarkings.aeaMarking.sigmas.containsAll(sigmas)

        where:
        markings                                                    | type      | cnwdi | sigmas
        'TOP SECRET//RESTRICTED DATA'                               | RD        | false | []
        'TOP SECRET//FORMERLY RESTRICTED DATA'                      | FRD       | false | []
        'SECRET//RD'                                                | RD        | false | []
        'SECRET//TK//RD-N//NOFORN'                                  | RD        | true  | []
        'SECRET//RESTRICTED DATA-N'                                 | RD        | true  | []
        'SECRET//RESTRICTED DATA-SIGMA 1 2'                         | RD        | false | [1, 2]
        'SECRET//FORMERLY RESTRICTED DATA-SIGMA 14'                 | FRD       | false | [14]
        'UNCLASSIFIED//DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION'   | DOD_UCNI  | false | []
        'UNCLASSIFIED//DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION'   | DOE_UCNI  | false | []
    }

    def 'test ucni markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.dodUcni == dod
        bannerMarkings.doeUcni == doe

        where:
        markings                                                  | dod   | doe
        'UNCLASSIFIED//DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION' | true  | false
        'UNCLASSIFIED//DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION' | false | true
        'SECRET//FORMERLY RESTRICTED DATA-SIGMA 14'               | false | false
    }

    def 'test usfgi markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.usFgiCountryCodes != null
        bannerMarkings.usFgiCountryCodes.size() == countryCodes.size()
        bannerMarkings.usFgiCountryCodes.containsAll(countryCodes)

        where:
        markings                       | countryCodes
        'TOP SECRET//FGI DEU GBR'      | ['DEU', 'GBR']
        'TOP SECRET//FGI DEU GBR NATO' | ['DEU', 'GBR', 'NATO']
        'SECRET//TK//FGI//RELIDO'      | []
    }

    def 'test dissemination markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.disseminationControls.size() == dissem.size();
        bannerMarkings.disseminationControls.containsAll(dissem)

        bannerMarkings.otherDissemControl.size() == otherDissem.size();
        bannerMarkings.otherDissemControl.containsAll(otherDissem)

        bannerMarkings.accm.size() == accm.size()
        bannerMarkings.accm.containsAll(accm)

        where:
        markings                                                  | dissem                  |
                otherDissem          | accm
        'SECRET//NOFORN/DEA SENSITIVE'                            | [NOFORN, DEA_SENSITIVE] |
                []                   | []
        'SECRET//TK//NOFORN/DEA SENSITIVE'                        | [NOFORN, DEA_SENSITIVE] |
                []                   | []
        'SECRET//ORCON/PROPIN/NOFORN'                             | [NOFORN, ORCON, PROPIN] |
                []                   | []
        'SECRET//DEA SENSITIVE'                                   | [DEA_SENSITIVE]         |
                []                   | []
        'SECRET//DEA SENSITIVE//LIMDIS/'                          | [DEA_SENSITIVE]         |
                [LIMDIS]             | []
        'SECRET//DEA SENSITIVE/NOFORN//SBU NOFORN'                | [NOFORN, DEA_SENSITIVE] |
                [SBU_NOFORN]         | []
        'SECRET//EXDIS'                                           | []                      |
                [EXDIS]              | []
        'SECRET//LIMITED DISTRIBUTION/ACCM-FOOBAR'                | []                      |
                [LIMDIS]             | ['FOOBAR']
        'SECRET//LIMITED DISTRIBUTION/ACCM-FOOBAR'                | []                      |
                [LIMDIS]             | ['FOOBAR']
        'SECRET//LIMITED DISTRIBUTION/ACCM-FOOBAR/BAZ'            | []                      |
                [LIMDIS]             | ['FOOBAR', 'BAZ']
        'SECRET//LIMITED DISTRIBUTION/ACCM-FOOBAR/BAZ/SBU NOFORN' | []                      |
                [LIMDIS, SBU_NOFORN] | ['FOOBAR', 'BAZ']
        'SECRET//ACCM-FOOBAR/BAZ/LIMITED DISTRIBUTION/SBU NOFORN' | []                      |
                [LIMDIS, SBU_NOFORN] | ['FOOBAR', 'BAZ']
    }

    def 'test relto and display only markings'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.usFgiCountryCodes.size() == countryCodes.size()
        bannerMarkings.usFgiCountryCodes.containsAll(countryCodes)
        bannerMarkings.relTo.size() == relTo.size()
        bannerMarkings.relTo.containsAll(relTo)
        bannerMarkings.displayOnly.size() == displayTo.size()
        bannerMarkings.displayOnly.containsAll(displayTo)

        where:
        markings                                                         | countryCodes           |
                relTo                  | displayTo
        'TOP SECRET//FGI DEU GBR//REL TO USA, DEU, NATO'                 | ['DEU', 'GBR']         |
                ['DEU', 'NATO', 'USA'] | []
        'TOP SECRET//FGI DEU GBR NATO//REL TO USA, DEU/DISPLAY ONLY NZL' | ['DEU', 'GBR', 'NATO'] |
                ['DEU', 'USA']         | ['NZL']
        'SECRET//TK//FGI//DISPLAY ONLY AFG, NZL'                         | []                     |
                []                     | ['NZL', 'AFG']
        'SECRET//TK//FGI//NOFORN'                                        | []                     |
                []                     | []
    }

    def 'test complex marking'() {
        setup:
        def markings = 'TOP SECRET//ABC/COMINT/SI-GAMMA-XXX/FOO-O XYZ//SAR-BP/GB/TC//RD-SIGMA 2 5 6//FGI CAN NZL//' +
                'IMCON/PROPIN/REL TO USA, CAN, NZL/DISPLAY ONLY AFG/DEA SENSITIVE//' +
                'LIMITED DISTRIBUTION/ACCM-LEMONADE/PARTY FAVORS/SBU NOFORN'

        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        for (SciControl sci : bannerMarkings.sciControls) {
            StringBuilder sb = new StringBuilder(sci.getControl());
            for (Map.Entry<String, List<String>> comp : sci.getCompartments()
                    .entrySet()) {
                sb.append("-").append(comp.getKey());
                for (String subComp : comp.getValue()) {
                    sb.append(" ").append(subComp);
                }
            }
        }

        bannerMarkings.classification == TOP_SECRET
        bannerMarkings.sciControls.size() == 4
        for (SciControl sciControl : bannerMarkings.sciControls) {
            assert ['ABC', 'COMINT', 'SI', 'FOO'].contains(sciControl.control)
            if (sciControl.control in ['ABC', 'COMINT']) {
                assert sciControl.compartments.isEmpty()
            }
            if (sciControl.control == 'SI') {
                assert sciControl.compartments.size() == 2
                assert sciControl.compartments.keySet().containsAll(['XXX', 'GAMMA'])
                sciControl.compartments.values().every({ subComp -> assert subComp.empty })
            }
            if (sciControl.control == 'FOO') {
                assert sciControl.compartments.size() == 1
                assert sciControl.compartments.get('O').size() == 1
                assert sciControl.compartments.get('O').containsAll(['XYZ'])
            }
        }

        !bannerMarkings.sapControl.multiple
        bannerMarkings.sapControl.programs.size() == 3
        bannerMarkings.sapControl.programs.containsAll(['BP', 'GB', 'TC'])

        bannerMarkings.aeaMarking.type == RD
        bannerMarkings.aeaMarking.sigmas.size() == 3
        bannerMarkings.aeaMarking.sigmas.containsAll([2, 5, 6])

        bannerMarkings.usFgiCountryCodes.size() == 2
        bannerMarkings.usFgiCountryCodes.containsAll(['NZL', 'CAN'])

        bannerMarkings.displayOnly.size() == 1
        bannerMarkings.displayOnly.containsAll(['AFG'])

        bannerMarkings.disseminationControls.size() == 3
        bannerMarkings.disseminationControls.containsAll([IMCON, PROPIN, DEA_SENSITIVE])

        bannerMarkings.accm.size() == 2
        bannerMarkings.accm.containsAll(['LEMONADE', 'PARTY FAVORS'])

        bannerMarkings.otherDissemControl.size() == 2
        bannerMarkings.otherDissemControl.containsAll([LIMDIS, SBU_NOFORN])
    }

    @Unroll
    def 'test fgi/joint validations'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                            | validationParaRefs
        '//DEU TOP SECRET//BOHEMIA'         | ['4.b.2.c.']
        '//COSMIC TOP SECRET//BALK//NOFORN' | ['4.b.3.']
        '//NATO SECRET//ATOMAL//NOFORN'     | ['4.b.3.']
        '//COSMIC SECRET'                   | ['4.b.2.a.']
        '//NATO TOP SECRET'                 | ['4.b.2.a.']
        '//NATO SECRET//BALK'               | ['4.b.2.c.']
        '//NATO SECRET//BOHEMIA//NOFORN'    | ['4.b.2.c.', '4.b.3.']
        '//JOINT RESTRICTED CAN USA'        | ['5.d.']
    }

    @Unroll
    def 'test sci validation passes'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.classification == classification

        where:
        markings                                | classification
        'TOP SECRET//HCS-X ABC DEF//NOFORN'     | TOP_SECRET
        'SECRET//KLONDIKE//NOFORN'              | SECRET
        'SECRET//MADE-UP-SCI//REL TO USA CAN'   | SECRET
        'SECRET//MADE-UP-SCI//DISPLAY ONLY AFG' | SECRET
        'SECRET//MADE-UP-SCI//RELIDO'           | SECRET
        'SECRET//MADE-UP-SCI//ORCON'            | SECRET
        'SECRET//MADE-UP-SCI//NOFORN'           | SECRET
    }

    @Unroll
    def 'test sci validation fails'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                    | validationParaRefs
        'TOP SECRET//HCS-X ABC DEF' | ['6.f.']
        'SECRET//KLONDIKE//ORCON'   | ['6.f.']
        'SECRET//MADE-UP-SCI'       | ['6.c.']
    }

    def 'test sap validation passes'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.classification == classification

        where:
        markings                                                        | classification
        'TOP SECRET//SPECIAL ACCESS REQUIRED-BUTTERED POPCORN/AA/BB/CC' | TOP_SECRET
        'TOP SECRET//SAR-BUTTERED POPCORN/AA/BB/CC'                     | TOP_SECRET
        'TOP SECRET//SAR-BUTTERED POPCORN/AA/BB/CC//WAIVED'             | TOP_SECRET
    }

    def 'test sap validation fails'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                                                           | validationParaRefs
        'TOP SECRET//SPECIAL ACCESS REQUIRED-BUTTERED POPCORN/AA/BB/CC/DD' | ['7.e.']
        'TOP SECRET//SAR-BUTTERED POPCORN/AA/BB/CC/DD'                     | ['7.e.']
        'SECRET//WAIVED'                                                   | ['7.f.']
    }

    def 'test aea validation passes'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.classification == classification

        where:
        markings                        | classification
        'CONFIDENTIAL//RD-N'            | CONFIDENTIAL
        'TOP SECRET//RESTRICTED DATA-N' | TOP_SECRET
        'TOP SECRET//RD-SIGMA 1 12 40'  | TOP_SECRET
    }

    def 'test aea validation fails'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                                 | validationParaRefs
        'RESTRICTED//RD'                         | ['8.a.4.']
        'RESTRICTED//RESTRICTED DATA'            | ['8.a.4.']
        'RESTRICTED//FORMERLY RESTRICTED DATA'   | ['8.b.2.']
        'UNCLASSIFIED//FRD'                      | ['8.b.2.']
        'SECRET//FRD-N'                          | ['8.c.3.']
        'TOP SECRET//FORMERLY RESTRICTED DATA-N' | ['8.c.3.']
        'CONFIDENTIAL//FRD-SIGMA 1 112 240'      | ['8.d.3.']
    }

    @Unroll
    def 'test fgi validation'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                    | validationParaRefs
        '//DEU SECRET//FGI CAN'     | ['9.a.']
        'CONFIDENTIAL//FGI CAN USA' | ['-']
        'RESTRICTED//FGI CAN'       | ['9.b.']
        'RESTRICTED//FGI CAN USA'   | ['9.b.', '-']
        'SECRET//FGI NZL CAN'       | ['9.d.']
    }

    @Unroll
    def 'test dissemination validation'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                      | validationParaRefs
        'RESTRICTED//ORCON'           | ['10.d.3.']
        'CONFIDENTIAL//IMCON'         | ['1.b.', '1.c.']
        'SECRET//IMCON/FISA'          | ['1.c.']
        'RESTRICTED//NOFORN'          | ['2.c.']
        'CONFIDENTIAL//NOFORN/RELIDO' | ['2.d.']
        'RESTRICTED//PROPIN'          | ['3.b.']
        'CONFIDENTIAL//NOFORN/RELIDO' | ['2.d.']
        'RESTRICTED//RELIDO'          | ['4.c.']
    }

    def 'test relto and displayonly validation'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                          | validationParaRefs
        'RESTRICTED//REL TO CAN'          | ['10.e.3.']
        'CONFIDENTIAL//REL TO USA'        | ['10.e.5.']
        'SECRET//NOFORN/REL TO CAN'       | ['2.d.', '10.e.7.']
        'SECRET//REL TO CAN, USA, GBR'    | ['10.e.4.']
        'RESTRICTED//DISPLAY ONLY AFG'    | ['10.g.3.']
        'SECRET//NOFORN/DISPLAY ONLY AFG' | ['10.g.4.']
        'SECRET//RELIDO/DISPLAY ONLY AFG' | ['10.g.4.']
        'SECRET//DISPLAY ONLY NZL, AFG'   | ['10.g.5.']
    }

    def 'test other dissem validation'() {
        when:
        BannerMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                    | validationParaRefs
        'SECRET//EXDIS/NODIS'       | ['1.d.']
        'SECRET//REL TO CAN//EXDIS' | ['1.c.']
        'SECRET//REL TO CAN//NODIS' | ['2.d.']
    }

    def 'test ucni'() {
        when:
        def bannerMarkings = BannerMarkings.parseMarkings(markings)

        then:
        bannerMarkings.aeaMarking.type == type
        bannerMarkings.dodUcni == dodUcni
        bannerMarkings.doeUcni == doeUcni

        where:
        markings                                                    | type      | dodUcni | doeUcni
        'TOP SECRET//RESTRICTED DATA'                               | RD        | false   | false
        'TOP SECRET//FORMERLY RESTRICTED DATA'                      | FRD       | false   | false
        'SECRET//RD'                                                | RD        | false   | false
        'SECRET//TK//RD-N//NOFORN'                                  | RD        | false   | false
        'SECRET//RESTRICTED DATA-N'                                 | RD        | false   | false
        'SECRET//RESTRICTED DATA-SIGMA 1 2'                         | RD        | false   | false
        'SECRET//FORMERLY RESTRICTED DATA-SIGMA 14'                 | FRD       | false   | false
        'UNCLASSIFIED//DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION'   | DOD_UCNI  | true | false
        'UNCLASSIFIED//DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION'   | DOE_UCNI  | false | true
    }
}
