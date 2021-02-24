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

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.ws.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.core.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b> This code is experimental. While this interface is functional and tested, it may change or be
 * removed in a future version of the library. </b>
 */
public class EmailSenderImpl implements EmailSender {

  private static final int BUFFER_SIZE = 4096;

  private static final int DEFAULT_PORT = 25;

  private static final long MEGABYTE = 1024L * 1024L;

  private static final long DEFAULT_MAX_ATTACHMENT_SIZE = Long.MAX_VALUE;

  private static final String SMTP_HOST_PROPERTY = "mail.smtp.host";

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderImpl.class);

  private static final String SMTP_PORT_PROPERTY = "mail.smtp.port";

  private static final String SMTP_AUTH_PROPERTY = "mail.smtp.auth";

  private int mailPort = DEFAULT_PORT;

  private String mailHost;

  private long maxAttachmentSize = DEFAULT_MAX_ATTACHMENT_SIZE;

  public void setMailPort(int mailPort) {
    this.mailPort = mailPort;
  }

  public void setMailHost(String mailHost) {
    this.mailHost = mailHost;
  }

  /** @param maxAttachmentSize unit is 1024*1024 bytes */
  public void setMaxAttachmentSize(long maxAttachmentSize) {
    this.maxAttachmentSize = maxAttachmentSize * MEGABYTE;
  }

  /** sendEmail method sends email after receiving input parameters */
  @Override
  public void sendEmail(
      String fromEmail,
      String toEmail,
      String subject,
      String body,
      List<Pair<String, InputStream>> attachments)
      throws IOException {
    notNull(fromEmail, "fromEmail must be non-null");
    notNull(toEmail, "toEmail must be non-null");
    notNull(subject, "subject must be non-null");
    notNull(body, "body must be non-null");
    notNull(attachments, "attachments must be non-null");

    if (StringUtils.isBlank(mailHost)) {
      throw new IOException("the mail server hostname has not been configured");
    }

    List<File> tempFiles = new LinkedList<>();

    try {
      InternetAddress emailAddr = new InternetAddress(toEmail);
      emailAddr.validate();

      Properties properties = createSessionProperties();

      Session session = Session.getDefaultInstance(properties);

      MimeMessage mimeMessage = new MimeMessage(session);
      mimeMessage.setFrom(new InternetAddress(fromEmail));
      mimeMessage.addRecipient(Message.RecipientType.TO, emailAddr);
      mimeMessage.setSubject(subject);

      BodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setText(body);

      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(messageBodyPart);

      Holder<Long> bytesWritten = new Holder<>(0L);

      for (Pair<String, InputStream> attachment : attachments) {

        messageBodyPart = new MimeBodyPart();
        File file = File.createTempFile("email-sender-", ".dat");
        tempFiles.add(file);

        copyDataToTempFile(file, attachment.getValue(), bytesWritten);
        messageBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
        messageBodyPart.setFileName(attachment.getKey());
        multipart.addBodyPart(messageBodyPart);
      }

      mimeMessage.setContent(multipart);

      send(mimeMessage);

      LOGGER.debug("Email sent to {}", toEmail);

    } catch (AddressException e) {
      throw new IOException("invalid email address: email=" + toEmail, e);
    } catch (MessagingException e) {
      throw new IOException("message error occurred on send", e);
    } finally {
      tempFiles.forEach(
          file -> {
            if (!file.delete()) {
              LOGGER.debug("unable to delete tmp file: path={}", file);
            }
          });
    }
  }

  @Override
  public void sendEmail(String fromEmail, String toEmail, String subject, String body)
      throws IOException {
    sendEmail(fromEmail, toEmail, subject, body, Collections.emptyList());
  }

  private void copyDataToTempFile(File file, InputStream inputStream, Holder<Long> bytesWritten)
      throws IOException {

    try (OutputStream outputStream = new FileOutputStream(file)) {

      byte[] buffer = new byte[BUFFER_SIZE];

      int c;
      while ((c = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, c);
        bytesWritten.value = bytesWritten.value + c;
        if (bytesWritten.value > maxAttachmentSize) {
          throw new IOException("total attachment size exceeds limit: limit=" + maxAttachmentSize);
        }
      }
    }
  }

  void send(Message message) throws MessagingException {
    Transport.send(message);
  }

  private Properties createSessionProperties() {
    Properties properties = System.getProperties();
    properties.setProperty(SMTP_HOST_PROPERTY, mailHost);
    properties.setProperty(SMTP_AUTH_PROPERTY, "false");
    properties.setProperty(SMTP_PORT_PROPERTY, Integer.toString(mailPort));
    return properties;
  }

  @Override
  public String toString() {
    return "EmailSenderImpl{"
        + "mailPort="
        + mailPort
        + ", mailHost='"
        + mailHost
        + '\''
        + ", maxAttachmentSize="
        + maxAttachmentSize
        + '}';
  }
}
