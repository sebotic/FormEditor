package com.icad.maskeneditor;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import com.icad.utilities.*;
/**
 *
 * @author Sebastian Burgstaller
 */
public class MaskSaverDialog extends JDialog implements ActionListener, MouseListener{
    
    /**
     * Creates a new instance of MaskSaverDialog
     */
    public MaskSaverDialog(JFrame parent){
        super(parent, "Speichern unter", true);
        
        setSize(400,250);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        setLocation((screenWidth - 400) / 2,(screenHeight - 250) / 2);
        
        Container contentPane = this.getContentPane();
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        
        contentPane.add(centerPanel);
        centerPanel.add(listPanel,"Center");
        
        vorhandeneMasken = getTables();
        maskList = new JList(vorhandeneMasken);
        maskList.addMouseListener(this);
        
        JScrollPane scrollPane = new JScrollPane(maskList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listPanel.add(scrollPane,"Center");
        
        JPanel maskenNamePanel = new JPanel();
        //maskenNamePanel.setLayout(new BorderLayout());
        lbMaskenName = new JLabel("Maskenname: ");
        txtMaskenName = new JTextField(25);
        
        maskenNamePanel.add(lbMaskenName);
        maskenNamePanel.add(txtMaskenName);
        listPanel.add(maskenNamePanel,"South");
        
        
        JPanel buttonPane = new JPanel();
        contentPane.add(buttonPane,"South");
        
        
        okButton = new JButton("Ok");
        okButton.addActionListener(this);
        buttonPane.add(okButton,"South");
        
        cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton,"South");
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Ok")){
            
            //die for-Schleife untersucht, ob der maskenname bereits vorhanden ist.
            for(int i = 0; i < vorhandeneMasken.length; i++){
                if(txtMaskenName.getText().equals(vorhandeneMasken[i])){
                    isMaskExisting = true;
                    
                }
            }
            maskName = txtMaskenName.getText();
            //der gewählte maskenname soll nicht akzeptiert werden, wenn er mit _templ endet
            if(maskName.endsWith("_templ")){
                    isMasknameLikeTemplate = true;
            }
            dispose();
        }
        else dispose();
    }
    
    /** gibt zurück, ob der Maskenname bereits existiert
     *
     */
    public boolean isMaskNameExisting(){
	return isMaskExisting;
    }
    
    /** gibt zurück, ob der gewählte Maskenname mit _templ endet
     *
     */
    public boolean isMasknameLikeTemplate(){
        return isMasknameLikeTemplate;
    }
    
    private String[] getTables(){
        String[] tableList = new String[1];
        
        try{
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server").trim()+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            String query = "SHOW TABLES";
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
                
                //diese if anweisung filtert alle tabellen raus die mit _Templ enden.
                //damit werden die Vorlagen aussortiert.
                if(!table.endsWith("_templ"))
                    tableList[i]= table;
                i++;
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
        
        return tableList;
    }
    
    /** gibt den ausgewählten Maskennamen zurück
     *
     */
    public String getMaskName(){
        return maskName;
    }
    
    //dieser MouseListener fügt auf einen doppelklick in die liste das markierte feld in das txtMaskenName Textfeld ein.
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount()==2){
            String s = (String)maskList.getSelectedValue();
            txtMaskenName.setText(s);
        }
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    private String maskName = "";
    private String[] vorhandeneMasken;
    
    private boolean isMaskExisting = false; //wir auf true gesetzt, wenn maske in der liste bereits vorhanden
    private boolean isMasknameLikeTemplate = false; //wird auf true gesetzt, wenn der gewählte maskenname auf _templ endet.
    
    private JButton okButton;
    private JButton cancelButton;
    private JTextField txtMaskenName;
    private JLabel lbMaskenName;
    private JList maskList;
}
