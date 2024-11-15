package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHelper {
    Connection connection;
    String ip, port, db, un, pass;

    @SuppressLint("NewApi")
    public Connection conclass() {
        ip = "192.168.33.159"; // Corrected IP and Port format ethernet="192.168.56.1" home=192.168.8.180 hotspot=192.168.33.159
        port = "1433";
        db = "PetDocDB";
        un = "sa";
        pass = "991831";

        StrictMode.ThreadPolicy tpolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tpolicy);

        Connection con = null;
        String ConnectionURL = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + ip + "/" + db + ";user=" + un + ";password=" + pass + ";";
            con = DriverManager.getConnection(ConnectionURL);
        } catch (ClassNotFoundException e) {
            Log.e("Class not found error: ", e.getMessage());
        } catch (SQLException e) {
            Log.e("SQL Exception: ", e.getMessage());
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return con;
    }
}
