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

import ddf.catalog.data.Attribute
import ddf.catalog.data.Metacard
import ddf.catalog.data.impl.AttributeImpl
import ddf.catalog.data.impl.MetacardImpl
import ddf.catalog.data.impl.MetacardTypeImpl
import org.codice.alliance.catalog.core.api.types.Security
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import spock.lang.Specification
import spock.lang.Unroll

import static org.codice.alliance.security.banner.marking.ClassificationLevel.*

@RunWith(JUnitPlatform.class)
class BannerCommonMarkingExtractorSpec extends Specification {
    private BannerCommonMarkingExtractor extractor
    private Metacard metacard
    private BannerMarkings bannerMarkings
    private Exception validationException

    void setup() {
        extractor = new BannerCommonMarkingExtractor()
        metacard = new MetacardImpl(new MetacardTypeImpl("BCMETest_metacard", extractor.metacardAttributes))
    }

    def 'test list deduper'() {
        when:
        def dedupedList = extractor.dedupedList(listA, listB)

        then:
        dedupedList.size() == result.size()
        dedupedList.containsAll(result)

        where:
        listA                           | listB                              || result
        ['hello', 'world', 'goodnight'] | ['world', 'goodnight', 'columbus'] || ['hello', 'world', 'goodnight', 'columbus']
        []                              | ['hello', 'world']                 || ['hello', 'world']
        ['hello']                       | []                                 || ['hello']
        []                              | []                                 || []
        ['hello', 'world']              | ['goodnight', 'columbus']          || ['hello', 'world', 'goodnight', 'columbus']
    }

    def 'test metacard attribute retrieval'() {
        when:
        def attributes = extractor.getMetacardAttributes()
        def attributeNames = [Security.CLASSIFICATION, Security.RELEASABILITY, Security.CODEWORDS,
                              Security.DISSEMINATION_CONTROLS, Security.OWNER_PRODUCER,
                              Security.CLASSIFICATION_SYSTEM]

        then:
        attributes*.name.containsAll(attributeNames)
    }

    @Unroll
    def 'test classification translator'() {
        when:
        def translatedClassification =
                extractor.translateClassification(classification, isNato, natoQual)

        then:
        translatedClassification == output

        where:
        classification | isNato | natoQual  || output
        UNCLASSIFIED   | false  | null      || 'U'
        RESTRICTED     | false  | null      || 'R'
        CONFIDENTIAL   | false  | null      || 'C'
        SECRET         | false  | null      || 'S'
        TOP_SECRET     | false  | null      || 'TS'
        UNCLASSIFIED   | true   | null      || 'NU'
        RESTRICTED     | true   | null      || 'NR'
        CONFIDENTIAL   | true   | null      || 'NC'
        SECRET         | true   | null      || 'NS'
        TOP_SECRET     | true   | null      || 'CTS'
        CONFIDENTIAL   | true   | 'ATOMAL'  || 'NCA'
        SECRET         | true   | 'ATOMAL'  || 'NSAT'
        TOP_SECRET     | true   | 'ATOMAL'  || 'CTSA'
        TOP_SECRET     | true   | 'BOHEMIA' || 'CTS-B'
        TOP_SECRET     | true   | 'BALK'    || 'CTS-BALK'
    }

    @Unroll
    def 'test class markings processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processClassMarking(metacard, bannerMarkings)
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
        markings                         | validationError | attributeValue
        'SECRET//TK//NOFORN'             | false           | ['S']
        'TOP SECRET'                     | false           | ['TS']
        'UNKNOWN CLASSIFICATION'         | true            | null
        'COSMIC TOP SECRET'              | true            | null
        'CAN RESTRICTED'                 | true            | null
        '//NATO CONFIDENTIAL'            | false           | ['NC']
        '//NATO CONFIDENTIAL//ATOMAL'    | false           | ['NCA']
        '//NATO SECRET'                  | false           | ['NS']
        '//NATO SECRET//ATOMAL'          | false           | ['NSAT']
        '//COSMIC TOP SECRET'            | false           | ['CTS']
        '//COSMIC TOP SECRET//ATOMAL'    | false           | ['CTSA']
        '//COSMIC TOP SECRET//BALK'      | false           | ['CTS-BALK']
        '//COSMIC TOP SECRET//BOHEMIA'   | false           | ['CTS-B']
        '//COSMIC SECRET'                | true            | null
        '//DEU SECRET'                   | false           | ['S']
        '//DEU'                          | true            | null
        '//JOINT RESTRICTED CAN GBR'     | false           | ['R']
        '//JOINT SECRET CAN GBR'         | false           | ['S']
        '//JOINT TOP SECRET CAN DEU USA' | false           | ['TS']
    }

    @Unroll
    def 'test releasability processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processReleasability(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            validationException != null
            attribute == null
        } else {
            if (attributeValue) {
                attribute.values.size() == attributeValue.size()
                attribute.values.containsAll(attributeValue)
            } else {
                attribute == null
            }
        }

        where:
        markings                           | validationError | attributeValue
        'SECRET//TK//REL TO CAN, DEU, NZL' | false           | ['CAN', 'DEU', 'NZL']
        'SECRET//TK/NOFORN'                | false           | null
        'SECRET//REL TO CAN, USA, DEU'     | true            | null
        'SECRET//REL TO USA, CAN, DEU'     | false           | ['USA', 'CAN', 'DEU']
        'SECRET//REL TO USA'               | true            | null
    }

    @Unroll
    def 'test codewords processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processCodewords(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            if (attributeValue) {
                assert attribute.values.size() == attributeValue.size()
                assert attribute.values.containsAll(attributeValue)
            } else {
                attribute == null
            }
        }

        where:
        markings                                     | validationError | attributeValue
        'SECRET//TK//REL TO CAN, DEU, NZL'           | false           | ['TK']
        'SECRET//TK//NOFORN'                         | false           | ['TK']
        'SECRET//TK'                                 | true            | null
        'SECRET//HCS-O//REL TO CAN'                  | true            | null
        'SECRET//SI-GAMMA//REL TO USA, CAN, DEU'     | false           | ['SI-GAMMA']
        'SECRET//SI-GAMMA XYZ//REL TO USA, CAN, DEU' | false           | ['SI-GAMMA XYZ']
        'SECRET//SI-GAMMA-X//REL TO CAN'             | false           | ['SI-GAMMA', 'SI-X']
        'SECRET//SI-GAMMA-X ABC DEF//REL TO CAN'     | false           | ['SI-GAMMA', 'SI-X ABC DEF']
    }

    @Unroll
    def 'test dissemination processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processDissem(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            if (attributeValue) {
                assert attribute.values.size() == attributeValue.size()
                assert attribute.values.containsAll(attributeValue)
            } else {
                attribute == null
            }
        }

        where:
        markings                | validationError | attributeValue
        'SECRET//NOFORN'        | false           | ['NOFORN']
        'SECRET//NOFORN/PROPIN' | false           | ['NOFORN', 'PROPIN']
        'SECRET//ORCON'         | false           | ['ORCON']
        'RESTRICTED//ORCON'     | true            | null
        'SECRET//DEA SENSITIVE' | false           | ['DEA SENSITIVE']
    }

    @Unroll
    def 'test owner-producer processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processOwnerProducer(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            if (attributeValue) {
                assert attribute.values.size() == attributeValue.size()
                assert attribute.values.containsAll(attributeValue)
            } else {
                attribute == null
            }
        }

        where:
        markings                       | validationError | attributeValue
        'SECRET//NOFORN'               | false           | ['USA']
        '//NATO SECRET'                | false           | ['NATO']
        '//NATO SECRET//ATOMAL'        | false           | ['NATO']
        '//GBR SECRET'                 | false           | ['GBR']
        '//COSMIC TOP SECRET'          | false           | ['NATO']
        '//COSMIC TOP SECRET//ATOMAL'  | false           | ['NATO']
        '//COSMIC TOP SECRET//BALK'    | false           | ['NATO']
        '//COSMIC TOP SECRET//BOHEMIA' | false           | ['NATO']
        '//COSMIC SECRET//ATOMAL'      | true            | null
        '//COSMIC SECRET//BALK'        | true            | null
        '//COSMIC SECRET//BOHEMIA'     | true            | null
        '//COSMIC RESTRICTED//BOHEMIA' | true            | null
    }

    /**
     * At this time, classification-system and owner-producer are using the same underlying
     * logic and returning the same attribute values. That may change in the future.
     */
    @Unroll
    def 'test classification-system processor'() {
        when:
        initBannerMarkings(markings)
        def attribute = null
        if (bannerMarkings) {
            attribute = extractor.processClassSystem(metacard, bannerMarkings)
        }

        then:
        if (validationError) {
            assert validationException != null
            assert attribute == null
        } else {
            if (attributeValue) {
                assert attribute.values.size() == attributeValue.size()
                assert attribute.values.containsAll(attributeValue)
            } else {
                attribute == null
            }
        }

        where:
        markings                       | validationError | attributeValue
        'SECRET//NOFORN'               | false           | ['USA']
        '//NATO SECRET'                | false           | ['NATO']
        '//NATO SECRET//ATOMAL'        | false           | ['NATO']
        '//GBR SECRET'                 | false           | ['GBR']
        '//COSMIC TOP SECRET'          | false           | ['NATO']
        '//COSMIC TOP SECRET//ATOMAL'  | false           | ['NATO']
        '//COSMIC TOP SECRET//BALK'    | false           | ['NATO']
        '//COSMIC TOP SECRET//BOHEMIA' | false           | ['NATO']
        '//COSMIC SECRET//ATOMAL'      | true            | null
        '//COSMIC SECRET//BALK'        | true            | null
        '//COSMIC SECRET//BOHEMIA'     | true            | null
        '//COSMIC RESTRICTED//BOHEMIA' | true            | null
    }

    def 'test security conflict check empty attr'() {
        when:
        initBannerMarkings("TOP SECRET")
        metacard.setAttribute(new AttributeImpl(Security.CLASSIFICATION, "      "))
        validationException = null
        Attribute result = extractor.processClassMarking(metacard, bannerMarkings)

        then:
        result.getValue() == "TS"
    }

    def 'test security conflict classification'() {
        when:
        initBannerMarkings("TOP SECRET")
        metacard.setAttribute(new AttributeImpl(Security.CLASSIFICATION, "SECRET"))
        validationException = null
        extractor.processClassMarking(metacard, bannerMarkings)

        then:
        thrown(MarkingMismatchException.class)
    }

    def 'test security conflict releasability'() {
        when:
        initBannerMarkings("SECRET//TK//REL TO CAN, DEU, NZL")
        metacard.setAttribute(new AttributeImpl(Security.RELEASABILITY, "GBR"))
        validationException = null
        extractor.processReleasability(metacard, bannerMarkings)

        then:
        thrown(MarkingMismatchException.class)
    }

    def 'test security conflict codewords'() {
        when:
        initBannerMarkings("SECRET//TK//REL TO CAN, DEU, NZL")
        metacard.setAttribute(new AttributeImpl(Security.CODEWORDS, "ORCON"))
        validationException = null
        extractor.processCodewords(metacard, bannerMarkings)

        then:
        thrown(MarkingMismatchException.class)
    }

    def 'test security conflict dissemination'() {
        when:
        initBannerMarkings("SECRET//NOFORN/PROPIN")
        metacard.setAttribute(new AttributeImpl(Security.DISSEMINATION_CONTROLS, "GBR"))
        validationException = null
        extractor.processDissem(metacard, bannerMarkings)

        then:
        thrown(MarkingMismatchException.class)
    }

    def 'test security conflict owner producer'() {
        when:
        initBannerMarkings("//COSMIC TOP SECRET//BALK")
        metacard.setAttribute(new AttributeImpl(Security.OWNER_PRODUCER, "USA"))
        validationException = null
        extractor.processOwnerProducer(metacard, bannerMarkings)

        then:
        thrown(MarkingMismatchException.class)
    }

    def 'test security conflict classification system'() {
        when:
        initBannerMarkings("//COSMIC TOP SECRET//BALK")
        metacard.setAttribute(new AttributeImpl(Security.CLASSIFICATION_SYSTEM, "USA"))
        validationException = null
        extractor.processClassSystem(metacard, bannerMarkings)

        then:
        thrown(MarkingMismatchException.class)
    }

    private def initBannerMarkings(String markings) {
        try {
            bannerMarkings = BannerMarkings.parseMarkings(markings)
        } catch (Exception exception) {
            validationException = exception
        }
    }
}
