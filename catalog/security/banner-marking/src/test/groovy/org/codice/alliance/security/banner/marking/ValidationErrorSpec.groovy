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

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import spock.lang.Specification

@RunWith(JUnitPlatform.class)
class ValidationErrorSpec extends Specification {
    def 'test ctors'() {
        expect:
        new ValidationError('message', 'appendix', 'para').message == 'message'
        new ValidationError('message', 'appendix', 'para').appendix == 'appendix'
        new ValidationError('message', 'appendix', 'para').paragraph == 'para'
        new ValidationError('message', 'para').message == 'message'
        new ValidationError('message', 'para').appendix == ''
        new ValidationError('message', 'para').paragraph == 'para'
        new ValidationError('message').message == 'message'
        new ValidationError('message').appendix == ''
        new ValidationError('message').paragraph == '-'
    }

    def 'test toString'() {
        expect:
        new ValidationError('msg', 'apx', 'para').toString() ==
                '{msg: DoD MANUAL NUMBER 5200.01, Volume 2, Enc 4, Appendix apx, Para para}'
        new ValidationError('msg', 'para').toString() ==
                '{msg: DoD MANUAL NUMBER 5200.01, Volume 2, Enc 4, Para para}'
        new ValidationError('msg').toString() ==
                '{msg: DoD MANUAL NUMBER 5200.01, Volume 2, Enc 4}'
    }
}
