package java_backend.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class ConnectorBD {
    private Connection connectionSQLite;
    String url_bd = "../mydatabase.db";
    public ConnectorBD() throws SQLException {
        String string_url_sqlite = "jdbc:sqlite:" + url_bd;
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }

        this.connectionSQLite = DriverManager.getConnection(string_url_sqlite);

        System.out.println("Connected to SQLite database: " + string_url_sqlite);

        if (this.connectionSQLite != null && !this.connectionSQLite.isClosed()) {
            System.out.println("Db connection is valid and open");
        } else {
            System.out.println("Db connection failed");
        }
        // checkExistingTables();
        
    }
    
    public Connection getDatabaseConnector(){
        return connectionSQLite;
    }
    
}
