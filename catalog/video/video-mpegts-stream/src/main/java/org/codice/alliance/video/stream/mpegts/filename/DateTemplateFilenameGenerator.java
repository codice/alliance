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
package org.codice.alliance.video.stream.mpegts.filename;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Date;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces one or more tokens within a filename with the current time. The format of the token is
 * <code>%{date=FMT}</code>, where <code>FMT</code> is format string defined by
 * SimpleDateFormat.
 */
public class DateTemplateFilenameGenerator extends BaseFilenameGenerator {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DateTemplateFilenameGenerator.class);

    private static final String PREFIX = "%{date=";

    private static final String SUFFIX = "}";

    private static final String REGEX = ".*" + Pattern.quote(PREFIX) + "([^}]+)" + Pattern.quote(
            SUFFIX) + ".*";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * This capture group number must agree with the groupings in {@link #REGEX}.
     */
    private static final int CAPTURE_GROUP = 1;

    /**
     * By default, new Date objects are created by calling {@link Date#Date()}. This can
     * be changed by setting a new {@link Supplier} with {@link #setDateSupplier(Supplier)}.
     */
    private Supplier<Date> dateSupplier = Date::new;

    /**
     * Set an alternative DateSupplier to the default.
     *
     * @param dateSupplier must be non-null
     */
    public void setDateSupplier(Supplier<Date> dateSupplier) {
        notNull(dateSupplier, "dateSupplier must be non-null");
        this.dateSupplier = dateSupplier;
    }

    @Override
    protected String doGenerateFilename(String baseFilename) {

        String tmp = baseFilename;

        Matcher m = PATTERN.matcher(tmp);
        while (m.matches()) {
            String fmt = m.group(CAPTURE_GROUP);
            String formattedDate = FastDateFormat.getInstance(fmt)
                    .format(dateSupplier.get());
            String newBaseFilename = tmp.replace(PREFIX + fmt + SUFFIX, formattedDate);
            if (newBaseFilename.equals(tmp)) {
                LOGGER.debug("failed to replace date tokens: baseFilename={}", baseFilename);
                return tmp;
            }
            tmp = newBaseFilename;
            m = PATTERN.matcher(tmp);
        }

        return tmp;
    }

    @Override
    public String toString() {
        return "DateTemplateFilenameGenerator{}";
    }
}
