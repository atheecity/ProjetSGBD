package Utilitaires;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;



/**
 *
 * @author Benjamin Bezançon
 */
public class IdentifiantsConnexion implements Serializable {
    
    //Variables
    private String _user, _adresse, _nomBase, _typeBase;
    private int _port;
    
    public IdentifiantsConnexion(String user, String adresse, int port, String nomBase, String typeBase)
    {
        _user = user;
        _adresse = adresse;
        _nomBase = nomBase;
        _typeBase = typeBase;
        _port = port;
    }
    //Sans user
    public IdentifiantsConnexion(String adresse, int port, String nomBase, String typeBase)
    {
        _adresse = adresse;
        _nomBase = nomBase;
        _typeBase = typeBase;
        _port = port;
    }
    
    public IdentifiantsConnexion()
    {
        _user = "Utilisateur inconnu";
        _adresse = "Adresse inconnue";
        _nomBase = "Nom de Base inconnue";
        _port = 0;
        _typeBase = "Type de la Base inconnu";
    }

    public String toStringURL()
    {
        return  _adresse + ":" + _port + "/" + _nomBase;
    }
    
    
    public String toString()
    {
        return "Base " + _nomBase + " sur " + _adresse + ":" + _port;
    }
    
     public String toStringDetails()
    {
        return "Base " + _nomBase + " sur " + _adresse + ":" + _port;
    }

    /**
     * @return the _user
     */
    public String getUser() {
        return _user;
    }

    /**
     * @param user the _user to set
     */
    public void setUser(String user) {
        this._user = user;
    }

    /**
     * @return the _adresse
     */
    public String getAdresse() {
        return _adresse;
    }

    /**
     * @param adresse the _adresse to set
     */
    public void setAdresse(String adresse) {
        this._adresse = adresse;
    }

    /**
     * @return the _nomBase
     */
    public String getNomBase() {
        return _nomBase;
    }

    /**
     * @param nomBase the _nomBase to set
     */
    public void setNomBase(String nomBase) {
        this._nomBase = nomBase;
    }

    /**
     * @return the _port
     */
    public int getPort() {
        return _port;
    }

    /**
     * @param port the _port to set
     */
    public void setPort(int port) {
        this._port = port;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null)
            return super.equals(o);
        else
        {
        IdentifiantsConnexion co = (IdentifiantsConnexion) o;
        return (this._adresse == co.getAdresse()) && (this._nomBase == co.getNomBase()) && (this._port == co.getPort()) && (this._user == co.getUser());
        }
    }

    /**
     * @return the _typeBase
     */
    public String getTypeBase() {
        return _typeBase;
    }

    /**
     * @param typeBase the _typeBase to set
     */
    public void setTypeBase(String typeBase) {
        this._typeBase = typeBase;
    }
}
