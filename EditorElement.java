package com.icad.maskeneditor;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import com.icad.utilities.*;


public class EditorElement extends com.icad.main.EditorElement {
   public EditorElement(){
   }
    
    public EditorElement(int t) {
       super(t);
        
    }
    
    public void positionHandles(Maskeneditor.GlassPane glassPane){
            if(handle == null){
                handle = new Handle[9];
            
            handle[0] = new Handle(this,Handle.NW_HANDLE);
            handle[1] = new Handle(this,Handle.N_HANDLE);
            handle[2] = new Handle(this,Handle.NE_HANDLE);
            handle[3] = new Handle(this,Handle.W_HANDLE);
            handle[4] = new Handle(this,Handle.E_HANDLE);
            handle[5] = new Handle(this,Handle.C_HANDLE);
            handle[6] = new Handle(this,Handle.SW_HANDLE);
            handle[7] = new Handle(this,Handle.S_HANDLE);
            handle[8] = new Handle(this,Handle.SE_HANDLE);
            
            for(int i = 0;i<handle.length;i++){
                handle[i].positionHandle();
                glassPane.add(handle[i]);
            }
        }
        else {
            
        }
    }
    
    public void removeHandles(Maskeneditor.GlassPane glassPane){
        for(int i = 0;i<handle.length;i++){
                glassPane.remove(handle[i]);
            }
            handle = null;
    }
    
    public Handle getHandle(int i){
        return handle[i];
    }
    
    
    private Handle [] handle = null;
}

