package com.icad.maskeneditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import com.icad.utilities.CustomScrollPane;

/**
 *
 * @author  Sebastian
 */
public class MaskTemplateChooserDialog extends JDialog implements ActionListener{
    
    /** Creates a new instance of MaskeneditorVolagenDialog */
    public MaskTemplateChooserDialog(JFrame parent) {
        super(parent,"Vorlage auswählen",true);
        
        setSize(200,300);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        setLocation((int)(d.getWidth() - 200) / 2,(int)(d.getHeight() - 300) / 2);
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        buttonPane = new JPanel();
        //buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT,20,0));
        buttonPane.setLayout(new GridLayout(13,1,5,5));
        contentPane.add(new CustomScrollPane(buttonPane));
        
        group = new ButtonGroup();
        
        addRadioButton(buttonPane, group, "Standardvorlage",true);
        addRadioButton(buttonPane, group, "Standardmodul", true);
        
        
        JPanel buttonPanel = new JPanel();
        
        contentPane.add(buttonPanel,"South");
        
        okButton = new JButton("Ok");
        okButton.addActionListener(this);
        buttonPanel.add(okButton,"South");
        
        setTemplateRadioButtonList();
        
        //abbrechenButton = new JButton("Abbrechen");
        //abbrechenButton.addActionListener(this);
        //buttonPanel.add(abbrechenButton,"South");
    }
    
    /** Invoked when an action occurs.
     *:TODO: ist hier ev ein bug weil die aktionsbehandlung der buttons eigentlich keine aktion zeigt????????
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if(cmd.equals("Abbrechen")){
            dispose();
        }
        else if(cmd.equals("Ok")){
            dispose();
        }
    }
    
    public String getSelectedTemplate(){
        return group.getSelection().getActionCommand()+"_templ";
    }
    
    //methode zum anlegen von radionButtons
    public void addRadioButton(JPanel buttonPanel,
    ButtonGroup g, String buttonName, boolean v)
    {  
       //erzeugt einen neuen radioButton
        JRadioButton button = new JRadioButton(buttonName, v);
       //fügt ihn zur buttonGroup g hinzu
       g.add(button);
       //fügt ihn außerdem zum buttonPanel hinzu damit er auch angezeigt wird
       buttonPanel.add(button);
       button.setActionCommand(buttonName);
    }
    
    
    //diese methode erzeugt aus den aus der datenbank gelesenen Vorlagennamen (enden mit _templ)
    //eine liste mit radiobuttons
    public void setTemplateRadioButtonList(){
        
        try{
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server").trim()+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            String query = "SHOW TABLES";
            ResultSet rs = stmt.executeQuery(query);
            LinkedList items = new LinkedList();
            
            while (rs.next()){
                String templateName = rs.getString(1);
                if (templateName.endsWith("_templ") && !templateName.equals("standardvorlage_templ") 
                    && !templateName.equals("standardmodul_templ")){
                    //fügt einen radiobutton für jede in der db gefundene vorlage hinzu, ausgenommen für standarvorlage_templ
                    //weil diese als default gesetzt sein soll und an erster stelle stehen soll. wird daher im konstruktor
                    //bereits eingefügt.
                    addRadioButton(buttonPane, group, templateName.substring(0,templateName.length()-6),false);
                }
            }
          
            rs.close();
            con.close();
            
        }catch (SQLException ex)
        {  System.out.println("SQLException:");
           while (ex != null)
           {  System.out.println("SQLState: "
              + ex.getSQLState());
              System.out.println("Nachricht:  "
              + ex.getMessage());
              System.out.println("Anbieter:   "
              + ex.getErrorCode());
              ex = ex.getNextException();
              System.out.println("");
           }
        }
    }
    
    private JPanel buttonPane;
    private ButtonGroup group;
    private JButton okButton;
    private JButton abbrechenButton;
}
