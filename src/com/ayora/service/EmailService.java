package com.ayora.service;

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails pour les invitations de mariage.
 *
 * Supporte 3 templates :
 *   - classique : Tons roses, serif, romantique
 *   - moderne   : Dark mode, sans-serif, contemporain
 *   - luxe      : Noir et or, prestige, exclusif
 *
 * Configuration SMTP via variables statiques (a externaliser en env vars pour la production).
 */
public class EmailService {

	// === CONFIGURATION SMTP ===
	// Lus depuis les variables d'environnement (voir .env.example)
	// Aucun mot de passe en dur dans le code source.
	private static String SMTP_HOST = envOr("AYORA_SMTP_HOST", "smtp.gmail.com");
	private static int SMTP_PORT = Integer.parseInt(envOr("AYORA_SMTP_PORT", "587"));
	private static String EMAIL_FROM = envOr("AYORA_MAIL_FROM", "");
	private static String EMAIL_PASSWORD = envOr("AYORA_MAIL_PASSWORD", "");
	private static String SENDER_NAME = envOr("AYORA_MAIL_SENDER", "Ayora - Mariage");

	private Session mailSession;
	private boolean demoMode;

	private static String envOr(String key, String fallback) {
		String v = System.getenv(key);
		if (v == null || v.isEmpty()) v = System.getProperty(key);
		return (v == null || v.isEmpty()) ? fallback : v;
	}

	public EmailService() {
		// Mode demo si pas de credentials configures :
		// l'envoi est simule (log uniquement) pour ne pas planter en developpement.
		this.demoMode = (EMAIL_FROM == null || EMAIL_FROM.isEmpty()
			|| EMAIL_PASSWORD == null || EMAIL_PASSWORD.isEmpty());

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", SMTP_HOST);
		props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
		props.put("mail.smtp.ssl.trust", SMTP_HOST);
		props.put("mail.smtp.connectiontimeout", "10000");
		props.put("mail.smtp.timeout", "10000");

		mailSession = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
			}
		});

		if (demoMode) {
			System.out.println("[Ayora][EmailService] Mode DEMO actif (aucun email reel envoye). "
				+ "Definissez AYORA_MAIL_FROM et AYORA_MAIL_PASSWORD pour activer SMTP.");
		}
	}

	public static void configure(String email, String password, String senderName) {
		EMAIL_FROM = email;
		EMAIL_PASSWORD = password;
		if (senderName != null && !senderName.isEmpty()) {
			SENDER_NAME = senderName;
		}
	}

	/**
	 * Envoie une invitation par email avec le template specifie.
	 */
	public boolean sendInvitation(String toEmail, String guestName, String hostName,
			String dateMariage, String lieuMariage, String templateName, String messagePerso) {

		// Mode demo : on simule l'envoi (aucun mail reel) pour ne pas crasher en dev
		if (demoMode) {
			System.out.println("[Ayora][EmailService][DEMO] Simulation envoi a " + toEmail
				+ " | template=" + templateName + " | host=" + hostName);
			return true;
		}

		try {
			MimeMessage message = new MimeMessage(mailSession);
			message.setFrom(new InternetAddress(EMAIL_FROM, SENDER_NAME));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			message.setSubject(buildSubject(hostName, templateName), "UTF-8");

			String htmlContent = buildInvitationHtml(guestName, hostName, dateMariage, lieuMariage, templateName, messagePerso);
			message.setContent(htmlContent, "text/html; charset=UTF-8");

			Transport.send(message);
			System.out.println("[Ayora] Email envoye avec succes a : " + toEmail + " (template: " + templateName + ")");
			return true;

		} catch (MessagingException e) {
			System.err.println("[Ayora] Erreur envoi email a " + toEmail + " : " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.err.println("[Ayora] Erreur inattendue envoi email : " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private String buildSubject(String hostName, String templateName) {
		if ("video".equalsIgnoreCase(templateName)) {
			return "\u25b6 Invitation video - Mariage de " + hostName;
		}
		if ("luxe".equalsIgnoreCase(templateName)) {
			return "\u2666 Invitation Prestige - Mariage de " + hostName;
		} else if ("moderne".equalsIgnoreCase(templateName)) {
			return "Invitation | Mariage de " + hostName;
		}
		return "\u2727 Vous etes invite(e) au mariage de " + hostName;
	}

	// ==========================================
	//  TEMPLATE ROUTER
	// ==========================================

	private String buildInvitationHtml(String guestName, String hostName,
			String dateMariage, String lieuMariage, String templateName, String messagePerso) {

		// Detection auto d'un modele video : le messagePerso contient le marqueur
		// [VIDEO_INVITATION_URL]=https://... insere par le servlet.
		String videoUrl = null;
		String cleanMessage = messagePerso;
		if (messagePerso != null) {
			int idx = messagePerso.indexOf("[VIDEO_INVITATION_URL]=");
			if (idx >= 0) {
				videoUrl = messagePerso.substring(idx + "[VIDEO_INVITATION_URL]=".length()).trim();
				cleanMessage = (idx > 0) ? messagePerso.substring(0, idx).trim() : "";
			}
		}

		if ("video".equalsIgnoreCase(templateName) && videoUrl != null) {
			return buildVideoTemplate(guestName, hostName, dateMariage, lieuMariage, cleanMessage, videoUrl);
		}
		if ("luxe".equalsIgnoreCase(templateName)) {
			return buildLuxeTemplate(guestName, hostName, dateMariage, lieuMariage, cleanMessage);
		} else if ("moderne".equalsIgnoreCase(templateName)) {
			return buildModerneTemplate(guestName, hostName, dateMariage, lieuMariage, cleanMessage);
		}
		return buildClassiqueTemplate(guestName, hostName, dateMariage, lieuMariage, cleanMessage);
	}

	// ==========================================
	//  TEMPLATE 4 : VIDEO PREMIUM
	//  Lien cliquable vers la video d'invitation
	// ==========================================
	private String buildVideoTemplate(String guestName, String hostName,
			String dateMariage, String lieuMariage, String messagePerso, String videoUrl) {

		String gold = "#D4AF37";
		String bg = "#0d0904";

		StringBuilder h = new StringBuilder();
		h.append("<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'></head>");
		h.append("<body style='margin:0;padding:0;font-family:Georgia,serif;background:#000;'>");
		h.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#000;padding:24px 0;'>");
		h.append("<tr><td align='center'>");
		h.append("<table width='600' cellpadding='0' cellspacing='0' style='background:").append(bg).append(";border-radius:18px;overflow:hidden;border:1px solid rgba(212,175,55,.2);'>");

		// Top accent
		h.append("<tr><td style='height:3px;background:linear-gradient(90deg,transparent,").append(gold).append(",transparent);'></td></tr>");

		// Header
		h.append("<tr><td style='background:linear-gradient(135deg,#0d0904 0%,#3d1c00 50%,#0d0904 100%);padding:50px 30px 36px;text-align:center;'>");
		h.append("<div style='font-size:11px;letter-spacing:6px;color:").append(gold).append(";margin-bottom:14px;text-transform:uppercase;'>&#9670; Invitation Video Privee &#9670;</div>");
		h.append("<h1 style='margin:0;font-size:32px;color:").append(gold).append(";font-weight:normal;line-height:1.3;'>Vous etes convie(e)</h1>");
		h.append("<div style='width:120px;height:1px;background:linear-gradient(90deg,transparent,").append(gold).append(",transparent);margin:18px auto;'></div>");
		h.append("<p style='margin:10px 0 0;font-size:14px;color:rgba(212,175,55,.7);letter-spacing:2px;'>au mariage de</p>");
		h.append("<h2 style='margin:10px 0 0;font-size:26px;color:").append(gold).append(";font-weight:normal;letter-spacing:1px;'>").append(esc(hostName)).append("</h2>");
		h.append("</td></tr>");

		// Body
		h.append("<tr><td style='padding:34px 38px;text-align:center;'>");
		h.append("<p style='font-size:17px;color:rgba(212,175,55,.85);line-height:1.6;margin:0 0 14px;'>Cher(e) <strong style='color:").append(gold).append(";'>").append(esc(guestName)).append("</strong>,</p>");
		h.append("<p style='font-size:14px;color:rgba(212,175,55,.55);line-height:1.7;margin:0 0 24px;'>Nous avons l'honneur de vous transmettre notre invitation video personnalisee.<br>Decouvrez-la en cliquant ci-dessous :</p>");

		// Big CTA
		h.append("<table cellpadding='0' cellspacing='0' style='margin:8px auto 4px;'><tr><td>");
		h.append("<a href='").append(esc(videoUrl)).append("' target='_blank' rel='noopener' style='display:inline-block;background:linear-gradient(135deg,#D4AF37,#B8922F);color:#0d0904;font-family:Georgia,serif;font-size:16px;font-weight:bold;letter-spacing:2px;padding:18px 42px;border-radius:50px;text-decoration:none;box-shadow:0 8px 28px rgba(212,175,55,.35);'>");
		h.append("&#9658;&nbsp;&nbsp;Voir l'invitation video");
		h.append("</a>");
		h.append("</td></tr></table>");

		h.append("<p style='font-size:11px;color:rgba(212,175,55,.4);margin:18px 0 0;letter-spacing:1px;font-style:italic;'>Si le bouton ne fonctionne pas, copiez ce lien :<br>");
		h.append("<a href='").append(esc(videoUrl)).append("' style='color:").append(gold).append(";word-break:break-all;'>").append(esc(videoUrl)).append("</a></p>");

		// Details
		appendDetailsBlock(h, dateMariage, lieuMariage, gold, gold,
			"background:rgba(212,175,55,.04);border:1px solid rgba(212,175,55,.15);border-radius:12px;padding:24px;margin:30px 0 16px;");

		// Message perso
		if (messagePerso != null && !messagePerso.isEmpty()) {
			h.append("<div style='border-left:3px solid ").append(gold).append(";padding:14px 20px;margin:22px 0 0;text-align:left;background:rgba(212,175,55,.03);border-radius:0 8px 8px 0;'>");
			h.append("<p style='margin:0;font-size:14px;color:rgba(212,175,55,.7);font-style:italic;'>\"").append(esc(messagePerso)).append("\"</p>");
			h.append("</div>");
		}

		h.append("<p style='font-size:13px;color:rgba(212,175,55,.5);margin:30px 0 0;font-style:italic;'>Votre presence honorera cette celebration.</p>");
		h.append("</td></tr>");

		// Footer
		h.append("<tr><td style='background:rgba(212,175,55,.03);padding:22px 30px;text-align:center;border-top:1px solid rgba(212,175,55,.08);'>");
		h.append("<div style='font-size:14px;color:").append(gold).append(";margin-bottom:6px;'>&#9670;</div>");
		h.append("<p style='margin:0;font-size:12px;color:rgba(212,175,55,.5);'>Invitation Video Premium &mdash; envoyee via Ayora</p>");
		h.append("</td></tr>");

		h.append("<tr><td style='height:3px;background:linear-gradient(90deg,transparent,").append(gold).append(",transparent);'></td></tr>");

		h.append("</table></td></tr></table></body></html>");
		return h.toString();
	}

	// ==========================================
	//  TEMPLATE 1 : CLASSIQUE ELEGANT
	//  Tons roses, serif, romantique
	// ==========================================

	private String buildClassiqueTemplate(String guestName, String hostName,
			String dateMariage, String lieuMariage, String messagePerso) {

		String primary = "#8B1A4A";
		String accent = "#D4A574";
		String bgSoft = "#FDF2F8";

		StringBuilder h = new StringBuilder();
		h.append("<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'></head>");
		h.append("<body style='margin:0;padding:0;font-family:Georgia,\"Times New Roman\",serif;background:#f5f0eb;'>");
		h.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#f5f0eb;padding:24px 0;'>");
		h.append("<tr><td align='center'>");
		h.append("<table width='600' cellpadding='0' cellspacing='0' style='background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>");

		// Header
		h.append("<tr><td style='background:linear-gradient(135deg,").append(bgSoft).append(" 0%,#FECDD3 50%,").append(bgSoft).append(" 100%);padding:44px 30px;text-align:center;'>");
		h.append("<div style='font-size:13px;letter-spacing:5px;color:").append(accent).append(";margin-bottom:12px;'>&#10047; INVITATION AU MARIAGE &#10047;</div>");
		h.append("<h1 style='margin:0;font-size:32px;color:").append(primary).append(";font-weight:normal;font-style:italic;line-height:1.3;'>Vous &ecirc;tes invite(e)</h1>");
		h.append("<div style='width:80px;height:2px;background:").append(accent).append(";margin:16px auto;'></div>");
		h.append("<p style='margin:10px 0 0;font-size:16px;color:#888;'>au mariage de</p>");
		h.append("<h2 style='margin:8px 0 0;font-size:28px;color:").append(primary).append(";font-weight:normal;'>").append(esc(hostName)).append("</h2>");
		h.append("</td></tr>");

		// Body
		h.append("<tr><td style='padding:32px 40px;text-align:center;'>");
		h.append("<p style='font-size:18px;color:#444;line-height:1.6;margin:0 0 20px;'>Cher(e) <strong style='color:").append(primary).append(";'>").append(esc(guestName)).append("</strong>,</p>");
		h.append("<p style='font-size:16px;color:#666;line-height:1.8;margin:0 0 24px;'>Nous avons l'immense joie de vous inviter &agrave; partager avec nous ce moment unique et inoubliable.</p>");

		// Details
		appendDetailsBlock(h, dateMariage, lieuMariage, primary, accent,
			"background:linear-gradient(135deg," + bgSoft + ",#FECDD3);border-radius:12px;padding:28px;margin:20px 0;");

		// Message perso
		appendMessagePerso(h, messagePerso, accent, "#FFF9FB", "#555");

		h.append("<p style='font-size:15px;color:#aaa;margin:28px 0 0;line-height:1.6;font-style:italic;'>Votre pr&eacute;sence serait pour nous le plus beau des cadeaux.</p>");
		h.append("</td></tr>");

		// Footer
		h.append("<tr><td style='background:").append(primary).append(";padding:22px 30px;text-align:center;'>");
		h.append("<p style='margin:0;font-size:13px;color:rgba(255,255,255,0.7);'>Invitation envoy&eacute;e via Ayora - Planification de Mariage &agrave; F&egrave;s</p>");
		h.append("<p style='margin:6px 0 0;font-size:11px;color:rgba(255,255,255,0.4);'>&#10084; Avec tout notre amour &#10084;</p>");
		h.append("</td></tr>");

		h.append("</table></td></tr></table></body></html>");
		return h.toString();
	}

	// ==========================================
	//  TEMPLATE 2 : MODERNE MINIMAL
	//  Dark mode, sans-serif, contemporain
	// ==========================================

	private String buildModerneTemplate(String guestName, String hostName,
			String dateMariage, String lieuMariage, String messagePerso) {

		String primary = "#E8A0BF";
		String bg = "#1a1a2e";
		String textMain = "rgba(255,255,255,0.85)";
		String textMuted = "rgba(255,255,255,0.45)";

		StringBuilder h = new StringBuilder();
		h.append("<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'></head>");
		h.append("<body style='margin:0;padding:0;font-family:\"Helvetica Neue\",Arial,sans-serif;background:#0f0f1e;'>");
		h.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#0f0f1e;padding:24px 0;'>");
		h.append("<tr><td align='center'>");
		h.append("<table width='600' cellpadding='0' cellspacing='0' style='background:").append(bg).append(";border-radius:16px;overflow:hidden;border:1px solid rgba(255,255,255,0.06);'>");

		// Header
		h.append("<tr><td style='background:linear-gradient(160deg,#1a1a2e 0%,#16213e 50%,#0f3460 100%);padding:48px 30px;text-align:center;'>");
		h.append("<div style='font-size:10px;letter-spacing:8px;color:").append(primary).append(";margin-bottom:18px;text-transform:uppercase;'>Invitation</div>");
		h.append("<h1 style='margin:0;font-size:28px;color:#fff;font-weight:300;letter-spacing:2px;line-height:1.3;'>Vous &ecirc;tes invite(e)</h1>");
		h.append("<div style='width:40px;height:1px;background:").append(primary).append(";margin:18px auto;'></div>");
		h.append("<p style='margin:10px 0 0;font-size:14px;color:").append(textMuted).append(";letter-spacing:2px;text-transform:uppercase;'>au mariage de</p>");
		h.append("<h2 style='margin:10px 0 0;font-size:24px;color:#fff;font-weight:400;letter-spacing:1px;'>").append(esc(hostName)).append("</h2>");
		h.append("</td></tr>");

		// Body
		h.append("<tr><td style='padding:32px 40px;text-align:center;'>");
		h.append("<p style='font-size:17px;color:").append(textMain).append(";line-height:1.6;margin:0 0 18px;'>Cher(e) <strong style='color:").append(primary).append(";'>").append(esc(guestName)).append("</strong>,</p>");
		h.append("<p style='font-size:15px;color:").append(textMuted).append(";line-height:1.8;margin:0 0 24px;'>Nous avons l'immense joie de vous inviter &agrave; partager avec nous ce moment unique et inoubliable.</p>");

		// Details
		appendDetailsBlock(h, dateMariage, lieuMariage, "#fff", primary,
			"background:rgba(232,160,191,0.06);border:1px solid rgba(232,160,191,0.12);border-radius:12px;padding:28px;margin:20px 0;");

		// Message perso
		if (messagePerso != null && !messagePerso.isEmpty()) {
			h.append("<div style='border-left:3px solid ").append(primary).append(";padding:14px 20px;margin:24px 0;text-align:left;background:rgba(255,255,255,0.02);border-radius:0 8px 8px 0;'>");
			h.append("<p style='margin:0;font-size:14px;color:").append(textMuted).append(";font-style:italic;'>\"").append(esc(messagePerso)).append("\"</p>");
			h.append("</div>");
		}

		h.append("<p style='font-size:14px;color:").append(textMuted).append(";margin:28px 0 0;'>Votre pr&eacute;sence serait pour nous le plus beau des cadeaux.</p>");
		h.append("</td></tr>");

		// Footer
		h.append("<tr><td style='background:rgba(255,255,255,0.02);padding:20px 30px;text-align:center;border-top:1px solid rgba(255,255,255,0.04);'>");
		h.append("<p style='margin:0;font-size:12px;color:").append(textMuted).append(";'>Invitation envoy&eacute;e via Ayora</p>");
		h.append("</td></tr>");

		h.append("</table></td></tr></table></body></html>");
		return h.toString();
	}

	// ==========================================
	//  TEMPLATE 3 : PREMIUM DORE
	//  Noir profond, or, prestige
	// ==========================================

	private String buildLuxeTemplate(String guestName, String hostName,
			String dateMariage, String lieuMariage, String messagePerso) {

		String gold = "#D4AF37";
		String goldMuted = "rgba(212,175,55,0.6)";
		String textMuted = "rgba(212,175,55,0.45)";
		String bg = "#1a0a00";

		StringBuilder h = new StringBuilder();
		h.append("<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'></head>");
		h.append("<body style='margin:0;padding:0;font-family:Georgia,\"Times New Roman\",serif;background:#0d0500;'>");
		h.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#0d0500;padding:24px 0;'>");
		h.append("<tr><td align='center'>");
		h.append("<table width='600' cellpadding='0' cellspacing='0' style='background:").append(bg).append(";border-radius:16px;overflow:hidden;border:1px solid rgba(212,175,55,0.15);'>");

		// Top gold accent line
		h.append("<tr><td style='height:3px;background:linear-gradient(90deg,transparent,").append(gold).append(",transparent);'></td></tr>");

		// Header
		h.append("<tr><td style='background:linear-gradient(135deg,#1a0a00 0%,#3d1c00 40%,#1a0a00 100%);padding:48px 30px;text-align:center;'>");
		h.append("<div style='font-size:11px;letter-spacing:6px;color:").append(gold).append(";margin-bottom:14px;'>&#9670; INVITATION PRESTIGE &#9670;</div>");
		h.append("<div style='width:120px;height:1px;background:linear-gradient(90deg,transparent,").append(gold).append(",transparent);margin:0 auto 14px;'></div>");
		h.append("<h1 style='margin:0;font-size:30px;color:").append(gold).append(";font-weight:normal;line-height:1.3;'>Vous &ecirc;tes invite(e)</h1>");
		h.append("<div style='width:120px;height:1px;background:linear-gradient(90deg,transparent,").append(gold).append(",transparent);margin:14px auto;'></div>");
		h.append("<p style='margin:12px 0 0;font-size:14px;color:").append(goldMuted).append(";letter-spacing:2px;'>au mariage de</p>");
		h.append("<h2 style='margin:10px 0 0;font-size:26px;color:").append(gold).append(";font-weight:normal;letter-spacing:1px;'>").append(esc(hostName)).append("</h2>");
		h.append("<div style='margin-top:14px;font-size:16px;color:").append(gold).append(";'>&#9670; &#9670; &#9670;</div>");
		h.append("</td></tr>");

		// Body
		h.append("<tr><td style='padding:32px 40px;text-align:center;'>");
		h.append("<p style='font-size:17px;color:").append(goldMuted).append(";line-height:1.6;margin:0 0 18px;'>Cher(e) <strong style='color:").append(gold).append(";'>").append(esc(guestName)).append("</strong>,</p>");
		h.append("<p style='font-size:15px;color:").append(textMuted).append(";line-height:1.8;margin:0 0 24px;'>Nous avons l'immense honneur de vous convier &agrave; c&eacute;l&eacute;brer avec nous cette union sacr&eacute;e.</p>");

		// Details with gold border
		appendDetailsBlock(h, dateMariage, lieuMariage, gold, gold,
			"background:rgba(212,175,55,0.04);border:1px solid rgba(212,175,55,0.15);border-radius:12px;padding:28px;margin:20px 0;");

		// Message perso
		if (messagePerso != null && !messagePerso.isEmpty()) {
			h.append("<div style='border-left:3px solid ").append(gold).append(";padding:14px 20px;margin:24px 0;text-align:left;background:rgba(212,175,55,0.03);border-radius:0 8px 8px 0;'>");
			h.append("<p style='margin:0;font-size:14px;color:").append(goldMuted).append(";font-style:italic;'>\"").append(esc(messagePerso)).append("\"</p>");
			h.append("</div>");
		}

		h.append("<p style='font-size:14px;color:").append(textMuted).append(";margin:28px 0 0;'>Votre pr&eacute;sence honorera cette c&eacute;l&eacute;bration de la plus belle des mani&egrave;res.</p>");
		h.append("</td></tr>");

		// Footer
		h.append("<tr><td style='background:rgba(212,175,55,0.03);padding:20px 30px;text-align:center;border-top:1px solid rgba(212,175,55,0.08);'>");
		h.append("<div style='font-size:14px;color:").append(gold).append(";margin-bottom:6px;'>&#9670;</div>");
		h.append("<p style='margin:0;font-size:12px;color:").append(textMuted).append(";'>Invitation Prestige envoy&eacute;e via Ayora</p>");
		h.append("</td></tr>");

		// Bottom gold accent line
		h.append("<tr><td style='height:3px;background:linear-gradient(90deg,transparent,").append(gold).append(",transparent);'></td></tr>");

		h.append("</table></td></tr></table></body></html>");
		return h.toString();
	}

	// ==========================================
	//  HELPERS PARTAGES
	// ==========================================

	private void appendDetailsBlock(StringBuilder h, String dateMariage, String lieuMariage,
			String valueColor, String labelColor, String containerStyle) {

		h.append("<table width='100%' cellpadding='0' cellspacing='0' style='").append(containerStyle).append("'>");
		h.append("<tr><td style='text-align:center;'>");

		boolean hasDate = dateMariage != null && !dateMariage.isEmpty();
		boolean hasLieu = lieuMariage != null && !lieuMariage.isEmpty();

		if (hasDate) {
			h.append("<div style='margin-bottom:").append(hasLieu ? "16px" : "0").append(";'>");
			h.append("<div style='font-size:11px;letter-spacing:3px;color:").append(labelColor).append(";margin-bottom:5px;'>DATE</div>");
			h.append("<div style='font-size:20px;color:").append(valueColor).append(";font-weight:bold;'>").append(esc(dateMariage)).append("</div>");
			h.append("</div>");
		}
		if (hasLieu) {
			h.append("<div>");
			h.append("<div style='font-size:11px;letter-spacing:3px;color:").append(labelColor).append(";margin-bottom:5px;'>LIEU</div>");
			h.append("<div style='font-size:20px;color:").append(valueColor).append(";font-weight:bold;'>").append(esc(lieuMariage)).append("</div>");
			h.append("</div>");
		}
		if (!hasDate && !hasLieu) {
			h.append("<div style='font-size:15px;color:").append(labelColor).append(";font-style:italic;'>Les d&eacute;tails vous seront communiqu&eacute;s prochainement</div>");
		}

		h.append("</td></tr></table>");
	}

	private void appendMessagePerso(StringBuilder h, String messagePerso, String borderColor,
			String bgColor, String textColor) {
		if (messagePerso != null && !messagePerso.isEmpty()) {
			h.append("<div style='border-left:3px solid ").append(borderColor).append(";padding:14px 20px;margin:24px 0;text-align:left;background:").append(bgColor).append(";border-radius:0 8px 8px 0;'>");
			h.append("<p style='margin:0;font-size:14px;color:").append(textColor).append(";font-style:italic;'>\"").append(esc(messagePerso)).append("\"</p>");
			h.append("</div>");
		}
	}

	private String esc(String text) {
		if (text == null) return "";
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
