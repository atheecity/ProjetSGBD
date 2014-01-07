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
    private ArrayList<ArrayList<String>> resultatJointure;
    
    public Base(String user, String passwd, String adresse, int port, String nomBase, ProjetBDD ui)
    {
        _passwd = passwd;
        _user = user;
        _adresse = adresse;
        _nomBase = nomBase;
        _port = port;
        _enTeteURL = "jdbc:oracle:thin:@";
        UIProjetBDD = ui;
        resultatJointure = new ArrayList<>();
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
    
    public void setPort(int port)
    {
        _port = port;
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
        ResultSet result = state.executeQuery("SELECT table_name FROM " + dim + "_tables WHERE table_name <> 'PLAN_TABLE' AND table_name NOT LIKE 'GRAPHE_%' ORDER BY table_name ASC");

        while(result.next()){         
            al.add(result.getString(1));
        }

        result.close();
        state.close();
        
        return al;
    }
    
    //Première mouture très lente
    public void jointureOld(String tableA, String critereA, String tableB, String critereB) 
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
            //createGraphe(tableA, tableB, state);
            
            prepstate = _conn.prepareStatement("SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(t1.rowid) as nobloc1,DBMS_ROWID.ROWID_BLOCK_NUMBER(t2.rowid) as nobloc2 "
                    + "FROM " + tableA + " t1," + tableB + " t2 "
                    + "WHERE t1." + critereA + " = t2." + critereB + "");
            System.out.println("SELECT FINI");
            for( Integer nbBlocA : listeBlocA)
            {
                //prepstate.setInt(1, nbBlocA);
                for( Integer nbBlocB : listeBlocB)
                {
                    //prepstate.setInt(2, nbBlocB);
                    result = prepstate.executeQuery();
                    result.next();
                    if(result.getInt(1) > 0) //des tuples en commun on ajoute les blocs au graphe
                    {
                        System.out.println("OUI");
                        //UIProjetBDD.printOuput("Ajout d'un arc entre les deux blocs dans le graphe");
                        //result = state.executeQuery("INSERT INTO GRAPHE_" + tableA +"_" + tableB + " VALUES(" + nbBlocA + ", " + listeBlocB.get(0) + ")");
                    }
                    else
                       System.out.println("NON");
                }
            }
            
            UIProjetBDD.printOuput("Jointure terminée...");
            result.close();
            state.close();
            prepstate.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fail", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    
    public ArrayList<ArrayList<String>> jointure(String tableA, String critereA, String tableB, String critereB, boolean recreerGraphe) 
    {
        resultatJointure = new ArrayList<>();
        try {
            
            UIProjetBDD.printOuput("Début de la jointure...");
            
            PreparedStatement prepstate;
            Statement state = _conn.createStatement();
            ResultSet result;
                       
         //   prepstate = _conn.prepareStatement("SELECT distinct substr(tabA.rowid,0 ,15), substr(tabB.rowid,0 ,15) "
            prepstate = _conn.prepareStatement("SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid), DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA);
            result = prepstate.executeQuery();
            int cpt = 1;
            
            String nomGraphe = "GRAPHE_" + tableA +"_" + tableB;
            if(nomGraphe.length() > 29) //Si le nom est trop grand
                nomGraphe = "GRAPHE_T1_T2";
            
            if(recreerGraphe)
            {
                //On crée la table qui servira de graphe : 
                
                try {

                    state.executeQuery("DROP TABLE " + nomGraphe);
                } catch (SQLException ex) {
                    System.out.println("Table inexistante");
                }
                state.executeQuery("CREATE TABLE " + nomGraphe + "(BLOCK_ID_TABLE_" + tableA + " INTEGER,  BLOCK_ID_TABLE_" + tableB + " INTEGER)");
                UIProjetBDD.printOuput("Graphe " + nomGraphe + " créé...");
                prepstate = _conn.prepareStatement("INSERT INTO " + nomGraphe + " VALUES(?,?)");

                while (result.next()) {
                    prepstate.setObject(1, result.getInt(1), Types.INTEGER);
                    prepstate.setObject(2, result.getInt(2), Types.INTEGER);
                    prepstate.executeUpdate();
                    System.out.println(cpt);
                    cpt++;
                }
            
            }
            result.close();
            state.close();
            prepstate.close();
            
            UIProjetBDD.printOuput("Jointure terminée...");
            parcourGraphe(nomGraphe, tableA, critereA, tableB, critereB);
            
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fail", JOptionPane.ERROR_MESSAGE);
        }
        return resultatJointure;
        
    }
    
    public void parcourGraphe(String nomGraphe, String tableA, String critereA, String tableB, String critereB) throws SQLException
    {
        UIProjetBDD.printOuput("Début du parcours du Graphe " + nomGraphe + "...");
        //On doit regarder le noeud(bloc) qui possède le moins d'arc(de ligne dans la table)
        Bloc block = new Bloc(), blockG = new Bloc(), blockD = new Bloc();
        ArrayList<String> ligne = new ArrayList<>();
        
        
        PreparedStatement prepstateRecherche;
        //On récupère tous les tuples joint de ces deux blocks
        PreparedStatement prepstateRecup = _conn.prepareStatement("SELECT * "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA
                    + " AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) = ?"
                    + " AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) = ?");
        ResultSet result;
        ResultSetMetaData resultMeta;
        
        
        prepstateRecherche = _conn.prepareStatement("SELECT c.bloc, min(c.occu) "
                + "FROM (select BLOCK_ID_TABLE_" + tableA +" as bloc, count(BLOCK_ID_TABLE_" + tableA +") as occu "
                        + "FROM " + nomGraphe + " group by BLOCK_ID_TABLE_" + tableA +") c "
                + "WHERE ROWNUM < 2 GROUP BY c.bloc");
        
        result = prepstateRecherche.executeQuery();        
        result.next();
        blockG = new Bloc(result.getInt(1), result.getInt(2),false,"BLOCK_ID_TABLE_" + tableA);
        
        prepstateRecherche = _conn.prepareStatement("SELECT c.bloc, min(c.occu) "
                + "FROM (select BLOCK_ID_TABLE_" + tableB +" as bloc, count(BLOCK_ID_TABLE_" + tableB +") as occu "
                        + "FROM " + nomGraphe + " group by BLOCK_ID_TABLE_" + tableB +") c "
                + "WHERE ROWNUM < 2 GROUP BY c.bloc");
        
        result = prepstateRecherche.executeQuery();        
        result.next();
        blockD = new Bloc(result.getInt(1), result.getInt(2),true,"BLOCK_ID_TABLE_" + tableA);
        
        block = (blockD.getOccu() < blockG.getOccu())?blockD:blockG; //On met dans block le plus petit des deux blocks
        
        //On récupère tous les tuples joint de ces deux blocks
        UIProjetBDD.printOuput("Récupération des tuples vérifiant la jointure des deux blocs...");
        prepstateRecup.setInt(1, blockD.getId());
        prepstateRecup.setInt(2, blockG.getId());
        result = prepstateRecup.executeQuery();   
        resultMeta = result.getMetaData();
        while(result.next())
        {
            ligne = new ArrayList<>();
            for(int i = 1; i <= resultMeta.getColumnCount(); i++)
            {
                if(resultatJointure.isEmpty())
                    ligne.add(i-1,resultMeta.getColumnName(i));
                else
                    ligne.add(i-1,result.getObject(i).toString());
            }
            resultatJointure.add(ligne);
        }
//        UIProjetBDD.printOuput("Suppression de l'arc/tuple " + result.getInt(1) + " | " + result.getInt(2));
        
        
        result.close();
        prepstateRecherche.close();
        prepstateRecup.close();
        UIProjetBDD.printOuput("Parcours du Graphe terminé...");
    }
    
    public void jointureDeuxView(String tableA, String critereA, String tableB, String critereB) 
    {
        try {
            
            UIProjetBDD.printOuput("Début de la jointure avec la vue...");
            String nomGraphe = "GRAPHE_" + tableA +"_" + tableB;
            if(nomGraphe.length() > 29)
                nomGraphe = "GRAPHE_T1_T2";
            PreparedStatement prepstate;
            Statement state = _conn.createStatement();
            ResultSet result;
            
            
            prepstate = _conn.prepareStatement("CREATE OR REPLACE VIEW " + nomGraphe + " AS SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) AS BLOCK_ID_" + tableA + ", DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) AS BLOCK_ID_" + tableB
                    + " FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA);
            result = prepstate.executeQuery();
            UIProjetBDD.printOuput("Création et remplissage du graphe effectués...");
            UIProjetBDD.printOuput("Jointure...");
            
            prepstate = _conn.prepareStatement("SELECT * "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB, " + nomGraphe
                    + " WHERE DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) = BLOCK_ID_" + tableA
                    + " AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) = BLOCK_ID_" + tableB
                    + " AND tabB."+ critereB + " = tabA."+ critereA);
           /* 
             prepstate = _conn.prepareStatement("CREATE OR REPLACE VIEW " + nomGraphe + " AS SELECT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) AS BLOCK_ID_" + tableA + ", DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) AS BLOCK_ID_" + tableB + " FROM "+ tableA +" tabA, "+ tableB +" tabB, "
                        +"(SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA1.rowid) AS blockid_tableA1, DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB1.rowid) AS blockid_tableB1, tabB1."+ critereB +" AS Critere "
                        + "FROM "+ tableA +" tabA1, "+ tableB +" tabB1 "
                        + "WHERE tabA1."+ critereA +" = tabB1."+ critereB
                        + " GROUP BY (tabB1."+ critereB +", DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA1.rowid), DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB1.rowid))) liste_block "
                + "WHERE DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) = liste_block.blockid_tableA1 "
                + "AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) = liste_block.blockid_tableB1 "
                + "AND tabB."+ critereB +" = liste_block.critere "
                + "AND tabA."+ critereA +" = liste_block.critere");*/
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
