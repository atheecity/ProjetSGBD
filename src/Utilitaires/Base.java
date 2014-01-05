package Utilitaires;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import Interface.ProjetBDD;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private ProjetBDD UIProjetBDD;
    
    public Base(String user, String passwd, String adresse, int port, String nomBase, ProjetBDD ui)
    {
        _passwd = passwd;
        _user = user;
        _adresse = adresse;
        _nomBase = nomBase;
        _port = port;
        _enTeteURL = "jdbc:oracle:thin:@";
        UIProjetBDD = ui;
    }
    
        
    public boolean seConnecter() throws SQLException
    {
        UIProjetBDD.printOuput("Tentative de Connexion à la base " + _nomBase + "...");
        String url = _enTeteURL + getAdresse() + ":" + _port + "/" + _nomBase;
        if((_user == "") && (_passwd == ""))
        {
            String log[] = demandeLogin();
            _user = log[0];
            _passwd = log[1];
        }
        
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        _conn = DriverManager.getConnection(url, _user, _passwd);
        UIProjetBDD.printOuput("Connexion réussie à la base " + _nomBase + "..."); 
        return ! _conn.isClosed();
    }
    
    public void seDeconnecter() throws SQLException
    {
        if(! _conn.getAutoCommit())
        {
            _conn.commit();
            UIProjetBDD.printOuput("Commit de la Base effectué");
        }
        else
            UIProjetBDD.printOuput("Pas de Commit de la Base, AutoCommit en place");
            
        _conn.close();
        UIProjetBDD.printOuput("Deconnexion de la Base effectuée");
        
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
        ResultSet result = state.executeQuery("SELECT DISTINCT(utc.column_name), utc.data_type "
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
        ResultSet result = state.executeQuery("SELECT table_name FROM " + dim + "_tables WHERE table_name <> 'PLAN_TABLE' ");

        while(result.next()){         
            al.add(result.getString(1));
        }

        result.close();
        state.close();
        
        return al;
    }
    
    public void jointure(String tableA, String critereA, String tableB, String critereB) 
    {
        try {
            
            UIProjetBDD.printOuput("Début de la jointure...");
            //On récupère la liste des blocs des deux tables
            ArrayList<Integer> listeBlocA = new ArrayList<>(getListeBloc(tableA));
            ArrayList<Integer> listeBlocB = new ArrayList<>(getListeBloc(tableB));
            
            PreparedStatement prepstate;
            Statement state = _conn.createStatement();
            ResultSet result = null;
            ResultSetMetaData resultdata;
            
            //On crée la table qui servira de graphe : pour un tuple on a l id du bloc de chaque table
            createGraphe(tableA, tableB, state);
            
            prepstate = _conn.prepareStatement("SELECT count(*) "
                    + "FROM " + tableA + " t1," + tableB + " t2 "
                    + "WHERE DBMS_ROWID.ROWID_BLOCK_NUMBER(t1.rowid) = ? "
                    + "AND DBMS_ROWID.ROWID_BLOCK_NUMBER(t2.rowid) = ? "
                    + "AND t1." + critereA + " = t2." + critereB + "");
            prepstate.setInt(2, listeBlocB.get(0));
            for( Integer nbBlocA : listeBlocA)
            {
                prepstate.setInt(1, nbBlocA);
                result = prepstate.executeQuery();
                result.next();
                if(result.getInt(1) > 0) //des tuples en commun on ajoute les blocs au graphe
                {
                    //UIProjetBDD.printOuput("Les deux blocs " + nbBlocA + " et " + listeBlocB.get(0) +" au moins un couple de tuple en commun");
                    //UIProjetBDD.printOuput("Ajout d'un arc entre les deux blocs dans le graphe");
                    result = state.executeQuery("INSERT INTO GRAPHE_" + tableA +"_" + tableB + " VALUES(" + nbBlocA + ", " + listeBlocB.get(0) + ")");
                }
                //else
                   //UIProjetBDD.printOuput("Les deux blocs " + nbBlocA + " et " + listeBlocB.get(0) +" n'ont pas de couple de tuple en commun");
            }
            
            UIProjetBDD.printOuput("Jointure terminée...");
            result.close();
            state.close();
            prepstate.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fail", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    private void createGraphe(String tableA, String tableB, Statement state) throws SQLException
    {
        ResultSet result;
        String nomGraphe = "GRAPHE_" + tableA +"_" + tableB;
        try {
            
            result = state.executeQuery("DROP TABLE " + nomGraphe);
        } catch (SQLException ex) {
            System.out.println("Table inexistante");
        }
        result = state.executeQuery("CREATE TABLE " + nomGraphe + "(BLOCK_ID_TABLE_" + tableA + " NUMBER, BLOCK_ID_TABLE_" + tableB + " NUMBER)");
        UIProjetBDD.printOuput("Graphe " + nomGraphe + " créé...");
    }
    
    public void jointureDeux(String tableA, String critereA, String tableB, String critereB) 
    {
        try {
            
            UIProjetBDD.printOuput("Début de la jointure...");
            
            PreparedStatement prepstate;
            Statement state = _conn.createStatement();
            ResultSet result, resultInsert = null;
            
            //On crée la table qui servira de graphe : pour un tuple on a l id du bloc de chaque table
            createGraphe(tableA, tableB, state);
            
            prepstate = _conn.prepareStatement("SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid), DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA);
            /*
            prepstate = _conn.prepareStatement("SELECT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid), DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) FROM "+ tableA +" tabA, "+ tableB +" tabB, "
                        +"(SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA1.rowid) AS blockid_tableA1, DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB1.rowid) AS blockid_tableB1, tabB1."+ critereB +" AS Critere "
                        + "FROM "+ tableA +" tabA1, "+ tableB +" tabB1 "
                        + "WHERE tabA1."+ critereA +" = tabB1."+ critereB
                        + " GROUP BY (tabB1."+ critereB +", DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA1.rowid), DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB1.rowid))) liste_block "
                + "WHERE DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) = liste_block.blockid_tableA1 "
                + "AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) = liste_block.blockid_tableB1 "
                + "AND tabB."+ critereB +" = liste_block.critere "
                + "AND tabA."+ critereA +" = liste_block.critere"); 
            */
            result = prepstate.executeQuery();
            int cpt = 1;
            while (result.next()) {
                
                resultInsert = state.executeQuery("INSERT INTO GRAPHE_" + tableA +"_" + tableB + " VALUES(" + result.getInt(1) + ", " + result.getInt(2) + ")");
                //System.out.println("INSERT de " + result.getInt(1) + ", " + result.getInt(2));
                System.out.println(cpt);
                cpt++;
            }
            
            UIProjetBDD.printOuput("Jointure terminée...");
            //resultInsert.close();
            result.close();
            state.close();
            prepstate.close();
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fail", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    public void jointureDeuxView(String tableA, String critereA, String tableB, String critereB) 
    {
        try {
            
            UIProjetBDD.printOuput("Début de la jointure avec la vue...");
            String nomGraphe = "GRAPHE_" + tableA +"_" + tableB;
            PreparedStatement prepstate;
            Statement state = _conn.createStatement();
            ResultSet result;
            
            
            prepstate = _conn.prepareStatement("CREATE OR REPLACE VIEW " + nomGraphe + " AS SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) AS BLOCK_ID_" + tableA + ", DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) AS BLOCK_ID_" + tableB
                    + " FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA);
            result = prepstate.executeQuery();
            
            
            
            UIProjetBDD.printOuput("Jointure terminée...");
            result.close();
            state.close();
            prepstate.close();
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fail", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    private ArrayList<Integer> getListeBloc(String nomTable) throws SQLException
    {
        Statement state = _conn.createStatement();
        ArrayList<Integer> listeBloc = new ArrayList<>();
        ResultSet result = state.executeQuery("SELECT DISTINCT(DBMS_ROWID.ROWID_BLOCK_NUMBER(rowid)) as nobloc FROM " + nomTable);
        while(result.next()){         
            listeBloc.add(result.getInt(1));
        }
        
        UIProjetBDD.printOuput("Liste des Blocs de la table " + nomTable + " récupérée");
        result.close();
        state.close();
        
        return listeBloc;
    }
}
