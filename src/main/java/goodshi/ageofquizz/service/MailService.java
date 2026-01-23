package goodshi.ageofquizz.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

	@Value("${app.frontend.base-url}")
	private String frontendBaseUrl;

	private final JavaMailSender mailSender;

	public MailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Value("${spring.mail.username}")
	private String from;

	public void sendPasswordResetEmail(String toEmail, String resetToken) {
		String subject = "Réinitialisation de votre mot de passe";
		String resetUrl = frontendBaseUrl + "/reset-password?token=" + resetToken;

		String content = """
				<p>Bonjour,</p>
				<p>Vous avez demandé une réinitialisation de votre mot de passe.</p>
				<p>Cliquez sur le lien ci-dessous pour le réinitialiser :</p>
				<p><a href="%s">Réinitialiser le mot de passe</a></p>
				<p>Ce lien expirera dans 15 minutes.</p>
				<br>
				<p>Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.</p>
				""".formatted(resetUrl);

		sendHtmlEmail(toEmail, subject, content);
	}

	public void sendEmailVerification(String toEmail, String verifyToken) {
		String subject = "Vérification de votre adresse email";
		String verifyUrl = frontendBaseUrl + "/verify-email?token=" + verifyToken;

		String content = """
				<p>Bonjour,</p>
				<p>Merci de vous être inscrit sur Age of Quizz !</p>
				<p>Pour activer votre compte, veuillez cliquer sur le lien ci-dessous :</p>
				<p><a href="%s" style="color: #1a73e8;">Vérifier mon adresse email</a></p>
				<p>Ce lien expirera dans 24 heures.</p>
				<br>
				<p>Si vous n’avez pas créé de compte, vous pouvez ignorer cet email.</p>
				<p>L’équipe Age of Quizz</p>
				""".formatted(verifyUrl);

		sendHtmlEmail(toEmail, subject, content);
	}

	public void sendHtmlEmail(String to, String subject, String htmlBody) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);

			mailSender.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
		}
	}
}
