/*
 * Copyright (C) 2014 DandC89
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rimpro.pkg2.pkg0;

import com.sun.mail.imap.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Flags;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 * @author Boatswain & Record Keeper
 */
public class JavamailExtraction {

    private static List<String> messages = new ArrayList();
    private static List<Referral> referralArrayList = new ArrayList();

    public static void getMail(String protocol, String host, String user, String password) {
        try {
            // Create Properties object
            Properties props = System.getProperties();

            // Create Session object
            Session session = Session.getInstance(props, null);
            session.setDebug(false);

            // Creat Store object
            Store store = session.getStore(protocol);

            // Connect to the Mother Ship!!
            store.connect(host, 993, user, password);

            // Open the folder
            Folder folder = store.getDefaultFolder();
            folder = folder.getFolder("INBOX");
            try {
                folder.open(Folder.READ_WRITE);
            } catch (MessagingException ex) {
                folder.open(Folder.READ_ONLY);
            }

            // Start reading messages
            int totalMessages = folder.getMessageCount();

            if (totalMessages == 0) {
                System.out.println("There are no messages");
                folder.close(false);
                store.close();
                System.exit(1);
            }

            System.out.println("There are: " + folder.getNewMessageCount() + " new messages");
            System.out.println("There are: " + totalMessages + " total messages");

            // Beam up the messages Scotty...
            Message[] msgs = folder.getMessages();

            // FetchProfile...?
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add("X-Mailer");
            folder.fetch(msgs, fp);

            Folder sent = store.getFolder("Sent");
            if (!sent.exists()) {
                sent.create(Folder.HOLDS_MESSAGES);
            }

            for (Message msg : msgs) {
                //System.out.println(msg.getContent().toString());
                if (msg.getFolder() != sent){
                    messages.add(msg.getContent().toString());
                }
            }
            sent.appendMessages(msgs);
            
            for(Message msg : msgs) {
                msg.setFlag(Flags.Flag.DELETED, true);
            }

            folder.close(false);
            store.close();

        } catch (Exception ex) {
            System.out.println("Oops, you fail! " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static List<Referral> extractContent() throws IOException {
        System.out.println(messages.size());
        for (String msg : messages) {
            Referral ref = new Referral();
            StringReader temp = new StringReader(msg);
            BufferedReader buff = new BufferedReader(temp);
            
            // read through header and read email
            String str;
            while((str = buff.readLine()) != null) {
                if(str.length() < 5) {
                    continue;
                }
                if(str.substring(0, 5).equals("Email")) {
                    ref.setEmail(str.split(":")[1]);
                    break; // finished reading through the header and ready to read the information of the referral
                }
            }
            
            // read name
            buff.readLine();
            str = buff.readLine();
            ref.setName(str.split(":")[1].trim());
            System.out.println(ref.getName());
            
            // read address
            buff.readLine();
            str = buff.readLine();
            ref.setStreetName(str.split(":")[1].trim());
            //System.out.println(ref.getStreetName());
            
            // read postcode
            buff.readLine();
            str = buff.readLine();
            ref.setFullPostcode(str.split(":")[1].trim());
            //System.out.println(ref.getPostCode());
            
            // read city
            buff.readLine();
            str = buff.readLine();
            ref.setCity(str.split(":")[1].trim());
            //System.out.println(ref.getCity());
            
            // read country
            buff.readLine();
            str = buff.readLine();
            ref.setCountry(str.split(":")[1].toLowerCase().trim());
            //System.out.println(ref.getCountry());
            
            // read phone number
            buff.readLine();
            str = buff.readLine();
            ref.setPhone(str.split(":")[1].trim());
            //System.out.println(ref.getPhone());
            
            buff.close();
            
            referralArrayList.add(ref);
        }
        return referralArrayList;
    }
}
