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
package org.codice.alliance.core.email.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.xml.ws.Holder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;

public class EmailSenderImplTest {

  private static final String TO_ADDR = "to@test.com";

  private static final String FROM_ADDR = "from@test.com";

  private static final String SUBJECT_LINE = "subject line";

  private static final String BODY = "body text";

  private static final String HOST = "host.com";

  private EmailSenderImpl emailSender;

  private Holder<Message> capturedMessage = new Holder<>();

  @Before
  public void setup() {
    emailSender =
        new EmailSenderImpl() {
          @Override
          void send(Message message) throws MessagingException {
            capturedMessage.value = message;
          }
        };
    emailSender.setMailHost(HOST);
  }

  @Test
  public void testSendEmailSubject() throws IOException, MessagingException {

    emailSender.sendEmail(FROM_ADDR, TO_ADDR, SUBJECT_LINE, BODY, Collections.emptyList());

    assertThat(capturedMessage.value.getSubject(), is(SUBJECT_LINE));
  }

  @Test
  public void testToAddress() throws IOException, MessagingException {

    emailSender.sendEmail(FROM_ADDR, TO_ADDR, SUBJECT_LINE, BODY, Collections.emptyList());

    assertThat(capturedMessage.value.getAllRecipients()[0].toString(), is(TO_ADDR));
  }

  @Test
  public void testFromAddress() throws IOException, MessagingException {

    emailSender.sendEmail(FROM_ADDR, TO_ADDR, SUBJECT_LINE, BODY, Collections.emptyList());

    assertThat(capturedMessage.value.getFrom()[0].toString(), is(FROM_ADDR));
  }

  @Test
  public void testBody() throws IOException, MessagingException {

    emailSender.sendEmail(FROM_ADDR, TO_ADDR, SUBJECT_LINE, BODY, Collections.emptyList());

    MimeMultipart mimeMultipart = (MimeMultipart) capturedMessage.value.getContent();

    Object content = mimeMultipart.getBodyPart(0).getContent();

    assertThat(content, is(BODY));
  }

  @Test
  public void testAttachment() throws IOException, MessagingException {

    final List<String> attachmentLines = new LinkedList<>();
    Holder<String> filename = new Holder<>();

    int bodyPartIndex = 1;

    emailSender =
        new EmailSenderImpl() {
          @Override
          void send(Message message) throws MessagingException {
            try {
              FileInputStream fis =
                  (FileInputStream)
                      ((MimeMultipart) message.getContent())
                          .getBodyPart(bodyPartIndex)
                          .getContent();
              filename.value =
                  ((MimeMultipart) message.getContent()).getBodyPart(bodyPartIndex).getFileName();
              attachmentLines.addAll(IOUtils.readLines(fis));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        };

    emailSender.setMailHost(HOST);

    String attachedData = "attached data";
    String attachedFilename = "fileName.txt";

    emailSender.sendEmail(
        FROM_ADDR,
        TO_ADDR,
        SUBJECT_LINE,
        BODY,
        Collections.singletonList(
            new ImmutablePair<>(
                attachedFilename, new ByteArrayInputStream(attachedData.getBytes()))));

    assertThat(filename.value, is(attachedFilename));
    assertThat(attachmentLines, is(Collections.singletonList(attachedData)));
  }

  /** Set the max attachment size to 1MB and send an attachment of size 2MB. */
  @Test(expected = IOException.class)
  public void testTooBigAttachment() throws IOException, MessagingException {

    final List<String> attachmentLines = new LinkedList<>();
    Holder<String> filename = new Holder<>();

    int bodyPartIndex = 1;

    emailSender =
        new EmailSenderImpl() {
          @Override
          void send(Message message) throws MessagingException {
            try {
              FileInputStream fis =
                  (FileInputStream)
                      ((MimeMultipart) message.getContent())
                          .getBodyPart(bodyPartIndex)
                          .getContent();
              filename.value =
                  ((MimeMultipart) message.getContent()).getBodyPart(bodyPartIndex).getFileName();
              attachmentLines.addAll(IOUtils.readLines(fis));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        };

    emailSender.setMaxAttachmentSize(1);

    emailSender.setMailHost(HOST);

    String attachedData = generateString(2000000);
    String attachedFilename = "fileName.txt";

    emailSender.sendEmail(
        FROM_ADDR,
        TO_ADDR,
        SUBJECT_LINE,
        BODY,
        Collections.singletonList(
            new ImmutablePair<>(
                attachedFilename, new ByteArrayInputStream(attachedData.getBytes()))));
  }

  private String generateString(int length) {
    char[] charArray = new char[length];
    Arrays.fill(charArray, ' ');
    return new String(charArray);
  }
}
