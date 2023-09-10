import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogData
{

    //Log class that creates a log folder and write logs for every peer seperately to find the exceptions and workflow
    static FileOutputStream file;
    static OutputStreamWriter writer;
    LogData(String fileName) throws Exception
    {
        File dir = new File("logsfolder");
        dir.mkdir();
        File logsFile = new File("logsfolder", fileName);
        file=new FileOutputStream("logsfolder//"+fileName);
        writer=new OutputStreamWriter(file, StandardCharsets.UTF_8);
    }

    // PrintLog function that prints the log data on our system console while runnning the project
    public void printLog(String s)
    {
        try
        {
            writer.write(s+"\n");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

    //shows the log data with timestamp and respective trace message at that instance of time
    public  void showLog(String message)
    {
        Calendar c=Calendar.getInstance();
        SimpleDateFormat d=new SimpleDateFormat("HH:mm:ss");
        printLog(d.format(c.getTime())+" Peer "+ message);
        System.out.println(d.format(c.getTime())+" Peer "+ message);
    }

    //Closing the Log file and ending with teminantion of the log class
    public  void closeLog()
    {
        try
        {
            writer.flush();
            file.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}
