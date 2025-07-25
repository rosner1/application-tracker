package com.application_tracker;

import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.internet.MimeMessage;
import com.sun.mail.imap.IMAPFolder;

import java.io.IOException;
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


        while (true) {
            try {
                if (!inbox.isOpen()) {
                    inbox.open(Folder.READ_ONLY);
                }
                ((IMAPFolder)inbox).idle();
            } catch (FolderClosedException e) {
                System.out.println("Folder closed by server, reopening...");
                inbox = (IMAPFolder) store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
            }
        }


        // inbox.close(false);
        // store.close();
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
            Message newMessage = messages[messages.length - 1];

            System.out.println("Start: " + messages.length);


            if (newMessage.isMimeType("text/plain")) {
                openInbox(inbox);
                System.out.println((String) newMessage.getContent());
            } else if (newMessage.isMimeType("multipart/*")) {
                openInbox(inbox);
                Multipart multipart = (Multipart) newMessage.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
        
                    if (part.isMimeType("text/plain")) {
                        System.out.println((String) part.getContent());
                    }
                }
            }
            System.out.println("Done");


        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openInbox(Folder inbox) {
        if (!inbox.isOpen()) {
            try {
                inbox.open(Folder.READ_ONLY);
                System.out.println("Open");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

}

