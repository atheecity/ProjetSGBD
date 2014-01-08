package Utilitaires;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import Interface.ProjetBDD;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
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
    private ArrayList<Integer>  grapheG, grapheD;
    private ArrayList<ArrayList<String>> resultatJointure;
    private Bloc blockGauche, blockDroite;
    
    public Base(String user, String passwd, String adresse, int port, String nomBase, ProjetBDD ui)
    {
        _passwd = passwd;
        _user = user;
        _adresse = adresse;
        _nomBase = nomBase;
        _port = port;
        _enTeteURL = "jdbc:oracle:thin:@";
        UIProjetBDD = ui;
        grapheG = new ArrayList<>();
        grapheD = new ArrayList<>();
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
    
    public ArrayList jointure(String tableA, String critereA, String tableB, String critereB, boolean stockageSurDB) 
    {
        resultatJointure = new ArrayList<>();
        
            
        UIProjetBDD.printOuput("Début de la jointure...");


        if(stockageSurDB)
        {
            
            String nomGraphe = "GRAPHE_" + tableA +"_" + tableB;
            if(nomGraphe.length() > 29) //Si le nom est trop grand
                nomGraphe = "GRAPHE_T1_T2";
            try {
                parcourGrapheDB(nomGraphe, tableA, critereA, tableB, critereB);
            } catch (SQLException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else
        {
            if(grapheG.isEmpty())
                createGraphe(tableA, critereA, tableB, critereB);
            try {
                parcourGraphe(tableA, critereA, tableB, critereB);
            } catch (SQLException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        UIProjetBDD.printOuput("Jointure terminée... " +resultatJointure.size() + " tuples vérifiant la jointure ont été trouvé");
            
        return resultatJointure;
        
    }
    
    public void choixCreateGraphe(String tableA, String critereA, String tableB, String critereB, boolean stockSurDB)
    {
        if(stockSurDB)
        {
            String nomGraphe = "GRAPHE_" + tableA +"_" + tableB;
            if(nomGraphe.length() > 29) //Si le nom est trop grand
                nomGraphe = "GRAPHE_T1_T2";
            createGrapheDB(nomGraphe,tableA, critereA, tableB, critereB);
        }
        else
            createGraphe(tableA, critereA, tableB, critereB);
    }
    
    public void createGraphe(String tableA, String critereA, String tableB, String critereB)
    {
        try {
            UIProjetBDD.printOuput("Stockage du Graphe hors Base...");
            //On crée la table qui servira de graphe sur la base:
            grapheG = new ArrayList<>();
            grapheD = new ArrayList<>();
            PreparedStatement prepstate;
            Statement state = _conn.createStatement();
            ResultSet result;
            
            //prepstate = _conn.prepareStatement("SELECT distinct substr(tabA.rowid,0 ,15), substr(tabB.rowid,0 ,15) "
            prepstate = _conn.prepareStatement("SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid), DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA);
            result = prepstate.executeQuery();
            while (result.next()) {
                grapheG.add(result.getInt(1));
                grapheD.add(result.getInt(2));
            }
            
            
            result.close();
            state.close();
            prepstate.close();
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    public void createGrapheDB(String nomGraphe, String tableA, String critereA, String tableB, String critereB)
    {
        try {
            UIProjetBDD.printOuput("Stockage du Graphe dans la Base...");
            //On crée la table qui servira de graphe sur la base:
            PreparedStatement prepstate;
            Statement state = _conn.createStatement();
            ResultSet result;
            
            //prepstate = _conn.prepareStatement("SELECT distinct substr(tabA.rowid,0 ,15), substr(tabB.rowid,0 ,15) "
            prepstate = _conn.prepareStatement("SELECT DISTINCT DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid), DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA);
            result = prepstate.executeQuery();
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
            }
            
            
            result.close();
            state.close();
            prepstate.close();
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fail", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /*public ArrayList listeArc(boolean aDroite, Bloc b)
    {
        ArrayList<Integer> al = new ArrayList<>();
        int i = aDroite?1:0;
        if(aDroite)
        {
            
        }
        return al;
    }*/
    
    public void parcourGraphe(String tableA, String critereA, String tableB, String critereB) throws SQLException
    {
        UIProjetBDD.printOuput("Début du parcours du Graphe...");
        //On doit regarder le noeud(bloc) qui possède le moins d'arc(de ligne dans la table)
        boolean estADroite = true;
        blockGauche = new Bloc(); blockDroite = new Bloc();
        ArrayList<String> ligne = new ArrayList<>();
        
        //Prepared pour récupérer tous les tuples joint de ces deux blocks
        PreparedStatement prepstateRecup = _conn.prepareStatement("SELECT * "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA
                    + " AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) = ?"
                    + " AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) = ?");
        //Statement state = _conn.createStatement();
        ResultSet result;
        ResultSetMetaData resultMeta;
        estADroite = rechercheMeilleurBloc();
        
        boolean first = true;
        
        while(! grapheG.isEmpty())
        {
            //On récupère tous les tuples joint de ces deux blocks
            //UIProjetBDD.printOuput("Récupération des tuples vérifiant la jointure des deux blocs...");
            prepstateRecup.setInt(1, blockGauche.getId());
            prepstateRecup.setInt(2, blockDroite.getId());
            result = prepstateRecup.executeQuery();   
            resultMeta = result.getMetaData();
            boolean estVide = ! result.next();
            if(first)
            {
                first = false;
                ligne = new ArrayList<>();
                for(int i = 1; i <= resultMeta.getColumnCount(); i++)
                    ligne.add(i-1,resultMeta.getColumnName(i));
                resultatJointure.add(ligne);
            }
            if(estVide)
                System.out.println("Blanquette");
            while(result.next())
            {
                ligne = new ArrayList<>();
                for(int i = 1; i <= resultMeta.getColumnCount(); i++)
                    ligne.add(i-1,result.getObject(i).toString());
                resultatJointure.add(ligne);
            }
            System.out.println("Suppression de l'arc/tuple " + blockGauche.getId() + " | " + blockDroite.getId());
            grapheG.remove(grapheG.indexOf(blockGauche.getId()));
            grapheD.remove(grapheD.indexOf(blockDroite.getId()));
            
            if(!grapheD.isEmpty())
            {
                if(estADroite)
                {
                    int id = grapheG.indexOf(blockGauche.getId());
                    if(id > -1)
                        blockDroite = new Bloc(grapheD.get(id));
                    //else
                        //estADroite = rechercheMeilleurBlocDB(nomGraphe, tableA, critereA, tableB, critereB);
                }
                else
                {
                    int id = grapheD.indexOf(blockDroite.getId());
                    if(id > -1)
                        blockGauche = new Bloc(grapheG.get(id));
                    //else
                      //  estADroite = rechercheMeilleurBlocDB(nomGraphe, tableA, critereA, tableB, critereB);
                }
            }
            
        }
        prepstateRecup.close();
        UIProjetBDD.printOuput("Parcours du Graphe terminé...");
        
    }
    public void parcourGrapheDB(String nomGraphe, String tableA, String critereA, String tableB, String critereB) throws SQLException
    {
        UIProjetBDD.printOuput("Début du parcours du Graphe " + nomGraphe + "...");
        //On doit regarder le noeud(bloc) qui possède le moins d'arc(de ligne dans la table)
        boolean estADroite = true;
        blockGauche = new Bloc(); blockDroite = new Bloc();
        ArrayList<String> ligne = new ArrayList<>();
        
        
        PreparedStatement prepstateRechercheAGauche, prepstateRechercheADroite;
        //Prepared pour récupérer tous les tuples joint de ces deux blocks
        PreparedStatement prepstateRecup = _conn.prepareStatement("SELECT * "
                    + "FROM "+ tableA +" tabA, "+ tableB +" tabB "
                    + "WHERE tabB."+ critereB +" = tabA."+ critereA
                    + " AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabA.rowid) = ?"
                    + " AND DBMS_ROWID.ROWID_BLOCK_NUMBER(tabB.rowid) = ?");
        
        //Prepared pour supprimer un arc/ligne du graphe
        PreparedStatement prepstateDelete = _conn.prepareStatement("DELETE FROM "+ nomGraphe 
                + " WHERE BLOCK_ID_TABLE_" + tableA + " = ?" 
                + " AND BLOCK_ID_TABLE_" + tableB + " = ?");
        
        Statement state = _conn.createStatement();
        ResultSet result;
        ResultSetMetaData resultMeta;
        
        result = state.executeQuery("select count(*) from " + nomGraphe);        
        result.next();
        int nbLigne = result.getInt(1);
        
        estADroite = rechercheMeilleurBlocDB(nomGraphe, tableA, critereA, tableB, critereB);
        
        boolean first = true;
        
        prepstateRechercheAGauche = _conn.prepareStatement("SELECT c.bloc, min(c.occu) "
               + "FROM (select BLOCK_ID_TABLE_" + tableA +" as bloc, count(BLOCK_ID_TABLE_" + tableA +") as occu "
                       + "FROM " + nomGraphe + " WHERE BLOCK_ID_TABLE_" + tableB +" = ? group by BLOCK_ID_TABLE_" + tableA +") c "
               + "WHERE ROWNUM < 2 GROUP BY c.bloc");
        prepstateRechercheADroite = _conn.prepareStatement("SELECT c.bloc, min(c.occu) "
                + "FROM (select BLOCK_ID_TABLE_" + tableB +" as bloc, count(BLOCK_ID_TABLE_" + tableB +") as occu "
                        + "FROM " + nomGraphe + " WHERE BLOCK_ID_TABLE_" + tableA +" = ? group by BLOCK_ID_TABLE_" + tableB +") c "
                + "WHERE ROWNUM < 2 GROUP BY c.bloc");
        
        
        while(nbLigne > 0)
        {
            //On récupère tous les tuples joint de ces deux blocks
            //UIProjetBDD.printOuput("Récupération des tuples vérifiant la jointure des deux blocs...");
            prepstateRecup.setInt(1, blockGauche.getId());
            prepstateRecup.setInt(2, blockDroite.getId());
            result = prepstateRecup.executeQuery();   
            resultMeta = result.getMetaData();
            boolean estVide = ! result.next();
            if(first)
            {
                first = false;
                ligne = new ArrayList<>();
                for(int i = 1; i <= resultMeta.getColumnCount(); i++)
                    ligne.add(i-1,resultMeta.getColumnName(i));
                resultatJointure.add(ligne);
            }
            if(estVide)
                System.out.println("Blanquette");
            while(result.next())
            {
                ligne = new ArrayList<>();
                for(int i = 1; i <= resultMeta.getColumnCount(); i++)
                    ligne.add(i-1,result.getObject(i).toString());
                resultatJointure.add(ligne);
            }
            System.out.println("Suppression de l'arc/tuple " + blockGauche.getId() + " | " + blockDroite.getId());
            prepstateDelete.setInt(1, blockGauche.getId());
            prepstateDelete.setInt(2, blockDroite.getId());
            int nbDelete = prepstateDelete.executeUpdate();
            if(nbDelete == 0)
                System.out.println("Blanquette de veau");
            nbLigne--;
            
            if(nbLigne > 0)
            {
                if(estADroite)
                {
                    prepstateRechercheADroite.setInt(1, blockGauche.getId());
                    result = prepstateRechercheADroite.executeQuery();  
                    estADroite = !estADroite;
                    if(result.next())
                        blockDroite = new Bloc(result.getInt(1), "BLOCK_ID_TABLE_" + tableB);
                    else
                        estADroite = rechercheMeilleurBlocDB(nomGraphe, tableA, critereA, tableB, critereB);
                }
                else
                {
                    prepstateRechercheAGauche.setInt(1, blockDroite.getId());
                    result = prepstateRechercheAGauche.executeQuery();   
                    estADroite = !estADroite;     
                    if(result.next())
                        blockGauche = new Bloc(result.getInt(1), "BLOCK_ID_TABLE_" + tableA);
                    else
                        estADroite = rechercheMeilleurBlocDB(nomGraphe, tableA, critereA, tableB, critereB);
                }
            }
            
        }
        result.close();
        state.close();
        prepstateDelete.close();
        prepstateRechercheAGauche.close();
        prepstateRechercheADroite.close();
        prepstateRecup.close();
        UIProjetBDD.printOuput("Parcours du Graphe terminé...");
    }
    
    public boolean rechercheMeilleurBloc()
    {
        ArrayList<Integer> resG = new ArrayList<>(), resD = new ArrayList<>();
        int i = 0;
        for(Integer iG : grapheG)
            resG.add(Collections.frequency(grapheG,iG));
        for(Integer iD : grapheD)
            resD.add(Collections.frequency(grapheD,iD));
        boolean res = (Collections.min(resG) < Collections.min(resD))?false:true;
        if(res)
            i =resG.indexOf(Collections.min(resG));
        else
            i =resD.indexOf(Collections.min(resD));
        blockGauche = new Bloc(grapheG.get(i));
        blockDroite = new Bloc(grapheD.get(i));
        return res;
    }
    public boolean rechercheMeilleurBlocDB(String nomGraphe, String tableA, String critereA, String tableB, String critereB) throws SQLException
    {
        PreparedStatement prepstateRechercheAGauche, prepstateRechercheADroite;
        //Prepared pour récupérer tous les tuples joint de ces deux blocks
        
        Statement state = _conn.createStatement();
        ResultSet result;
        prepstateRechercheAGauche = _conn.prepareStatement("SELECT c.bloc, min(c.occu) "
                + "FROM (select BLOCK_ID_TABLE_" + tableA +" as bloc, count(BLOCK_ID_TABLE_" + tableA +") as occu "
                        + "FROM " + nomGraphe + " group by BLOCK_ID_TABLE_" + tableA +") c "
                + "WHERE ROWNUM < 2 GROUP BY c.bloc");
        
        result = prepstateRechercheAGauche.executeQuery();        
        result.next();
        blockGauche = new Bloc(result.getInt(1),"BLOCK_ID_TABLE_" + tableA);
        int occu = result.getInt(2);
        
        prepstateRechercheADroite = _conn.prepareStatement("SELECT c.bloc, min(c.occu) "
                + "FROM (select BLOCK_ID_TABLE_" + tableB +" as bloc, count(BLOCK_ID_TABLE_" + tableB +") as occu "
                        + "FROM " + nomGraphe + " group by BLOCK_ID_TABLE_" + tableB +") c "
                + "WHERE ROWNUM < 2 GROUP BY c.bloc");
        
        result = prepstateRechercheADroite.executeQuery();        
        result.next();
        blockDroite = new Bloc(result.getInt(1),"BLOCK_ID_TABLE_" + tableB);
        
        return (occu < result.getInt(2))?false:true; //On met dans block le plus petit des deux blocks
    }
       
}
