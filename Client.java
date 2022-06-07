import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Client program handles user interaction and makes calls to the server
 */
public class Client extends UnicastRemoteObject
{
    static boolean serverRecieved = true;
    static ReentrantLock lock = new ReentrantLock();

    public Client() throws RemoteException {}

    /**
     * Decides which user file to upload, transparent 
     * @param stub Client stub for communication with server
     * @param filename Name of the file to upload
     * @throws Exception
     */
    public static void Upload(ServerInterface stub, File filename) throws Exception
    {
        String response = "";
        try (FileInputStream f = new FileInputStream(filename))
        {
            int noMore = 0;
            byte[] buffer = new byte[1024];
            noMore = f.read(buffer);
            if(noMore < 1)
            {
                stub.Upload(buffer, System.getProperty("user.dir") + "/Server/" + filename.getName(), 0, false);
            }
            else
            {
                stub.Upload(buffer, System.getProperty("user.dir") + "/Server/" + filename.getName(), noMore, false);
                noMore = f.read(buffer);
                while(noMore != -1)
                {
                    response = stub.Upload(buffer, System.getProperty("user.dir") + "/Server/" + filename.getName(), noMore, true);
                    noMore = f.read(buffer);
                }
            }
        }
        System.out.println(response);
    }


    /**
     * Decides which server file to delete, transparent
     * @param stub Client stub for communication with the server
     * @param f File to delete
     * @throws Exception
     */
    public static void Delete(ServerInterface stub, File f) throws Exception
    {
        String response = stub.Delete(System.getProperty("user.dir") + "/Server/" + f.getName());
        System.out.println(response);
    }

   /**
    * Determines whethere item is in list 0
    * @param o List 
    * @param item Item to check for in List o
    * @return True if item is in o, false otherwise
    */
    public static boolean in(ArrayList <File> o, File item)
    {
        for(File i : o)
        {
            if(i.getName().equals(item.getName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Transparency mode allows the user to do file operations on the server via the client folder
     * @param stub Client stub
     * @param scanner Scanner for user input
     * @param last Hash map holding the client files as a key and the last time they were updated(ms) as the value
     * @param serverFiles List of files on the server
     * @throws Exception
     */
    public static void Transparency(ServerInterface stub, HashMap<File, Long> last, ArrayList<File> serverFiles) throws Exception
    {
        for(File f : new File(System.getProperty("user.dir") + "/Client").listFiles())
        {
            if(last.containsKey(f))
            {
                if(f.lastModified() != last.get(f))
                {
                    Delete(stub, f);

                    Upload(stub, f);

                }
            }
            else
            {
                Upload(stub, f);
            }
            last.put(f, f.lastModified());
            if(!in(serverFiles, f))
            {
                serverFiles.add(f);
            }
          /*if(last.containsKey(f))
            {
                // If the file is in the server folder but has been updated
                if(f.lastModified() != last.get(f))
                {
                    Upload(stub, f);

                    last.put(f, f.lastModified());
                    if(!in(serverFiles, f))
                    {
                        serverFiles.add(f);
                    }
                }
            }
            //Else if the file is not on the server
            else if(!in(serverFiles, f))
            {
                Upload(stub, f);

                last.put(f,f.lastModified());
                serverFiles.add(f);
            }*/
        }

        ArrayList<File> clientFiles= new ArrayList<File>();
        Collections.addAll(clientFiles, new File(System.getProperty("user.dir") + "/Client").listFiles());
        ArrayList<File> temp = new ArrayList<File>(serverFiles);

        // Check if file has been deleted from client folder, if so delete from server
        for(File f : temp)
        {
            if(!in(clientFiles, f))
            {
                Delete(stub, f);
                serverFiles.remove(f);
                last.remove(f);
                break;
            }
        }
    }

    public static void main(String args[]) throws Exception
    {
        File clientDir = new File(System.getProperty("user.dir") + "/Client");
        if(!clientDir.exists())
        {
            clientDir.mkdir();
        }

        String host = (args.length < 1) ? null : args[0];
        try
        {

            Registry registry = LocateRegistry.getRegistry(host);
            ServerInterface stub = (ServerInterface) registry.lookup("//localhost/Server");

            boolean run = true;

            
            while(run)
            {
                ArrayList<File> serverFiles = new ArrayList<File>();
                    Collections.addAll(serverFiles, stub.filesOnServer());
                    HashMap<File, Long>  last = new HashMap<File, Long>();
                    for (File f : clientDir.listFiles())
                    {
                        last.put(f, f.lastModified());
                    }

                while(run)
                {
                    Transparency(stub, last, serverFiles);
                }  
            }
        }
        catch (Exception e)
        {
            System.err.println("Client Exception: " + e.toString());
            e.printStackTrace();
            
        }
        System.exit(0);
    }
}
