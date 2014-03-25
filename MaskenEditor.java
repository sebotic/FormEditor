/*
 *Der Beginn ist der wichtigste Teil der Arbeit. (Platon) ;)
 *
 *@Author Sebastian Burgstaller
 * 
 */
package com.icad.maskeneditor;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.icad.utilities.*;
import com.icad.main.*;

/** diese Klasse bildet den Maskeneditor
 */
public class Maskeneditor extends JFrame implements ActionListener {
    public Maskeneditor() {
        setLocation(0,0);
        setBounds(0,0,1024,750);
        setTitle("Maskeneditor - "+openedFile);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = tk.getImage("./com/icad/images/Maskeneditoricon.gif");
        setIconImage(img);
        
        contentPane = getContentPane();
        
        contentPane.setLayout(new BorderLayout());
        
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //dispose();
                System.exit(0);
            }
            
            public void windowOpened(WindowEvent e){
                //zeigt einen dialog zum öffnen einer vorlage an
                MaskTemplateChooserDialog d = new MaskTemplateChooserDialog(Maskeneditor.this);
                d.setVisible(true);
                loadTemplate(d.getSelectedTemplate());
                
            }
        });
        
        menuBar = new JMenuBar();
        
        menuBar.add(CreateMenuBar.makeMenu("Datei",new Object[]
        { "Neu","Speichern","Laden",null,"Als Vorlage speichern",null,"Beenden"},this));
        menuBar.add(CreateMenuBar.makeMenu("Bearbeiten",new Object[]
        { "Feld löschen","Maske löschen",null,"Unsichtbare Felder ...",null,"Ausschneiden","Kopieren","Einfügen"},this));
        menuBar.add(CreateMenuBar.makeMenu("Module",new Object[]{"Modul laden","Modul speichern",null,"Modul löschen"},this));
        menuBar.add(CreateMenuBar.makeMenu("Hilfe",new Object[]
        { "Hilfe",null,"Info"},this));
        
        
        
        setJMenuBar(menuBar);
        
        btGroup = new ButtonGroup();
        
        bar = new JToolBar();
        bar.setBounds(0,0,1020,30);
        
        addToButtonGroup(bar,"Mauszeiger");
        addToButtonGroup(bar,"Bezeichnungsfeld");
        addToButtonGroup(bar,"Textfeld");
        addToButtonGroup(bar,"Kombinationsfeld");
        addToButtonGroup(bar,"Kontrollkästchen");
        addToButtonGroup(bar,"Textbereich");
        addToButtonGroup(bar,"Linie");
        
        
        //bar.putClientProperty("JToolBar.isRollover",Boolean.TRUE);
        
        contentPane.add(bar,"North");
        
        elementsContainer = new CustomPanel();
        elementsContainer.setBounds(0,0,1200,2000);
        elementsContainer.setLayout(new NullLayout());
        
        scrollPane = new CustomScrollPane(elementsContainer,0,0,800,670);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        glassPane = new GlassPane();
        glassPane.setBounds(elementsContainer.getBounds());
        
        glassPane.addMouseMotionListener(new MouseMotionListener());
        glassPane.addMouseListener(new MouseListener());
        //addKeyListener(this);
        elementsContainer.setGlassPane(glassPane);
        elementsContainer.getGlassPane().setVisible(true);
        
        panel = new JPanel();
        //panel.setBounds(0,0,200,670);
        panel.setLayout(new BorderLayout());
        
        propertiesPanel = new PropertiesPanel(new EditorElement(EditorElement.LABEL));
        panel.add(propertiesPanel);
        panel.remove(propertiesPanel);//das entfernen verhindert eine NullPointerException, wenn der Fokus auf dem propertiesPanel steht, und
        //man dann auf die Symbolleiste klickt.
        
        sPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scrollPane,panel);
        sPane.setContinuousLayout(true);
        sPane.setDividerLocation(800);
        sPane.setResizeWeight(1);
        sPane.setDividerSize(10);
        sPane.setOneTouchExpandable(true);
        //sPane.setBounds(0,32,1015,665);
        
        contentPane.add(sPane);
        
        
        clearEditorArea(); //setzt die Felder die jede Maske enthalten muss
        
        //Diese Systemproperties werden hier nur zu testzwecken gesetzt, später werden sie beim start
        //des programmes in der Klasse MainWindow gesetzt
        
        /*
        System.setProperty("jdbc.drivers","com.mysql.jdbc.Driver");
        System.setProperty("username","seb");
        System.setProperty("pwd","seb");
        System.setProperty("dbpath","Jdbc:mysql://192.168.1.8:3306/icaddb");
        System.setProperty("server","192.168.1.8");
        System.setProperty("port", "3306");
        */
        
        
        //folgender code zeigt ein Textfeld an, welches die koordianten anzeigt an denen die
        //maus sich gerade befindet
                /*
                coordinatesTextField = new JTextField();
                coordinatesTextField.setBounds(150,150,100,20);
                coordinatesTextField.setEnabled(false);
                coordinatesTextField.setDisabledTextColor(Color.black);
                contentPane.add(coordinatesTextField);
                 */
    }
    
    /**dieser listener generiert die einzelnen Elemente auf basis der EditorElements-Klasse
     *außerdem setzt er alle eigenschaften für die jeweiligen felder und fügt die Listener
     *hinzu. jedes objekt erhält eine id und wird in das "components"-array eingetragen.
     */
    class MouseListener extends MouseAdapter {
        
        public void mouseClicked(MouseEvent evt){
            Point p = evt.getPoint();
            
            ListIterator<EditorElement> compIter = components.listIterator();
            EditorElement e;
            boolean noEditorElementFound = false;
            
            while(compIter.hasNext()){
                e = compIter.next();
                Rectangle r = e.getBounds();
                if((p.x >= r.x) && ((r.x+r.width)>=p.x) && (p.y >= r.y) && ((r.y+r.height)>=p.y)){
                    setSelection(e);
                    noEditorElementFound = false;
                    break;
                }
                else noEditorElementFound = true;
                
            }
            if (noEditorElementFound)
                removeAllSelections();
            
            repaint();
            validate();
        }
        
        
        public void mousePressed(MouseEvent evt) {
            Point p = evt.getPoint();
            
            if(selection.equals("Mauszeiger") && singleSelectedComponent != null) {
                startPt = p;
                //hier wird ermittelt, ob der angeklickte punkt in einer komponente liegt oder nicht. Bei mehrfachauswahl wird
                //die components-liste durchsucht, bei einfachauswahl wird nur die singleSelectedComponent verwendet
                if(isMultipleSelections){
                    ListIterator<EditorElement> compIter = components.listIterator();
                    EditorElement e;
                    while (compIter.hasNext()){
                        e = compIter.next();
                        if(e.isSelected()){
                            for(int i = 0;i<9;i++){
                                Rectangle b = e.getHandle(i).getBounds();
                                if(b.x <= p.x && b.y <= p.y && p.x <= b.x + b.width && p.y <= b.y + b.height){
                                    dragHandle = e.getHandle(i);
                                    singleSelectedComponent = e; //zuweisung wird hier geändert, um in der Dragged-methode die herkunft bestimmen zu können.
                                    break;
                                }
                            }
                        }
                    }
                }
                else{
                    
                    for(int i = 0;i<9;i++){
                        Rectangle b = singleSelectedComponent.getHandle(i).getBounds();
                        if(b.x <= p.x && b.y <= p.y && p.x <= b.x + b.width && p.y <= b.y + b.height){
                            dragHandle = singleSelectedComponent.getHandle(i);
                            break;
                        }
                    }
                }
            }
            else if(selection.equals("Mauszeiger") && singleSelectedComponent == null){
                startPt = p;
                
            }
            else if (selection.equals("Bezeichnungsfeld")) {
                EditorElement label = new EditorElement(EditorElement.LABEL);
                
                startPt = p;
                
                String name = setFieldname("Label"); //diese methode wird aufgerufen, um einen Namen für das Objekt zu finden, der nicht vorhanden ist
                label.setOldName(name);//oldName muss initialisiert werden, da sonst beim umbenennen der ursprüngliche Name verloren geht
                label.setText(name);
                label.setName(name);
                
                label.setAdded(true);//setzt die Eigenschaft, dass dieses element hinzugefügt wurde auf wahr
                
                setSelection(label);
                
                //die add-Methoden müssen am ende hinzugefügt werden,da sonst Fehler auftreten
                elementsContainer.add(label);
                components.addLast(label);
            }
            else if (selection.equals("Textfeld")) {
                EditorElement txtField = new EditorElement(EditorElement.TEXTFIELD);
                
                startPt = p;
                
                String name = setFieldname("Textfeld");
                txtField.setText(name);
                txtField.setName(name);
                txtField.setOldName(name);
                
                txtField.setAdded(true);
                
                elementsContainer.add(txtField);
                components.addLast(txtField);
                
                setSelection(txtField);
                
            }
            else if (selection.equals("Kombinationsfeld")) {
                EditorElement comboBox = new EditorElement(EditorElement.COMBOBOX);
                startPt = p;
                
                String name = setFieldname("ComboBox");
                comboBox.setName(name);
                comboBox.setOldName(name);
                
                comboBox.setAdded(true);
                
                elementsContainer.add(comboBox);
                components.addLast(comboBox);
                
                setSelection(comboBox);
                
            }
            else if (selection.equals("Kontrollkästchen")) {
                EditorElement checkBox = new EditorElement(EditorElement.CHECKBOX);
                
                startPt = p;
                
                String name = setFieldname("Checkbox");
                checkBox.setText(name);
                checkBox.setName(name);
                checkBox.setOldName(name);
                
                checkBox.setAdded(true);
                
                elementsContainer.add(checkBox);
                components.addLast(checkBox);
                
                setSelection(checkBox);
            }
            else if (selection.equals("Textbereich")){
                EditorElement textArea = new EditorElement(EditorElement.TEXTAREA);
                
                startPt = p;
                String name = setFieldname("Textarea");
                textArea.setText(name);
                textArea.setName(name);
                textArea.setOldName(name);
                
                textArea.setAdded(true);
                
                elementsContainer.add(textArea);
                components.addLast(textArea);
                
                setSelection(textArea);
            }
            else if (selection.equals("Linie")){
                EditorElement separator = new EditorElement(EditorElement.SEPARATOR);
                
                startPt = p;
                String name = setFieldname("Separator");
                
                separator.setText(name);
                separator.setName(name);
                separator.setOldName(name);
                
                separator.setAdded(true);
                
                elementsContainer.add(separator);
                components.addLast(separator);
                
                setSelection(separator);
            }
        }
        
        public void mouseReleased(MouseEvent evt) {
            
            if (selection.equals("Bezeichnungsfeld") || selection.equals("Textfeld") || selection.equals("Kombinationsfeld")||selection.equals("Kontrollkästchen") || selection.equals("Textbereich") || selection.equals("Linie")) {
                endPt = evt.getPoint();
                selection = "Mauszeiger";
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                JToggleButton tb = (JToggleButton)bar.getComponentAtIndex(0);
                tb.setSelected(true);
                
                singleSelectedComponent.setBounds(startPt.x,startPt.y,endPt.x-startPt.x,endPt.y-startPt.y);
                singleSelectedComponent.removeHandles(glassPane);
                singleSelectedComponent.positionHandles(glassPane);
                propertiesPanel.updateBounds();
                startPt = null;
                endPt = null;
                validate();
                repaint();
            }
            else{ //diese if-anweisung dient der Ermittlung der Veränderung und setzt die neuen Bounds der einzelnen Komponenten
                if(isMultipleSelections && dragHandle != null && startPt != null && endPt != null){
                    Rectangle r = singleSelectedComponent.getBounds();
                    Point xChange = new Point(0,0);
                    Point yChange = new Point(0,0);
                    
                    xChange.x = startPt.x - r.x;
                    xChange.y = startPt.y - r.y;
                    
                    yChange.x = r.x+r.width - endPt.x;
                    yChange.y = r.y+r.height - endPt.y;
                    
                    ListIterator<EditorElement> compIter = components.listIterator();
                    
                    while(compIter.hasNext()){
                        EditorElement e = compIter.next();
                        Rectangle er = e.getBounds();
                        if(e.isSelected()){
                            switch (dragHandle.getType()){
                                case Handle.NW_HANDLE: e.setBounds(er.x+xChange.x,er.y+xChange.y,er.width-xChange.x,er.height-xChange.y); break;
                                case Handle.N_HANDLE: e.setBounds(er.x,er.y+xChange.y,er.width,er.height - xChange.y); break;
                                case Handle.NE_HANDLE:e.setBounds(er.x+xChange.x,er.y+xChange.y,er.width-yChange.x,er.height-xChange.y); break;
                                case Handle.W_HANDLE: e.setBounds(er.x+xChange.x,er.y,er.width-xChange.x,er.height); break;
                                case Handle.E_HANDLE: e.setBounds(er.x,er.y,er.width-yChange.x,er.height); break;
                                case Handle.C_HANDLE: e.setBounds(er.x+xChange.x,er.y+xChange.y,er.width,er.height); break;
                                case Handle.SW_HANDLE:e.setBounds(er.x+xChange.x,er.y,er.width-xChange.x,er.height - yChange.y); break;
                                case Handle.S_HANDLE: e.setBounds(er.x,er.y,er.width,er.height-yChange.y); break;
                                case Handle.SE_HANDLE: e.setBounds(er.x,er.y,er.width-yChange.x,er.height-yChange.y); break;
                                default: break;
                            }
                            //folgende Anweisungen verhindern, dass eine komponente kleiner als 10x10 pixel wird
                            Rectangle rec = e.getBounds();
                            int width = (int) rec.width;
                            int height = (int) rec.height;
                            if(rec.width < 10 || rec.height < 10){
                                if(rec.width < 10)
                                    width = 10;
                                if(rec.height < 10)
                                    height = 10;
                                e.setBounds(rec.x,rec.y,width,height);
                            }
                            e.removeHandles(glassPane);
                            e.positionHandles(glassPane);
                        }
                    }
                    propertiesPanel.updateBounds();
                    startPt = null;
                    endPt = null;
                    dragHandle = null;
                    //Cursor wird sofort nach dem auslassen wieder auf default gesetzt
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    validate();
                    repaint();
                    
                }
                else {
                    //Diese if-anweisung ermöglicht die Auswahl der Komponenten mittels eines gezogenen Rahmens um die gewünschten Komponenten
                    if(startPt != null && endPt != null){
                        ListIterator<EditorElement> compIter = components.listIterator();
                        EditorElement e;
                        
                        
                        while(compIter.hasNext()){
                            e = compIter.next();
                            Rectangle markedRect = new Rectangle(startPt.x,startPt.y,endPt.x-startPt.x,endPt.y-startPt.y);
                            if(markedRect.contains(e.getBounds()))
                                setSelection(e);
                        }
                    }
                    startPt = null;
                    endPt = null;
                    validate();
                    repaint();
                }
            }
        }
    }
    
    /********************************************************************************************************
     * innere klasse die die mausbewegungen für das hauptfenster abfängt
     */
    
    class MouseMotionListener extends MouseMotionAdapter {
        /*diese Methode legt die position für start und endpunkt fest, jenachdem, in welche richtung das Handle gezogen wurde.
         *daraus wird die Veränderung bei - falls ausgewählt - den anderen Komponenten errechnet
         */
        public void mouseDragged(MouseEvent evt) {
            
            if(dragHandle != null){
                Rectangle compBounds = singleSelectedComponent.getBounds();
                switch (dragHandle.getType()){
                    case Handle.NW_HANDLE:{startPt = evt.getPoint(); endPt = new Point(compBounds.x +compBounds.width,compBounds.y +compBounds.height);} break;
                    case Handle.N_HANDLE:{startPt = new Point(compBounds.x,evt.getPoint().y); endPt = new Point(compBounds.x +compBounds.width,compBounds.y +compBounds.height);} break;
                    case Handle.NE_HANDLE:{startPt = new Point(compBounds.x,evt.getPoint().y); endPt = new Point(evt.getPoint().x,evt.getPoint().y+compBounds.y+compBounds.height - evt.getPoint().y);} break;
                    case Handle.W_HANDLE: {startPt = new Point(evt.getPoint().x,compBounds.y); endPt = new Point(compBounds.x+compBounds.width,compBounds.y+compBounds.height);} break;
                    case Handle.E_HANDLE: {startPt = new Point(compBounds.x,compBounds.y); endPt = new Point(evt.getPoint().x,compBounds.y+compBounds.height);} break;
                    case Handle.C_HANDLE: {startPt = new Point(evt.getPoint().x - compBounds.width/2,evt.getPoint().y - compBounds.height /2); endPt = new Point(startPt.x + compBounds.width,startPt.y + compBounds.height);}break;
                    case Handle.SW_HANDLE:{startPt = new Point(evt.getPoint().x,compBounds.y);endPt = new Point(compBounds.x+compBounds.width,evt.getPoint().y);} break;
                    case Handle.S_HANDLE: {startPt = new Point(compBounds.x,compBounds.y); endPt = new Point(compBounds.x+compBounds.width,evt.getPoint().y);} break;
                    case Handle.SE_HANDLE: {startPt = new Point(compBounds.x,compBounds.y); endPt = evt.getPoint();} break;
                    default: break;
                }
            }
            else {
                endPt = evt.getPoint();
            }
            
            repaint();
        }
    }
    
    //dies ist das panel welches die eigenschaften eines elements im rechten splitpane-breich
    //anzeigt, die focus-listener sorgen für die aktualisierung des elements wenn eine
    //eigenschaft geändert wird
    public class PropertiesPanel extends JPanel implements FocusListener, ActionListener {
        public PropertiesPanel(EditorElement ee) {
            editorElement = ee;
            elementType = editorElement.getType();
            
            setLayout(new NullLayout());
            setBounds(0,0,200,670);
            
            Rectangle bounds = editorElement.getBounds();
            
            //if-bedingung verhindert, dass bei Separatoren ein Name-Feld angezeigt wird, Name wäre unnötig für der benutzer
            if(editorElement.getType() != EditorElement.SEPARATOR){
                lbName = new JLabel("Name");
                lbName.setBounds(10,10,70,20);
                add(lbName);
                txtName = new JTextField();
                txtName.setBounds(85,10,100,20);
                
                //kein Element darf im namen leerzeichen enthalten, da sonst fehler in der DB-Abfrage auftreten
                //ausgenommen sind Labels da dort im Namen nicht der tatsächliche Name sondern nur die Beschriftung festgelegt wird
                //außerdem werden Labels nicht in die DB eingetragen
                if(elementType != EditorElement.LABEL)
                    txtName.setDocument(new SpaceRefuseDocument());
                
                txtName.addFocusListener(new FocusListener(){
                    public void focusLost(FocusEvent evt){
                        String name = txtName.getText().trim();
                        if(editorElement.getName().equals(name)){
                        }
                        else{
                            if (isFieldnameExisting(name) && elementType != EditorElement.LABEL){
                                
                                txtName.setText(editorElement.getName());
                                JOptionPane optionPane = new JOptionPane("Ein Feld mit dem Namen '"+name+"' ist bereits vorhanden. Bitte wählen Sie einen anderen Feldnamen!",JOptionPane.ERROR_MESSAGE,JOptionPane.OK_OPTION,null,new Object[]{"OK"});
                                JDialog dialog = optionPane.createDialog(null,"Warnung");
                                dialog.setVisible(true);
                                //Object selval = optionPane.getValue();
                            }
                            else{
                                
                                //diese if-bedingung ist notwendig, denn der Name eines Labels oder Separators darf nicht dopplelt vorhanden sein
                                //(Fehler bei markierung), die Bezeichnung muss jedoch doppel vorhanden sein können
                                if(elementType == EditorElement.LABEL)
                                    editorElement.setText(name);
                                else{
                                    editorElement.setText(name);
                                    editorElement.setName(name);
                                }
                                if(!editorElement.getAdded()){
                                    editorElement.setRenamed(true);
                                    editorElement.setName(name);
                                }
                            }
                        }
                    }
                    
                    public void focusGained(FocusEvent evt){}
                });
                
                if(elementType == EditorElement.LABEL){
                    JLabel lb = (JLabel) editorElement.getElement();
                    txtName.setText(lb.getText());
                }
                else txtName.setText(editorElement.getName());
                
                add(txtName);
            }
            
            lbX = new JLabel("X");
            lbX.setBounds(10,34,70,20);
            add(lbX);
            txtX = new JTextField();
            txtX.setDocument(new IntegerDocument());//es dürfen nur ganze zahlen eingegeben werden
            txtX.setBounds(85,34,80,20);
            txtX.addFocusListener(this);
            txtX.setText(Integer.toString((int)bounds.getX()));
            add(txtX);
            
            lbY = new JLabel("Y");
            lbY.setBounds(10,58,70,20);
            add(lbY);
            txtY = new JTextField();
            txtY.setDocument(new IntegerDocument());
            txtY.setBounds(85,58,80,20);
            txtY.addFocusListener(this);
            txtY.setText(Integer.toString((int)bounds.getY()));
            add(txtY);
            
            lbWidth = new JLabel("Breite");
            lbWidth.setBounds(10,82,70,20);
            add(lbWidth);
            txtWidth = new JTextField();
            txtWidth.setDocument(new IntegerDocument());
            txtWidth.setBounds(85,82,80,20);
            txtWidth.addFocusListener(this);
            txtWidth.setText(Integer.toString((int)bounds.getWidth()));
            add(txtWidth);
            
            lbHeight = new JLabel("Höhe");
            lbHeight.setBounds(10,106,70,20);
            add(lbHeight);
            txtHeight = new JTextField();
            txtHeight.setDocument(new IntegerDocument());
            txtHeight.setBounds(85,106,80,20);
            txtHeight.addFocusListener(this);
            txtHeight.setText(Integer.toString((int)bounds.getHeight()));
            add(txtHeight);
            
            if (elementType == EditorElement.LABEL || elementType == EditorElement.SEPARATOR || elementType == EditorElement.CHECKBOX || elementType == EditorElement.COMBOBOX) {
                if(elementType == EditorElement.COMBOBOX){
                    lbItems = new JLabel("Elemente"); //dieses Feld wird im Properties-Panel einer ComboBox angezeigt, in das Textfeld
                    lbItems.setBounds(10,130,70,20);  //schreibt man die einzelnen Items die die ComboBox enthalten soll
                    add(lbItems);                     //getrennt durch einen Beistrich
                    txtItems = new JTextField();
                    txtItems.setBounds(85,130,80,20);
                    txtItems.setText(editorElement.getComboBoxItems());
                    txtItems.addFocusListener(this);
                    add(txtItems);
                    
                    comboBoxDbButton = new JButton("...");
                    comboBoxDbButton.setBounds(170,130,20,20);
                    comboBoxDbButton.setToolTipText("Datenbank hinterlegen");
                    comboBoxDbButton.addActionListener(this);
                    add(comboBoxDbButton);
                }
            }
            else{
                
                lbType = new JLabel("Typ");
                lbType.setBounds(10,130,70,20);
                add(lbType);
                String[] diffTypes = new String[]{"Text","Integer","Double","Währung","Datum (Auswahl)","Datum (Feld)","Integer auto_increment"};
                cbType = new JComboBox(diffTypes);
                cbType.setSelectedItem(editorElement.getTextFieldType()); //Wird benötigt, damit die ComboBox die Auswahl behält
                cbType.setBounds(85,130,110,20);
                final String selectedItem = (String)cbType.getSelectedItem();
                cbType.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent evt){
                        
                        if(evt.getItem().equals("Text")){
                            if(txtLength != null){ //zuständig für das Anzeigen des Längen-Textfeldes
                                remove(lbLength);
                                remove(txtLength);
                                txtLength = null;
                                lbLength = null;
                            }
                            
                            lbLength = new JLabel("Länge");
                            lbLength.setBounds(10,202,70,20);
                            add(lbLength);
                            txtLength = new JTextField();
                            txtLength.setDocument(new IntegerDocument());
                            txtLength.setBounds(85,202,80,20);
                            txtLength.setText(editorElement.getTextLength()+"");
                            txtLength.addFocusListener(propertiesPanel);
                            
                            add(txtLength);
                        }
                        else{  //folgende Anweisungen werden benötigt, damit das Längen-Textfeld korrekt hinzugefügt oder entfernt wird, je nach Auswahl
                            if(txtLength != null){
                                remove(lbLength);
                                remove(txtLength);
                                txtLength = null;
                                lbLength = null;
                            }
                        }
                        /*wird benötigt, um festzustellen, ob sich der textfeld-typ geändert hat, 
                         *wenn, dann muss auch die Datenbank upgedatet werden.
                         *update wird ausgelöst indem auf renamed gesetzt wird.
                         */
                        if(evt.getItem().equals(selectedItem))
                            editorElement.setRenamed(true);
                        
                        editorElement.setDataType((String)evt.getItem());
                        
                        repaintArea();
                    }
                });
                add(cbType);
                
                //in dieses Feld wird eine mögliche Berechnung eingegeben
                lbCalc = new JLabel("Berechnung");
                lbCalc.setBounds(10,154,70,20);
                add(lbCalc);
                txtCalc= new JTextField();
                txtCalc.setBounds(85,154,120,20);
                txtCalc.setDocument(new LimitedLengthDocument(255));
                txtCalc.setText(editorElement.getCalc());
                txtCalc.addFocusListener(this);
                add(txtCalc);
                
                //mit dieser checkbox kann festgelegt werden, ob ein Textfeld aktiv ist oder nicht
                ckbEnabled = new JCheckBox("aktiviert");
                ckbEnabled.setBounds(85,178,80,20);
                ckbEnabled.setSelected(editorElement.isEnabled());
                ckbEnabled.addFocusListener(this);
                add(ckbEnabled);
                
                if(cbType.getSelectedItem().equals("Text")){
                    lbLength = new JLabel("Länge");
                    lbLength.setBounds(10,202,70,20);
                    add(lbLength);
                    txtLength = new JTextField();
                    txtLength.setDocument(new IntegerDocument());
                    txtLength.setBounds(85,202,80,20);
                    txtLength.setText(editorElement.getTextLength()+"");
                    txtLength.addFocusListener(this);
                    add(txtLength);
                    
                }
            }
        }
        
        public void focusLost(FocusEvent evt) {
            if(elementType == EditorElement.COMBOBOX)
                editorElement.setComboBoxItems(txtItems.getText());
            if(elementType == EditorElement.TEXTFIELD){
                if(cbType.getSelectedItem().equals("Text"))
                    editorElement.setTextLength(txtLength.getText().trim()); //setzt die Länge eines Textfeldes
                editorElement.setCalc(txtCalc.getText());
                if(!txtCalc.getText().trim().equals("")){
                    editorElement.setEnabled(false);//das Textfeld muss deaktiviert werden, wenn es berechnet werden soll
                    ckbEnabled.setSelected(false);
                }
                else{
                    editorElement.setEnabled(ckbEnabled.isSelected());
                }
            }
            editorElement.setBounds(Integer.parseInt(txtX.getText()),Integer.parseInt(txtY.getText()),Integer.parseInt(txtWidth.getText()),Integer.parseInt(txtHeight.getText()));
            
            if(editorElement.isSelected()){//if-bedingung verhindert null-pointer exc. beim handles-entfernen (entsteht beim klick auf leeren editor-bereich weil die komponente von setSelection deselektiert wird)
                editorElement.removeHandles(glassPane); //die handles müssen zuerst entfernt werden
                editorElement.positionHandles(glassPane);  //dann können sie wieder hinzugefügt werden.
                //dies dient dazu, um wenn die Koordinaten in den Textfeldern verändert werden
                //auch die Handles neu positioniert werden.
            }
            editorElement.setRenamed(true); //setze Renamed auf true damit eventuell veränderte Eigenschaften in der Properties-Leiste
            //auch in der Datenbank geändert werden.
            repaintArea();
        }
        
        public void focusGained(FocusEvent evt) {
            //System.out.println("Focus Gained, lol");
        }
        
        //Folgende Methode setzt die aktuellen Bounds in die Textfelder
        //denn beim neu erzeugen eines elements wird im Feld höhe und breite
        //null angezeigt
        public void updateBounds(){
            Rectangle bounds = editorElement.getBounds();
            txtX.setText(Integer.toString((int)bounds.getX()));
            txtY.setText(Integer.toString((int)bounds.getY()));
            txtWidth.setText(Integer.toString((int)bounds.getWidth()));
            txtHeight.setText(Integer.toString((int)bounds.getHeight()));
        }
        
        //Diese Methode wird vom JDialog aufgerufen und ein String mit Tabellenname und den feldern wird übergeben.
        public void setComboBoxDbString(String s){
            
            if(s.equals("nofields")){
                txtItems.setEnabled(true);
                txtItems.setText("");
                //wenn alle elemente der auswahl wieder entfernt werden, muss das txtItems-Textfeld wieder aktiviert werden
            }
            else{
                txtItems.setText(s);
                txtItems.setEnabled(false);
                editorElement.setComboBoxItems(s);
                //sollen die Items einer ComboBox aus einer DB abgerufen werden, werden die entsprechenden Felder aus dem ComboBoxDbDialog
                //in das txtItems-Textfeld geschrieben. Dieses darf dann nicht mehr vom Benutzer verändert werden und wird daher disabled
            }
            
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String cmd = evt.getActionCommand();
            
            if(cmd.equals("...")){
                showJDialog();
            }
        }
        
        private int elementType;
        private EditorElement editorElement;
        
        private JButton comboBoxDbButton;
        
        private JTextField txtX,txtY,txtHeight,txtWidth,txtName,txtItems,txtCalc,txtLength;
        private JLabel lbX,lbY,lbHeight,lbWidth,lbName,lbType,lbItems,lbCalc,lbLength;
        private JComboBox cbType;
        private JCheckBox ckbEnabled;
    }
    
    
    public void setSelection(EditorElement jc){
        //erste if-bedingung dient der Mehrfachauswahl, die components-liste wird nach ausgewählten komponenten durchsucht
        if(isMultipleSelections){
            ListIterator<EditorElement> compIter = components.listIterator();
            boolean findNewSingleSelected = false;
            EditorElement e;
            while(compIter.hasNext()){
                e = compIter.next();
                
                if (e.getName().equals(jc.getName()) && e.isSelected()){
                    jc.setSelected(false);
                    jc.removeHandles(glassPane);
                    //ist die deselektierte komponente gleichzeitig die singleSelectedComponent, muss dafür eine andere verwendet werden
                    if(jc.getName().equals(singleSelectedComponent.getName()))
                        findNewSingleSelected = true;
                }
                else if (e.getName().equals(jc.getName()) && e.isRemoved() == false){
                    jc.setSelected(true);
                    jc.positionHandles(glassPane);
                    panel.remove(propertiesPanel); //hier wird das Properties-Panel entfernt, da es in der Mehrfachauswahl keine gemeinsamen
                    //optionen gibt.
                }
            }
            //hier wird der singleSelectedComponent eine neue Komponente zugewiesen
            if(findNewSingleSelected == true){
                ListIterator<EditorElement> tempIter = components.listIterator();
                EditorElement ee;
                int count = 0;
                while(tempIter.hasNext()){
                    ee = tempIter.next();
                    if(ee.isSelected()){
                        count++;
                        singleSelectedComponent = ee;
                    }
                }
                //die selektierten komponenten werden gezählt, und je nach ihrer anzahl wird die Variable isMultipleSelections gesetzt.
                if(count == 0){
                    isMultipleSelections = false;
                    removeAllSelections();
                }
            }
        }
        else{ //deselektiert eine singleSelectedComponent
            if (singleSelectedComponent != null && singleSelectedComponent.getName().equals(jc.getName())) {
                removeAllSelections();
                isMultipleSelections = false;
            }
            
            else { //selektiert eine singleSelectedComponent
                if(jc.isRemoved() == false){ //diese if bedingung verhindert, dass handles von entfernten Elementen angezeigt werden, sollten
                    singleSelectedComponent = jc;//diese Bereiche ausgewählt werden.
                    jc.setSelected(true);
                    singleSelectedComponent.positionHandles(glassPane);
                    
                    panel.remove(propertiesPanel);
                    propertiesPanel = new PropertiesPanel(jc);
                    panel.add(propertiesPanel);
                    isMultipleSelections = true;
                }
            }
        }
        
    }
    
    //diese Methode hebt jede Auswahl auf, es wird dazu die components Liste durchsucht
    public void removeAllSelections(){
        ListIterator<EditorElement> compIter = components.listIterator();
        
        EditorElement e;
        while(compIter.hasNext()){
            e = compIter.next();
            if(e.isSelected()){
                e.setSelected(false);
                e.removeHandles(glassPane);
            }
            
        }
        singleSelectedComponent = null;
        panel.remove(propertiesPanel);
        validate();
        repaint();
        isMultipleSelections = false;
        dragHandle = null;
        
    }
    
    
    public void addToButtonGroup(JToolBar toolBar,String buttonName) {
        JToggleButton tb = new JToggleButton(buttonName);
        toolBar.add(tb);
        toolBar.addSeparator();
        btGroup.add(tb);
        tb.setActionCommand(buttonName);
        tb.addActionListener(this);
        
    }
    
    // diese methode sucht in der LinkedList components nach komponenten mit dem namen der im methodenparameter
    // übergeben wird
    private boolean isFieldnameExisting(String fieldname){
        ListIterator<EditorElement> tempIter = components.listIterator();
        while(tempIter.hasNext()){
            if(fieldname.trim().toLowerCase().equals(tempIter.next().getName().trim().toLowerCase()))
                //die strings müssen unbedingt überprüft werden, wenn alle zeichen klein geschrieben sind, denn mysql kennt keinen unterschied
                //zwischen groß und kleinschreibung
                return true;
        }
        return false;
    }
    
    /**erstellt einen feldnamen mit fortlaufender nummer
     *@param typ übergibt einen string der den feldtyp festlegt
     */
    private String setFieldname(String typ){
        int i = 1;
        while(isFieldnameExisting(typ+i)){
            i++;
        }
        return typ+i;
    }
    
    
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        
        if(cmd.equals("Mauszeiger")) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            selection = "Mauszeiger";
        }
        else if(cmd.equals("Info")){
            
        }
        else if(cmd.equals("Unsichtbare Felder ...")){
            InvisibleFieldsDialog dialog = new InvisibleFieldsDialog(this,components,elementsContainer);
            //diese if-anweisung wird benötigt, um zu vermeiden, dass der dialog angezeigt wird, falls keine
            //unsichtbaren felder vorhanden sind. sollte zwar selten eintreten, kann aber vorkommen.
            if(dialog.areFieldsInvisible())
                dialog.setVisible(true);
            
            validate();
            repaint();
        }
        else if(cmd.equals("Beenden")){
            dispose();
        }
        else if (cmd.equals("Bezeichnungsfeld")) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            selection = "Bezeichnungsfeld";
            removeAllSelections();
        }
        else if (cmd.equals("Textfeld")) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            selection = "Textfeld";
            removeAllSelections();
        }
        else if (cmd.equals("Kombinationsfeld")) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            selection = "Kombinationsfeld";
            removeAllSelections();
        }
        else if (cmd.equals("Kontrollkästchen")) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            selection = "Kontrollkästchen";
            removeAllSelections();
        }
        else if(cmd.equals("Textbereich")){
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            selection = "Textbereich";
            removeAllSelections();
        }
        else if(cmd.equals("Linie")){
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            selection = "Linie";
            removeAllSelections();
        }
        else if(cmd.equals("Kopieren")){
            LinkedList<EditorElement> zwa = new LinkedList<EditorElement>();
            
            ListIterator<EditorElement> compIter = components.listIterator();
            while(compIter.hasNext()){
                EditorElement e = compIter.next(); //die ausgewählten Elemente werden einfach in eine neue LinkedList
                if(e.isSelected()){                                //eingefügt, und in der datei zwischenablage.zwa gespeichert
                    zwa.addLast(e);
                }
            }
            try{
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("zwischenablage.zwa"));
                
                out.writeObject(zwa);
                out.close();
            }catch(Exception exc) {
                System.out.println("Fehler: "+exc);
            }
        }
        else if(cmd.equals("Einfügen")){
            removeAllSelections();
            
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream("zwischenablage.zwa"));  //daten werden aus der Datei
                LinkedList<EditorElement> zwa = (LinkedList<EditorElement>) in.readObject();                                            //zwischenablage.zwa geladen
                in.close(); //der ObjectInputStream muss geschlossen werden, da die datei sonst nicht gelöscht werden kann
                ListIterator<EditorElement> compIter = zwa.listIterator();
                while(compIter.hasNext()) {
                    EditorElement e = compIter.next();
                    if(e.isSelected()){             //sollte das Element beim Kopiere oder Ausschneiden markiert gewesen sein, muss es deselektiert
                        e.removeHandles(glassPane); //werden. Zuerst müssen die Handles entfernt werden, sonst können keine neuen kreiert werden
                        e.setSelected(false);       //dann kann die variable setSelected(boolean) auf false gesetz werden.
                        
                    }
                    
                    e.setRemoved(false); //sehr wichtig, da das Element sonst im Maskeneditor nicht richtig behandelt wird
                    int i = 0;
                    while(isFieldnameExisting(e.getName())){ //der Name muss, falls ein identischer vorhanden, geändert werden
                        i++;                                 //das heißt, auch die variable oldName und die beschriftung müssen geändert werden
                        String newName = e.getName()+"_"+i;
                        e.setName(newName);
                        e.setOldName(newName);
                        if(e.getType() != EditorElement.LABEL){
                            //if-bedingung verhindert Beschriftungsänderung bei Labels beim Ausschneiden/Kopieren und späterem Einfügen
                            e.setText(newName);
                        }   
                    }
                    Rectangle r = e.getBounds();                   //die koordinaten müssen auch geändert werden, damit die eingefügten
                    e.setBounds(r.x+20,r.y+20,r.width,r.height);   //komponenten die vorigen nicht völlig überdecken
                    e.setAdded(true);
                    elementsContainer.add(e);
                    components.addLast(e);
                    setSelection(e);
                }
                
                validate();
                repaint();
                
            }catch(Exception exc) {
                System.out.println("Fehler: "+exc);
            }
        }
        else if(cmd.equals("Ausschneiden")){
            LinkedList<EditorElement> zwa = new LinkedList<EditorElement>();
            
            ListIterator<EditorElement> compIter = components.listIterator();
            while(compIter.hasNext()){
                EditorElement e = compIter.next(); //die ausgewählten Elemente werden einfach in eine neue LinkedList
                if(e.isSelected()){                                //eingefügt, und in der datei zwischenablage.zwa gespeichert
                    zwa.addLast(e);
                }
            }
            deleteElement();
            try{
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("zwischenablage.zwa"));
                
                out.writeObject(zwa);
                out.close();
            }catch(Exception exc) {
                System.out.println("Fehler: "+exc);
            }
        }
        else if(cmd.equals("Laden")) {
            MaskLoaderDialog msk = new MaskLoaderDialog(this);
            msk.setVisible(true);
            
            String maskName = msk.getSelectedMask();
            if (maskName != null){
                clearEditorArea(); //setzt alle Variablen so, als wäre die Anwendung gerade neu gestartet worden
                
                loadDocument(maskName);
                openedFile = maskName;
                
                this.setTitle("Maskeneditor - " + openedFile);
                
                isFileNew = false;
            }
        }
        else if (cmd.equals("Feld löschen")) {
            deleteElement();
        }
        else if(cmd.equals("Maske löschen")){
            if(!isFileNew){
                Object[] options = { "Ja","Nein"};
                int selection = JOptionPane.showOptionDialog(null,"Sind Sie sicher, dass sie diese Maske löschen wollen?\n Alle Daten, die in dieser Maske gespeichert wurden, gehen verloren!","Maske löschen", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (selection == JOptionPane.OK_OPTION){
                    try{
                        //zuerst wird die Datentabelle gelöscht
                        execSQLMetaCmd("DROP TABLE "+openedFile.toLowerCase());
                        //:TODO: Abfragehandling vereinheitlichen (z.b. datenbankname an methode übergeben, batch-update?) und ev mit transactions versehen
                        //dann die strukturtabelle
                        Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
                        Statement stmt = con.createStatement();
                        stmt.execute("DROP TABLE "+openedFile.toLowerCase());
                        stmt.close();
                        con.close();
                    }catch (SQLException ex){
                        System.out.println("Beim löschen ist ein Fehler aufgetreten!");
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
                    clearEditorArea();
                }
            }
        }
        else if(cmd.equals("Neu")){
            clearEditorArea();
            setTitle("Maskeneditor - [Neu]");
            MaskTemplateChooserDialog d = new MaskTemplateChooserDialog(this);
            d.setVisible(true);
            String selectedTemplate = d.getSelectedTemplate();
            //if(!selectedTemplate.equals("Standardvorlage"))
            loadTemplate(selectedTemplate);
            
        }
        else if(cmd.equals("Als Vorlage speichern")){
            saveAsTemplate();
        }
        else if (cmd.equals("Speichern")) {
            if(openedFile.endsWith("_mod")){
                JOptionPane.showMessageDialog(this, "Diese Speicherfunktion dient zum Speichern von Masken.\nModule müssen über \"Module\" -> \"Modul speichern\" gespeichert werden!",
                                              "Falsche Speicherfunktion", JOptionPane.OK_OPTION);
                return;
            }
            if(!(isFileNew)){
                saveDocument(openedFile.toLowerCase());
            }
            else{
                //zeigt den Speicher-Dialog an
                MaskSaverDialog d = new MaskSaverDialog(this);
                d.setVisible(true);
                
                //falls der dateiname bereits existiert fragt das Programm via dialogfeld nach, ob
                //die datei überschrieben werden soll, oder nicht
                if(d.isMasknameLikeTemplate()){
                    Object [] options ={"Ok"};
                    JOptionPane.showOptionDialog(null,"Eine Vorlage mit diesem Namen existiert bereits, Maske kann unter diesem Namen nicht gespeichert werden",
                    "Speichern", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                    return;
                }
                
                String maskName = d.getMaskName();
                
                
                if(d.isMaskNameExisting() && !maskName.equals("")){
                    Object[] options = { "Ja","Nein"};
                    int selection = JOptionPane.showOptionDialog(null,"Diese Datei existiert bereits. Wollen Sie sie überschreiben?"
                    + "Dabei gehen alle zur Maske gehörenden Daten verloren!!!"
                    ,"Speichern", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                    
                    if (selection == JOptionPane.OK_OPTION){
                        //da die maske überschieben wird, müssen die tabellen vorher gelöscht werden.
                        try{
                            execSQLMetaCmd("DROP TABLE "+maskName.toLowerCase()); //löscht tabelle in datenbank icaddb
                            
                            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
                            Statement stmt = con.createStatement();
                            stmt.execute("DROP TABLE "+maskName.toLowerCase()); //löscht tabelle in datenbank masken
                            stmt.close();
                            con.close();
                        }catch (SQLException ex) {
                            System.out.println("Beim Löschen der Tabelle Fehler aufgetreten!");
                            System.out.println("SQLState: "
                            + ex.getSQLState());
                            System.out.println("Nachricht:  "
                            + ex.getMessage());
                            System.out.println("Anbieter:   "
                            + ex.getErrorCode());
                            ex = ex.getNextException();
                            System.out.println("");
                        }
                        openedFile = maskName.toLowerCase();
                        saveDocument(openedFile);
                        isFileNew = false;
                    }
                    else if (selection == JOptionPane.NO_OPTION){}
                }
                else if(!maskName.equals("")){
                    openedFile = maskName.toLowerCase();
                    saveDocument(openedFile);
                    isFileNew = false;
                    
                }
            }
        }
        else if(cmd.equals("Modul laden")){
            
        }
        else if(cmd.equals("Modul speichern")){
            if(!openedFile.endsWith("_mod") && !isFileNew){
                JOptionPane.showMessageDialog(this, "Diese Speicherfunktion dient zum Speichern von Modulen.\nMasken müssen über \"Datei\" -> \"Speichern\" gespeichert werden!",
                                              "Falsche Speicherfunktion", JOptionPane.OK_OPTION);
                return;
            }
            ModuleSaverDialog saveDialog = new ModuleSaverDialog(this, components, openedFile, isFileNew);
            saveDialog.setVisible(true);
            String modulName = saveDialog.getModulName();
            hauptMaskenName = saveDialog.getHauptMaskenName();
            
            if(isFileNew && !modulName.equals("")){
                openedFile = modulName;
                saveDocument(openedFile);
            }
            else if(!isFileNew && !modulName.equals("")){
                saveDocument(openedFile);
            }
        }
        else if(cmd.equals("Modul löschen")){
            
        }
        
    }
    
    /*lädt eine Vorlage mit dem angegebenen Namen
      es wird nur noch der tabellenname übergeben, den eigentlichen ladevorgang übernimmt
      die methode loadDocument(String)
     */
    public void loadTemplate(String template){
        loadDocument(template.toLowerCase());
    }
    
    /** speichert eine Maske als Vorlage ab. Die Attribute der einzelnen EditorElemente
     *  werden so gesetzt, dass alle felder als added gekennzeichnet sind,
     */
    public void saveAsTemplate(){
        MaskTemplateDialog d = new MaskTemplateDialog(this);
        d.setVisible(true);
        String templateName = d.getNewTemplatesName();
        
        //wenn vorlagenname leer ist, wurde entweder in das dialogfeld nichts eingegeben
        //oder es wurde auf abbrechen geklickt.
        if(!templateName.equals("")){
            if(!templateName.endsWith("_templ")){
                templateName += "_templ";
            }
            
            
            removeAllSelections();
            
            try{
                Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
                Statement stmt = con.createStatement();
                
                ResultSet rs = stmt.executeQuery("SHOW tables");
                
            /* um feststellen zu können, ob eine vorlage mit gleichem namen bereits vorhanden ist, werden
             * alle tabellennamen abgefragt und mit dem zu speichernden namen verglichen.
             * wird ein gleicher tabellenname gefunden, wird ein Dialog zur nachfrage ausgegeben.
             */
                boolean isTemplateExisting = false;
                while (rs.next()){
                    if(rs.getString(1).equals(templateName)){
                        String [] options = {"Ja","Nein"};
                        int selectedOption = JOptionPane.showOptionDialog(this, "Vorlage ist bereits vorhanden! Soll sie überschrieben werden?",
                        "Fehler", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,options , options[0]);
                        
                        if(selectedOption == 1)
                            return;
                        isTemplateExisting = true;
                        
                    }
                }
                stmt.close();
                
                if(isTemplateExisting){
                    stmt = con.createStatement();
                    //Truncate entfernt alle zeilen aus der tabelle
                    stmt.executeUpdate("TRUNCATE "+templateName.toLowerCase());
                    stmt.close();
                }
                else{
                    stmt = con.createStatement();
                    stmt.execute("CREATE table "+templateName.toLowerCase()+" "+maskTableStructure);
                    stmt.close();
                }
                
            /*es werden alle zeilen in der tabelle gelöscht, soll sie überschrieben werden, da sonst eventuelle änderungen
             *z.b. Koordinaten, nicht berücksichtigt würden, denn es gibt keinen flag für solche
             *änderungen. die einzelnen zeilen werden dann aus den informationen der textfelder neu
             *aufgebaut.
             */
                
                String insertionString = "INSERT INTO "+templateName.toLowerCase()+" VALUES ";
                
                ListIterator<EditorElement> iter = components.listIterator();
                while(iter.hasNext()){
                    String value = "(";
                    
                    EditorElement e = iter.next();
                    
                    //wurde ein feld gelöscht, soll es natürlich nicht in der Vorlage gespeichert werden
                    if(!e.isRemoved()){
                        value += "'"+e.getName()+"',";
                        value += e.getType()+",";
                        
                        value += e.isEnabled() ? "1," : "0,";
                        value += e.isProtected() ? "1," : "0,";
                        
                        Rectangle r = e.getBounds();
                        
                        value += r.getX()+",";
                        value += r.getY()+",";
                        value += r.getHeight()+",";
                        value += r.getWidth()+",";
                        
                        value += "'"+e.getDataType()+"',";
                        
                        //diese if-else bed. setzt die label-bezeichnung richtig in das comboBoxItems feld der tabele ein
                        if (e.getType() == 1){
                            JLabel lb = (JLabel)e.getElement();
                            value += "'"+lb.getText()+"',";
                        }
                        else{
                            value += "'"+e.getComboBoxItems()+"',";
                        }
                        value += "'"+e.getCalc()+"',";
                        value += "'"+e.getTextFieldType()+"',";
                        value += e.getId()+",";
                        value += e.getTextLength()+")";
                        
                        if (insertionString.endsWith("VALUES ")){
                            insertionString += value;
                        }
                        else
                            insertionString += ","+value;
                    }   
                }
                stmt = con.createStatement();
                stmt.execute(insertionString);
                
                stmt.close();
                con.close();
                
            }catch(SQLException e){
                String [] options = {"OK"};
                JOptionPane.showOptionDialog(this, "Beim Speichern der Vorlage ist ein Fehler aufgetreten! Stellen Sie sicher, das die Datenbank erreichbar ist und versuchen sie es erneut!",
                "Fehler", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,options , options[0]);
                e.printStackTrace();
            }
        }
    }
    
    /**führt alle operationen zum ändern der datensturktur der datendb und der felderdb durch
     *
     */
    public void saveDocument(String maskName) {
        removeAllSelections();
        
        try {
            updateDbMetaData();
            
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            
            stmt.execute("DROP TABLE IF EXISTS "+maskName);
            stmt.execute("CREATE table "+maskName+" "+maskTableStructure);
            stmt.close();
            
            String insertionString = "INSERT INTO "+maskName+" VALUES ";
            
            ListIterator<EditorElement> iter = components.listIterator();
            while(iter.hasNext()){
                String value = "(";
                
                EditorElement e = iter.next();
                
                //wurde ein feld gelöscht, soll es natürlich nicht in der Vorlage gespeichert werden
                if(!e.isRemoved()){
                    value += "'"+e.getName()+"',";
                    value += e.getType()+",";
                    
                    value += e.isEnabled() ? "1," : "0,";
                    value += e.isProtected() ? "1," : "0,";
                    
                    Rectangle r = e.getBounds();
                    
                    value += r.getX()+",";
                    value += r.getY()+",";
                    value += r.getHeight()+",";
                    value += r.getWidth()+",";
                    
                    value += "'"+e.getDataType()+"',";
                                        
                    //diese if-else bed. setzt die label-bezeichnung richtig in das comboBoxItems feld der tabele ein
                    if (e.getType() == 1){
                        JLabel lb = (JLabel)e.getElement();
                        value += "'"+lb.getText()+"',";
                    }
                    else{
                        value += "'"+e.getComboBoxItems()+"',";
                    }
                    value += "'"+e.getCalc()+"',";
                    value += "'"+e.getTextFieldType()+"',";
                    value += e.getId()+",";
                    value += e.getTextLength()+")";
                    
                    if (insertionString.endsWith("VALUES ")){
                        insertionString += value;
                    }
                    else
                        insertionString += ","+value;
                }
            }
            stmt = con.createStatement();
            stmt.execute(insertionString);
            
            stmt.close();
            con.close();
            
            openedFile = maskName;
            this.setTitle("Maskeneditor - "+openedFile);
            isFileNew = false;
            validate();
            repaint();
        }catch(SQLException ex) {
            String [] options = {"OK"};
            JOptionPane.showOptionDialog(this, "Beim Speichern der Maske ist ein Fehler aufgetreten! Stellen Sie sicher, das die Datenbank erreichbar ist und versuchen sie es erneut!",
            "Fehler", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,options , options[0]);
            
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
    
    /*
     *Diese methode lädt die maskenfelder aus der datenbank und erstellt dann für jedes Feld
     *ein EditorElement mit den passenden eigenschaften.
     */
    public void loadDocument(String tableName) {
        
        try{
            Connection con = DriverManager.getConnection("Jdbc:mysql://"+System.getProperty("server")+":"+System.getProperty("port")+"/masken",System.getProperty("username"),System.getProperty("pwd"));
            Statement stmt = con.createStatement();
            
            String maskRequest = "SELECT * From "+tableName;
            
            ResultSet rs = stmt.executeQuery(maskRequest);
            
            components = new LinkedList<EditorElement>();
            
            while(rs.next()){
                
                //hier werden aus den feldern der datenbank die einzelnen editorelemente
                //erstellt.
                //code und vorgehensweise sehr ähnlich zum Felderhinzufügen des ContainerGenerators
                EditorElement e = new EditorElement(rs.getInt("Type"));
                
                String name = rs.getString("Name").trim();
                e.setName(name);
                e.setOldName(name); //muss gesetzt werden, da sonst beim speichern fehler auftreten können
                e.setText(name);
                
                //setzt, ob das feld in der Maske aktiviert oder dekativiert ist (z.b bei textfeldern)
                e.setEnabled(rs.getBoolean("Enabled"));
                //setzt, ob das feld protected ist, also ein wichtiges feld in der maske darstellt.
                e.setProtected(rs.getBoolean("ProtectedField"));
                
                //setzt die Koordinaten des Feldes
                e.setBounds(rs.getInt("X"),rs.getInt("Y"),rs.getInt("Length"),rs.getInt("Height"));
                
                
                //setzt die comboBox items
                String comboBoxItems = rs.getString("ComboBoxItems");
                if(comboBoxItems == null){
                    comboBoxItems = "";
                }
                e.setComboBoxItems(rs.getString("ComboBoxItems"));
                
                //dieser aufruf setzt den richtigen Text im Label, weil dieser für Labels
                //im feld comboBoxItems gespeichert wird. sonst würde wie oben die bezeichung
                //auf den Labelname gesetzt, welche dann Labelx lauten würde
                if(e.getType() == 1){
                    e.setText(comboBoxItems);
                }
                
                //setzt den berechnungsstring, falls vorhanden
                String calc = rs.getString("Calc");
                if(calc == null){
                    calc = "";
                }
                e.setCalc(rs.getString("Calc"));
                
                //setzt die Feldlänge, muss unbedingt vor dem TextFieldType gesetzt werden, sonst wird der gesetzte Typ wieder überschrieben
                e.setTextLength(Integer.toString(rs.getInt("TextLength")));
                
                if(e.getType() == EditorElement.TEXTFIELD){
                    e.setTextFieldType(rs.getString("TextFieldType"));
                }
                
                //setzt die ID für das jeweilige Feld, ev erforderlich für die Focusreihenfolge
                e.setId(rs.getInt("ID"));
                
                //dieser add Befehl platziert die felder auf dem container
                components.addLast(e);
                
                elementsContainer.add(e);
            }
            con.close();
            
            validate();
            repaint();
            
        }catch(SQLException ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            System.out.println("");
            
            System.out.println("SQLState: "
            + ex.getSQLState());
            System.out.println("Nachricht:  "
            + ex.getMessage());
            System.out.println("Anbieter:   "
            + ex.getErrorCode());
            ex = ex.getNextException();
            System.out.println("");
        }
    }
    
    /**löscht ein EditorElement
     *
     */
    private void deleteElement() {
        ListIterator<EditorElement> tempIter = components.listIterator();
        while(tempIter.hasNext()){
            EditorElement editorElement = tempIter.next();
            if (editorElement.isSelected()){
                editorElement.setSelected(false); //muss auf false gesetzt werden
                editorElement.removeHandles(glassPane); //handles müssen unbedingt entfernt werden
                elementsContainer.remove(editorElement); //das Element muss auch vom container entfernt werden
                if(isFileNew){ //Wenn die Maske nicht neu angelegt wurde, darf das gelöschte
                    tempIter.remove(); //element nicht aus der List entfernt werden, da sonst der String
                    //zum Updaten der DB nicht korrekt erstellt wir und das Feld in der DB nicht gelöscht wird
                }
                else editorElement.setRemoved(true);
            }
        }
        singleSelectedComponent = null;
        panel.remove(propertiesPanel);
        isMultipleSelections = false;
        validate();
        repaint();
    }
    
    /** stellt den sql-String für das updaten der datentabelle für eine masken zusammen, oder erstellt eine datentabelle neu
     *  die felder werden dabei je nach attribut (added, removed, renamed) behandelt.
     */
    private void updateDbMetaData(){
        String create = "";
        ListIterator<EditorElement> tempIter = components.listIterator();
        
        if (isFileNew){
            create = "CREATE TABLE "+openedFile.toLowerCase()+" (";
            while(tempIter.hasNext()){
                
                EditorElement editorElement = tempIter.next();
                if(editorElement.getType() != EditorElement.LABEL && editorElement.getType() != EditorElement.SEPARATOR){
                    if(!create.endsWith("("))
                        create += ",";
                    
                    //beim datentyp auto_increment darf nicht mehr NOT NULL angefügt werden, sonst Syntaxerror
                    if(editorElement.getTextFieldType().equals("Integer auto_increment")){
                        create += editorElement.getName()+" "+editorElement.getDataType();
                    }
                    else
                        create += editorElement.getName()+" "+editorElement.getDataType() + " NOT NULL";

                    
                    //folgende Anweisungen setzen die verschiedenen attribute auf "Unverändert"
                    editorElement.setRemoved(false);
                    editorElement.setAdded(false);
                    editorElement.setRenamed(false);
                    editorElement.setOldName(editorElement.getName());
                }
            }
            if(openedFile.endsWith("_mod")){
                create += ", INDEX (index_table_id), FOREIGN KEY (index_table_id) REFERENCES mod_index_"
                +hauptMaskenName+"(id) ON DELETE CASCADE) TYPE = INNODB";
            }
            else
                create += ") Type = INNODB";
            
        }
        else {
            //Diese anweisungen setzen aus den Eigenschaften der einzelnen Elemente einen String
            //zusammen der dann die Datenbank modifiziert
            create = "ALTER TABLE "+openedFile.toLowerCase()+" ";
            String createAdded="", createRemoved="", createRenamed="";
            while(tempIter.hasNext()){
                EditorElement editorElement = tempIter.next();
                if(editorElement.getType() != EditorElement.LABEL && editorElement.getType() != EditorElement.SEPARATOR){
                    if(editorElement.isRemoved() == true && editorElement.getAdded() == false){
                        if(!(createRemoved.length()==0))
                            createRemoved += ",";
                        createRemoved += "DROP COLUMN "+editorElement.getOldName();
                        tempIter.remove();
                    }
                    else if(editorElement.isRenamed()== true && editorElement.isRemoved() == false && editorElement.getAdded() == false){
                        if(!(createRenamed.length()==0))
                            createRenamed += ",";
                        if(editorElement.getTextFieldType().equals("Integer auto_increment")){
                            /*diese anweisung ermöglich das korrekte changen des primary keys
                             */
                            createAdded += "CHANGE "+editorElement.getOldName()+" "+editorElement.getName()+
                            " INT NOT NULL AUTO_INCREMENT, ADD PRIMARY KEY("+editorElement.getName()+")";
                        }
                        else
                            createRenamed += "CHANGE "+editorElement.getOldName() + " " + editorElement.getName()
                            + " " + editorElement.getDataType() + " NOT NULL";
                    }
                    else if(!(editorElement.isRemoved() == true) && editorElement.getAdded() == true){
                        if(!(createAdded.length()==0))
                            createAdded += ",";
                        if(editorElement.getTextFieldType().equals("Integer auto_increment")){
                            /*diese anweisung ermöglich das korrekte adden des primary keys
                             *kann ev entfernt werden durch die umstellung auf ADD(feld1,feld2), 
                             *bei dieser methode der abfragezusammensetzung allerdings schwierig
                             */
                            createAdded += "ADD "+editorElement.getName()+" INT NOT NULL AUTO_INCREMENT, " +
                            "ADD PRIMARY KEY("+editorElement.getName()+")";
                        }
                        else
                            createAdded += "ADD "+editorElement.getName()+" "+editorElement.getDataType() + " NOT NULL";
                    }
                    else if(editorElement.isRemoved() == true && editorElement.getAdded()==true)
                        tempIter.remove();
                    
                    /*diese anweisungen müssen unbedingt innerhalb der if-anweisung zum aussschluss von labels und separatoren
                     *da sonst auch deren attribute zurückgesetzt werden und sie in der maskenstruktur nicht mehr korrekt
                     *gespeichert oder falls sie gelöscht wurden nicht gespeichert werden.
                     */
                    editorElement.setRemoved(false);
                    editorElement.setAdded(false);
                    editorElement.setRenamed(false);
                    editorElement.setOldName(editorElement.getName());
                }
                
            }
            create+=createRemoved;
            if(createRemoved.length() > 0 && createAdded.length()>0)
                create += ","+createAdded;
            else create += createAdded;
            
            if((createAdded.length() > 0 || createRemoved.length() > 0) && createRenamed.length()>0)
                create += ","+createRenamed;
            else create += createRenamed;
        }
        
        //System.out.println(create);//Hilfe zum Debuggen, muss später entfernt werden
        try{
        if(!create.endsWith(" ")){
            execSQLMetaCmd(create);
        }
        }catch(SQLException ex) {
            System.out.println("SQLState: "
            + ex.getSQLState());
            System.out.println("Nachricht:  "
            + ex.getMessage());
            System.out.println("Anbieter:   "
            + ex.getErrorCode());
            ex = ex.getNextException();
            System.out.println("");
        }
    }
    
    /**Führt das übergebenen SQL-Kommando aus, wenn ein fehler auftritt, wirft es wieder eine SQLException
     *damit diese beim aufrufer abgefangen werden kann und damit die ausführung weiterer Sql-Kommandos verhindert wird.
     */
    public void execSQLMetaCmd(String cmd) throws SQLException{
        try {
            Connection con = DriverManager.getConnection(System.getProperty("dbpath"),System.getProperty("username"),System.getProperty("pwd"));
            
            Statement stmt = con.createStatement();
            stmt.execute(cmd);
            
            stmt.close();
            con.close();
            
            
        }catch (SQLException ex)
        {  throw ex;/*
            System.out.println("SQLException:");
           while (ex != null)
           {  System.out.println("SQLState: "
              + ex.getSQLState());
              System.out.println("Nachricht:  "
              + ex.getMessage());
              System.out.println("Anbieter:   "
              + ex.getErrorCode());
              ex = ex.getNextException();
              System.out.println("");
           }*/
        }
    }
    
    /**diese methode wird dazu verwendet, um alle elementbezogenen varialben auf null zurückzusetzen
       (counter, die Elementen Liste, maskenname(opendedFile), isFileNew, ...)
     */
    public void clearEditorArea(){
        removeAllSelections(); //wird benötigt, damit alle handles entfernt werden
        //folgender ListIterator und dazugehörige Schleife bewirken, dass alle objekte auf dem hauptfenster entfernt werden
        ListIterator<EditorElement> compIter = components.listIterator();
        
        while(compIter.hasNext()){
            elementsContainer.remove(compIter.next());
        }
        
        components = new LinkedList<EditorElement>();
        singleSelectedComponent = null; //alle counter müsssen auf ihre ausgangswerte gesetzt werden
        
        openedFile = "[Neu]"; //name der geöffneten Datei
        isFileNew = true;
        
        validate();
        repaint();
    }
    
    class GlassPane extends JPanel implements KeyListener {
        
        public GlassPane()  {
            setOpaque(false);
            setLayout(new NullLayout());
            
            //diese beiden aufrufe braucht man, um KeyEvents machbar zu machen
            //this.setFocusable(true);
            //this.addKeyListener(this);
            //??FocusEvent evt = new FocusEvent(this,FocusEvent.FOCUS_GAINED);
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (startPt != null && endPt != null)  {
                g.setColor(Color.black);
                
                //diese Anweisung zeichnet ein Rechteck aus startPt und entPt.
                g.drawRect(startPt.x, startPt.y,
                endPt.x - startPt.x, endPt.y - startPt.y);
                
                //diese if-bedingung dient der Zeichnung der Rechtecke bei Mehrfachauswahl
                if(isMultipleSelections && selection.equals("Mauszeiger") && dragHandle != null){
                    Rectangle r = singleSelectedComponent.getBounds();
                    Point xChange = new Point(0,0);
                    Point yChange = new Point(0,0);
                    
                    xChange.x = startPt.x - r.x;
                    xChange.y = startPt.y - r.y;
                    
                    yChange.x = r.x+r.width - endPt.x;
                    yChange.y = r.y+r.height - endPt.y;
                    
                    ListIterator<EditorElement> compIter = components.listIterator();
                    
                    while(compIter.hasNext()){
                        EditorElement e = compIter.next();
                        Rectangle er = e.getBounds();
                        if(e.isSelected()){
                            switch (dragHandle.getType()){
                                case Handle.NW_HANDLE: g.drawRect(er.x+xChange.x,er.y+xChange.y,er.width-xChange.x,er.height-xChange.y); break;
                                case Handle.N_HANDLE: g.drawRect(er.x,er.y+xChange.y,er.width,er.height - xChange.y); break;
                                case Handle.NE_HANDLE:g.drawRect(er.x+xChange.x,er.y+xChange.y,er.width-yChange.x,er.height-xChange.y); break;
                                case Handle.W_HANDLE: g.drawRect(er.x+xChange.x,er.y,er.width-xChange.x,er.height); break;
                                case Handle.E_HANDLE: g.drawRect(er.x,er.y,er.width-yChange.x,er.height); break;
                                case Handle.C_HANDLE: g.drawRect(er.x+xChange.x,er.y+xChange.y,er.width,er.height); break;
                                case Handle.SW_HANDLE:g.drawRect(er.x+xChange.x,er.y,er.width-xChange.x,er.height - yChange.y); break;
                                case Handle.S_HANDLE: g.drawRect(er.x,er.y,er.width,er.height-yChange.y); break;
                                case Handle.SE_HANDLE: g.drawRect(er.x,er.y,er.width-yChange.x,er.height-yChange.y); break;
                                default: break;
                            }
                        }
                    }
                }
            }
        }
        
        //diese Methoden behandeln tastatureingaben
        public void keyTyped(KeyEvent evt) {
            //sollte beim drücken auf entfernen die markierten felder löschen
            System.out.println(evt.getKeyCode()+evt.getKeyChar());
            if(evt.getKeyCode() == evt.VK_DELETE)
                deleteElement();
        }
        public void keyPressed(KeyEvent evt) {
            System.out.println(evt.getKeyCode()+"s");
        }
        
        
        public void keyReleased(KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_DELETE)  {
                System.out.println("Alle markierten Felder sollen gelöscht werden");
            }
        }
    }
    
    class CustomPanel extends JRootPane{
        public CustomPanel(){
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            //diese paint-Anweisungen zeichen das Raster im Editorbereich
            Rectangle r = getBounds();
            
            int verticalLines = r.width/10;
            int horizontalLines = r.height/10;
            for(int i = 0;i<verticalLines;i++){
                for(int ii = 0;ii<horizontalLines;ii++){
                    g.fillRect(i*10,ii*10,1,1);
                }
            }
        }
    }
    
    //ruft den ComboBoxDbDialog auf, da vom der PropertiesPanel-Klasse nicht auf den JFrame zugegriffen werden kann
    //kann durch optionen (entweder Strings oder Integer) erweiter werden und so verschiedene Dialoge aufrufen
    public void showJDialog(){
        JDialog d = new ComboBoxDbDialog(this,propertiesPanel);
        d.setVisible(true);
    }
    
    //dient der Neuzeichnung des Editors aus dem PropertiesPanel heraus, kann auch generell zum neuzeichnen aufgerufen werden
    public void repaintArea(){
        validate();
        repaint();
    }
    
    
    /*
    public static void main(String [] args) {
        JFrame f = new Maskeneditor();
        f.show();
    }
    */
    
    //dieser String enthält die Struktur für die Tabellen in der Maskendatenbank.
    private static final String maskTableStructure = "(Name VARCHAR(50), Type TINYINT(1), Enabled TINYINT(1), ProtectedField TINYINT(1), X INTEGER, Y INTEGER, Height INTEGER, Length INTEGER, DataType VARCHAR(100), ComboBoxItems VARCHAR(255), Calc VARCHAR(255), TextFieldType VARCHAR(40), ID INTEGER, TextLength INTEGER)";
    
    private JMenuBar menuBar;
    private LinkedList<EditorElement> components = new LinkedList<EditorElement>(); //enthält alle editorElemente
    private ButtonGroup btGroup; //Die in der Symbolleiste verwendeten Buttons sind in einer Button group organisiert
    private String selection = "Mauszeiger"; //setzt fest, welcher Button gerade selektiert ist.
    private Container contentPane; //die contentPane der Editorfläche
    private EditorElement singleSelectedComponent; //enhält das EditorElement wenn nur eines ausgewählt ist
    private JToolBar bar; //toolbar für die buttonGroup
    private JRootPane elementsContainer; //rootpane auf der die EditorElemente plaziert werden
    private PropertiesPanel propertiesPanel; //grundfläche für die eigenschaftenleist
    private JSplitPane sPane; //split-pane, also 2teiliges pane um die Eigenschaftenleiste vom editorbereich zu trennen
    private JPanel panel;
    private CustomScrollPane scrollPane; //scrollPane um den editorbereich scrollbar zu machen
    
    private String openedFile = "[Neu]"; //name der geöffneten Datei
    private String hauptMaskenName;
    private boolean isFileNew = true; //wird benötigt, damit beim speichern klar wird ob die datei neu erstellt wurde oder schon
                                      //oder schon vorhanden war
    private boolean isMultipleSelections = false; //wird auf true gesetzt wenn mehrere felder ausgewählt sind weil diese dann
                                                  //anders behandelt werden (andere Eigenschaftenleiste, etc)
    
    private GlassPane glassPane;
    private Point startPt;
    private Point endPt;
    private Handle dragHandle;
    
    private static final long serialVersionUID = 35L;
    
    //private JTextField coordinatesTextField;
}
