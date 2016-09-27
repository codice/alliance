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

package org.codice.alliance.nsili.endpoint;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSenderImpl implements EmailSender {

    private static final String SMTP_HOST_PROPERTY = "mail.smtp.host";

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderImpl.class);

    private String fromEmail;

    private String mailHost;

    private String subject;

    private String emailBody;

    private String filename;

    private int maxAttachmentSize;

    /**
     * EmailSenderImpl formats emails and sends them to the desired address using the fromEmail
     * and through the desired mailHost.
     *
     * @param mailHost: non-null origin SMTP mailHost to send through
     */
    public EmailSenderImpl(String mailHost) {
        notNull(mailHost, "mailHost must be non-null");

        this.mailHost = mailHost;
    }

    /**
     * sendEmail method sends email after receiving input parameters
     *
     * @param fromEmail: non-null origin address
     * @param subject:   non-null subject line of email to be sent
     * @param emailBody: non-null body of email to be sent
     */
    @Override
    public void sendEmail(String fromEmail, String emailDest, String subject, String emailBody,
            String filename, InputStream attachment) {
        notNull(fromEmail, "fromEmail must be non-null");
        notNull(subject, "subject must be non-null");
        notNull(emailBody, "emailBody must be non-null");
        notNull(filename, "filename must be non-null");

        this.fromEmail = fromEmail;
        this.subject = subject;
        this.emailBody = emailBody;
        this.filename = filename;

        try {
            InternetAddress emailAddr = new InternetAddress(emailDest);
            emailAddr.validate();

            Properties properties = createSessionProperies();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", mailHost);
            properties.put("mail.smtp.port", "25");

            Session session = Session.getDefaultInstance(properties);

            /* Set up message text fields */
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(fromEmail));
            mimeMessage.addRecipient(Message.RecipientType.TO, emailAddr);
            mimeMessage.setSubject(subject);

            /* Create BodyPart contain message components inside larger multiPartEmail structure */
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(emailBody);

            /* Create multipart email to compile body parts together */
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            /* Create attachment */
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(getFileFromInputStream(attachment));
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            multipart.addBodyPart(messageBodyPart);
            
            /* Package parts into mimeMessage and send */
            mimeMessage.setContent(multipart);
            Transport.send(mimeMessage);

            LOGGER.debug("Email sent to " + emailDest);

        } catch (AddressException e) {
            LOGGER.debug("Invalid email address entered", e);
        } catch (MessagingException e) {
            LOGGER.debug("Message error occured on send", e);
        }
    }

    private File getFileFromInputStream(InputStream inputStream) {
        File file = new File("/tmp/newfile");
        OutputStream outputStream;

        try {
            outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private Properties createSessionProperies() {
        Properties properties = System.getProperties();
        properties.setProperty(SMTP_HOST_PROPERTY, mailHost);
        return properties;
    }

    @Override
    public String toString() {
        return "EmailSenderImpl{" + "fromEmail=" + fromEmail + ", mailHost=" + mailHost + '}';
    }

    @Override
    public String getFromEmail() {
        notNull(fromEmail, "fromEmail must be non-null");
        return fromEmail;
    }

    @Override
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    @Override
    public String getMailHost() {
        notNull(mailHost, "mailHost must be non-null");
        return mailHost;
    }

    @Override
    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    @Override
    public void setAttachment() {

    }
}
