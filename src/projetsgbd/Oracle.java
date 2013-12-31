/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package projetsgbd;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author cmolin
 */
public class Oracle {
    
    private static String url = "jdbc:oracle:thin:@ufrsciencestech.u-bourgogne.fr:25561:ensb2013";
    
    private String user;
    
    private String passwd;
    
    private static Connection connect;
    
    public Oracle(String user, String passwd)
    {
        this.user = user;
        this.passwd = passwd;
    }
    
    public static Connection getInstance(String login, String mdp) 
    {
        if (connect == null)
        {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connect = DriverManager.getConnection(url, login, mdp);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return connect;
    }
    
}
