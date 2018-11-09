/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

/**
 *
 * @author Antonio López Higuer
 */
public class Semantica2 {
    String regla, tp, valorr, linea, estado, ambito;

    public Semantica2(String regla, String tp, String valorr, String linea, String estado, String ambito) {
        this.regla = regla;
        this.tp = tp;
        this.valorr = valorr;
        this.linea = linea;
        this.estado = estado;
        this.ambito = ambito;
    }

    public Semantica2(String regla, String tp, String valorr, String linea, String ambito) {
        this.regla = regla;
        this.tp = tp;
        this.valorr = valorr;
        this.linea = linea;
        this.ambito = ambito;
    }
    
    public Semantica2(){
        
    }

    public String getRegla() {
        return regla;
    }

    public void setRegla(String regla) {
        this.regla = regla;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getValorr() {
        return valorr;
    }

    public void setValorr(String valorr) {
        this.valorr = valorr;
    }

    public String getLinea() {
        return linea;
    }

    public void setLinea(String linea) {
        this.linea = linea;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }
    
    
}
