package fr.xephi.authme;

import java.io.File;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;

import org.apache.commons.mail.HtmlEmail;
import org.bukkit.Bukkit;

import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.settings.Settings;

/**
 *
 * @author Xephi59
 */
public class SendMailSSL {

    public AuthMe plugin;

    public SendMailSSL(AuthMe plugin) {
        this.plugin = plugin;
    }

    public void main(final PlayerAuth auth, final String newPass) {
        String sendername;

        if (Settings.getmailSenderName == null || Settings.getmailSenderName.isEmpty()) {
            sendername = Settings.getmailAccount;
        } else {
            sendername = Settings.getmailSenderName;
        }

        final String sender = sendername;
        final int port = Settings.getMailPort;
        final String acc = Settings.getmailAccount;
        final String subject = Settings.getMailSubject;
        final String smtp = Settings.getmailSMTP;
        final String password = Settings.getmailPassword;
        final String mailText = Settings.getMailText.replace("<playername>", auth.getNickname()).replace("<servername>", plugin.getServer().getServerName()).replace("<generatedpass>", newPass);
        final String mail = auth.getEmail();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                    HtmlEmail email = new HtmlEmail();
                    email.setSmtpPort(port);
                    email.setHostName(smtp);
                    email.addTo(mail);
                    email.setFrom(acc, sender);
                    email.setSubject(subject);
                    email.setHtmlMsg(mailText);
                    email.setAuthentication(acc, password);
                    email.setStartTLSEnabled(true);
                    email.setStartTLSRequired(true);
                    email.setSSLCheckServerIdentity(true);
                    // Generate an image ?
                    File file = null;
                    if (Settings.generateImage) {
                        try {
                            ImageGenerator gen = new ImageGenerator(newPass);
                            file = new File(plugin.getDataFolder() + File.separator + auth.getNickname() + "_new_pass.jpg");
                            ImageIO.write(gen.generateImage(), "jpg", file);
                            DataSource source = new FileDataSource(file);
                            email.embed(source, auth.getNickname() + "_new_pass.jpg");
                        } catch (Exception e) {
                            ConsoleLogger.showError("Unable to send new password as image! Using normal text! Dest: " + mail);
                        }
                    }
                    try {
                        email.send();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ConsoleLogger.showError("Fail to send a mail to " + mail);
                    }
                    if (file != null)
                        file.delete();

                } catch (Exception e) {
                    // Print the stack trace
                    e.printStackTrace();
                    ConsoleLogger.showError("Some error occurred while trying to send a email to " + mail);
                }
            }

        });
    }
}
