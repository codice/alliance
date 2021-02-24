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
package org.codice.alliance.libs.stanag4609;

import static org.codice.ddf.libs.klv.data.Klv.KeyLength;
import static org.codice.ddf.libs.klv.data.Klv.LengthEncoding;

import com.google.common.io.ByteSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.Klv;
import org.codice.ddf.libs.klv.data.numerical.KlvInt;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;
import org.codice.ddf.libs.klv.data.numerical.KlvLong;
import org.codice.ddf.libs.klv.data.numerical.KlvShort;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedByte;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedShort;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;
import org.codice.ddf.libs.klv.data.text.KlvEncodingDetectedString;
import org.codice.ddf.libs.klv.data.text.KlvString;
import org.codice.ddf.libs.mpeg.transport.MpegTransportStreamMetadataExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses an MPEG-2 transport stream according to the STANAG 4609 standard. It supports a subset of
 * the UAS Datalink Local Set KLV (MISB ST 0601).
 */
public class Stanag4609TransportStreamParser {
  public static final KlvContext UAS_DATALINK_LOCAL_SET_CONTEXT =
      new KlvContext(KeyLength.SIXTEEN_BYTES, LengthEncoding.BER);

  public static final String UAS_DATALINK_LOCAL_SET = "UAS Datalink Local Set";

  public static final String CHECKSUM = "checksum";

  public static final String TIMESTAMP = "timestamp";

  public static final String MISSION_ID = "mission id";

  public static final String PLATFORM_TAIL_NUMBER = "platform tail number";

  public static final String PLATFORM_DESIGNATION = "platform designation";

  public static final String IMAGE_SOURCE_SENSOR = "image source sensor";

  public static final String IMAGE_COORDINATE_SYSTEM = "image coordinate system";

  public static final String SENSOR_LATITUDE = "sensor latitude";

  public static final String SENSOR_LONGITUDE = "sensor longitude";

  public static final String SENSOR_TRUE_ALTITUDE = "sensor true altitude";

  public static final String SLANT_RANGE = "slant range";

  public static final String TARGET_WIDTH = "target width";

  public static final String FRAME_CENTER_LATITUDE = "frame center latitude";

  public static final String FRAME_CENTER_LONGITUDE = "frame center longitude";

  public static final String FRAME_CENTER_ELEVATION = "frame center elevation";

  public static final String OFFSET_CORNER_LATITUDE_1 = "offset corner latitude 1";

  public static final String OFFSET_CORNER_LONGITUDE_1 = "offset corner longitude 1";

  public static final String OFFSET_CORNER_LATITUDE_2 = "offset corner latitude 2";

  public static final String OFFSET_CORNER_LONGITUDE_2 = "offset corner longitude 2";

  public static final String OFFSET_CORNER_LATITUDE_3 = "offset corner latitude 3";

  public static final String OFFSET_CORNER_LONGITUDE_3 = "offset corner longitude 3";

  public static final String OFFSET_CORNER_LATITUDE_4 = "offset corner latitude 4";

  public static final String OFFSET_CORNER_LONGITUDE_4 = "offset corner longitude 4";

  public static final String TARGET_LOCATION_LATITUDE = "target location latitude";

  public static final String TARGET_LOCATION_LONGITUDE = "target location longitude";

  public static final String TARGET_LOCATION_ELEVATION = "target location elevation";

  public static final String GROUND_RANGE = "ground range";

  public static final String PLATFORM_CALL_SIGN = "platform call sign";

  public static final String EVENT_START_TIME = "event start time";

  public static final String OPERATIONAL_MODE = "operational mode";

  public static final String CORNER_LATITUDE_1 = "corner latitude 1";

  public static final String CORNER_LONGITUDE_1 = "corner longitude 1";

  public static final String CORNER_LATITUDE_2 = "corner latitude 2";

  public static final String CORNER_LONGITUDE_2 = "corner longitude 2";

  public static final String CORNER_LATITUDE_3 = "corner latitude 3";

  public static final String CORNER_LONGITUDE_3 = "corner longitude 3";

  public static final String CORNER_LATITUDE_4 = "corner latitude 4";

  public static final String CORNER_LONGITUDE_4 = "corner longitude 4";

  public static final String SECURITY_LOCAL_METADATA_SET = "security local metadata set";

  public static final String SECURITY_CLASSIFICATION = "security classification";

  public static final String CLASSIFYING_COUNTRY_CODING_METHOD = "country coding method";

  public static final String CLASSIFYING_COUNTRY = "classifying country";

  public static final String OBJECT_COUNTRY_CODING_METHOD = "object country coding method";

  public static final String OBJECT_COUNTRY_CODES = "object country codes";

  public static final String SECURITY_SCI_SHI_INFORMATION = "security sci/shi information";

  public static final String CAVEATS = "caveats";

  public static final String RELEASING_INSTRUCTIONS = "releasing instructions";

  private static final Logger LOGGER =
      LoggerFactory.getLogger(Stanag4609TransportStreamParser.class);

  private static final int MAX_UNSIGNED_SHORT = (1 << 16) - 1;

  private static final long MAX_UNSIGNED_INT = (1L << 32) - 1;

  static {
    final KlvContext localSetContext = new KlvContext(Klv.KeyLength.ONE_BYTE, LengthEncoding.BER);
    final KlvLocalSet outerSet =
        new KlvLocalSet(
            new byte[] {
              0x06, 0x0E, 0x2B, 0x34, 0x02, 0x0B, 0x01, 0x01, 0x0E, 0x01, 0x03, 0x01, 0x01, 0x00,
              0x00, 0x00
            },
            UAS_DATALINK_LOCAL_SET,
            localSetContext);

    localSetContext.addDataElement(new KlvUnsignedShort(new byte[] {1}, CHECKSUM));
    localSetContext.addDataElement(new KlvLong(new byte[] {2}, TIMESTAMP));
    localSetContext.addDataElement(new KlvString(new byte[] {3}, MISSION_ID));
    localSetContext.addDataElement(new KlvString(new byte[] {4}, PLATFORM_TAIL_NUMBER));
    localSetContext.addDataElement(new KlvString(new byte[] {10}, PLATFORM_DESIGNATION));
    localSetContext.addDataElement(new KlvString(new byte[] {11}, IMAGE_SOURCE_SENSOR));
    localSetContext.addDataElement(new KlvString(new byte[] {12}, IMAGE_COORDINATE_SYSTEM));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {13}, SENSOR_LATITUDE, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -90,
            90));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {14}, SENSOR_LONGITUDE, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -180,
            180));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvUnsignedShort(new byte[] {15}, SENSOR_TRUE_ALTITUDE),
            0,
            MAX_UNSIGNED_SHORT,
            -900,
            19000));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvLong(new byte[] {21}, SLANT_RANGE), 0, MAX_UNSIGNED_INT, 0, 5000000));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvUnsignedShort(new byte[] {22}, TARGET_WIDTH), 0, MAX_UNSIGNED_SHORT, 0, 10000));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {23}, FRAME_CENTER_LATITUDE, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -90,
            90));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {24}, FRAME_CENTER_LONGITUDE, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -180,
            180));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvUnsignedShort(new byte[] {25}, FRAME_CENTER_ELEVATION),
            0,
            MAX_UNSIGNED_SHORT,
            -900,
            19000));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {26}, OFFSET_CORNER_LATITUDE_1, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {27}, OFFSET_CORNER_LONGITUDE_1, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {28}, OFFSET_CORNER_LATITUDE_2, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {29}, OFFSET_CORNER_LONGITUDE_2, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {30}, OFFSET_CORNER_LATITUDE_3, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {31}, OFFSET_CORNER_LONGITUDE_3, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {32}, OFFSET_CORNER_LATITUDE_4, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvShort(new byte[] {33}, OFFSET_CORNER_LONGITUDE_4, Optional.of((short) 0x8000)),
            Short.MIN_VALUE + 1L,
            Short.MAX_VALUE,
            -0.075,
            0.075));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {40}, TARGET_LOCATION_LATITUDE, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -90,
            90));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {41}, TARGET_LOCATION_LONGITUDE, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -180,
            180));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvUnsignedShort(new byte[] {42}, TARGET_LOCATION_ELEVATION),
            0,
            MAX_UNSIGNED_SHORT,
            -900,
            19000));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvLong(new byte[] {57}, GROUND_RANGE), 0, MAX_UNSIGNED_INT, 0, 5000000));

    localSetContext.addDataElement(new KlvString(new byte[] {59}, PLATFORM_CALL_SIGN));
    localSetContext.addDataElement(new KlvLong(new byte[] {72}, EVENT_START_TIME));
    localSetContext.addDataElement(new KlvUnsignedByte(new byte[] {77}, OPERATIONAL_MODE));

    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {82}, CORNER_LATITUDE_1, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -90,
            90));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {83}, CORNER_LONGITUDE_1, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -180,
            180));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {84}, CORNER_LATITUDE_2, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -90,
            90));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {85}, CORNER_LONGITUDE_2, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -180,
            180));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {86}, CORNER_LATITUDE_3, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -90,
            90));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {87}, CORNER_LONGITUDE_3, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -180,
            180));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {88}, CORNER_LATITUDE_4, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -90,
            90));
    localSetContext.addDataElement(
        new KlvIntegerEncodedFloatingPoint(
            new KlvInt(new byte[] {89}, CORNER_LONGITUDE_4, Optional.of(0x80000000)),
            Integer.MIN_VALUE + 1L,
            Integer.MAX_VALUE,
            -180,
            180));

    final KlvContext securityLocalSetContext =
        new KlvContext(Klv.KeyLength.ONE_BYTE, LengthEncoding.BER);

    securityLocalSetContext.addDataElement(
        new KlvUnsignedByte(new byte[] {1}, SECURITY_CLASSIFICATION));
    securityLocalSetContext.addDataElement(
        new KlvUnsignedByte(new byte[] {2}, CLASSIFYING_COUNTRY_CODING_METHOD));
    securityLocalSetContext.addDataElement(new KlvString(new byte[] {3}, CLASSIFYING_COUNTRY));
    securityLocalSetContext.addDataElement(
        new KlvString(new byte[] {4}, SECURITY_SCI_SHI_INFORMATION));
    securityLocalSetContext.addDataElement(new KlvString(new byte[] {5}, CAVEATS));
    securityLocalSetContext.addDataElement(new KlvString(new byte[] {6}, RELEASING_INSTRUCTIONS));

    securityLocalSetContext.addDataElement(
        new KlvUnsignedByte(new byte[] {12}, OBJECT_COUNTRY_CODING_METHOD));
    securityLocalSetContext.addDataElement(
        new KlvEncodingDetectedString(new byte[] {13}, OBJECT_COUNTRY_CODES));

    localSetContext.addDataElement(
        new KlvLocalSet(new byte[] {48}, SECURITY_LOCAL_METADATA_SET, securityLocalSetContext));

    UAS_DATALINK_LOCAL_SET_CONTEXT.addDataElement(outerSet);
  }

  private final MpegTransportStreamMetadataExtractor extractor;

  private final KlvDecoder decoder;

  /**
   * Constructs a {@code Stanag4609TransportStreamParser} with the given {@link ByteSource} as the
   * provider of the transport stream bytes.
   *
   * @param byteSource the {@code ByteSource} providing the transport stream bytes
   */
  public Stanag4609TransportStreamParser(final ByteSource byteSource) {
    extractor = new MpegTransportStreamMetadataExtractor(byteSource);
    decoder = new KlvDecoder(UAS_DATALINK_LOCAL_SET_CONTEXT);
  }

  /**
   * Parses the transport stream and calls the given callback for each decoded KLV metadata packet
   * in each metadata stream found in the transport stream. The callback is called immediately upon
   * finding a complete KLV metadata packet.
   *
   * @param callback a callback that will be called for each decoded KLV metadata packet in each
   *     metadata stream found in the transport stream, where the first parameter is the packet ID
   *     of the metadata stream and the second parameter is the decoded metadata packet
   * @throws Exception if the transport stream cannot be parsed
   */
  public void parse(final BiConsumer<Integer, DecodedKLVMetadataPacket> callback) throws Exception {
    extractor.getMetadata(
        (klvStreamPid, pesPacketBytes) -> {
          try {
            final DecodedKLVMetadataPacket decodedKLVMetadataPacket =
                handlePESPacketBytes(pesPacketBytes);
            if (decodedKLVMetadataPacket != null) {
              callback.accept(klvStreamPid, decodedKLVMetadataPacket);
            }
          } catch (KlvDecodingException e) {
            LOGGER.debug("The KLV could not be decoded.", e);
          } catch (RuntimeException e) {
            LOGGER.debug("An error occurred while handling the metadata packet bytes.", e);
          }
        });
  }

  /**
   * Parses the transport stream and returns all the decoded KLV metadata packets (in the order in
   * which they were encountered) that belong to each metadata stream.
   *
   * @return a {@link Map} whose keys are the packet IDs of the metadata streams and whose values
   *     are the decoded KLV metadata packets belonging to that stream
   * @throws Exception if the transport stream cannot be parsed
   */
  public Map<Integer, List<DecodedKLVMetadataPacket>> parse() throws Exception {
    final Map<Integer, List<DecodedKLVMetadataPacket>> decodedStreams = new HashMap<>();

    parse(
        (klvStreamPid, decodedKLVMetadataUnit) -> {
          if (!decodedStreams.containsKey(klvStreamPid)) {
            decodedStreams.put(klvStreamPid, new ArrayList<>());
          }

          decodedStreams.get(klvStreamPid).add(decodedKLVMetadataUnit);
        });

    return decodedStreams;
  }

  private DecodedKLVMetadataPacket handlePESPacketBytes(final byte[] pesPacketBytes)
      throws KlvDecodingException {
    return PESUtilities.handlePESPacketBytes(pesPacketBytes, decoder);
  }
}
