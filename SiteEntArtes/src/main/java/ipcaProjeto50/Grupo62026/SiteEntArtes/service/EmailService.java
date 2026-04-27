package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender; // Alterado aqui
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {

    // Use JavaMailSender para ter acesso ao createMimeMessage()
    private final JavaMailSender mailSender;

    // Adicionado o ${} para buscar o valor real nas configurações
    @Value("${spring.mail.username}")
    private String emailGeral;

    public void enviaEmail(String emailDestino, String cabecalho, String corpo) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(emailGeral);
            helper.setTo(emailDestino);
            helper.setSubject(cabecalho);
            helper.setText(corpo, true);

            mailSender.send(mensagem);

        } catch (MessagingException e) {
            // Log do erro e possivelmente lançar uma exceção personalizada de negócio
            System.err.println("Falha ao enviar e-mail: " + e.getMessage());
        }
    }
}