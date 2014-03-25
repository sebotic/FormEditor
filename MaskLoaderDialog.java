package com.icad.maskeneditor;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import com.icad.utilities.*;
/**
 *Ermöglich das auslesen der Tabellen aus der Masken-Datenbank und aus das auswählen dieser.
 *
 * @author  Sebastian
 */
public class MaskLoaderDialog extends JDialog implements ActionListener{
    /** Creates a new instance of MaskLoader */
    public MaskLoaderDialog(JFrame parent) {
        super(parent,"Maske auswählen",true);
        setSize (170,300);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        setLocation((screenWidth - 170) / 2,(screenHeight - 300) / 2);
        
        Container contentPane = this.getContentPane();
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());

        contentPane.add(centerPanel);
        
        maskList = new JList(getTables());
        
        JScrollPane scrollPane = new JScrollPane(maskList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(scrollPane,"Center");
        
        JPanel buttonPane = new JPanel();
        //buttonPane.setLayout(new FlowLayout());
        contentPane.add(buttonPane,"South");
        
        
        okButton = new JButton("Ok");
        okButton.addActionListener(this);
        buttonPane.add(okButton,"South");
        
        cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton,"South");
        
       
    }
    
    
    /**holt sich alle Tabellennamen aus der datenbank "masken" und gibt sie in einem string-array zurück.
     *@return Befülltes Array mit den Maskennamen
     */
    public String[] getTables(){
        String[] tableList = new String[1];
        
        try{
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server").trim()+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            String query = "SHOW TABLES FROM masken";
            ResultSet rs = stmt.executeQuery(query);
            LinkedList items = new LinkedList();
            
            int i = 0;
            while (rs.next()){
                items.addLast(rs.getString(1));
                i++;
            }
            
            
            ListIterator iter = items.listIterator();
            tableList = new String[i];
            i=0;
            while(iter.hasNext()){
                String table = (String)iter.next();
                
                /*diese if anweisung filtert alle tabellen raus die mit _templ oder _mod enden.
                 *die tabelle mod_index wird ebenfalls gefilter
                 *damit werden die Vorlagen aussortiert.
                 */
                if(!table.endsWith("_templ") /*|| !table.endsWith("_mod")*/ && !table.equals("mod_index"))
                    tableList[i]= table;
                i++;
            }
            rs.close();
            con.close();
            
            
            
        }catch (SQLException ex){
            com.icad.utilities.SQLExceptionHandler.handle(ex, "MaskLoaderDialog.getTables(): Laden der Maskenliste");
        }
        
        return tableList;
    }
    
    /**Gibt den Namen der ausgewählten Maske zurück
     *@return Maskenname der ausgewählt wurde
     */
    public String getSelectedMask(){
        return selectedMask;
    }
    
    /**setzt den Wert für die ausgewählte Maske
     *@param mask Der Maskenname der Maske
     */
    public void setSelectedMask(String mask){
        selectedMask = mask;
    }
    
    /**Behandelt die ActionEvents ausgelöst durch ok und cancelButton
     *@param e ActionEvent welches behandelt werden soll
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Ok")){
            selectedMask = (String) maskList.getSelectedValue();
            dispose();
        }
        else dispose();
        
    }    
    
    private String selectedMask;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private JList maskList;
    
}
