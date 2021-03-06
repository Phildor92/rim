/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rimpro.pkg2.pkg0;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author Secretary
 */
public class RImPro20 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean test = true; // if true, no text message is sent, if false text messages are sent
        List<Referral> referrals = new ArrayList();
        
        // Get mail using Javamail and Input data from emails into referral list
        try {
            String protocol = "imaps";
            String host = "imap.gmail.com";
            String user = "belnethreferral@gmail.com";
            String password = "Moroni10";
            JavamailExtraction.getMail(protocol, host, user, password);
            referrals = JavamailExtraction.extractContent();
        } catch (IOException ex) {
            Logger.getLogger(JavamailExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Initialize areaNumber and postCodeArea lists
        Area_number areaNumber = new Area_number();
        areaNumber.readFile();
        areaNumber.generateAreaZoneIndex();
        
        PostcodeArea postcode = new PostcodeArea();
        
        // Assign area and area phone numbers
        for(Referral ref : referrals) {
            try {
                ref.setAssignedArea(postcode.getArea(ref.getPostCode(), ref.isNLD()));
                if(ref.getAssignedArea().equals("null")) {
                    ref.setValid(false);
                    throw new myException("Referral ID: " + ref.getId() + " with Name: " + ref.getName() + " has an invalid postcode");
                }
                else
                    ref.setValid(true);
            } catch (myException e) {
                System.out.println(e.getMessage());
                continue;
            }
            
            if(ref.isValid()) {
                System.out.println("Assigned Area: " + ref.getAssignedArea());
                ref.setAreaPhone(areaNumber.chooseNumber(ref.getAssignedArea()));
                ref.setZone(areaNumber.getZone(ref.getAssignedArea()));
            }
        }
        
        // Send SMS
        if(!test){
            for (Referral ref : referrals) {
                if(ref.isValid()) {
                    MessageBirdApi smsApi = new MessageBirdApi();

                    smsApi.authenticate("bbn2015900", "8ldsleiden");        //authenticate with MessageBird SMS API
                    smsApi.setSender("Office");                     //set the name or number from where the message come from
                    smsApi.addDestination(ref.getAreaPhone().toString()); //add number to destination list, this function can be called multiple times for more receivers
                    smsApi.setReference(ref.getId());                   //your unique reference
                    //smsApi.setTimestamp(2012, 2, 27, 11, 30);         //only use if you want to schedule message
                    try {
                        smsApi.send("Facebook referral from the My Family Campaign. The book has been sent out today. \n " + ref.toString()); //send the message to the receiver(s)
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(RImPro20.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(smsApi.getResponseCode().equals("01")) {
                        ref.setSent(true);
                        System.out.println("sent message: " + ref.getId());

                    }
                    else
                        ref.setSent(false);
                }
                //SENTref.setSent(false);
            }
        }
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = new Date();
         // Export to csv file and to Google Sheets
        try {
            DataToCsv writer = new DataToCsv(referrals);
            writer.GenerateCsvFile("Z:\\12 Supervisor\\Referral Improvement Program\\ReferralLog.csv");
            //writer.GenerateCsvFile("Z:\\12 Supervisor\\Referral Improvement Program\\" + dateFormat.format(date) + " ReferralLog.csv");
            writer.pushToGoogleSheets();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
}
