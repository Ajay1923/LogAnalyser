package com.crud.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmailWithAttachment(String subject, String message, List<String> toEmails, List<File> attachments) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setSubject(subject);
        helper.setText(message, true); // Set to true if the email is HTML formatted
        helper.setTo(toEmails.toArray(new String[0]));

        // Add attachments
        for (File attachment : attachments) {
            FileSystemResource file = new FileSystemResource(attachment);
            helper.addAttachment(file.getFilename(), file);
        }

        mailSender.send(mimeMessage);
    }

    public void sendEmailWithAttachment(String subject, String message, List<String> toEmails, String attachmentName, byte[] attachmentData) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setSubject(subject);
        helper.setText(message, true);
        helper.setTo(toEmails.toArray(new String[0]));

        
        helper.addAttachment(attachmentName, new ByteArrayResource(attachmentData));

        mailSender.send(mimeMessage);
    }

    public void sendEmail(String subject, String message, List<String> toEmails) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);

        helper.setSubject(subject);
        helper.setText(message, true);
        helper.setTo(toEmails.toArray(new String[0]));

        mailSender.send(mimeMessage);
    }
}
