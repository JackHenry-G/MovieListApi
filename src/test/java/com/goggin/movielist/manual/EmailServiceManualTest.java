package com.goggin.movielist.manual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

import com.goggin.movielist.service.EmailService;

//@SpringBootTest
public class EmailServiceManualTest {

    @Autowired
    private EmailService emailService;

    // @MockBean // mock does not execute the acutal logic of the bean
    @Autowired
    private JavaMailSender javaMailSender;

    // @Test
    public void testSendEmail() {
        // arrange
        String to = "jackhenryg@hotmail.co.uk";
        String subject = "Test Subject";
        String body = "Test Body";

        // act
        emailService.sendEmail(to, subject, body);
    }
}
