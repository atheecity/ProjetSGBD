package Utilitaires;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


/**
 *
 * @author Benjamin
 */
public class Base {
    //Variables
    private String _user, _adresse, _nomBase, _passwd, _enTeteURL;
    private int _port;
    private Connection _conn;
    
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
        String url = _enTeteURL + getAdresse() + ":" + _port + "/" + _nomBase;
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
    
    public ArrayList<String[]> selectCrit(String table) throws SQLException // dim = user ou all ou admin
    {
        ArrayList<String[]> al = new ArrayList<>();
        //Création d'un objet Statement
        Statement state = _conn.createStatement();
        //L'objet ResultSet contient le résultat de la requête SQL
        ResultSet result = state.executeQuery("SELECT utc.column_name, utc.data_type "
                                            + "FROM user_ind_columns uic, user_tab_columns utc "
                                            + "WHERE uic.column_name = utc.column_name "
                                            + "AND uic.table_name = '" + table + "'");
        
        while(result.next()){         
            al.add(new String[]{result.getString(1), result.getString(2)});
        }

        result.close();
        state.close();
        
        return al;
    }
    
    public ArrayList<String> selectTable(String dim) throws SQLException // dim = user ou all ou admin
    {
        ArrayList<String> al = new ArrayList<>();
        //Création d'un objet Statement
        Statement state = _conn.createStatement();
        //L'objet ResultSet contient le résultat de la requête SQL
        ResultSet result = state.executeQuery("SELECT table_name FROM " + dim + "_tables");

        while(result.next()){         
            al.add(result.getString(1));
        }

        result.close();
        state.close();
        
        return al;
    }
    
    public void jointure(String tableA, String critereA, String tableB, String critereB) throws SQLException
    {
        Statement state = _conn.createStatement();
        String indexA = "", indexB = "";
        //On récupère l'index sur critereA
        ResultSet result = state.executeQuery("SELECT ui.index_name "
                + "FROM USER_IND_COLUMNS uic, USER_INDEXES ui "
                + "WHERE ui.index_name = uic.index_name "
                + "AND COLUMN_NAME = '" + critereA + "'");
        result.next();
        indexA = result.getString(1);
        //On récupère les numéros des blocks de l'index de critereA
        result = state.executeQuery("SELECT block_id "
                 + "FROM USER_SEGMENTS us, DBA_EXTENTS de "
                 + "WHERE us.SEGMENT_NAME = de.SEGMENT_NAME "
                 + "AND us.SEGMENT_NAME = '" + indexA + "'") ;
        /*
        //On récupère l'index sur critereB
        result = state.executeQuery("SELECT ui.index_name "
                + "FROM USER_IND_COLUMNS uic, USER_INDEXES ui "
                + "WHERE ui.index_name = uic.index_name "
                + "AND COLUMN_NAME = '" + critereB + "'");
        //On récupère les numéros des blocks de l'index de critereB*/
        

        while(result.next()){         
            System.out.println(result.getString(1));
        }

        result.close();
        state.close();
    }
}
