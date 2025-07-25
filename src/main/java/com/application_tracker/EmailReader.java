package com.application_tracker;

import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.internet.MimeMessage;
import com.sun.mail.imap.IMAPFolder;

import java.util.Properties;

public class EmailReader {
    public static void main(String[] args) throws Exception {
        String host = "imap.gmail.com";
        String username = System.getenv("EMAIL_ADDRESS");
        String password = System.getenv("EMAIL_PASSWORD");

        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", host);
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");

        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore("imap");
        store.connect(host, username, password);

        Folder inbox = store.getFolder("INBOX");
        listen(inbox);


        inbox.open(Folder.READ_ONLY);
        ((IMAPFolder) inbox).idle();


        inbox.close(false);
        store.close();
    }

    public static void listen(Folder inbox) throws MessagingException{
        inbox.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                System.out.println("New message received: " + e.getMessages().length);
                handleNewMessage(inbox);
            }
        });
    }

    public static void handleNewMessage(Folder inbox) {
        try {
            Message[] messages = inbox.getMessages();
            Message newMessage = messages[0];

            String content = newMessage.getContentType();
            System.out.println(content);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}

