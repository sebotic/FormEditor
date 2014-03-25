package com.icad.maskeneditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.sql.*;
import java.util.*;
import com.icad.utilities.*;



class ComboBoxDbDialog extends JDialog implements ActionListener{
    public ComboBoxDbDialog(JFrame parent,Maskeneditor.PropertiesPanel p){
        super(parent,"Datenfelder auswählen",true);
        
        propP = p; //Das aktuelle Properties Panel muss übergeben werden, damit beim Drücken des Ok-Buttons einer Methode implements Properties-
                    //Panel der String mit der Tabelle und den Feldern übergeben werden kann.
                    //Wäre super, wenn es für solche probleme einfachere Lösungen gäbe
        
        setSize(360,290);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        setLocation((screenWidth - 360) / 2,(screenHeight - 290) / 2);
        
        Container contentPane = this.getContentPane();
        
        contentPane.setLayout(new NullLayout());
        
        lbTables = new JLabel("Tabellen");
        lbTables.setBounds(10,10,80,20);
        contentPane.add(lbTables);
        
        tables = new JComboBox(createList("TABLES FROM icaddb"));
        tables.setBounds(90,10,120,20);
        contentPane.add(tables);
        tables.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent evt){
                addButton.setEnabled(false);
                columnList.setListData(createList("FIELDS FROM "+(String)evt.getItem()));
                items = new String[0];
                itemsList.setListData(items);
            }
        });
        
        //Beide JList haben einen mouseListener. Damit werden die Buttons aktiviert oder deaktiviert
        columnList = new JList(createList("FIELDS FROM "+tables.getSelectedItem()));
        columnList.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent evt){
                JList list = (JList)evt.getSource();
                if(list.getSelectedValue() != null){
                    addButton.setEnabled(true);
                }
            }
        });
        
        //jede JList ist in eine ScrollPane eingebettet
        JScrollPane scrollPane1 = new CustomScrollPane(columnList,10,40,120,180);
        scrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrollPane1);
        
        
        itemsList = new JList();
        itemsList.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent evt){
                JList list = (JList)evt.getSource();
                if(list.getSelectedValue() != null){
                    removeButton.setEnabled(true);
                }
            }
        });
        JScrollPane scrollPane2 = new CustomScrollPane(itemsList,220,40,120,180);
        scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrollPane2);
        
        addButton = new JButton(">>");
        addButton.setBounds(145,60,60,20);
        addButton.addActionListener(this);
        addButton.setToolTipText("Element hinzufügen");
        addButton.setEnabled(false);
        contentPane.add(addButton);
        
        removeButton = new JButton("<<");
        removeButton.setBounds(145,110,60,20);
        removeButton.addActionListener(this);
        removeButton.setEnabled(false);
        removeButton.setToolTipText("Element entfernen");
        contentPane.add(removeButton);
        
        okButton = new JButton("Ok");
        okButton.setBounds(160,230,70,20);
        okButton.addActionListener(this);
        contentPane.add(okButton);
        
        cancelButton = new JButton("Abbrechen");
        cancelButton.setBounds(240,230,100,20);
        cancelButton.addActionListener(this);
        contentPane.add(cancelButton);
    }
    
    
    //diese Methode ruft aus der Datenbank die Namen der Tabellen und der Felder der einzelnen Tabellen ab
    public String[] createList(String s){
        String[] tableList = new String[1];
        
        try{
            Connection con = DriverManager.getConnection(System.getProperty("dbpath"),System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            String query = "SHOW "+s;
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
                tableList[i]= (String)iter.next();
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
    
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        
        if(cmd.equals(">>")){
            removeButton.setEnabled(false);
            if(items.length == 0){
                items = new String[]{(String)columnList.getSelectedValue()};
                itemsList.setListData(items);
            }
            else{
                boolean containsItem = false;
                for(int i = 0;i<items.length;i++){
                    if(columnList.getSelectedValue().equals(items[i]))
                        containsItem = true;
                }
                if(!containsItem){
                    String[] temp = new String[items.length+1];
                    System.arraycopy(items,0,temp,0,items.length);
                    items = temp;
                    items[items.length-1] = (String)columnList.getSelectedValue();
                    itemsList.setListData(items);
                }
            }
        }
        if(cmd.equals("<<")){
            if(items.length > 1){
                for(int i = 0;i < items.length;i++){
                    if(itemsList.getSelectedValue().equals(items[i])){
                        String[] temp = items;
                        items = new String[items.length-1];
                        System.arraycopy(temp,0,items,0,i);
                        System.arraycopy(temp,i+1,items,i,temp.length-i-1);
                    }
                }
                itemsList.setListData(items);
            }
            else{
                items = new String[0];
                itemsList.setListData(items);
                
            }
            removeButton.setEnabled(false);
        }
        
        if(cmd.equals("Ok")){
            if(items.length > 0){
                String s = "";
                for(int i = 0;i<items.length;i++){
                    s = s+items[i]+",";
                }
                propP.setComboBoxDbString("Tabelle:"+tables.getSelectedItem()+";"+s+"@#27_dbselect");
                //Der String der den Tabellen- und die Feldnamen enthält wird dem PropertiesPanel durch Aufruf der Methode
                //setComboBoxDbString übergeben. Der String enthält ausßerdem am Ende einen String, damit der ContainerGenerator später
                //zwischen gewöhlichen ComboBoxItems und Items aus einer Datenbank unterscheiden kann
            }
            else
                propP.setComboBoxDbString("nofields");
            
            this.dispose();
        }
        if(cmd.equals("Abbrechen")){
            this.dispose();
        }
    }
    
    private JLabel lbTables;
    private JComboBox tables;
    private JList columnList;
    private JList itemsList;
    private JButton addButton;
    private JButton removeButton;
    private JButton okButton;
    private JButton cancelButton;
    private String[] items = new String[0];
    private Maskeneditor.PropertiesPanel propP;
}





