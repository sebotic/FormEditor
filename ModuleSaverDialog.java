package com.icad.maskeneditor;

/**
 *
 * @author  Benedikt Burgstaller
 */

/**:TODO: Ändern des Modulname ermöglichen
 * :TODO: Layout für die beiden "Alternativname" Labels finden
 */

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.ListIterator;

/**Generiert einen Dialog, der ein Modul speichert und einen String zusammen stellt, der die
 *Spalten für die Übersichtstabelle für das Modul speichert.
 */
class ModuleSaverDialog extends JDialog{
    public ModuleSaverDialog(JFrame owner, LinkedList components, String openedFile, boolean FileNew){
        super(owner, "Modul speichern", true);
        isFileNew = FileNew;
        
        Container contentPane = getContentPane();
        
        setLocation(200, 250);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2,0));
                
        /*Oberer Teil für die Hauptmaskenauswahl, Untergruppe und Modulname
         */
        JPanel upperPanel = new JPanel();  //allgemeines oberes Panel
        upperPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Maskenauswahl"));
        GridBagLayout gridBag = new GridBagLayout();
        upperPanel.setLayout(gridBag);
        
        JPanel hauptmaskenPanel = new JPanel();
        hauptmaskenPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Hauptmaskenauswahl"));
        hauptmaskenPanel.add(generateHauptmaskenfeld());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(upperPanel, hauptmaskenPanel, constraints, 0, 0, 4, 2);
        
        constraints.weightx = 0;
        constraints.weighty = 0;
        add(upperPanel, new JLabel("Untergruppe : "), constraints, 0, 2, 1, 1);
        add(upperPanel, new JLabel("Modulname : "), constraints, 0, 3, 1, 1);
        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 100;
        untergruppenCoBox = new JComboBox();
        add(upperPanel, untergruppenCoBox, constraints, 1, 2, 1, 1);
        modulNameField = rightSizeTextField(20);
        add(upperPanel, modulNameField, constraints, 1, 3, 1, 1);
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        untergruppenNamensfeld = rightSizeTextField(17);
        untergruppenNamensfeld.setValue("");
        add(upperPanel, new JLabel("Neue Untergruppe : "), constraints, 2, 2,  1, 1);
        add(upperPanel, untergruppenNamensfeld, constraints, 3, 2, 1, 1);
        
        /*Alle Felder mit den richtigen Werten setzen
         */
        if(isFileNew){
            tabellenFelder = "";
            modulNameField.setValue("");
            loadUntergruppennamen();
        }
        else{
            modulNameField.setValue(openedFile);
            modulName = openedFile;
            modulNameField.setEditable(false);
            loadTabFelder();
        }
        
        /*Unterer Teil für die Tabellenauswahl
         */
        JPanel lowerPanel = new JPanel();
        lowerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Spaltenauswahl"));
        
        JPanel beschriftung = new JPanel(); //Beschriftungszeile "Alternativname"
        Box beschriftungsBox = Box.createHorizontalBox();
        beschriftungsBox.add(Box.createHorizontalStrut(175));
        beschriftungsBox.add(new JLabel("Alternativname"));
        beschriftungsBox.add(Box.createHorizontalStrut(250));
        beschriftungsBox.add(new JLabel("Alternativname"));
        beschriftung.add(beschriftungsBox);
        
        lowerPanel.add(beschriftung, BorderLayout.NORTH);

        generateTFieldsandCBoxes(components);
        Box[] hBox = generateLowerPanelLayout();
        for(int i = 0; i < hBox.length; i++)
            lowerPanel.add(hBox[i]);

        /*Speichern und Abbrechnen Buttons
         */
        JPanel buttonPanel = new JPanel();
        JButton save = new JButton("Speichern");
        JButton cancel = new JButton("Abbrechen");
        
        save.addActionListener(new
        ActionListener(){
            public void actionPerformed(ActionEvent event){
                for(int i = 0; i < maskRadioButton.length; i++)
                    if(maskRadioButton[i].isSelected())
                        hauptMaskenName = maskRadioButton[i].getText();

                if(isFileNew){  //Suchen nach bereits vorhandenem Modul mit diesem Namen
                    modulName = (String)modulNameField.getValue() + "_mod"; //die Endung muss bereits hier angefügt werden, da sonst der Vergleich nicht funktioniert
                    try{
                        Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")
                                                                        +"/masken",System.getProperty("username"),System.getProperty("pwd"));
                        Statement stmt = con.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT name FROM mod_index WHERE name = '"+modulName+"'");
                        
                        if(rs.next()){
                            JOptionPane.showMessageDialog(ModuleSaverDialog.this, "Modul \""+modulName+"\" existiert bereits!",
                                                    "Modul vorhanden", JOptionPane.OK_OPTION);
                            return ;
                        }
                        stmt.close();
                        con.close();
                    }
                    catch(SQLException ex){
                        com.icad.utilities.SQLExceptionHandler.handle(ex,"ModuleSaverDialog.ActionListener.actionPerformed(): sucht in der Spalte name, ob bereits ein Modul mit dem eingegebenen Namen existiert.");
                    }
                }
                
                if(((String)modulNameField.getValue()).equals(""))
                    JOptionPane.showMessageDialog(ModuleSaverDialog.this, "Es wurde kein Modulname eingegeben!",
                                                    "Modulname", JOptionPane.ERROR_MESSAGE);
                else{
                    tabellenFelder = "";
                    for(int i = 0; i < 8; i++){
                        if(!((String)tabCoBox[i].getSelectedItem()).equals("--Auswählen--")){
                            if(((String)altNameField[i].getValue()).equals(""))
                                tabellenFelder += (String)tabCoBox[i].getSelectedItem();
                            else
                                tabellenFelder += (String)altNameField[i].getValue();
                            tabellenFelder += " ";
                            tabellenFelder += (String)tabCoBox[i].getSelectedItem();
                            tabellenFelder += ";";
                        }
                        else
                            continue;
                    }
                    
                    save();
                  
                    dispose();
                }
            }
        });
        
        cancel.addActionListener(new
        ActionListener(){
            public void actionPerformed(ActionEvent event){
                modulName = "";
                dispose();
            }
        });
        
        /*alles zusammenfügen
         */
        mainPanel.add(upperPanel);
        mainPanel.add(lowerPanel);
        buttonPanel.add(save);
        buttonPanel.add(cancel);
        contentPane.add(mainPanel);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(WIDTH, HEIGHT);
    }
    
    /**Fügt eine Componente des GridBagLayout zum Panel hinzu
     *@param panel zu dem die Componente hinzugefügt werden soll
     *@param c hinzuzufügende Componente
     *@param constraints das zu verwendende GridBagConstraints
     *@param x zu verwendende Spalte im Grid
     *@param y zu verwendende Zeile im Grid
     *@param w Anzahl der Spalten
     *@param h Anzahl der Zeilen
     */
    public void add(JPanel panel, Component c, GridBagConstraints constraints, int x, int y, int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        panel.add(c, constraints);
    }
    
    /**Erzeugt das Boxlayout für das Panel mit den HauptmaskenRadioButtons
     *@return fertig gelayoutete Horizontal Box
     */
    public Box generateHauptmaskenfeld(){
        ButtonGroup maskButtonGroup = new ButtonGroup();
        maskRadioButton = new JRadioButton[6];
        Box vBoxMasken1 = Box.createVerticalBox();
                
        vBoxMasken1.add(maskRadioButton[0] = new JRadioButton("adressen", true));
        vBoxMasken1.add(maskRadioButton[1] = new JRadioButton("gesellschaften", false));
        
        Box vBoxMasken2 = Box.createVerticalBox();
        vBoxMasken2.add(maskRadioButton[2] = new JRadioButton("kunden", false));
        vBoxMasken2.add(maskRadioButton[3] = new JRadioButton("schaden", false));
        
        Box vBoxMasken3 = Box.createVerticalBox();
        vBoxMasken3.add(maskRadioButton[4] = new JRadioButton("vermittler", false));
        vBoxMasken3.add(maskRadioButton[5] = new JRadioButton("vertragsstammtabelle", false));
        
        for(int i = 0; i < maskRadioButton.length; i++)
            maskButtonGroup.add(maskRadioButton[i]);
        
        Box hBoxMasken = Box.createHorizontalBox();
        hBoxMasken.add(Box.createHorizontalStrut(10));
        hBoxMasken.add(vBoxMasken1);
        hBoxMasken.add(Box.createHorizontalStrut(40));
        hBoxMasken.add(vBoxMasken2);
        hBoxMasken.add(Box.createHorizontalStrut(40));
        hBoxMasken.add(vBoxMasken3);
        hBoxMasken.add(Box.createHorizontalStrut(20));
        return hBoxMasken;
    }
    
    /**Initialisiert die TextFelder und ComboBoxes mit den Werten aus dem TabellenString,
     *oder "--Auswählen--", falls der String leer ist oder nicht genug Elemente enthält
     */
    public void generateTFieldsandCBoxes(LinkedList ElementList){
        altNameField = new JFormattedTextField[rowEntries];
        tabCoBox = new JComboBox[rowEntries];

        StringTokenizer tokenizer = new StringTokenizer(tabellenFelder, "; ");
        for(int i = 0; i < altNameField.length; i++){
            altNameField[i] = rightSizeTextField(10);
            tabCoBox[i] = felderComboBox(ElementList);
            //Felder und ComboBoxes werden nun mit den Werten aus dem Tabellenstring gesetzt, oder mit einem leeren String
            if(tokenizer.hasMoreTokens()){
                altNameField[i].setValue(tokenizer.nextToken());
                if(tokenizer.hasMoreTokens())
                    tabCoBox[i].setSelectedItem(tokenizer.nextToken());
            }
            else
                altNameField[i].setValue("");
        }
    }
    
   /**Generiert JFormattedTextFields mit bestimmter Länge und richtiger Size für das Layout
     *@return JFormattedTextField mit richtiger Größe
     */
    public JFormattedTextField rightSizeTextField(int maxlength){
        JFormattedTextField tField = new JFormattedTextField();
        tField.setColumns(maxlength);
        tField.setMaximumSize(tField.getPreferredSize());
        
        return tField;
    }
    
    /**Generiert JComboBoxes samt Items aus einer LinkedList und richtiger Maximalgröße fürs Layout
     *@return fertige JComboBox
     */
    public JComboBox felderComboBox(LinkedList ElementList){
        JComboBox cBox = new JComboBox();
        ListIterator iter = ElementList.listIterator();
        EditorElement tempElement;
        cBox.addItem("--Auswählen--");
        while(iter.hasNext())
            if((tempElement = (EditorElement)iter.next()).getType() == 1 || tempElement.getType() == 6)
                continue;
            else
                cBox.addItem(tempElement.getName());

        cBox.setMaximumSize(cBox.getPreferredSize());
        
        return cBox;
    }
    
    /**Erzeugt die Horizontal Boxes für das Boxlayout der Spaltenauswahl und
     * setzt das Panel für die Spaltenauswahl aus den Horizontal Boxes zusammen
     */
    public Box[] generateLowerPanelLayout(){
        String title;
        
        Box[] hBox = new Box[rowEntries/2];

        for(int i = 0, j = 0; i < 4; i++){
            hBox[i] = Box.createHorizontalBox();
            for(int k = 0, l = j+1; j < 8 && k < 2; k++, j++, l = j+1){
                title = "Spalte " + l +" : ";
                hBox[i].add(new JLabel(title));
                hBox[i].add(Box.createHorizontalStrut(10));
                hBox[i].add(tabCoBox[j]);
                hBox[i].add(Box.createHorizontalStrut(10));
                hBox[i].add(altNameField[j]);
                hBox[i].add(Box.createHorizontalStrut(20));
            }
        }
        return hBox;
    }
    
    /**Wertet ComboBox und Textfeld der Unterguppe aus und setzt dann den Rückgabestring für die Modgroup
     *@return String mit der zu speichernden Untergruppe
     */
    public String determineModGroup(){
        String modGroup;
        if(((String)untergruppenNamensfeld.getValue()).equals("") && ((String)untergruppenCoBox.getSelectedItem()).equals("--Auswählen--"))
            modGroup = "";
        else if(((String)untergruppenNamensfeld.getValue()).equals(""))
            modGroup = (String)untergruppenCoBox.getSelectedItem();
        else
            modGroup = (String)untergruppenNamensfeld.getValue();
        
        return modGroup;
    }
    
    /**Speicherroutine für das Speichern eines vorhanden oder neuen Moduls, mit allen Einstellungen wie Hauptmasken,
     * Untergruppen und Spaltenauswahl, in der Tabelle mod_index.
     */
    public void save(){
        try{
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")
            +"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            
            if(isFileNew)
                stmt.executeUpdate("INSERT INTO mod_index (hauptmaske,modgroup,name,tabellenfelder) VALUES ('"
                                            +hauptMaskenName+"','"+determineModGroup()+"','"+modulName
                                            +"','"+tabellenFelder+"')");
            else
                for(int i = 0; i < maskRadioButton.length; i++)
                    if(maskRadioButton[i].isSelected())
                        stmt.executeUpdate("UPDATE mod_index SET hauptmaske = '"+hauptMaskenName+"', modgroup = '"
                                            +determineModGroup()+"', tabellenfelder = '"+tabellenFelder+"' WHERE name = '"+modulName+"'");
            stmt.close();
            con.close();
        }catch (SQLException ex){
            com.icad.utilities.SQLExceptionHandler.handle(ex,"ModuleSaverDialog.save(): speichert ein neues Modul in der mod_index tabelle");
        }
    }
    
    /**Lädt die Einstellungen für ein vorhandenes Modul aus der Tabelle mod_index
     */
    public void loadTabFelder(){
        try{
            String modGroup = "";   //zum Zwischenspeichern der Untergruppe im Modul
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")
                                                         +"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT hauptmaske, modgroup, name, tabellenfelder FROM mod_index WHERE name = '"+modulName+"'");
            
            if(rs.next()){
                modGroup = rs.getString("modgroup");
                modulNameField.setValue(rs.getString("name"));
                modulName = rs.getString("name");
                tabellenFelder = rs.getString("tabellenfelder");
                
                for(int i = 0; i < maskRadioButton.length; i++)
                    if((maskRadioButton[i].getText()).equals(rs.getString("hauptmaske")))
                        maskRadioButton[i].setSelected(true);
            }
            loadUntergruppennamen(con, stmt);
            untergruppenCoBox.setSelectedItem(modGroup);
            
            stmt.close();
            con.close();
        }catch(SQLException ex){
            com.icad.utilities.SQLExceptionHandler.handle(ex,"ModuleSaverDialog.loadTabFelder(): lädt die Spalte tabellenfelder des aktuellen Moduls aus der mod_index tabelle");
        }
    }
    
    /**Lädt die Namen aller Untergruppen, die bereits in der Datenbank gespeichert sind und fügt sie zur ComboBox hinzu
     */
    public void loadUntergruppennamen(){
        try{
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")
                                                         +"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
                        
            ResultSet rslt = stmt.executeQuery("SELECT DISTINCT modgroup FROM mod_index");
            untergruppenCoBox.addItem("--Auswählen--");
            while(rslt.next())
                if((rslt.getString("modgroup")).equals(""))
                    continue;
                else
                    untergruppenCoBox.addItem(rslt.getString("modgroup"));
            
            stmt.close();
            con.close();
            
        }catch(SQLException ex){
            com.icad.utilities.SQLExceptionHandler.handle(ex,"ModuleSaverDialog.loadUntergruppennamen(): Lädt die Namen der bereits existierenden Untergruppen aus der Spalte modgroup in mod_index.");
        }
    }
    
   /**Lädt die Namen aller Untergruppen, die bereits in der Datenbank gespeichert sind und fügt sie zur ComboBox hinzu
    *@param con Verbindung zur Datenbank
    *@param stmt Statement für die Datenbankverbindung
    */
    public void loadUntergruppennamen(Connection con, Statement stmt){
        try{
            ResultSet rslt = stmt.executeQuery("SELECT DISTINCT modgroup FROM mod_index");
            untergruppenCoBox.addItem("--Auswählen--");
            while(rslt.next())
                if((rslt.getString("modgroup")).equals(""))
                    continue;
                else
                    untergruppenCoBox.addItem(rslt.getString("modgroup"));
            
        }catch(SQLException ex){
            com.icad.utilities.SQLExceptionHandler.handle(ex,"ModuleSaverDialog.loadUntergruppennamen(): Lädt die Namen der bereits existierenden Untergruppen aus der Spalte modgroup in mod_index.");
        }
    }
    
    public String getModulName(){
        return modulName;
    }
    
    public String getHauptMaskenName(){
        return hauptMaskenName;
    }

    private JFormattedTextField[] altNameField;    //TextFelder für den Alternativnamen
    private JComboBox[] tabCoBox;  //Auswahl für die EditorElemente
    private String tabellenFelder;      //String mit den Tabellenfeldern
    private JRadioButton[] maskRadioButton;  //CheckBoxen für die Hauptmasken
    private String hauptMaskenName;
    private JFormattedTextField modulNameField;
    private String modulName;   //Name des aktuellen Moduls
    private JComboBox untergruppenCoBox;    //ComboBox mit den Untergruppen
    private JFormattedTextField untergruppenNamensfeld; //Für die Eingabe neuer Untergruppen
    private boolean isFileNew;  //gibt an, ob das Modul bereits existiert, oder neu erstellt wurde
    private static final int rowEntries = 8;    //Anzahl der Spalten für die Tabelle
    private static final int WIDTH = 720;
    private static final int HEIGHT = 500;
}










