/*Die handler liegen auf der Glass-Pane, die alle Events abfängt. Es wird beim verschieben
 *eines Handles oder bei der Neuerstellung eines Elements zuerst ein Rechteck in der entsprechenden
 *Größe gezeichnet(auf die Glass-Pane), dann werden die Handles auf die Glass-Pane gezeichnet, und
 *schließlich wird die eigentliche Komponente auf das darunterliegende Panel gezeichnet.
 *Um ein Element auszuwählen, klickt man einfach auf das Element. Dies funktioniert mit
 *elementsContainer.getComponentAt(Point p) Dann werden die Handles gezeichnet.
 *Die Handles bestehen aus JComponents, die dann mit einer schwarzen oberfläche versehen werden
 *alle benötigen handles werden in einem Array gespeichert, je nachdem welches handle angeklickt
 *und verschoben wird, wird das Element entsprechend verschoben.
 */

package com.icad.maskeneditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.icad.main.*;


public class Handle extends JComponent {
    
    public Handle(EditorElement e,int t){
        type = t;
        editorElement = e;
    }
        
    protected void paintComponent(Graphics g){
        
        g.setPaintMode();
        g.setColor(Color.black);
        g.fillRect(0,0,HL,HH);
    }
    
    public void positionHandle(){
        Rectangle r = editorElement.getBounds();
        Rectangle b = new Rectangle(r.x-5,r.y-5,HL,HH);
        
        switch(type){
            case NW_HANDLE: setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)); break;
            case N_HANDLE: {b.translate((r.width - HL)/2+5,0);setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));} break;
            case NE_HANDLE: {b.translate(r.width - HL+10,0);setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));} break;
            case W_HANDLE: {b.translate(0,(r.height-HH)/2+5);setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));} break;
            case E_HANDLE: {b.translate(r.width-HL+10,(r.height-HH)/2+5);setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));} break;
            case C_HANDLE: {b.translate((r.width-HL)/2+5,(r.height-HH)/2+5);setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));}break;
            case SW_HANDLE: {b.translate(0,r.height - HH+10);setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));}break;
            case S_HANDLE: {b.translate((r.width-HL)/2+5,r.height-HH+10);setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));}break;
            case SE_HANDLE: {b.translate(r.width-HL+10,r.height-HH+10);setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));}break;
            default: break;
        }
        this.setBounds(b);
    }
   
    public int getType(){
        return type;
    }
    
       
    public static final int NW_HANDLE =  0;
    public static final int N_HANDLE  =  1;
    public static final int NE_HANDLE  = 2;
    public static final int W_HANDLE  =  3;
    public static final int E_HANDLE  =  4;
    public static final int C_HANDLE  =  5;
    public static final int SW_HANDLE  = 6;
    public static final int S_HANDLE   = 7;
    public static final int SE_HANDLE  =  8;
    
    private static final int HH = 6; //Konstante für die Höhe eines Handles
    private static final int HL = 6; //Konstante für die Länge eines Handles
    
    private int type;
    private EditorElement editorElement;
    
}


