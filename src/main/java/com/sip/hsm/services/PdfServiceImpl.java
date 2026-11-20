package com.sip.hsm.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sip.hsm.entities.Chambre;
import com.sip.hsm.entities.Hotel;
import com.sip.hsm.entities.Reservation;
import com.sip.hsm.entities.StatutReservation;
import com.sip.hsm.exceptions.BusinessException;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class PdfServiceImpl implements PdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color VIOLET = new Color(94, 114, 228);    // couleur principale d'Argon
    private static final Color GRIS = new Color(136, 152, 170);
    private static final Color GRIS_CLAIR = new Color(246, 249, 252);

    @Override
    public byte[] genererFacture(Reservation r) {
        // règle métier : facture seulement pour une réservation confirmée ou terminée
        if (r.getStatut() != StatutReservation.CONFIRMEE && r.getStatut() != StatutReservation.TERMINEE) {
            throw new BusinessException("La facture n'est disponible que pour une réservation confirmée ou terminée"
                    + " (statut actuel : " + r.getStatut() + ")");
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Chambre ch = r.getChambre();
            Hotel h = ch.getHotel();
            long nuits = ChronoUnit.DAYS.between(r.getDateArrivee(), r.getDateDepart());

            // polices
            Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, VIOLET);
            Font grand = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Font gras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font petit = FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS);
            Font blanc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font total = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, VIOLET);

            // ===== EN-TÊTE : hôtel à gauche, "FACTURE" à droite =====
            PdfPTable entete = new PdfPTable(2);
            entete.setWidthPercentage(100);

            PdfPCell gauche = new PdfPCell();
            gauche.setBorder(Rectangle.NO_BORDER);
            gauche.addElement(new Paragraph(h.getNom(), titre));
            gauche.addElement(new Paragraph(h.getAdresse() + ", " + h.getVille(), normal));
            gauche.addElement(new Paragraph("Tél : " + nvl(h.getTel()) + "   -   " + nvl(h.getEmail()), normal));
            entete.addCell(gauche);

            PdfPCell droite = new PdfPCell();
            droite.setBorder(Rectangle.NO_BORDER);
            Paragraph pFacture = new Paragraph("FACTURE", grand);
            pFacture.setAlignment(Element.ALIGN_RIGHT);
            Paragraph pNumero = new Paragraph("N° RES-" + String.format("%04d", r.getId()), gras);
            pNumero.setAlignment(Element.ALIGN_RIGHT);
            Paragraph pDate = new Paragraph("Date : " + LocalDate.now().format(DATE), normal);
            pDate.setAlignment(Element.ALIGN_RIGHT);
            droite.addElement(pFacture);
            droite.addElement(pNumero);
            droite.addElement(pDate);
            entete.addCell(droite);

            doc.add(entete);

            // ===== CLIENT =====
            Paragraph client = new Paragraph();
            client.setSpacingBefore(25);
            client.add(new Chunk("Facturé à : ", gras));
            client.add(new Chunk(r.getNomClient() + "   -   Tél : " + r.getTelClient()
                    + (r.getEmailClient() != null ? "   -   " + r.getEmailClient() : ""), normal));
            doc.add(client);

            // ===== TABLEAU DES PRESTATIONS =====
            PdfPTable table = new PdfPTable(new float[]{4f, 3f, 1f, 1.6f, 1.6f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(15);

            for (String colonne : new String[]{"Désignation", "Période", "Nuits", "Prix / nuit", "Total"}) {
                PdfPCell c = new PdfPCell(new Phrase(colonne, blanc));
                c.setBackgroundColor(VIOLET);
                c.setPadding(7);
                c.setBorderColor(VIOLET);
                table.addCell(c);
            }
            table.addCell(cellule("Chambre n° " + ch.getNumero() + " - " + ch.getLibelle()
                    + " (" + ch.getType() + ")", normal));
            table.addCell(cellule("du " + r.getDateArrivee().format(DATE)
                    + " au " + r.getDateDepart().format(DATE), normal));
            table.addCell(cellule(String.valueOf(nuits), normal));
            table.addCell(cellule(String.format("%.2f DT", ch.getPrix()), normal));
            table.addCell(cellule(String.format("%.2f DT", r.getPrixTotal()), gras));
            doc.add(table);

            // ===== TOTAL =====
            Paragraph pTotal = new Paragraph("Total à payer : " + String.format("%.3f DT", r.getPrixTotal()), total);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            pTotal.setSpacingBefore(15);
            doc.add(pTotal);

            Paragraph pInfos = new Paragraph("Nombre de personnes : " + r.getNbPersonnes()
                    + "   -   Statut : " + r.getStatut(), petit);
            pInfos.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pInfos);

            // ===== PIED DE PAGE =====
            Paragraph merci = new Paragraph("Merci de votre confiance et excellent séjour !", gras);
            merci.setAlignment(Element.ALIGN_CENTER);
            merci.setSpacingBefore(40);
            doc.add(merci);

            Paragraph genere = new Paragraph("Document généré automatiquement par Hotels Manager", petit);
            genere.setAlignment(Element.ALIGN_CENTER);
            doc.add(genere);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de la facture PDF : " + e.getMessage(), e);
        }
    }

    // cellule de tableau avec fond gris clair
    private PdfPCell cellule(String texte, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(texte, font));
        c.setPadding(7);
        c.setBackgroundColor(GRIS_CLAIR);
        c.setBorderColor(new Color(233, 236, 239));
        return c;
    }

    private String nvl(String valeur) {
        return valeur == null ? "-" : valeur;
    }
}
