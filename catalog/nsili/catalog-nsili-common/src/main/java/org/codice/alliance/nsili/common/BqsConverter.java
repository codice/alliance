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
package org.codice.alliance.nsili.common;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Metacard;
import ddf.catalog.filter.FilterBuilder;
import ddf.measure.Distance;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.grammar.BqsLexer;
import org.codice.alliance.nsili.common.grammar.BqsListener;
import org.codice.alliance.nsili.common.grammar.BqsParser;
import org.codice.ddf.libs.geo.GeoFormatException;
import org.codice.ddf.libs.geo.util.GeospatialUtil;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BqsConverter {

  private static final String BQS_SHORT_DATE_FORMAT = "yyyy/MM/dd";

  private static final String BQS_FULL_DATE_FORMAT = BQS_SHORT_DATE_FORMAT + " HH:mm:ss[.SSS]";

  private static final DateTimeFormatter LONG_DATE_FORMATTER =
      DateTimeFormatter.ofPattern(BQS_FULL_DATE_FORMAT).withZone(ZoneOffset.UTC);

  private static final DateTimeFormatter SHORT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern(BQS_SHORT_DATE_FORMAT).withZone(ZoneOffset.UTC);

  private FilterBuilder filterBuilder;

  private boolean removeSourceLibrary;

  private static final Logger LOGGER = LoggerFactory.getLogger(BqsConverter.class);

  public BqsConverter(FilterBuilder filterBuilder, boolean removeSourceLibrary) {
    if (filterBuilder == null) {
      throw new IllegalArgumentException("FilterBuilder must be set");
    }

    this.removeSourceLibrary = removeSourceLibrary;
    this.filterBuilder = filterBuilder;
  }

  public Filter convertBQSToDDF(Query query) {
    String bqsQuery = query.bqs_query;
    return convertBQSToDDF(bqsQuery);
  }

  public Filter convertBQSToDDF(String query) {
    query = query.trim();

    LOGGER.debug("Original Query: {}", query);

    ANTLRInputStream inputStream = new ANTLRInputStream(query);
    BqsLexer lex = new BqsLexer(inputStream); // transforms characters into tokens

    CommonTokenStream tokens = new CommonTokenStream(lex); // a token stream
    BqsParser parser = new BqsParser(tokens); // transforms tokens into parse trees
    BqsTreeWalkerListener bqsListener = new BqsTreeWalkerListener(filterBuilder);
    ParseTree tree = parser.query();
    ParseTreeWalker.DEFAULT.walk(bqsListener, tree);

    Filter filter = bqsListener.getFilter();
    if (filter != null && StringUtils.isNotBlank(filter.toString())) {
      LOGGER.debug("Parsed Query: {}", filter);
    } else {
      filter = filterBuilder.attribute(Metacard.ANY_TEXT).is().text("*");
      LOGGER.debug(
          "After parsing filter, didn't have any query parameters. Defaulting to everything search: {}",
          filter);
    }

    return filter;
  }

  class BqsTreeWalkerListener implements BqsListener {
    private Filter currFilter = null;

    private FilterBuilder filterBuilder;

    private String attribute = "";

    private Stack<String> nestedOperatorStack = new Stack<>();

    private Map<String, List<Filter>> filterBy = new HashMap<>();

    private Stack<BqsOperator> bqsOperatorStack = new Stack<>();

    private HashFunction hashFunction;

    private String dateStr = "";

    private String quotedStr = "";

    private String numberStr = "";

    private String builtWkt = "";

    private String upperLeftLatLon[] = new String[2];

    private BqsShape buildingShape = null;

    private double latDecimalDeg = 0.0;

    private double lonDecimalDeg = 0.0;

    private double radiusInMeters;

    private Distance.LinearUnit distanceUnit;

    private double ellipseMajorLenMeters;

    private double ellipseMinorLenMeters;

    private int northAngle;

    double relativeDistInMeters;

    public BqsTreeWalkerListener(FilterBuilder filterBuilder) {
      this.filterBuilder = filterBuilder;
      hashFunction = Hashing.sha512();
    }

    public Filter getFilter() {
      return currFilter;
    }

    @Override
    public void enterQuery(BqsParser.QueryContext ctx) {
      if (!ctx.OR().isEmpty()) {
        bqsOperatorStack.push(BqsOperator.OR);
        print("OR: ");

        String currOperHash = getHash(ctx.getText());
        nestedOperatorStack.push(currOperHash);
        filterBy.put(currOperHash, new ArrayList<>());
      }
    }

    @Override
    public void exitQuery(BqsParser.QueryContext ctx) {
      if (!ctx.OR().isEmpty()) {
        bqsOperatorStack.pop();

        String operHash = nestedOperatorStack.pop();
        List<Filter> filters = filterBy.get(operHash);

        if (filters != null && !filters.isEmpty()) {
          if (currFilter != null) {
            filters.add(currFilter);
          }
          currFilter = filterBuilder.anyOf(filters);
        }
      }
    }

    @Override
    public void enterTerm(BqsParser.TermContext ctx) {
      if (!ctx.AND().isEmpty()) {
        bqsOperatorStack.push(BqsOperator.AND);
        print("AND");

        String currOperHash = getHash(ctx.getText());
        nestedOperatorStack.push(currOperHash);
        filterBy.put(currOperHash, new ArrayList<>());
      }
    }

    @Override
    public void exitTerm(BqsParser.TermContext ctx) {
      if (!ctx.AND().isEmpty()) {
        bqsOperatorStack.pop();

        String operHash = nestedOperatorStack.pop();
        List<Filter> filters = filterBy.get(operHash);
        if (filters != null || !filters.isEmpty()) {
          if (currFilter != null) {
            filters.add(currFilter);
          }
          currFilter = filterBuilder.allOf(filters);
        }
      }
    }

    @Override
    public void enterFactor(BqsParser.FactorContext ctx) {
      if (ctx.NOT() != null) {
        print("NOT");
        bqsOperatorStack.push(BqsOperator.NOT);

        String currOperHash = getHash(ctx.getText());
        nestedOperatorStack.push(currOperHash);
        filterBy.put(currOperHash, new ArrayList<>());
      }
    }

    @Override
    public void exitFactor(BqsParser.FactorContext ctx) {
      if (ctx.NOT() != null) {
        bqsOperatorStack.pop();
        String operHash = nestedOperatorStack.pop();
        List<Filter> filters = filterBy.get(operHash);

        Filter notFilter;
        if (filters.size() > 1) {
          notFilter = filterBuilder.not(filterBuilder.anyOf(filters));
        } else {
          notFilter = filterBuilder.not(filters.iterator().next());
        }

        if (currFilter == null) {
          currFilter = notFilter;
        } else {
          filters.add(currFilter);
          if (!nestedOperatorStack.isEmpty()) {
            List<Filter> parentFilters = filterBy.get(nestedOperatorStack.peek());
            parentFilters.add(notFilter);
          } else {
            LOGGER.debug("Unable to find parent filter for: {}", notFilter);
          }
        }
      }
    }

    @Override
    public void enterPrimary(BqsParser.PrimaryContext ctx) {
      if (ctx.LIKE() != null) {
        print("LIKE");
        bqsOperatorStack.push(BqsOperator.LIKE);
      } else if (ctx.EXISTS() != null) {
        print("EXISTS");
        bqsOperatorStack.push(BqsOperator.EXISTS);
      } else if (ctx.OF() != null) {
        print("OF");
        bqsOperatorStack.push(BqsOperator.OF);
      }
    }

    @Override
    public void exitPrimary(BqsParser.PrimaryContext ctx) {
      if (ctx.LIKE() != null) {
        bqsOperatorStack.pop();
      } else if (ctx.EXISTS() != null) {
        bqsOperatorStack.pop();
      } else if (ctx.OF() != null) {
        bqsOperatorStack.pop();
      }
    }

    @Override
    public void enterAttribute_name(BqsParser.Attribute_nameContext ctx) {
      print(ctx.getText());
      attribute = NsiliAttributeMap.getDdfAttributeForNsili(ctx.getText(), removeSourceLibrary);
    }

    @Override
    public void exitAttribute_name(BqsParser.Attribute_nameContext ctx) {}

    @Override
    public void enterComp_op(BqsParser.Comp_opContext ctx) {
      print(ctx.getText());
      if (ctx.EQUAL() != null) {
        bqsOperatorStack.push(BqsOperator.EQUAL);
      } else if (ctx.NOTEQ() != null) {
        bqsOperatorStack.push(BqsOperator.NOTEQ);
      } else if (ctx.GT() != null) {
        bqsOperatorStack.push(BqsOperator.GT);
      } else if (ctx.GTE() != null) {
        bqsOperatorStack.push(BqsOperator.GTE);
      } else if (ctx.LT() != null) {
        bqsOperatorStack.push(BqsOperator.LT);
      } else if (ctx.LTE() != null) {
        bqsOperatorStack.push(BqsOperator.LTE);
      }
    }

    @Override
    public void exitComp_op(BqsParser.Comp_opContext ctx) {}

    @Override
    public void enterConstant_expression(BqsParser.Constant_expressionContext ctx) {}

    @Override
    public void exitConstant_expression(BqsParser.Constant_expressionContext ctx) {
      BqsOperator bqsOperator = bqsOperatorStack.pop();

      if (!dateStr.isEmpty() && StringUtils.isNotBlank(attribute)) {
        parseDate(bqsOperator);
      } else if (!numberStr.isEmpty() && StringUtils.isNotBlank(attribute)) {
        parseNumber(bqsOperator);
      } else if (!quotedStr.isEmpty() && StringUtils.isNotBlank(attribute)) {
        Filter filter = null;
        quotedStr = normalizeSearchString(quotedStr);

        if (bqsOperator == BqsOperator.NOTEQ) {
          filter = filterBuilder.not(filterBuilder.attribute(attribute).equalTo().text(quotedStr));
        } else if (bqsOperator == BqsOperator.EQUAL) {
          filter = filterBuilder.attribute(attribute).equalTo().text(quotedStr);
        }

        if (filter != null) {
          List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
          filters.add(filter);
        }
      }

      // Reset local variables
      attribute = "";
      dateStr = "";
      numberStr = "";
      quotedStr = "";
    }

    private void parseNumber(BqsOperator bqsOperator) {
      Filter filter = null;
      try {
        Number number = getNumber(numberStr);
        if (number != null) {
          if (number instanceof Short) {
            filter = getShortFilter(bqsOperator, (short) number);
          } else if (number instanceof Long) {
            filter = getLongFilter(bqsOperator, (long) number);
          } else if (number instanceof Integer) {
            filter = getIntFilter(bqsOperator, (int) number);
          } else if (number instanceof Float) {
            filter = getFloatFilter(bqsOperator, (float) number);
          } else if (number instanceof Double) {
            filter = getDoubleFilter(bqsOperator, (double) number);
          } else {
            LOGGER.debug("Number type not handled by filter builder: {}", number.getClass());
          }
        }

        if (filter != null) {
          List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
          filters.add(filter);
        }
      } catch (NumberFormatException e) {
        LOGGER.debug("Unable to convert to a number: {}", numberStr);
      }
    }

    private Filter getDoubleFilter(BqsOperator bqsOperator, double number) {
      Filter filter = null;
      if (bqsOperator == BqsOperator.GTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).greaterThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.GT) {
        filter = filterBuilder.attribute(attribute).greaterThan().number(number);
      } else if (bqsOperator == BqsOperator.LT) {
        filter = filterBuilder.attribute(attribute).lessThan().number(number);
      } else if (bqsOperator == BqsOperator.LTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).lessThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.NOTEQ) {
        filter = filterBuilder.not(filterBuilder.attribute(attribute).equalTo().number(number));
      } else if (bqsOperator == BqsOperator.EQUAL) {
        filter = filterBuilder.attribute(attribute).equalTo().number(number);
      }
      return filter;
    }

    private Filter getFloatFilter(BqsOperator bqsOperator, Float number) {
      Filter filter = null;
      number.getClass().cast(number);
      if (bqsOperator == BqsOperator.GTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).greaterThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.GT) {
        filter = filterBuilder.attribute(attribute).greaterThan().number(number);
      } else if (bqsOperator == BqsOperator.LT) {
        filter = filterBuilder.attribute(attribute).lessThan().number(number);
      } else if (bqsOperator == BqsOperator.LTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).lessThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.NOTEQ) {
        filter = filterBuilder.not(filterBuilder.attribute(attribute).equalTo().number(number));
      } else if (bqsOperator == BqsOperator.EQUAL) {
        filter = filterBuilder.attribute(attribute).equalTo().number(number);
      }
      return filter;
    }

    private Filter getIntFilter(BqsOperator bqsOperator, int number) {
      Filter filter = null;
      if (bqsOperator == BqsOperator.GTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).greaterThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.GT) {
        filter = filterBuilder.attribute(attribute).greaterThan().number(number);
      } else if (bqsOperator == BqsOperator.LT) {
        filter = filterBuilder.attribute(attribute).lessThan().number(number);
      } else if (bqsOperator == BqsOperator.LTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).lessThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.NOTEQ) {
        filter = filterBuilder.not(filterBuilder.attribute(attribute).equalTo().number(number));
      } else if (bqsOperator == BqsOperator.EQUAL) {
        filter = filterBuilder.attribute(attribute).equalTo().number(number);
      }
      return filter;
    }

    private Filter getLongFilter(BqsOperator bqsOperator, long number) {
      Filter filter = null;
      if (bqsOperator == BqsOperator.GTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).greaterThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.GT) {
        filter = filterBuilder.attribute(attribute).greaterThan().number(number);
      } else if (bqsOperator == BqsOperator.LT) {
        filter = filterBuilder.attribute(attribute).lessThan().number(number);
      } else if (bqsOperator == BqsOperator.LTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).lessThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.NOTEQ) {
        filter = filterBuilder.not(filterBuilder.attribute(attribute).equalTo().number(number));
      } else if (bqsOperator == BqsOperator.EQUAL) {
        filter = filterBuilder.attribute(attribute).equalTo().number(number);
      }
      return filter;
    }

    private Filter getShortFilter(BqsOperator bqsOperator, short number) {
      Filter filter = null;
      if (bqsOperator == BqsOperator.GTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).greaterThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.GT) {
        filter = filterBuilder.attribute(attribute).greaterThan().number(number);
      } else if (bqsOperator == BqsOperator.LT) {
        filter = filterBuilder.attribute(attribute).lessThan().number(number);
      } else if (bqsOperator == BqsOperator.LTE) {
        filter =
            filterBuilder.anyOf(
                filterBuilder.attribute(attribute).lessThanOrEqualTo().number(number));
      } else if (bqsOperator == BqsOperator.NOTEQ) {
        filter = filterBuilder.not(filterBuilder.attribute(attribute).equalTo().number(number));
      } else if (bqsOperator == BqsOperator.EQUAL) {
        filter = filterBuilder.attribute(attribute).equalTo().number(number);
      }
      return filter;
    }

    private void parseDate(BqsOperator bqsOperator) {
      Filter filter = null;
      try {
        Date date;
        try {
          TemporalAccessor temporalAccessor = LONG_DATE_FORMATTER.parse(dateStr);
          LocalDateTime dateTime = LocalDateTime.from(temporalAccessor);
          date = new Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
        } catch (DateTimeParseException pe) {
          TemporalAccessor temporalAccessor = SHORT_DATE_FORMATTER.parse(dateStr);
          LocalDate localDate = LocalDate.from(temporalAccessor);
          LocalDateTime dateTime = localDate.atStartOfDay();
          date = new Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
        }

        if (bqsOperator == BqsOperator.GTE) {
          filter =
              filterBuilder.anyOf(
                  filterBuilder.attribute(attribute).after().date(date),
                  filterBuilder.attribute(attribute).equalTo().date(date));
        } else if (bqsOperator == BqsOperator.GT) {
          filter = filterBuilder.attribute(attribute).after().date(date);
        } else if (bqsOperator == BqsOperator.LT) {
          filter = filterBuilder.attribute(attribute).before().date(date);
        } else if (bqsOperator == BqsOperator.LTE) {
          filter =
              filterBuilder.anyOf(
                  filterBuilder.attribute(attribute).before().date(date),
                  filterBuilder.attribute(attribute).equalTo().date(date));
        } else if (bqsOperator == BqsOperator.NOT) {
          filter = filterBuilder.not(filterBuilder.attribute(attribute).equalTo().date(date));
        } else if (bqsOperator == BqsOperator.EQUAL) {
          filter = filterBuilder.attribute(attribute).equalTo().date(date);
        }

        if (filter != null && !nestedOperatorStack.isEmpty()) {
          List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
          filters.add(filter);
        } else {
          currFilter = filter;
        }
      } catch (DateTimeParseException e) {
        LOGGER.debug("Unable to parse date from: {}", dateStr);
      }
    }

    @Override
    public void enterDate(BqsParser.DateContext ctx) {
      print(ctx.getText());
      dateStr = normalizeSearchString(ctx.getText());
    }

    @Override
    public void exitDate(BqsParser.DateContext ctx) {
      // No-Op
    }

    @Override
    public void enterGeo_op(BqsParser.Geo_opContext ctx) {
      if (ctx.INTERSECT() != null) {
        bqsOperatorStack.push(BqsOperator.INTERSECT);
      } else if (ctx.INSIDE() != null) {
        bqsOperatorStack.push(BqsOperator.INSIDE);
      } else if (ctx.OUTSIDE() != null) {
        bqsOperatorStack.push(BqsOperator.OUTSIDE);
      }
    }

    @Override
    public void exitGeo_op(BqsParser.Geo_opContext ctx) {
      // No-Op
    }

    @Override
    public void enterRel_geo_op(BqsParser.Rel_geo_opContext ctx) {
      if (ctx.BEYOND() != null) {
        bqsOperatorStack.push(BqsOperator.BEYOND);
      } else if (ctx.WITHIN() != null) {
        bqsOperatorStack.push(BqsOperator.WITHIN);
      }
    }

    @Override
    public void exitRel_geo_op(BqsParser.Rel_geo_opContext ctx) {
      // No-Op
    }

    @Override
    public void enterDist_units(BqsParser.Dist_unitsContext ctx) {
      print(ctx.getText());

      if (ctx.FEET() != null || ctx.FEET_UPPER() != null) {
        distanceUnit = Distance.LinearUnit.FOOT_U_S;
      } else if (ctx.METERS() != null || ctx.METERS_UPPER() != null) {
        distanceUnit = Distance.LinearUnit.METER;
      } else if (ctx.KILOMETER() != null) {
        distanceUnit = Distance.LinearUnit.KILOMETER;
      } else if (ctx.NAUTICAL_MI() != null) {
        distanceUnit = Distance.LinearUnit.NAUTICAL_MILE;
      } else if (ctx.STATUTE_MI() != null) {
        distanceUnit = Distance.LinearUnit.MILE;
      }

      // We need to parse this if we are doing relative geo queries
      if (bqsOperatorStack.peek() == BqsOperator.WITHIN
          || bqsOperatorStack.peek() == BqsOperator.BEYOND) {
        double parsedDist = Double.parseDouble(numberStr);
        Distance distance = new Distance(parsedDist, distanceUnit);
        relativeDistInMeters = distance.getAs(Distance.LinearUnit.METER);
      }
    }

    @Override
    public void exitDist_units(BqsParser.Dist_unitsContext ctx) {
      // No-Op
    }

    @Override
    public void enterGeo_element(BqsParser.Geo_elementContext ctx) {
      print(ctx.getText());
    }

    @Override
    public void exitGeo_element(BqsParser.Geo_elementContext ctx) {
      BqsOperator operator = bqsOperatorStack.pop();
      boolean shouldNegate = false;
      // Peek and if it's negated pop that one off too
      if (bqsOperatorStack.peek() == BqsOperator.NOT) {
        shouldNegate = true;
      }

      Filter filter = null;

      if (!builtWkt.isEmpty()) {
        // Geo Operators
        if (operator == BqsOperator.INSIDE
            || operator == BqsOperator.INTERSECT
            || operator == BqsOperator.OUTSIDE) {

          filter = getGeoOperatorsFilter(operator);
        } else if (operator == BqsOperator.WITHIN || operator == BqsOperator.BEYOND) {
          // Relative Geo Operators
          filter = getRelativeGeoOperatorsFilter(operator, shouldNegate);
        }

        if (filter != null) {
          List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
          filters.add(filter);
        }
      }

      builtWkt = "";
      upperLeftLatLon = new String[2];
      buildingShape = null;
      radiusInMeters = 0.0;
      latDecimalDeg = 0.0;
      lonDecimalDeg = 0.0;
    }

    private Filter getRelativeGeoOperatorsFilter(BqsOperator operator, boolean shouldNegate) {
      Filter filter = null;
      if (shouldNegate) {
        if (operator == BqsOperator.WITHIN) {
          filter =
              filterBuilder.not(
                  filterBuilder
                      .attribute(attribute)
                      .withinBuffer()
                      .wkt(builtWkt, relativeDistInMeters));
        } else if (operator == BqsOperator.BEYOND) {
          filter =
              filterBuilder.attribute(attribute).withinBuffer().wkt(builtWkt, relativeDistInMeters);
        }
      } else {
        if (operator == BqsOperator.WITHIN) {
          filter =
              filterBuilder.attribute(attribute).withinBuffer().wkt(builtWkt, relativeDistInMeters);
        } else if (operator == BqsOperator.BEYOND) {
          filter =
              filterBuilder.not(
                  filterBuilder
                      .attribute(attribute)
                      .withinBuffer()
                      .wkt(builtWkt, relativeDistInMeters));
        }
      }
      return filter;
    }

    private Filter getGeoOperatorsFilter(BqsOperator operator) {
      Filter filter = null;
      if (buildingShape == BqsShape.CIRCLE) {
        if (operator == BqsOperator.INSIDE) {
          filter = filterBuilder.attribute(attribute).withinBuffer().wkt(builtWkt, radiusInMeters);
        } else if (operator == BqsOperator.INTERSECT) {
          filter = filterBuilder.attribute(attribute).withinBuffer().wkt(builtWkt, radiusInMeters);
        } else if (operator == BqsOperator.OUTSIDE) {
          filter =
              filterBuilder.not(
                  filterBuilder.attribute(attribute).withinBuffer().wkt(builtWkt, radiusInMeters));
        }
      } else {
        if (operator == BqsOperator.INSIDE) {
          filter = filterBuilder.attribute(attribute).within().wkt(builtWkt);
        } else if (operator == BqsOperator.INTERSECT) {
          filter = filterBuilder.attribute(attribute).intersecting().wkt(builtWkt);
        } else if (operator == BqsOperator.OUTSIDE) {
          filter = filterBuilder.not(filterBuilder.attribute(attribute).within().wkt(builtWkt));
        }
      }
      return filter;
    }

    @Override
    public void enterSign(BqsParser.SignContext ctx) {
      // No-Op
    }

    @Override
    public void exitSign(BqsParser.SignContext ctx) {
      // No-Op
    }

    @Override
    public void enterNumber(BqsParser.NumberContext ctx) {
      numberStr = numberStr + ctx.getText();
      print(ctx.getText());
    }

    @Override
    public void exitNumber(BqsParser.NumberContext ctx) {
      // No-Op
    }

    @Override
    public void enterDigit_seq(BqsParser.Digit_seqContext ctx) {
      // No-Op
    }

    @Override
    public void exitDigit_seq(BqsParser.Digit_seqContext ctx) {
      // No-Op
    }

    @Override
    public void enterQuoted_string(BqsParser.Quoted_stringContext ctx) {
      print(ctx.getText());
    }

    @Override
    public void exitQuoted_string(BqsParser.Quoted_stringContext ctx) {
      BqsOperator bqsOperator = bqsOperatorStack.peek();

      String stringValue = normalizeSearchString(ctx.getText());

      Filter filter = null;
      if (StringUtils.isNotBlank(attribute)) {
        if (attribute.equals(MetacardVersion.ACTION)) {
          if (stringValue.equals(NsiliCardStatus.CHANGED)) {
            filter =
                filterBuilder.allOf(
                    filterBuilder
                        .attribute(Metacard.TAGS)
                        .is()
                        .like()
                        .text(MetacardVersion.VERSION_TAG),
                    filterBuilder
                        .attribute(MetacardVersion.VERSION_TAGS)
                        .is()
                        .like()
                        .text(Metacard.DEFAULT_TAG),
                    filterBuilder.anyOf(
                        filterBuilder
                            .attribute(MetacardVersion.ACTION)
                            .is()
                            .like()
                            .text(MetacardVersion.Action.VERSIONED.getKey()),
                        filterBuilder
                            .attribute(MetacardVersion.ACTION)
                            .is()
                            .like()
                            .text(MetacardVersion.Action.VERSIONED_CONTENT.getKey())));
          } else if (stringValue.equals(NsiliCardStatus.OBSOLETE)) {
            filter =
                filterBuilder.allOf(
                    filterBuilder
                        .attribute(Metacard.TAGS)
                        .is()
                        .like()
                        .text(MetacardVersion.VERSION_TAG),
                    filterBuilder
                        .attribute(MetacardVersion.VERSION_TAGS)
                        .is()
                        .like()
                        .text(Metacard.DEFAULT_TAG),
                    filterBuilder
                        .attribute(MetacardVersion.ACTION)
                        .is()
                        .like()
                        .text(MetacardVersion.Action.DELETED.getKey()));
          }
        } else {
          filter = filterBuilder.attribute(attribute).like().text(stringValue);
        }

        if (bqsOperator == BqsOperator.NOTEQ) {
          filter = filterBuilder.not(filter);
        }
      }

      if (filter != null) {
        print("FILTER: " + filter);
        if (!nestedOperatorStack.isEmpty() && filter != null) {
          List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
          filters.add(filter);
        } else {
          currFilter = filter;
        }
      }

      attribute = "";
    }

    @Override
    public void enterLatitude(BqsParser.LatitudeContext ctx) {
      // No-Op
    }

    @Override
    public void exitLatitude(BqsParser.LatitudeContext ctx) {
      latDecimalDeg = Double.parseDouble(ctx.getText());
    }

    @Override
    public void enterLongitude(BqsParser.LongitudeContext ctx) {
      // No-Op
    }

    @Override
    public void exitLongitude(BqsParser.LongitudeContext ctx) {
      lonDecimalDeg = Double.parseDouble(ctx.getText());
    }

    @Override
    public void enterDms(BqsParser.DmsContext ctx) {
      // No-Op
    }

    @Override
    public void exitDms(BqsParser.DmsContext ctx) {
      // No-Op
    }

    @Override
    public void enterLatitude_DMS(BqsParser.Latitude_DMSContext ctx) {
      try {
        latDecimalDeg = GeospatialUtil.parseDMSLatitudeWithDecimalSeconds(ctx.getText());
      } catch (GeoFormatException gfe) {
        LOGGER.debug("Unable to parse DMS latitude: {}", ctx.getText(), gfe);
      }
    }

    @Override
    public void exitLatitude_DMS(BqsParser.Latitude_DMSContext ctx) {
      // No-Op
    }

    @Override
    public void enterLongitude_DMS(BqsParser.Longitude_DMSContext ctx) {
      try {
        lonDecimalDeg = GeospatialUtil.parseDMSLongitudeWithDecimalSeconds(ctx.getText());
      } catch (GeoFormatException gfe) {
        LOGGER.debug("Unable to parse DMS longitude: {}", ctx.getText(), gfe);
      }
    }

    @Override
    public void exitLongitude_DMS(BqsParser.Longitude_DMSContext ctx) {
      // No-Op
    }

    @Override
    public void enterLatlon(BqsParser.LatlonContext ctx) {
      // No-Op
    }

    @Override
    public void exitLatlon(BqsParser.LatlonContext ctx) {
      // No-Op
    }

    @Override
    public void enterCoordinate(BqsParser.CoordinateContext ctx) {
      // No-Op
    }

    @Override
    public void exitCoordinate(BqsParser.CoordinateContext ctx) {
      // Not all shapes are parsed this way, some we get the coordinate from higher level method
      if (buildingShape == BqsShape.POLYGON
          || buildingShape == BqsShape.LINE
          || buildingShape == BqsShape.POINT
          || buildingShape == BqsShape.CIRCLE) {
        if (!builtWkt.isEmpty() && builtWkt.charAt(builtWkt.length() - 1) != '(') {
          builtWkt = builtWkt + "," + lonDecimalDeg + " " + latDecimalDeg;
        } else {
          builtWkt = builtWkt + lonDecimalDeg + " " + latDecimalDeg;
        }
      } else if (buildingShape == BqsShape.ELLIPSE) {
        String[] latLonCoord = ctx.getText().split(",");
        latDecimalDeg = Double.parseDouble(latLonCoord[0]);
        lonDecimalDeg = Double.parseDouble(latLonCoord[1]);
      }
    }

    @Override
    public void enterPoint(BqsParser.PointContext ctx) {
      builtWkt = "POINT(";
      buildingShape = BqsShape.POINT;
    }

    @Override
    public void exitPoint(BqsParser.PointContext ctx) {
      builtWkt = builtWkt + ")";
    }

    @Override
    public void enterPolygon(BqsParser.PolygonContext ctx) {
      builtWkt = "POLYGON((";
      buildingShape = BqsShape.POLYGON;
    }

    @Override
    public void exitPolygon(BqsParser.PolygonContext ctx) {
      builtWkt = builtWkt + "))";
    }

    @Override
    public void enterRectangle(BqsParser.RectangleContext ctx) {
      builtWkt = "POLYGON((";
      buildingShape = BqsShape.RECTANGLE;
    }

    @Override
    public void exitRectangle(BqsParser.RectangleContext ctx) {
      builtWkt = builtWkt + "))";
    }

    @Override
    public void enterUpper_left(BqsParser.Upper_leftContext ctx) {
      // No-Op
    }

    @Override
    public void exitUpper_left(BqsParser.Upper_leftContext ctx) {
      upperLeftLatLon = ctx.getText().split(",");
    }

    @Override
    public void enterLower_right(BqsParser.Lower_rightContext ctx) {
      // No-Op
    }

    @Override
    public void exitLower_right(BqsParser.Lower_rightContext ctx) {
      String[] lowerLeftLatLon = ctx.getText().split(",");

      builtWkt =
          builtWkt
              + upperLeftLatLon[1]
              + " "
              + upperLeftLatLon[0]
              + ","
              + lowerLeftLatLon[1]
              + " "
              + upperLeftLatLon[0]
              + ","
              + lowerLeftLatLon[1]
              + " "
              + lowerLeftLatLon[0]
              + ","
              + upperLeftLatLon[1]
              + " "
              + lowerLeftLatLon[0]
              + ","
              + upperLeftLatLon[1]
              + " "
              + upperLeftLatLon[0];
    }

    @Override
    public void enterCircle(BqsParser.CircleContext ctx) {
      builtWkt = "POINT(";
      buildingShape = BqsShape.CIRCLE;
    }

    @Override
    public void exitCircle(BqsParser.CircleContext ctx) {
      builtWkt = builtWkt + ")";
    }

    @Override
    public void enterRadius(BqsParser.RadiusContext ctx) {
      numberStr = "";
    }

    @Override
    public void exitRadius(BqsParser.RadiusContext ctx) {
      double radius = Double.parseDouble(numberStr);
      Distance distance = new Distance(radius, distanceUnit);
      radiusInMeters = distance.getAs(Distance.LinearUnit.METER);
    }

    @Override
    public void enterEllipse(BqsParser.EllipseContext ctx) {
      builtWkt = "POLYGON(";
      buildingShape = BqsShape.ELLIPSE;
    }

    @Override
    public void exitEllipse(BqsParser.EllipseContext ctx) {
      Coordinate ellipseCenter = new Coordinate(lonDecimalDeg, latDecimalDeg);
      GeometricShapeFactory gsf = new GeometricShapeFactory();
      gsf.setCentre(ellipseCenter);
      gsf.setWidth(ellipseMajorLenMeters / GeoUtils.getLongLengthAtLatitude(latDecimalDeg));
      gsf.setHeight(ellipseMinorLenMeters / GeoUtils.getLatLengthAtLatitude(latDecimalDeg));
      gsf.setNumPoints(30);
      gsf.setRotation(northAngle);
      Polygon ellipse = gsf.createEllipse();
      WKTWriter writer = new WKTWriter();
      builtWkt = writer.write(ellipse);
    }

    @Override
    public void enterMajor_axis_len(BqsParser.Major_axis_lenContext ctx) {
      numberStr = "";
    }

    @Override
    public void exitMajor_axis_len(BqsParser.Major_axis_lenContext ctx) {
      double length = Double.parseDouble(numberStr);
      Distance distance = new Distance(length, distanceUnit);
      ellipseMajorLenMeters = distance.getAs(Distance.LinearUnit.METER);
    }

    @Override
    public void enterMinor_axis_len(BqsParser.Minor_axis_lenContext ctx) {
      numberStr = "";
    }

    @Override
    public void exitMinor_axis_len(BqsParser.Minor_axis_lenContext ctx) {
      double length = Double.parseDouble(numberStr);
      Distance distance = new Distance(length, distanceUnit);
      ellipseMinorLenMeters = distance.getAs(Distance.LinearUnit.METER);
    }

    @Override
    public void enterNorth_angle(BqsParser.North_angleContext ctx) {
      numberStr = "";
    }

    @Override
    public void exitNorth_angle(BqsParser.North_angleContext ctx) {
      northAngle = Integer.parseInt(numberStr);
    }

    @Override
    public void enterLine(BqsParser.LineContext ctx) {
      builtWkt = "LINESTRING(";
      buildingShape = BqsShape.LINE;
    }

    @Override
    public void exitLine(BqsParser.LineContext ctx) {
      builtWkt = builtWkt + ")";
    }

    @Override
    public void enterPolygon_set(BqsParser.Polygon_setContext ctx) {
      // No-Op
    }

    @Override
    public void exitPolygon_set(BqsParser.Polygon_setContext ctx) {
      // No-Op
    }

    @Override
    public void enterHemi(BqsParser.HemiContext ctx) {
      // No-Op
    }

    @Override
    public void exitHemi(BqsParser.HemiContext ctx) {
      // No-Op
    }

    @Override
    public void enterStart_term(BqsParser.Start_termContext ctx) {
      // No-Op
    }

    @Override
    public void exitStart_term(BqsParser.Start_termContext ctx) {
      // No-Op
    }

    @Override
    public void enterSearch_character(BqsParser.Search_characterContext ctx) {
      // No-Op
    }

    @Override
    public void exitSearch_character(BqsParser.Search_characterContext ctx) {
      // No-Op
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
      // No-Op
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
      // No-Op
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
      // No-Op
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
      // No-Op
    }

    private void print(String text) {
      if (LOGGER.isTraceEnabled()) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bqsOperatorStack.size(); i++) {
          sb.append("   ");
        }
        sb.append(text);
        LOGGER.trace(sb.toString());
      }
    }

    private Number getNumber(String numberStr) {
      return NumberUtils.createNumber(numberStr);
    }

    private String normalizeSearchString(String searchString) {
      return searchString.replaceAll("%", "*").replaceAll("'", "");
    }

    private String getHash(String str) {
      return hashFunction.hashBytes(str.getBytes()).toString();
    }
  }
}
