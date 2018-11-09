/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

/**
 *
 * @author Hp EliteDesk
 */
public class LoadArray {
    
    public static String[][] loadCSV(){
        String[][] matriz = new String[60][34];
        Scanner scanIn = null;
        int Rowc = 0;
        String inputLine = "";
        
        try{
            scanIn = new Scanner(new BufferedReader(new FileReader("D:\\Users\\Yueow\\Documents\\NetBeansProjects\\ProyectoX\\src\\Resources\\Matraca2.csv")));
            while(scanIn.hasNextLine()){
                inputLine = scanIn.nextLine();
                String line[] = inputLine.split("~");
                for (int i = 0; i < line.length; i++)
                    matriz[Rowc][i] = line[i];
                Rowc++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return matriz;
    }
    
    public static int[][] loadCSV2(){
        int[][] matriz = new int[52][97];
        Scanner scanIn = null;
        int Rowc = 0;
        String inputLine = "";
        
        try{
            scanIn = new Scanner(new BufferedReader(new FileReader("D:\\Users\\Yueow\\Documents\\NetBeansProjects\\ProyectoX\\src\\Resources\\Matraca4.csv")));
            while(scanIn.hasNextLine()){
                inputLine = scanIn.nextLine();
                String line[] = inputLine.split("~");
                for (int i = 0; i < line.length; i++)
                    matriz[Rowc][i] = Integer.parseInt(line[i]);
                Rowc++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return matriz;
    }
    
    public static int[][] loadRM(){
        int[][] matriz = new int[8][8];
        Scanner scanIn = null;
        int Rowc = 0;
        String inputLine = "";
        
        try{
            scanIn = new Scanner(new BufferedReader(new FileReader("D:\\Users\\Yueow\\Documents\\NetBeansProjects\\ProyectoX\\src\\Resources\\RM.csv")));
            while(scanIn.hasNextLine()){
                inputLine = scanIn.nextLine();
                String line[] = inputLine.split(",");
                for (int i = 0; i < line.length; i++)
                    matriz[Rowc][i] = Integer.parseInt(line[i]);
                Rowc++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return matriz;
    }
    
    public static String[][] loadSuma(){
        String[][] matriz = new String[8][8];
        Scanner scanIn = null;
        int Rowc = 0;
        String inputLine = "";
        
        try{
            scanIn = new Scanner(new BufferedReader(new FileReader("D:\\Users\\Yueow\\Documents\\NetBeansProjects\\ProyectoX\\src\\Resources\\Suma.csv")));
            while(scanIn.hasNextLine()){
                inputLine = scanIn.nextLine();
                String line[] = inputLine.split(",");
                for (int i = 0; i < line.length; i++)
                    matriz[Rowc][i] = line[i];
                Rowc++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return matriz;
    }
    
    public static String[][] loadDivision(){
        String[][] matriz = new String[8][8];
        Scanner scanIn = null;
        int Rowc = 0;
        String inputLine = "";
        
        try{
            scanIn = new Scanner(new BufferedReader(new FileReader("D:\\Users\\Yueow\\Documents\\NetBeansProjects\\ProyectoX\\src\\Resources\\Division.csv")));
            while(scanIn.hasNextLine()){
                inputLine = scanIn.nextLine();
                String line[] = inputLine.split(",");
                for (int i = 0; i < line.length; i++)
                    matriz[Rowc][i] = line[i];
                Rowc++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return matriz;
    }
    
    public static String[][] loadElevacion(){
        String[][] matriz = new String[8][8];
        Scanner scanIn = null;
        int Rowc = 0;
        String inputLine = "";
        
        try{
            scanIn = new Scanner(new BufferedReader(new FileReader("D:\\Users\\Yueow\\Documents\\NetBeansProjects\\ProyectoX\\src\\Resources\\Elevacion.csv")));
            while(scanIn.hasNextLine()){
                inputLine = scanIn.nextLine();
                String line[] = inputLine.split(",");
                for (int i = 0; i < line.length; i++)
                    matriz[Rowc][i] = line[i];
                Rowc++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return matriz;
    }
    
    public static String[] loadFile(String fileName, int y){
        String[] errorMessage = new String[y];
        
        Scanner scanIn = null;
        int line = 0;
        String inputLine = "";
        
        try{
            scanIn = new Scanner(new BufferedReader(new FileReader("D:\\Users\\Yueow\\Documents\\NetBeansProjects\\ProyectoX\\src\\Resources\\"+fileName+".txt")));
            while(scanIn.hasNextLine()){
                inputLine = scanIn.nextLine();
                errorMessage[line] = inputLine;
                line++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return errorMessage;
    }
}
