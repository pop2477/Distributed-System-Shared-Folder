import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.*;


// To start server if connection fails due to port in use (Only guarenteed to work on UNIX like system)
// use command lsof -i tcp:1099 to find the pid of the process
// Use command kill -9 PID, where PID is the pid from the previous step
// Run command rmiregistry &, then start the server, then client in seperate windows

/**
 * Server program implements the ServerInterface, handles operations from clinet
 */
public class Server implements ServerInterface
{
    ArrayList<String> table = new ArrayList<String>();

    /**
     * Default Constructor
     */
    public Server() 
    {}

    /**
     * Upload metho for writing file from client to server
     */
    public synchronized String Upload(byte[] filedata, String filename, int bytesRead, boolean append) throws IOException, RemoteException
    {
        try
        {
            File file = new File(filename);

            file.createNewFile();

            try(FileOutputStream s = new FileOutputStream(file, append))
            {
                if(bytesRead < 1)
                {
                    s.close();
                }
                else if(bytesRead > 0)
                {
                    s.write(filedata, 0, bytesRead);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return "Failed to Upload";
            }

            return "Upload Complete";
        
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "Failed to Upload";
        }
    }

    /**
     * Deletes file from server 
     */
    public synchronized String Delete(String filename) throws IOException, RemoteException
    {
        try
        {
            File file = new File(filename);
            
            file.delete();
            
            return "File Deleted";
        }
        catch(Exception e)
        {
            return "File Deltion Failed";
        }
    }

    /**
     * Returns to client list of files on server
     */
    public synchronized File[] filesOnServer() throws RemoteException
    {
        return new File(System.getProperty("user.dir") + "/Server/").listFiles();
    }

    public static void main(String args[]) throws Exception
    {
        File serverDir = new File(System.getProperty("user.dir") + "/Server/");
        if(!serverDir.exists())
        {
            serverDir.mkdir();
        }

        try 
        {
            Server obj = new Server();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("//localhost/Server", stub);
            System.out.println("Server Active");
        } 
        catch (Exception e) 
        {
            System.out.println("Server Registry Already Exists");
            e.printStackTrace();
        }

        
    }
}