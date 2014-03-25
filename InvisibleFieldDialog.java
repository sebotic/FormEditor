package com.icad.maskeneditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import com.icad.utilities.*;
import com.icad.main.*;
/**
 *
 * @author  Sebastian
 */
public class InvisibleFieldsDialog extends JDialog {
    /*Diese Klasse erzeugt ein Dialogfeld in dem in einer JList alle unsichtbaren Elemente
     *(Breite oder Höhe == 0) einer Maske angezeigt werden können. Es können diese Felder
     *sichtbar gemacht werden/die Korrdinaten beliebig verändert werden, es können
     *Felder gelöscht oder hinzugefügt werden, es können Name und Typ verändert werden.
     *Allerdings werden nur Textfelder akzeptiert.
    /** Creates a new instance of InvisibleFieldDialog */
    /**
     * @param parent
     * @param comp
     * @param elementsCont
     */    
    public InvisibleFieldsDialog(JFrame parent,LinkedList comp,JRootPane elementsCont) {
        super(parent,"Unsichtbare Felder",true);
        setSize(400,300);
        
        components = comp;
        elementsContainer = elementsCont;
        
        contentPane = getContentPane();
        
        contentPane.setLayout(new BorderLayout());
        
        /*Code für Ermittlung der Bildschirmgröße und setzen der Größe des
         *Fensters
         */
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        setLocation((screenWidth - 400) / 2,(screenHeight - 300) / 2);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new NullLayout());
        centerPanel.setBounds(0,0,400,430);
        contentPane.add(centerPanel);
        
        zeroBoundsComponents = new Vector(15);
        Vector itemNames = new Vector(15);
        ListIterator iter = components.listIterator();
        while(iter.hasNext()){
            EditorElement e = (EditorElement) iter.next();
            Rectangle r = e.getBounds();
            if((r.width == 0 || r.height == 0) && e.getType() == EditorElement.TEXTFIELD){
                zeroBoundsComponents.add(e);
                itemNames.add(e.getName());
            }
        }
        
        
        if(zeroBoundsComponents.size() < 1){
            String [] options = {"OK"};
            JOptionPane.showOptionDialog(this, "Es sind keine unsichtbaren Felder vorhanden",
            "Fehler", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,options , options[0]);
            areInvisible = false;
            return;
        }
        
        final JList list = new JList(itemNames);
        list.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent evt){
                //Lösung durch ListSelectionListener nicht optimal, da der code
                //beim clicken auf ein Objekt in der Liste 2 Mal ausgeführt wird
                if(editorElement != null)
                    saveElementProperties();
                String s = (String)list.getSelectedValue();
                fillPropertyFields(s);
            }
        });
        
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBounds(10,10,120,200);
        centerPanel.add(scrollPane);
        
        
        //nachfolgend alle Eigenschaftsfelder
        lbName = new JLabel("Name");
        lbName.setBounds(150,10,70,20);
        centerPanel.add(lbName);
        txtName = new JTextField();
        txtName.setBounds(230,10,100,20);
        txtName.addFocusListener(new FocusListener(){
            public void focusLost(FocusEvent evt){
                if(editorElement.getName().equals(txtName.getText())){
                    //wenn sich der Name nicht geändert hat darf nicht nach gleichen
                    //Feldnamen gesucht werden, denn sonst wird der eigenen Name gefunden
                    //und das Dialogfeld wird angezeigt.
                }
                else if (isFieldnameExisting(txtName.getText())){
                    
                    txtName.setText(editorElement.getName());
                    JOptionPane optionPane = new JOptionPane("Ein Feld mit dem Namen '"+txtName.getText()+"' ist bereits vorhanden. Bitte wählen Sie einen anderen Feldnamen!",JOptionPane.ERROR_MESSAGE,JOptionPane.OK_OPTION,null,new Object[]{"OK"});
                    JDialog dialog = optionPane.createDialog(null,"Warnung");
                    dialog.show();
                    Object selval = optionPane.getValue();
                }
            }
            public void focusGained(FocusEvent evt){}
        });
        txtName.setDocument(new SpaceRefuseDocument());
        centerPanel.add(txtName);
        
        lbX = new JLabel("X");
        lbX.setBounds(150,34,70,20);
        centerPanel.add(lbX);
        txtX = new JTextField();
        txtX.setDocument(new IntegerDocument());//es dürfen nur ganze zahlen eingegeben werden
        txtX.setBounds(230,34,80,20);
        centerPanel.add(txtX);
        
        lbY = new JLabel("Y");
        lbY.setBounds(150,58,70,20);
        centerPanel.add(lbY);
        txtY = new JTextField();
        txtY.setDocument(new IntegerDocument());
        txtY.setBounds(230,58,80,20);
        centerPanel.add(txtY);
        
        
        lbWidth = new JLabel("Breite");
        lbWidth.setBounds(150,82,70,20);
        centerPanel.add(lbWidth);
        txtWidth = new JTextField();
        txtWidth.setDocument(new IntegerDocument());
        txtWidth.setBounds(230,82,80,20);
        centerPanel.add(txtWidth);
        
        lbHeight = new JLabel("Höhe");
        lbHeight.setBounds(150,106,70,20);
        centerPanel.add(lbHeight);
        txtHeight = new JTextField();
        txtHeight.setDocument(new IntegerDocument());
        txtHeight.setBounds(230,106,80,20);
        centerPanel.add(txtHeight);
        
        lbType = new JLabel("Typ");
        lbType.setBounds(150,130,70,20);
        centerPanel.add(lbType);
        String[] diffTypes = new String[]{"Text","Integer","Double","Währung","Datum (Auswahl)","Datum (Feld)","Integer auto_increment"};
        cbType = new JComboBox(diffTypes);
        cbType.setBounds(230,130,110,20);
        centerPanel.add(cbType);
        
        okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                saveElementProperties();
                dispose();
            }
        });
        
        abbrechenButton = new JButton("Abbrechen");
        abbrechenButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                dispose();
            }
        });
        
        JPanel southerPane = new JPanel();
        southerPane.add(okButton);
        southerPane.add(abbrechenButton);
        contentPane.add(southerPane,"South");
        
        /*setzt die Auswahl auf die 1. Componente in der Liste
         *und löst gleichzeitig ein ListSelectionEvent aus
         */
        list.setSelectedIndex(0); 
    }
    
    //setzt die Werte eines EditorElements entsprechend den Eigenschaftsfeldern
    private void saveElementProperties(){
        if(!editorElement.getName().equals(txtName.getText())){
            editorElement.setRenamed(true);
            editorElement.setName(txtName.getText());
        }
        editorElement.setBounds(Integer.parseInt(txtX.getText()),Integer.parseInt(txtY.getText()),
                        Integer.parseInt(txtWidth.getText()),Integer.parseInt(txtHeight.getText()));
        
        elementsContainer.add(editorElement);
        
        if(!editorElement.getTextFieldType().equals((String)cbType.getSelectedItem())){
            editorElement.setDataType((String)cbType.getSelectedItem());
            editorElement.setRenamed(true); //muss auf renamed gesetzt werden damit die DB akualisiert wird
        }
    }
    
    //wird bei der Auswahl eines Feldes aufgerufen und schreibt die Eigenschaften
    //des ausgewählten Feldes in die Eigenschaftsfelder
    private void fillPropertyFields(String elementName){
        loop:{
            for(int i = 0; i < zeroBoundsComponents.size();i++){
                EditorElement e = (EditorElement) zeroBoundsComponents.get(i);
                if(e.getName().equals(elementName)){
                    editorElement = e;
                    break loop;
                }
            }
        }
        
        txtName.setText(editorElement.getName());
        Rectangle r = editorElement.getBounds();
        txtX.setText(Integer.toString(r.x));
        txtY.setText(Integer.toString(r.y));
        txtWidth.setText(Integer.toString(r.width));
        txtHeight.setText(Integer.toString(r.height));
        cbType.setSelectedItem(editorElement.getTextFieldType());
    }
    
    private boolean isFieldnameExisting(String fieldname){
        ListIterator tempIter = components.listIterator();
        while(tempIter.hasNext()){
            if(fieldname.trim().toLowerCase().equals(((EditorElement)tempIter.next()).getName().trim().toLowerCase()))
                //die strings müssen unbedingt überprüft werden, wenn alle zeichen klein geschrieben sind, denn mysql kennt keinen unterschied
                //zwischen groß und kleinschreibung
                return true;
        }
        return false;
    }
    
    private String setFieldname(String typ){
        int i = 1;
        while(isFieldnameExisting(typ+i)){
            i++;
        }
        return typ+i;
    }
    
    //wird benötigt, um abzufragen, ob felder unsichtbar sind. dadurch kann im maskeneditor vermieden werden, dass der dialog angezeigt
    //wird, falls keine unsichtbaren felder vorhanden sind.
    public boolean areFieldsInvisible(){
        return areInvisible;
    }
    
    private boolean areInvisible = true;
    private Container contentPane;
    
    private JLabel lbX,lbY,lbWidth,lbHeight,lbName,lbType;
    private JTextField txtX,txtY,txtWidth,txtHeight,txtName;
    private JComboBox cbType;
    
    private JRootPane elementsContainer;
    
    private JCheckBox anzeigen;
    
    private LinkedList components;
    
    private EditorElement editorElement;
    private Vector zeroBoundsComponents;
    
    private JButton hinzufügenButton, löschenButton, okButton, abbrechenButton;
}
