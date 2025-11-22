package java_backend.service;

import java.sql.SQLException;

import java_backend.connector.ConnectorBD;

public class FileService {
    private final ConnectorBD connectorBD;

    public FileService() throws SQLException {
        this.connectorBD = new ConnectorBD();
    }
    public boolean addingFile(String File){
       
       return true;
    }
}
