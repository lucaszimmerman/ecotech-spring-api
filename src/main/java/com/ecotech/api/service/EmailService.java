package com.ecotech.api.service;


import org.springframework.stereotype.Service;

import com.ecotech.api.config.aws.AwsSesProperties;
import com.ecotech.api.exceptions.EmailDeliveryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SesV2Client sesClient;
    private final AwsSesProperties sesProperties;

    public void sendEmailVerification(
            String destinationEmail,
            String verificationLink
    ){

        Destination destination = Destination.builder()
               .toAddresses(destinationEmail)
               .build();

        Content subject = Content.builder()
                .data("Confirme seu email - EcoTech")
                .charset("UTF-8")
                .build();

        Content body = Content.builder()
                .data(
                    """
                    Bem-vindo ao EcoTech!

                    Confirme seu endereço de email acessando o link abaixo:

                    %s

                    Se você não criou esta conta, ignore esta mensagem.
                    """.formatted(verificationLink)
                )
                .charset("UTF-8")
                .build();

        Message message = Message.builder()
                 .subject(subject)
                 .body(Body.builder()
                          .text(body)
                          .build())
                 .build();

        SendEmailRequest request = SendEmailRequest.builder()
                 .fromEmailAddress(sesProperties.fromEmail())
                 .destination(destination)
                 .content(EmailContent.builder()
                         .simple(message)
                        .build())
                .build();

        try {
            sesClient.sendEmail(request);
        } catch (SesV2Exception e) {
            log.warn(
                    "Falha ao enviar email de verificacao pelo SES. awsErrorCode={}, statusCode={}",
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
                    e.statusCode());

            throw new EmailDeliveryException(
                    "Nao foi possivel enviar o email de verificacao.",
                    e);
        }
    }

    
}
