import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.Scanner;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDate;
import java.util.Arrays;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;

class JSONData {
    JSONObject obj = new JSONObject();
    public void jsonDataConvertor(int id, HttpServletRequest req, HttpServletResponse res, int recNo, String eventId, String resKeyWord, String source, String computerName, String dateTime, String level, String task){
        JSONArray data = new JSONArray();
        data.put(Integer.toString(recNo));
        data.put(eventId);
        data.put(resKeyWord);
        data.put(source);
        data.put(computerName);
        data.put(dateTime);
        data.put(level);
        data.put(task);
        obj.put(Integer.toString(id),data);
    }
    public String getData(){
        String res = obj.toString();
        return res;
    }
}

public class Main extends HttpServlet {

    static String RecordNumber, EventId, ResKeyword, Source, ComputerName, DateTime;
    static String uri, pageName, insertSqlStatement;

    static int javaCount = 0, cppCount = 0;
    static String systemStatus = "false", applicationStatus = "false";

//  Methods to call native
    static {
        System.out.println("in dll");
        System.loadLibrary("event");
    }

    public static synchronized void callback(Properties events, String type){
//        System.out.println("events = "+events);
        try {
            String RecordNumber = events.getProperty("RecordNumber");
            String EventId = events.getProperty("EventId");
            String ResKeyword = events.getProperty("ResKeyword");
            String Source = events.getProperty("Source");
            String ComputerName = events.getProperty("ComputerName");
            String DateTime = events.getProperty("Date");
            String Level = events.getProperty("Level");
            String Task = events.getProperty("Task");

            if (type.equals("System"))
                insertSqlStatement = "INSERT INTO systemevents (RecordNumber, EventId, ResKeyword, Source, ComputerName, DateTime, Level, Task) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            else if (type.equals("Application"))
                insertSqlStatement = "INSERT INTO applicationevents (RecordNumber, EventId, ResKeyword, Source, ComputerName, DateTime, Level, Task) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/events", "root", "root");
            PreparedStatement preparedStatement = connection.prepareStatement(insertSqlStatement);
            preparedStatement.setInt(1, Integer.parseInt(RecordNumber));
            preparedStatement.setString(2, EventId);
            preparedStatement.setString(3, ResKeyword);
            preparedStatement.setString(4, Source);
            preparedStatement.setString(5, ComputerName);
            preparedStatement.setDate(6, java.sql.Date.valueOf(DateTime));
            preparedStatement.setString(7, Level);
            preparedStatement.setString(8, Task);
            preparedStatement.executeUpdate();

            String updateStatement = "UPDATE dates2 SET recno = ? WHERE date = ? AND query = ?;";
            preparedStatement = connection.prepareStatement(updateStatement);
            preparedStatement.setInt(1,Integer.parseInt(RecordNumber));
            preparedStatement.setDate(2, java.sql.Date.valueOf(DateTime));
            preparedStatement.setString(3, type);

            preparedStatement.executeUpdate();
            connection.close();
        }
        catch (Exception e){
            System.out.println("In callback exception...");
            e.printStackTrace();
        }
    }
    public native void getEventInfo(String path, String query);



//  Java Methods to perform table operations
    public void getCount(HttpServletRequest req, HttpServletResponse res){
        String pageName = req.getParameter("type");
        String date1 = req.getParameter("arg1");
        String date2 = req.getParameter("arg2");
//        System.out.println(pageName+" "+date1+" "+date2);

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/events", "root", "root");
            Statement statement = con.createStatement();
            PrintWriter writer = res.getWriter();
            String countOfData = "";
            if (pageName.equals("System")){
                String q = "SELECT COUNT(*) FROM systemevents WHERE DateTime BETWEEN '"+date1+"' AND '"+date2+"'";
                ResultSet rs = statement.executeQuery(q);
                while (rs.next()){
                    countOfData = Integer.toString(rs.getInt(1));
                }
            }
            else {
                String q = "SELECT COUNT(*) FROM applicationevents WHERE DateTime BETWEEN '"+date1+"' AND '"+date2+"'";
                ResultSet rs = statement.executeQuery(q);

                while (rs.next()){
                    countOfData = Integer.toString(rs.getInt(1));
                }
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("count",countOfData);
            jsonObject.put("systemStatus",systemStatus);
            jsonObject.put("applicationStatus",applicationStatus);

            writer.println(jsonObject.toString());

            con.close();
        }
        catch (Exception ex) {
            System.out.println("Counter exception");
            System.out.println(ex);
        }

    }

    public void printData(HttpServletRequest req, HttpServletResponse res){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/events", "root", "root");
            Statement statement = con.createStatement();

            String pageName = req.getParameter("type");
            String date1 = req.getParameter("arg1");
            String date2 = req.getParameter("arg2");

            int page = Integer.parseInt(req.getParameter("page"));
            int perPage = 20;
            int start = (page-1)*perPage;

            PrintWriter writer = res.getWriter();
            ResultSet rs;
            JSONData obj = new JSONData();

//            Choosing the type and fetching the data
            if (pageName.equals("System")){
                String s = "SELECT * FROM systemevents WHERE DateTime BETWEEN '"+date1+"' AND '"+date2+ "ORDER BY RecordNumber "+ "' LIMIT "+ start+", "+perPage;
                rs = statement.executeQuery(s);
            }
            else {
                String s = "SELECT * FROM applicationevents WHERE DateTime BETWEEN '"+date1+"' AND '"+date2+"ORDER BY RecordNumber "+"' LIMIT "+ start+", "+perPage;
                rs = statement.executeQuery(s);
            }
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");

//            Printing the data in Table format
            int id = 0;
            while (rs.next()) {
                obj.jsonDataConvertor(id, req, res, rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8));
                id++;
            }
            writer.println(obj.getData());
            con.close();
        }
        catch (Exception ex){
            System.out.println("in Printer exception....");
            System.out.println(ex);
        }
    }

    public void getDataBetweenDate(HttpServletRequest req, HttpServletResponse res){
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/events", "root", "root");
            Statement statement = con.createStatement();

            pageName = req.getParameter("type");
            String date1 = req.getParameter("arg1");
            LocalDate start = LocalDate.parse(date1);
            String date2 = req.getParameter("arg2");
            LocalDate end = LocalDate.parse(date2);

            if (pageName.equals("System")){
                systemStatus = "false";
            }
            else if (pageName.equals("Application")){
                applicationStatus = "false";
            }

//            System.out.println(date1 + " " + date2 + " => "+pageName);


            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        String type = Thread.currentThread().getName();
                        for (LocalDate currDate = start; (currDate.isBefore(end) || currDate.isEqual(end)); currDate = currDate.plusDays(1)) {
                            System.out.println(currDate + " => "+ type);
                            String dateQuery = "SELECT COUNT(date) FROM dates2 WHERE date='" + currDate + "' AND query='" + type + "'";
                            ResultSet rs = statement.executeQuery(dateQuery);

                            int validDate = 1;
                            while (rs.next()) {
                                validDate = rs.getInt(1);
                            }

                            int recNo = -1;
                            if (validDate == 1){
                                String query = "SELECT recno FROM dates2 WHERE date='"+ currDate +"' AND query='"+ type + "'";
                                rs = statement.executeQuery(query);
                                while (rs.next()){
                                    recNo = rs.getInt(1);
                                }
                                if (recNo == -1){
                                    validDate = 0;
                                }
                            }

                            if (validDate == 0) {
                                String query = "<QueryList><Query Id='0' Path='" + type + "'><Select Path='" + type + "'>*[System[TimeCreated[@SystemTime&gt;='";
                                query += currDate + "T00:00:01.000Z' and @SystemTime&lt;='" + currDate + "T23:59:59.999Z']]]</Select></Query></QueryList>";
                                String newDateQuery = "INSERT INTO dates2(date, query) VALUES (?,?)";
                                PreparedStatement preparedStatement = con.prepareStatement(newDateQuery);
                                preparedStatement.setDate(1, java.sql.Date.valueOf(currDate));
                                preparedStatement.setString(2, type);
                                preparedStatement.executeUpdate();
                                new Main().getEventInfo(type, query);
                            }
                            else {
                                String query = "<QueryList><Query Id='0' Path='" + type + "'><Select Path='" + type + "'>*[System[TimeCreated[@SystemTime&gt;='";
                                query += currDate + "T00:00:01.000Z' and @SystemTime&lt;='" + currDate + "T23:59:59.999Z'] and EventRecordID&gt;="+(recNo+1)+"]]</Select></Query></QueryList>";
                                new Main().getEventInfo(type, query);
                            }
                        }
                        System.out.println("over loop - "+ type);
                        if (type.equals("System")){
                            systemStatus = "true";
                        }
                        else if (type.equals("Application")){
                            applicationStatus = "true";
                        }
                        con.close();
                    } catch (Exception e) {
                        System.out.println("in run method...");
                        e.printStackTrace();
                    }
                }
            }, pageName);
            thread.start();
        }
        catch (Exception e) {
            System.out.println("in service exception...");
            e.printStackTrace();
        }
    }

//  Method to handle incoming requests
    public void service(HttpServletRequest req, HttpServletResponse res) {
        String requestURL = req.getRequestURL().toString();
        String[] path = requestURL.split("/");

        String methodName = path[path.length-1];

        try{
            Main obj = new Main();
            Method method = obj.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
            method.invoke(obj, req, res);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
