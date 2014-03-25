package com.icad.maskeneditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 *
 * @author  Sebastian
 *
 *diese Klasse erzeugt ein Dialogfeld, welches ein Textfeld enthält in das der Name
 *einer neuen Vorlage eigegeben werden kann. Unter diesem Namen wird die neue Vorlage 
 *dann gespeichert.
 */
public class MaskTemplateDialog extends JDialog implements ActionListener{
    
    /** Creates a new instance of MaskTemplateDialog */
    public MaskTemplateDialog(JFrame parent) {
        super(parent,"Speichern unter ...",true);
        
        setSize(300,120);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        setLocation((int)(d.getWidth() - 300) / 2,(int)(d.getHeight() - 120) / 2);
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout());
        
        JLabel lb = new JLabel("Namen der neuen Vorlage angeben:");
        contentPane.add(lb);
        
        txtNewTemplatesName = new JTextField(20);
        contentPane.add(txtNewTemplatesName);
        
        JPanel buttonPanel = new JPanel();
        contentPane.add(buttonPanel,"South");
        
        speichernButton = new JButton("Speichern");
        speichernButton.addActionListener(this);
        buttonPanel.add(speichernButton);
        
        abbrechenButton = new JButton("Abbrechen");
        abbrechenButton.addActionListener(this);
        buttonPanel.add(abbrechenButton);
    }
    
    /** Invoked when an action occurs.
     *
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if(cmd.equals("Abbrechen")){
            newTemplatesName = "";
            dispose();
        }
        else if(cmd.equals("Speichern")){
            newTemplatesName = txtNewTemplatesName.getText();
            dispose();
        }       
    }
    
    public String getNewTemplatesName(){
        return newTemplatesName;
    }
    
    private String newTemplatesName = "";
    
    private JButton speichernButton;
    private JButton abbrechenButton;
    private JTextField txtNewTemplatesName;
}
