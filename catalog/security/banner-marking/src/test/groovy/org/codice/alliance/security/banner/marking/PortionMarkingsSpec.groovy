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
class PortionMarkingsSpec extends Specification {
    def 'test type and classification'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.type == type
        portionMarkings.classification == classification

        where:
        markings                         | type  | classification
        'TS'                             | US    | TOP_SECRET
        'S//FGI FRA'                     | US    | SECRET
        'C'                              | US    | CONFIDENTIAL
        '//CAN R'                        | FGI   | RESTRICTED
        '//CTS//BOHEMIA'                 | FGI   | TOP_SECRET
        '//JOINT S CAN USA'              | JOINT | SECRET
        '//JOINT R CAN GBR'              | JOINT | RESTRICTED
        '//JOINT TS CAN DEU USA'         | JOINT | TOP_SECRET
    }

    def 'test fgi markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.type == FGI
        portionMarkings.classification == classification
        portionMarkings.fgiAuthority == fgiAuthority

        where:
        markings                | classification | fgiAuthority
        '//NS//ATOMAL'          | SECRET         | 'NATO'
        '//DEU R'               | RESTRICTED     | 'DEU'
    }

    @Unroll
    def 'test joint markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.type == JOINT
        portionMarkings.classification == classification
        portionMarkings.jointAuthorities.containsAll(jointAuthorities)
        portionMarkings.jointAuthorities.first() == firstAuthority

        where:
        markings                                      | classification | jointAuthorities               | firstAuthority
        '//JOINT S USA CAN'                           | SECRET         | ['CAN', 'USA']                 | 'CAN'
        '//JOINT TS CAN DEU USA'                      | TOP_SECRET     | ['CAN', 'DEU', 'USA']          | 'CAN'
        '//JOINT S CAN DEU USA NATO//TK//RELIDO'      | SECRET         | ['CAN', 'DEU', 'USA', 'NATO']  | 'CAN'
    }

    def 'U//FOUO'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings("U//FOUO")

        then:
        portionMarkings.classification == UNCLASSIFIED
        portionMarkings.disseminationControls.contains(FOUO)
    }

    def 'Invalid FOUO markings'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                    | validationParaRefs
        'C//FOUO'                   | ['10.b.1.']
        'S//FOUO'                   | ['10.b.1.']
        'TS//FOUO'                  | ['10.b.1.']
    }

    def 'test sci controls only'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.classification == classification
        portionMarkings.sciControls.size() == sciControl.size()

        for (SciControl control : portionMarkings.sciControls) {
            def expectedControl = sciControl.find { it.control == control.control }

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
        'S//HCS//NF'                                            | SECRET         |
                [[control: 'HCS', compartments: [:]]]
        'S//HCS-XXX//NF'                                        | SECRET         |
                [[control: 'HCS', compartments: ['XXX': []]]]
        'S//HCS-XXX-G//NF'                                      | SECRET         |
                [[control: 'HCS', compartments: ['XXX': [], 'G': []]]]
        'S//HCS-XXX/COMINT/TK-ABC-XYZ HELLO WORLD//NF'          | SECRET         |
                [[control: 'HCS', compartments: ['XXX': []]], [control: 'COMINT', compartments: [:]],
                 [control: 'TK', compartments: ['ABC': [], 'XYZ': ['HELLO', 'WORLD']]]]
        'TS//TK-ABC X Y Z/COMINT//OC'                           | TOP_SECRET     |
                [[control: 'TK', compartments: ['ABC': ['X', 'Y', 'Z']]], [control: 'COMINT', compartments: [:]]]
    }

    def 'test sap markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.classification == classification
        portionMarkings.sapControl.multiple == multiple
        portionMarkings.sapControl.programs.size() == programs.size()
        portionMarkings.sapControl.programs.containsAll(programs)
        portionMarkings.sapControl.hvsaco == hvsaco

        where:
        markings                                               | classification |
                multiple | programs             | hvsaco
        'TS//SAR-BUTTERED POPCORN'                             | TOP_SECRET     |
                false    | ['BUTTERED POPCORN'] | false
        'TS//SAR-BP'                                           | TOP_SECRET     |
                false    | ['BP']               | false
        'S//SAR-BP'                                            | SECRET         |
                false    | ['BP']               | false
        'S//TK//SAR-BP/GB/TC//NF'                              | SECRET         |
                false    | ['BP', 'GB', 'TC']   | false
        'S//SAR-MULTIPLE PROGRAMS'                             | SECRET         |
                true     | []                   | false
        'S//HVSACO'                                            | SECRET         |
                false    | []                   | true
    }

    def 'test sap waived'() {
        setup:
        def markings = 'TS//SAR-BP//WAIVED'

        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.classification == TOP_SECRET
        !portionMarkings.sapControl.multiple
        portionMarkings.sapControl.programs.size() == 1
        portionMarkings.sapControl.programs.containsAll(['BP'])
        portionMarkings.disseminationControls.size() == 1
        portionMarkings.disseminationControls.containsAll([WAIVED])
    }

    def 'test atomic energy markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.aeaMarking.type == type
        portionMarkings.aeaMarking.criticalNuclearWeaponDesignInformation == cnwdi
        portionMarkings.aeaMarking.sigmas.size() == sigmas.size()
        portionMarkings.aeaMarking.sigmas.containsAll(sigmas)

        where:
        markings                                                    | type      | cnwdi | sigmas
        'TS//RD'                                                    | RD        | false | []
        'TS//FRD'                                                   | FRD       | false | []
        'S//RD'                                                     | RD        | false | []
        'S//TK//RD-N//NF'                                           | RD        | true  | []
        'S//RD-N'                                                   | RD        | true  | []
        'S//RD-SG 1 2'                                              | RD        | false | [1, 2]
        'S//FRD-SG 14'                                              | FRD       | false | [14]
        'U//DCNI'                                                   | DOD_UCNI  | false | []
        'U//UCNI'                                                   | DOE_UCNI  | false | []
    }

    def 'test ucni markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.dodUcni == dod
        portionMarkings.doeUcni == doe

        where:
        markings            | dod   | doe
        'U//DCNI'           | true  | false
        'U//UCNI'           | false | true
        'S//FRD-SG 14'      | false | false
    }

    def 'test usfgi markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.usFgiCountryCodes != null
        portionMarkings.usFgiCountryCodes.size() == countryCodes.size()
        portionMarkings.usFgiCountryCodes.containsAll(countryCodes)

        where:
        markings                       | countryCodes
        'TS//FGI DEU GBR'              | ['DEU', 'GBR']
        'TS//FGI DEU GBR NATO'         | ['DEU', 'GBR', 'NATO']
        'S//TK//FGI//RELIDO'           | []
    }

    def 'test dissemination markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.disseminationControls.size() == dissem.size();
        portionMarkings.disseminationControls.containsAll(dissem)

        portionMarkings.otherDissemControl.size() == otherDissem.size();
        portionMarkings.otherDissemControl.containsAll(otherDissem)

        portionMarkings.accm.size() == accm.size()
        portionMarkings.accm.containsAll(accm)

        where:
        markings                                                  | dissem                  |
                otherDissem          | accm
        'S//NF/DSEN'                                              | [NOFORN, DEA_SENSITIVE] |
                []                   | []
        'S//TK//NF/DSEN'                                          | [NOFORN, DEA_SENSITIVE] |
                []                   | []
        'S//OC/PR/NF'                                             | [NOFORN, ORCON, PROPIN] |
                []                   | []
        'S//DSEN'                                                 | [DEA_SENSITIVE]         |
                []                   | []
        'S//DSEN//DS/'                                            | [DEA_SENSITIVE]         |
                [LIMDIS]             | []
        'S//DSEN/NF//SBU-NF'                                      | [NOFORN, DEA_SENSITIVE] |
                [SBU_NOFORN]         | []
        'S//XD'                                                   | []                      |
                [EXDIS]              | []
        'S//DS/ACCM-FOOBAR'                                       | []                      |
                [LIMDIS]             | ['FOOBAR']
        'S//DS/ACCM-FOOBAR'                                       | []                      |
                [LIMDIS]             | ['FOOBAR']
        'S//DS/ACCM-FOOBAR/BAZ'                                   | []                      |
                [LIMDIS]             | ['FOOBAR', 'BAZ']
        'S//DS/ACCM-FOOBAR/BAZ/SBU-NF'                            | []                      |
                [LIMDIS, SBU_NOFORN] | ['FOOBAR', 'BAZ']
        'S//ACCM-FOOBAR/BAZ/DS/SBU-NF'                            | []                      |
                [LIMDIS, SBU_NOFORN] | ['FOOBAR', 'BAZ']
    }

    def 'test relto and display only markings'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.usFgiCountryCodes.size() == countryCodes.size()
        portionMarkings.usFgiCountryCodes.containsAll(countryCodes)
        portionMarkings.relTo.size() == relTo.size()
        portionMarkings.relTo.containsAll(relTo)
        portionMarkings.displayOnly.size() == displayTo.size()
        portionMarkings.displayOnly.containsAll(displayTo)

        where:
        markings                                                    | countryCodes           |
                relTo                  | displayTo
        'TS//FGI DEU GBR//REL TO USA, DEU, NATO'                    | ['DEU', 'GBR']         |
                ['DEU', 'NATO', 'USA'] | []
        'TS//FGI DEU GBR NATO//REL TO USA, DEU/DISPLAY ONLY NZL'    | ['DEU', 'GBR', 'NATO'] |
                ['DEU', 'USA']         | ['NZL']
        'S//TK//FGI//DISPLAY ONLY AFG, NZL'                         | []                     |
                []                     | ['NZL', 'AFG']
        'S//TK//FGI//NF'                                            | []                     |
                []                     | []
    }

    def 'test complex marking'() {
        setup:
        def markings = 'TS//ABC/COMINT/SI-G-XXX/FOO-O XYZ//SAR-BP/GB/TC//RD-SG 2 5 6//FGI CAN NZL//' +
                'IMC/PR/REL TO USA, CAN, NZL/DISPLAY ONLY AFG/DSEN//' +
                'DS/ACCM-LEMONADE/PARTY FAVORS/SBU-NF'

        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        for (SciControl sci : portionMarkings.sciControls) {
            StringBuilder sb = new StringBuilder(sci.getControl());
            for (Map.Entry<String, List<String>> comp : sci.getCompartments()
                    .entrySet()) {
                sb.append("-").append(comp.getKey());
                for (String subComp : comp.getValue()) {
                    sb.append(" ").append(subComp);
                }
            }
        }

        portionMarkings.classification == TOP_SECRET
        portionMarkings.sciControls.size() == 4
        for (SciControl sciControl : portionMarkings.sciControls) {
            assert ['ABC', 'COMINT', 'SI', 'FOO'].contains(sciControl.control)
            if (sciControl.control in ['ABC', 'COMINT']) {
                assert sciControl.compartments.isEmpty()
            }
            if (sciControl.control == 'SI') {
                assert sciControl.compartments.size() == 2
                assert sciControl.compartments.keySet().containsAll(['XXX', 'G'])
                sciControl.compartments.values().every({ subComp -> assert subComp.empty })
            }
            if (sciControl.control == 'FOO') {
                assert sciControl.compartments.size() == 1
                assert sciControl.compartments.get('O').size() == 1
                assert sciControl.compartments.get('O').containsAll(['XYZ'])
            }
        }

        !portionMarkings.sapControl.multiple
        portionMarkings.sapControl.programs.size() == 3
        portionMarkings.sapControl.programs.containsAll(['BP', 'GB', 'TC'])

        portionMarkings.aeaMarking.type == RD
        portionMarkings.aeaMarking.sigmas.size() == 3
        portionMarkings.aeaMarking.sigmas.containsAll([2, 5, 6])

        portionMarkings.usFgiCountryCodes.size() == 2
        portionMarkings.usFgiCountryCodes.containsAll(['NZL', 'CAN'])

        portionMarkings.displayOnly.size() == 1
        portionMarkings.displayOnly.containsAll(['AFG'])

        portionMarkings.disseminationControls.size() == 3
        portionMarkings.disseminationControls.containsAll([IMCON, PROPIN, DEA_SENSITIVE])

        portionMarkings.accm.size() == 2
        portionMarkings.accm.containsAll(['LEMONADE', 'PARTY FAVORS'])

        portionMarkings.otherDissemControl.size() == 2
        portionMarkings.otherDissemControl.containsAll([LIMDIS, SBU_NOFORN])
    }

    @Unroll
    def 'test fgi/joint validations'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                            | validationParaRefs
        '//DEU TS//BOHEMIA'                 | ['4.b.2.c.']
        '//CTS//BALK//NF'                   | ['4.b.3.']
        '//NATO S//ATOMAL//NF'              | ['4.b.3.']
        '//COSMIC S'                        | ['4.b.2.a.']
        '//NATO TS'                         | ['4.b.2.a.']
        '//NATO S//BALK'                    | ['4.b.2.c.']
        '//NATO S//BOHEMIA//NF'             | ['4.b.2.c.', '4.b.3.']
        '//JOINT R CAN USA'                 | ['5.d.']
    }

    @Unroll
    def 'test sci validation passes'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.classification == classification

        where:
        markings                                | classification
        'TS//HCS-X ABC DEF//NF'                 | TOP_SECRET
        'S//KLONDIKE//NF'                       | SECRET
        'S//MADE-UP-SCI//REL TO USA CAN'        | SECRET
        'S//MADE-UP-SCI//DISPLAY ONLY AFG'      | SECRET
        'S//MADE-UP-SCI//RELIDO'                | SECRET
        'S//MADE-UP-SCI//OC'                    | SECRET
        'S//MADE-UP-SCI//NF'                    | SECRET
    }

    @Unroll
    def 'test sci validation fails'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                    | validationParaRefs
        'TS//HCS-X ABC DEF'         | ['6.f.']
        'S//KLONDIKE//OC'           | ['6.f.']
        'S//MADE-UP-SCI'            | ['6.c.']
    }

    def 'test sap validation passes'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.classification == classification

        where:
        markings                                                | classification
        'TS//SAR-BUTTERED POPCORN/AA/BB/CC'                     | TOP_SECRET
        'TS//SAR-BUTTERED POPCORN/AA/BB/CC'                     | TOP_SECRET
        'TS//SAR-BUTTERED POPCORN/AA/BB/CC//WAIVED'             | TOP_SECRET
    }

    def 'test sap validation fails'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                                                        | validationParaRefs
        'TS//SAR-BUTTERED POPCORN/AA/BB/CC/DD'                          | ['7.e.']
        'TS//SAR-BUTTERED POPCORN/AA/BB/CC/DD'                          | ['7.e.']
        'S//WAIVED'                                                     | ['7.f.']
    }

    def 'test aea validation passes'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.classification == classification

        where:
        markings             | classification
        'C//RD-N'            | CONFIDENTIAL
        'TS//RD-N'           | TOP_SECRET
        'TS//RD-SG 1 12 40'  | TOP_SECRET
    }

    def 'test aea validation fails'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                        | validationParaRefs
        'R//RD'                         | ['8.a.4.']
        'R//RESTRICTED DATA'            | ['8.a.4.']
        'R//FORMERLY RESTRICTED DATA'   | ['8.b.2.']
        'U//FRD'                        | ['8.b.2.']
        'S//FRD-N'                      | ['8.c.3.']
        'TS//FRD-N'                     | ['8.c.3.']
        'C//FRD-SG 1 112 240'           | ['8.d.3.']
    }

    @Unroll
    def 'test fgi validation'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                | validationParaRefs
        '//DEU S//FGI CAN'      | ['9.a.']
        'C//FGI CAN USA'        | ['-']
        'R//FGI CAN'            | ['9.b.']
        'R//FGI CAN USA'        | ['9.b.', '-']
        'S//FGI NZL CAN'        | ['9.d.']
    }

    @Unroll
    def 'test dissemination validation'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                      | validationParaRefs
        'R//OC'                       | ['10.d.3.']
        'C//IMC'                      | ['1.b.', '1.c.']
        'S//IMC/FISA'                 | ['1.c.']
        'R//NF'                       | ['2.c.']
        'C//NF/RELIDO'                | ['2.d.']
        'R//PR'                       | ['3.b.']
        'C//NF/RELIDO'                | ['2.d.']
        'R//RELIDO'                   | ['4.c.']
    }

    def 'test relto and displayonly validation'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                          | validationParaRefs
        'R//REL TO CAN'                   | ['10.e.3.']
        'C//REL TO USA'                   | ['10.e.5.']
        'S//NF/REL TO CAN'                | ['2.d.', '10.e.7.']
        'S//REL TO CAN, USA, GBR'         | ['10.e.4.']
        'R//DISPLAY ONLY AFG'             | ['10.g.3.']
        'S//NF/DISPLAY ONLY AFG'          | ['10.g.4.']
        'S//RELIDO/DISPLAY ONLY AFG'      | ['10.g.4.']
        'S//DISPLAY ONLY NZL, AFG'        | ['10.g.5.']
    }

    def 'test other dissem validation'() {
        when:
        PortionMarkings.parseMarkings(markings)

        then:
        def ex = thrown(MarkingsValidationException)
        def paraRefs = ex.errors.stream()
                .map({ v -> v.getParagraph() })
                .collect(Collectors.toSet())
        paraRefs.size() == validationParaRefs.size()
        paraRefs.containsAll(validationParaRefs)

        where:
        markings                    | validationParaRefs
        'S//XD/ND'                  | ['1.d.']
        'S//REL TO CAN//XD'         | ['1.c.']
        'S//REL TO CAN//ND'         | ['2.d.']
    }

    def 'test other portion marking classification'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.classification == classification

        portionMarkings.disseminationControls.size() == dissem.size();
        portionMarkings.disseminationControls.containsAll(dissem)

        where:
        markings                        | classification    | dissem
        'TS//TK//IMC/NF'                | TOP_SECRET        | [IMCON, NOFORN]
        'TS////NF'                      | TOP_SECRET        | [NOFORN]
        'TS//SI/TK//RS//IMC/NF'         | TOP_SECRET        | [IMCON, NOFORN]
    }

    def 'test ucni'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.aeaMarking.type == type
        portionMarkings.dodUcni == dodUcni
        portionMarkings.doeUcni == doeUcni

        where:
        markings                                                    | type      | dodUcni   | doeUcni
        'TS//FRD'                                                   | FRD       | false     | false
        'U//DCNI'                                                   | DOD_UCNI  | true      | false
        'U//UCNI'                                                   | DOE_UCNI  | false     | true
    }

    def 'test null ucni marking'() {
        when:
        def portionMarkings = PortionMarkings.parseMarkings(markings)

        then:
        portionMarkings.dodUcni == dodUcni
        portionMarkings.doeUcni == doeUcni

        where:
        markings                                                    | dodUcni   | doeUcni
        'TS'                                                        | false     | false
    }
}
