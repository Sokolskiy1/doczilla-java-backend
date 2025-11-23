package java_backend.service;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

import java_backend.connector.ConnectorBD;

public class FileService {
    private final ConnectorBD connectorBD;

    public FileService() throws SQLException {
        this.connectorBD = new ConnectorBD();
    }
    public boolean addingFile(String uuid_file,String uuid_user) {
       
        return true;
    }

    

}
