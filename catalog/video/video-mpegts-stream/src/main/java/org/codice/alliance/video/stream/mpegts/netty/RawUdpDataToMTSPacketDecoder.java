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
package org.codice.alliance.video.stream.mpegts.netty;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.codice.alliance.libs.mpegts.Constants;
import org.codice.ddf.security.common.Security;
import org.codice.ddf.security.handler.api.BaseAuthenticationToken;
import org.codice.ddf.security.handler.api.GuestAuthenticationToken;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.sources.MTSSources;
import org.taktik.mpegts.sources.ResettableMTSSource;

import com.google.common.io.ByteSource;

import ddf.security.Subject;
import ddf.security.service.SecurityManager;
import ddf.security.service.SecurityServiceException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Converts datagrams to a series of MTSPackets. Will discard data while looking for the MPEG-TS
 * sync byte.
 */
class RawUdpDataToMTSPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {

    public static final byte TS_SYNC = (byte) 0x47;

    public static final int BUFFER_SIZE = 4096;

    public static final int TS_PACKET_SIZE = Constants.TS_PACKET_SIZE;

    /**
     * Milliseconds to wait until checking the subject token for expiration.
     */
    public static final long TOKEN_CHECK_PERIOD = TimeUnit.SECONDS.toMillis(5);

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RawUdpDataToMTSPacketDecoder.class);

    private ByteBuf byteBuf;

    private PacketBuffer packetBuffer;

    private MTSParser mtsParser = MTSSources::from;

    private UdpStreamProcessor udpStreamProcessor;

    /**
     * Milliseconds since the subject token was checked for expiration.
     */
    private long lastTokenCheck = 0;

    public RawUdpDataToMTSPacketDecoder(PacketBuffer packetBuffer,
            UdpStreamProcessor udpStreamProcessor) {
        this.packetBuffer = packetBuffer;
        this.udpStreamProcessor = udpStreamProcessor;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (byteBuf != null) {
            byteBuf.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byteBuf = ctx.alloc()
                .buffer(BUFFER_SIZE);
    }

    private Subject getGuestSubject(String ipAddress) {
        Subject subject = null;
        GuestAuthenticationToken token =
                new GuestAuthenticationToken(BaseAuthenticationToken.DEFAULT_REALM, ipAddress);
        LOGGER.debug("Getting new Guest user token for {}", ipAddress);
        try {
            SecurityManager securityManager = getSecurityManager();
            if (securityManager != null) {
                subject = securityManager.getSubject(token);
            }
        } catch (SecurityServiceException sse) {
            LOGGER.warn("Unable to request subject for guest user.", sse);
        }

        return subject;
    }

    private SecurityManager getSecurityManager() {
        BundleContext context = getBundleContext();
        if (context != null) {
            ServiceReference securityManagerRef =
                    context.getServiceReference(SecurityManager.class);
            return (SecurityManager) context.getService(securityManagerRef);
        }
        LOGGER.warn("Unable to get Security Manager");
        return null;
    }

    private BundleContext getBundleContext() {
        Bundle bundle = FrameworkUtil.getBundle(Security.class);
        if (bundle != null) {
            return bundle.getBundleContext();
        }
        return null;
    }

    private boolean isTokenCheck() {
        return System.currentTimeMillis() - lastTokenCheck > TOKEN_CHECK_PERIOD;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> outputList)
            throws Exception {

        notNull(ctx, "ctx must be non-null");
        notNull(msg, "msg must be non-null");
        notNull(outputList, "outputList must be non-null");

        checkSecuritySubject(msg);

        byteBuf.writeBytes(msg.content());

        skipToSyncByte();

        while (byteBuf.readableBytes() >= TS_PACKET_SIZE) {

            byte[] payload = new byte[TS_PACKET_SIZE];

            byteBuf.readBytes(payload);

            ResettableMTSSource src = mtsParser.parse(ByteSource.wrap(payload));

            MTSPacket packet = null;
            try {
                packet = src.nextPacket();
            } catch (IOException e) {
                LOGGER.warn("unable to parse mpegst packet", e);
            }

            if (packet != null) {
                packetBuffer.write(payload);
                outputList.add(packet);
            }

            skipToSyncByte();
        }

        byteBuf.discardReadBytes();

    }

    private void checkSecuritySubject(DatagramPacket msg) {
        Subject subject = udpStreamProcessor.getSubject();

        if (subject == null || (isTokenCheck() && Security.getInstance()
                .tokenAboutToExpire(subject))) {
            String ip = getIpAddress(msg);
            subject = getGuestSubject(ip);
            LOGGER.debug("setting the subject: ip={} subject={}", ip, subject);
            udpStreamProcessor.setSubject(subject);
            lastTokenCheck = System.currentTimeMillis();
        }
    }

    private String getIpAddress(DatagramPacket msg) {
        return msg.sender()
                .getAddress()
                .getHostAddress();
    }

    private void skipToSyncByte() {

        int bytesBefore;

        if ((bytesBefore = byteBuf.bytesBefore(TS_SYNC)) > 0) {
            LOGGER.info("skipping bytes in raw data stream, looking for MPEG-TS sync {}",
                    bytesBefore);
            byteBuf.skipBytes(bytesBefore);
        }

    }

    public interface MTSParser {
        ResettableMTSSource parse(ByteSource byteSource) throws IOException;
    }

}
