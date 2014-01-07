/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Utilitaires;

/**
 *
 * @author unclezebuuu
 */
public class Bloc {
    
    private int id, occu;
    private String nomCol;
    
    
    public Bloc()
    {
        id = 0;
        occu = 0;
        nomCol = "";
    }
    public Bloc(int i)
    {
        id = i;
        occu = 0;
        nomCol = "";
    }

    public Bloc(int i, int o, String n)
    {
        id = i;
        occu = 0;
        nomCol = n;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the occu
     */
    public int getOccu() {
        return occu;
    }

    /**
     * @param occu the occu to set
     */
    public void setOccu(int occu) {
        this.occu = occu;
    }

    

    /**
     * @return the nomCol
     */
    public String getNomCol() {
        return nomCol;
    }

    /**
     * @param nomCol the nomCol to set
     */
    public void setNomCol(String nomCol) {
        this.nomCol = nomCol;
    }

}
