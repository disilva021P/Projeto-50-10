package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final MailSender mailSender;
    @Value("spring.mail.username")
    private String emailGeral;
    public void enviaEmail(String emailDestino, String cabecalho, String corpo){
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(emailGeral);
        email.setTo(emailDestino);
        email.setSubject(cabecalho);
        email.setText(corpo);
        mailSender.send(email);
    }
}
