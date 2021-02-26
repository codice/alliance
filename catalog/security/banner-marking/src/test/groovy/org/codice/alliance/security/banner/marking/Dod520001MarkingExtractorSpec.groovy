/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 **/
package org.codice.alliance.security.banner.marking

import ddf.catalog.data.Metacard
import ddf.catalog.data.impl.MetacardImpl
import ddf.catalog.data.impl.MetacardTypeImpl
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import spock.lang.Specification
import spock.lang.Unroll

@RunWith(JUnitPlatform.class)
class Dod520001MarkingExtractorSpec extends Specification {
    private Dod520001MarkingExtractor extractor
    private Metacard metacard
    private BannerMarkings bannerMarkings
    private Exception validationException

    void setup() {
        extractor = new Dod520001MarkingExtractor()
        metacard = new MetacardImpl(new MetacardTypeImpl("Dod5200Test_metacard",
                extractor.metacardAttributes))
    }

    def 'test metacard attribute retrieval'() {
        when:
        def attributes = extractor.getMetacardAttributes()
        def attributeNames = ['security.dod5200.sap', 'security.dod5200.aea',
                              'security.dod5200.doducni', 'security.dod5200.doeucni',
                              'security.dod5200.fgi', 'security.dod5200.otherDissem']

        then:
        attributes.size() == attributeNames.size()
        attributes*.name.containsAll(attributeNames)
    }

    @Unroll
    def 'test sap processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processSap(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            assert attribute.values.size() == attributeValue.size()
            assert attribute.values.containsAll(attributeValue)
        }

        where:
        markings                                               | validationError | attributeValue
        'TOP SECRET//SPECIAL ACCESS REQUIRED-BUTTERED POPCORN' | false           | ['SAR-BUTTERED POPCORN']
        'TOP SECRET//SAR-BP'                                   | false           | ['SAR-BP']
        'TOP SECRET//SAR-BP/GB/TC'                             | false           | ['SAR-BP/GB/TC']
        'TOP SECRET//SAR-MULTIPLE PROGRAMS'                    | false           | ['SAR-MULTIPLE PROGRAMS']
        'TOP SECRET//SAR-BP/GB/TC/FOO/BAR'                     | true            | null
    }

    @Unroll
    def 'test aea processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processAea(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            assert attribute.values.size() == attributeValue.size()
            assert attribute.values.containsAll(attributeValue)
        }

        where:
        markings                               | validationError | attributeValue
        'TOP SECRET//RESTRICTED DATA'          | false           | ['RESTRICTED DATA']
        'TOP SECRET//FORMERLY RESTRICTED DATA' | false           | ['FORMERLY RESTRICTED DATA']
        'TOP SECRET//RD-N'                     | false           | ['RESTRICTED DATA-N']
        'TOP SECRET//FRD-N'                    | true            | null
        'TOP SECRET//RD-SIGMA 1 2 3'           | false           | ['RESTRICTED DATA-SIGMA 1 2 3']
        'TOP SECRET//FRD-SIGMA 112 113'        | true            | null
    }

    @Unroll
    def 'test dod ucni processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processDodUcni(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            assert attribute.values.size() == attributeValue.size()
            assert attribute.values.containsAll(attributeValue)
        }

        where:
        markings                                                      |
                validationError | attributeValue
        'TOP SECRET//DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION' |
                false           | ['DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION']
    }

    @Unroll
    def 'test doe ucni processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processDoeUcni(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            assert attribute.values.size() == attributeValue.size()
            assert attribute.values.containsAll(attributeValue)
        }

        where:
        markings                                                      |
                validationError | attributeValue
        'UNCLASSIFIED//DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION' |
                false           | ['DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION']
    }

    @Unroll
    def 'test fgi processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processFgi(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            assert attribute.values.size() == attributeValue.size()
            assert attribute.values.containsAll(attributeValue)
        }

        where:
        markings                       | validationError | attributeValue
        'TOP SECRET//FGI DEU GBR'      | false           | ['FGI DEU GBR']
        'TOP SECRET//FGI DEU GBR NATO' | false           | ['FGI DEU GBR NATO']
        'TOP SECRET//FGI GBR DEU'      | true            | null
        'TOP SECRET//FGI'              | false           | ['FGI']
    }

    @Unroll
    def 'test other dissem processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processOtherDissem(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            assert attribute.values.size() == attributeValue.size()
            assert attribute.values.containsAll(attributeValue)
        }

        where:
        markings                                                  | validationError | attributeValue
        'SECRET//DEA SENSITIVE//LIMDIS/'                          | false           | ['LIMDIS']
        'SECRET//DEA SENSITIVE/NOFORN//SBU NOFORN'                | false           | ['SBU NOFORN']
        'SECRET//EXDIS'                                           | false           | ['EXDIS']
        'SECRET//LIMITED DISTRIBUTION/ACCM-FOOBAR'                | false           | ['LIMDIS', 'ACCM-FOOBAR']
        'SECRET//LIMITED DISTRIBUTION/ACCM-BAZ/FOOBAR'            | false           | ['LIMDIS', 'ACCM-BAZ', 'ACCM-FOOBAR']
        'SECRET//ACCM-BAZ/FOOBAR/LIMITED DISTRIBUTION/SBU NOFORN' | false           | ['LIMDIS', 'SBU NOFORN', 'ACCM-BAZ', 'ACCM-FOOBAR']
    }

    private def initBannerMarkings(String markings) {
        try {
            bannerMarkings = BannerMarkings.parseMarkings(markings)
        } catch (Exception exception) {
            validationException = exception
        }
    }
}
