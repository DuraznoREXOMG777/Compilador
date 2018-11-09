/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author Hp EliteDesk
 */
public class FileScanner {
    
    public static void leerArchivo(JTextArea taBase, JPanel target){
        
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(target);
        
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                JTextArea textArea = new JTextArea(20, 40); 
                textArea.read(br, null); // here we read in the text file
                taBase.setText(textArea.getText()); //ReloadTheTextArea. Triggers textAreas's changeUpdate event.
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
}
