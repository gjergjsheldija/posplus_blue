//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.forms;

import com.openbravo.pos.config.JFrmConfig;
import java.awt.BorderLayout;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import com.openbravo.pos.instance.AppMessage;
import com.openbravo.pos.instance.InstanceManager;
import java.io.IOException;
import javax.imageio.ImageIO;

import util.Util;
import java.security.*;
import java.util.prefs.Preferences;

/**
 *
 * @author  adrianromero
 */
public class JRootFrame extends javax.swing.JFrame implements AppMessage {
    
    // Gestor de que haya solo una instancia corriendo en cada maquina.
    private InstanceManager m_instmanager = null;
    
    private JRootApp m_rootapp;
    private AppProperties m_props;

    public static final int TOTAL_RUNS = 50;
    public static final int TIMES_RUN = 0;
    public static final String PREF_KEY = "exec_time";
    
    /** Creates new form JRootFrame */
    public JRootFrame() {
        
        initComponents();    
    }
    
    public void initFrame(AppProperties props) {
        
        m_props = props;
        
        m_rootapp = new JRootApp();
        boolean demo = true;
        int execTimes = RegistryValue();
        
        if (m_rootapp.initApp(m_props)) {


            if ("true".equals(props.getProperty("machine.uniqueinstance"))) {
                // Register the running application
                try {
                    m_instmanager = new InstanceManager(this);
                } catch (Exception e) {
                }
            }
        
            // Show the application
            add(m_rootapp, BorderLayout.CENTER);            
 
            try {
                this.setIconImage(ImageIO.read(JRootFrame.class.getResourceAsStream("/com/openbravo/images/favicon.png")));
            } catch (IOException e) {
            }  
            
            Long cpuId = Util.getMacAddressLong();
            String licenseNumber = m_props.getProperty("user.license");
            String username = m_props.getProperty("user.username");
            String licenseCheck = plainStringToMD5(cpuId.toString() + username);

            if(licenseNumber.compareTo(licenseCheck) == 0){
                demo = false;
            } else {
                if(execTimes == 1) {
                    javax.swing.JOptionPane.showConfirmDialog((java.awt.Component) null,
                            "Kjo eshte hera e FUNDIT qe programi ekzekutohet!\n"
                            + "Per me shume kontaktoni : info@acme-tech.net",
                            "Informacion rreth licenses",
                    javax.swing.JOptionPane.DEFAULT_OPTION);
                }else if(execTimes == 0) {
                    m_rootapp.tryToClose();
                } else {
                    javax.swing.JOptionPane.showConfirmDialog((java.awt.Component) null,
                            "Kan mbetur edhe " + execTimes + " ekzekutime para se programi te mbyllet!\n"
                            + "Per me shume kontaktoni : info@acme-tech.net",
                            "Informacion rreth licenses",
                    javax.swing.JOptionPane.DEFAULT_OPTION);
                }
            }

            if(demo == true) {
                setTitle(AppLocal.APP_NAME + " - " + AppLocal.APP_VERSION + " - VERSION DEMOSTRATIV" + " - Ekzekutime te mbetura : " + execTimes);
            } else {
                setTitle(AppLocal.APP_NAME + " - " + AppLocal.APP_VERSION + " - Licensuar : " +  username);
            }
            pack();
            setLocationRelativeTo(null);        
            
            setVisible(true);                        
        } else {
            new JFrmConfig(props).setVisible(true); // Show the configuration window.
        }
    }

    public int RegistryValue() {

        // HKLM\Software\JavaSoft\Prefs\
        Preferences systemPref = Preferences.systemRoot();

        int timesrun = systemPref.getInt(PREF_KEY,TIMES_RUN);
        systemPref.putInt(PREF_KEY, timesrun + 1);

        return TOTAL_RUNS - systemPref.getInt(PREF_KEY, TIMES_RUN);
    }

    public String plainStringToMD5(String input) { // Some stuff we will use later
        MessageDigest md = null;
        byte[] byteHash = null;
        StringBuffer resultString = new StringBuffer();
        // Bad things can happen here
        try { // Choose between MD5 and SHA1
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        }
        // Reset is always good
        md.reset();
        // We really need some conversion here
        md.update(input.getBytes());
        // There goes the hash
        byteHash = md.digest();
        // Now here comes the best part
        for (int i = 0; i < byteHash.length; i++) {
            resultString.append(Integer.toHexString(0xFF & byteHash[i]));
        }
        // That's it!
        return (resultString.toString());
    }

    public void restoreWindow() throws RemoteException {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (getExtendedState() == JFrame.ICONIFIED) {
                    setExtendedState(JFrame.NORMAL);
                }
                requestFocus();
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        m_rootapp.tryToClose();
        
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed

        System.exit(0);
        
    }//GEN-LAST:event_formWindowClosed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
