package org.codice.alliance.video.stream.mpegts.netty;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jcodec.containers.mps.MTSUtils;
import org.junit.Test;

/**
 * Created by glenhein on 6/28/16.
 */
public class TestStreamTypeToString {
    @Test
    public void testApply() {
        String r = new StreamTypeToString().apply(MTSUtils.StreamType.VIDEO_H264);
        assertThat(r, notNullValue());
    }
}
