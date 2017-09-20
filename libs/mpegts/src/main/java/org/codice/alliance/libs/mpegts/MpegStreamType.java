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
package org.codice.alliance.libs.mpegts;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.HashMap;
import java.util.Map;
import org.jcodec.containers.mps.MTSUtils;

/** These are the types of streams that may be contained in an MPEG-TS. */
public enum MpegStreamType {
  RESERVED(MTSUtils.StreamType.RESERVED.getTag()),
  VIDEO_MPEG1(MTSUtils.StreamType.VIDEO_MPEG1.getTag()),
  VIDEO_MPEG2(MTSUtils.StreamType.VIDEO_MPEG2.getTag()),
  AUDIO_MPEG1(MTSUtils.StreamType.AUDIO_MPEG1.getTag()),
  AUDIO_MPEG2(MTSUtils.StreamType.AUDIO_MPEG2.getTag()),
  PRIVATE_SECTION(MTSUtils.StreamType.PRIVATE_SECTION.getTag()),
  PRIVATE_DATA(MTSUtils.StreamType.PRIVATE_DATA.getTag()),
  MHEG(MTSUtils.StreamType.MHEG.getTag()),
  DSM_CC(MTSUtils.StreamType.DSM_CC.getTag()),
  ATM_SYNC(MTSUtils.StreamType.ATM_SYNC.getTag()),
  DSM_CC_A(MTSUtils.StreamType.DSM_CC_A.getTag()),
  DSM_CC_B(MTSUtils.StreamType.DSM_CC_B.getTag()),
  DSM_CC_C(MTSUtils.StreamType.DSM_CC_C.getTag()),
  DSM_CC_D(MTSUtils.StreamType.DSM_CC_D.getTag()),
  MPEG_AUX(MTSUtils.StreamType.MPEG_AUX.getTag()),
  AUDIO_AAC_ADTS(MTSUtils.StreamType.AUDIO_AAC_ADTS.getTag()),
  VIDEO_MPEG4(MTSUtils.StreamType.VIDEO_MPEG4.getTag()),
  AUDIO_AAC_LATM(MTSUtils.StreamType.AUDIO_AAC_LATM.getTag()),
  FLEXMUX_PES(MTSUtils.StreamType.FLEXMUX_PES.getTag()),
  FLEXMUX_SEC(MTSUtils.StreamType.FLEXMUX_SEC.getTag()),
  DSM_CC_SDP(MTSUtils.StreamType.DSM_CC_SDP.getTag()),
  META_PES(MTSUtils.StreamType.META_PES.getTag()),
  META_SEC(MTSUtils.StreamType.META_SEC.getTag()),
  DSM_CC_DATA_CAROUSEL(MTSUtils.StreamType.DSM_CC_DATA_CAROUSEL.getTag()),
  DSM_CC_OBJ_CAROUSEL(MTSUtils.StreamType.DSM_CC_OBJ_CAROUSEL.getTag()),
  DSM_CC_SDP1(MTSUtils.StreamType.DSM_CC_SDP1.getTag()),
  IPMP(MTSUtils.StreamType.IPMP.getTag()),
  VIDEO_H264(MTSUtils.StreamType.VIDEO_H264.getTag()),
  AUDIO_AAC_RAW(MTSUtils.StreamType.AUDIO_AAC_RAW.getTag()),
  SUBS(MTSUtils.StreamType.SUBS.getTag()),
  AUX_3D(MTSUtils.StreamType.AUX_3D.getTag()),
  VIDEO_AVC_SVC(MTSUtils.StreamType.VIDEO_AVC_SVC.getTag()),
  VIDEO_AVC_MVC(MTSUtils.StreamType.VIDEO_AVC_MVC.getTag()),
  VIDEO_J2K(MTSUtils.StreamType.VIDEO_J2K.getTag()),
  VIDEO_MPEG2_3D(MTSUtils.StreamType.VIDEO_MPEG2_3D.getTag()),
  VIDEO_H264_3D(MTSUtils.StreamType.VIDEO_H264_3D.getTag()),
  VIDEO_CAVS(MTSUtils.StreamType.VIDEO_CAVS.getTag()),
  IPMP_STREAM(MTSUtils.StreamType.IPMP_STREAM.getTag()),
  AUDIO_AC3(MTSUtils.StreamType.AUDIO_AC3.getTag()),
  AUDIO_DTS(MTSUtils.StreamType.AUDIO_DTS.getTag());

  private static final Map<Integer, MpegStreamType> LOOKUP = new HashMap<>();

  static {
    for (MpegStreamType streamType : values()) {
      LOOKUP.put(streamType.tag, streamType);
    }
  }

  private final int tag;

  MpegStreamType(int tag) {
    this.tag = tag;
  }

  /**
   * Find the MpegStreamType based on the MPEG-TS stream tag number.
   *
   * @param tag stream tag number
   * @return MpegStreamType (may be null)
   * @see MTSUtils.StreamType#getTag()
   */
  public static MpegStreamType lookup(int tag) {
    return LOOKUP.get(tag);
  }

  /**
   * Find the MpegStreamType based on a {@link org.jcodec.containers.mps.MTSUtils.StreamType}.
   *
   * @param streamType must be non-null
   * @return MpegStreamType (may be null)
   */
  public static MpegStreamType lookup(MTSUtils.StreamType streamType) {
    notNull(streamType, "streamType must be non-null");
    return lookup(streamType.getTag());
  }
}
