package com.sip.hsm.services;

import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.User;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;   // fourni automatiquement par spring-boot-starter-mail

    @Value("${app.mail.from}")
    private String expediteur;

    @Value("${app.mail.admin}")
    private String emailAdmin;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async   // exécuté en arrière-plan : l'inscription ne attend pas l'envoi
    @Override
    public void notifierAdminInscription(User user) {
        String sujet = "Nouvelle inscription à valider - Hotels Manager";
        String contenu = """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;border:1px solid #e9ecef;border-radius:8px;overflow:hidden">
              <div style="background:linear-gradient(87deg,#5e72e4,#825ee4);color:#fff;padding:18px 24px;font-size:18px;font-weight:bold">
                Hotels Manager - Nouvelle inscription
              </div>
              <div style="padding:20px 24px;color:#32325d">
                <p>Bonjour,</p>
                <p>Un nouvel utilisateur vient de s'inscrire et attend votre validation :</p>
                <table style="border-collapse:collapse">
                  <tr><td style="padding:4px 12px 4px 0"><b>Nom</b></td><td>%s %s</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Email</b></td><td>%s</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Téléphone</b></td><td>%s</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Date</b></td><td>%s</td></tr>
                </table>
                <p>Connectez-vous à l'application, page <b>Utilisateurs</b>, pour activer son compte.</p>
              </div>
            </div>
            """.formatted(user.getPrenom(), user.getNom(), user.getEmail(),
                          user.getTel() == null ? "-" : user.getTel(),
                          user.getDateCreation() == null ? "-" : user.getDateCreation().format(DATE_HEURE));
        envoyer(emailAdmin, sujet, contenu);
    }

    @Async
    @Override
    public void envoyerConfirmationReservation(Reservation r) {
        if (r.getEmailClient() == null || r.getEmailClient().isBlank()) {
            return;   // pas d'email client : rien à envoyer
        }
        long nuits = ChronoUnit.DAYS.between(r.getDateArrivee(), r.getDateDepart());
        String sujet = "Confirmation de votre réservation n°" + r.getId() + " - " + r.getChambre().getHotel().getNom();
        String contenu = """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;border:1px solid #e9ecef;border-radius:8px;overflow:hidden">
              <div style="background:linear-gradient(87deg,#2dce89,#2dcecc);color:#fff;padding:18px 24px;font-size:18px;font-weight:bold">
                Réservation confirmée
              </div>
              <div style="padding:20px 24px;color:#32325d">
                <p>Bonjour <b>%s</b>,</p>
                <p>Nous avons le plaisir de vous confirmer votre réservation :</p>
                <table style="border-collapse:collapse">
                  <tr><td style="padding:4px 12px 4px 0"><b>Hôtel</b></td><td>%s (%s)</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Chambre</b></td><td>n° %d - %s</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Arrivée</b></td><td>%s</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Départ</b></td><td>%s</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Nuits</b></td><td>%d</td></tr>
                  <tr><td style="padding:4px 12px 4px 0"><b>Total</b></td><td><b>%.2f DT</b></td></tr>
                </table>
                <p>Nous vous souhaitons un excellent séjour.</p>
              </div>
            </div>
            """.formatted(r.getNomClient(),
                          r.getChambre().getHotel().getNom(), r.getChambre().getHotel().getVille(),
                          r.getChambre().getNumero(), r.getChambre().getLibelle(),
                          r.getDateArrivee().format(DATE), r.getDateDepart().format(DATE),
                          nuits, r.getPrixTotal());
        envoyer(r.getEmailClient(), sujet, contenu);
    }

    // envoi d'un email HTML ; une erreur d'envoi ne doit jamais bloquer l'application
    private void envoyer(String destinataire, String sujet, String contenuHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(expediteur);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(contenuHtml, true);   // true = contenu HTML
            mailSender.send(message);
            log.info(">>> Email envoyé à {} : {}", destinataire, sujet);
        } catch (Exception e) {
            log.error(">>> Échec de l'envoi de l'email à {} : {}", destinataire, e.getMessage());
        }
    }
}