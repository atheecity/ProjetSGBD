package Utilitaires;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


/**
 *
 * @author Benjamin
 */
public class Base {
    //Variables
    private String _user, _adresse, _nomBase, _passwd;
    private int _port;
    private Connection _conn;
    private String _enTeteURL;
    
    public Base(String user, String passwd, String adresse, int port, String nomBase)
    {
        _passwd = passwd;
        _user = user;
        _adresse = adresse;
        _nomBase = nomBase;
        _port = port;
        _enTeteURL = "jdbc:oracle:thin:@";
    }
    
        
    public boolean seConnecter() throws SQLException
    {
        System.out.println("Driver O.K.");
        String url = _enTeteURL + _adresse + ":" + _port + "/" + _nomBase;
        if((_user == "") && (_passwd == ""))
        {
            String log[] = demandeLogin();
            _user = log[0];
            _passwd = log[1];
        }
        
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        _conn = DriverManager.getConnection(url, _user, _passwd);
        System.out.println("Connexion effective !"); 
        return ! _conn.isClosed();
    }
    
    public void seDeconnecter() throws SQLException
    {
        if(! _conn.getAutoCommit())
        {
            _conn.commit();
            System.out.println("Commit de la Base effectué");
        }
        else
            System.out.println("Pas de Commit de la Base, AutoCommit en place");
            
        _conn.close();
        System.out.println("Deconnexion de la Base effectuée");
        
    }
    
    public String[] demandeLogin()
    {
        String[] log = new String[2]; 
        
        //Données
        Object[] message = new Object[4];
        message[0] = "Saisissez votre Login pour  " + _nomBase; //Message apparaîssant dans le corps du dialog
        message[1] = new JTextField ();
        message[2] = "Saisissez votre Mot de Passe"; //Message apparaîssant dans le corps du dialog
        message[3] = new JPasswordField ();
 
//        Options (nom des boutons)
        String option[] = {"OK", "Annuler"};
 
        int result = JOptionPane.showOptionDialog(
                null, // fenêtre parente
                message, // corps du dialogue
                "Entrez vos logins",// Titre du dialogue
                JOptionPane.DEFAULT_OPTION, // type de dialogue
                JOptionPane.QUESTION_MESSAGE, // type icone
                null, // icône optionnelle
                option, // boutons
                message[1] // objet ayant le focus par défaut
        );
 
        if(result == 0){
            log[0] = ((JTextField )message[1]).getText();
            log[1] = new String(((JPasswordField )message[3]).getPassword());
        }
        
        return log;
    }

}