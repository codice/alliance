package org.codice.alliance.video.stream.mpegts.netty;

import static org.codice.alliance.video.stream.mpegts.netty.StreamType.*;

import java.util.function.Function;


public class StreamTypeToString implements Function<org.jcodec.containers.mps.MTSUtils.StreamType, String> {


    @Override
    public String apply(org.jcodec.containers.mps.MTSUtils.StreamType streamType) {

        switch(lookup(streamType.getTag())) {
        case RESERVED:
            return RESERVED.toString();
        VIDEO_MPEG1
                VIDEO_MPEG2
        AUDIO_MPEG1
                AUDIO_MPEG2
        PRIVATE_SECTION
                PRIVATE_DATA
        MHEG DSM_CC
        ATM_SYNC
        DSM_CC_A
        DSM_CC_B
        DSM_CC_C
        DSM_CC_D
        MPEG_AUX
        AUDIO_AAC_ADTS
                VIDEO_MPEG4
        AUDIO_AAC_LATM
        FLEXMUX_PES
        FLEXMUX_SEC
        DSM_CC_SDP
        META_PES
                META_SEC
                DSM_CC_DATA_CAROUSEL
        DSM_CC_OBJ_CAROUSEL
        DSM_CC_SDP1 IPMP VIDEO_H264 AUDIO_AAC_RAW SUBS
                AUX_3D VIDEO_AVC_SVC VIDEO_AVC_MVC VIDEO_J2K VIDEO_MPEG2_3D VIDEO_H264_3D VIDEO_CAVS IPMP_STREAM 
                AUDIO_AC3 AUDIO_DTS
        }

//        public static final MTSUtils.StreamType RESERVED = new MTSUtils.StreamType(0, false, false);
//        public static final MTSUtils.StreamType VIDEO_MPEG1 = new MTSUtils.StreamType(1, true, false);
//        public static final MTSUtils.StreamType VIDEO_MPEG2 = new MTSUtils.StreamType(2, true, false);
//        public static final MTSUtils.StreamType AUDIO_MPEG1 = new MTSUtils.StreamType(3, false, true);
//        public static final MTSUtils.StreamType AUDIO_MPEG2 = new MTSUtils.StreamType(4, false, true);
//        public static final MTSUtils.StreamType PRIVATE_SECTION = new MTSUtils.StreamType(5, false, false);
//        public static final MTSUtils.StreamType PRIVATE_DATA = new MTSUtils.StreamType(6, false, false);
//        public static final MTSUtils.StreamType MHEG = new MTSUtils.StreamType(7, false, false);
//        public static final MTSUtils.StreamType DSM_CC = new MTSUtils.StreamType(8, false, false);
//        public static final MTSUtils.StreamType ATM_SYNC = new MTSUtils.StreamType(9, false, false);
//        public static final MTSUtils.StreamType DSM_CC_A = new MTSUtils.StreamType(10, false, false);
//        public static final MTSUtils.StreamType DSM_CC_B = new MTSUtils.StreamType(11, false, false);
//        public static final MTSUtils.StreamType DSM_CC_C = new MTSUtils.StreamType(12, false, false);
//        public static final MTSUtils.StreamType DSM_CC_D = new MTSUtils.StreamType(13, false, false);
//        public static final MTSUtils.StreamType MPEG_AUX = new MTSUtils.StreamType(14, false, false);
//        public static final MTSUtils.StreamType AUDIO_AAC_ADTS = new MTSUtils.StreamType(15, false, true);
//        public static final MTSUtils.StreamType VIDEO_MPEG4 = new MTSUtils.StreamType(16, true, false);
//        public static final MTSUtils.StreamType AUDIO_AAC_LATM = new MTSUtils.StreamType(17, false, true);
//        public static final MTSUtils.StreamType FLEXMUX_PES = new MTSUtils.StreamType(18, false, false);
//        public static final MTSUtils.StreamType FLEXMUX_SEC = new MTSUtils.StreamType(19, false, false);
//        public static final MTSUtils.StreamType DSM_CC_SDP = new MTSUtils.StreamType(20, false, false);
//        public static final MTSUtils.StreamType META_PES = new MTSUtils.StreamType(21, false, false);
//        public static final MTSUtils.StreamType META_SEC = new MTSUtils.StreamType(22, false, false);
//        public static final MTSUtils.StreamType DSM_CC_DATA_CAROUSEL = new MTSUtils.StreamType(23, false, false);
//        public static final MTSUtils.StreamType DSM_CC_OBJ_CAROUSEL = new MTSUtils.StreamType(24, false, false);
//        public static final MTSUtils.StreamType DSM_CC_SDP1 = new MTSUtils.StreamType(25, false, false);
//        public static final MTSUtils.StreamType IPMP = new MTSUtils.StreamType(26, false, false);
//        public static final MTSUtils.StreamType VIDEO_H264 = new MTSUtils.StreamType(27, true, false);
//        public static final MTSUtils.StreamType AUDIO_AAC_RAW = new MTSUtils.StreamType(28, false, true);
//        public static final MTSUtils.StreamType SUBS = new MTSUtils.StreamType(29, false, false);
//        public static final MTSUtils.StreamType AUX_3D = new MTSUtils.StreamType(30, false, false);
//        public static final MTSUtils.StreamType VIDEO_AVC_SVC = new MTSUtils.StreamType(31, true, false);
//        public static final MTSUtils.StreamType VIDEO_AVC_MVC = new MTSUtils.StreamType(32, true, false);
//        public static final MTSUtils.StreamType VIDEO_J2K = new MTSUtils.StreamType(33, true, false);
//        public static final MTSUtils.StreamType VIDEO_MPEG2_3D = new MTSUtils.StreamType(34, true, false);
//        public static final MTSUtils.StreamType VIDEO_H264_3D = new MTSUtils.StreamType(35, true, false);
//        public static final MTSUtils.StreamType VIDEO_CAVS = new MTSUtils.StreamType(66, false, true);
//        public static final MTSUtils.StreamType IPMP_STREAM = new MTSUtils.StreamType(127, false, false);
//        public static final MTSUtils.StreamType AUDIO_AC3 = new MTSUtils.StreamType(129, false, true);
//        public static final MTSUtils.StreamType AUDIO_DTS = new MTSUtils.StreamType(138, false, true);

        return streamType.toString();
    }
}
