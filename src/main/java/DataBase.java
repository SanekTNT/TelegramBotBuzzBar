import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class DataBase {

    private static String DATABASE_URL;
    private static String DATABASE_USER;
    private static String DATABASE_PASSWORD;
    private static Statement statement;


    DataBase(){
        readConfig();
        try {
            statement = getDBConnection().createStatement();
            createTables();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void readConfig() {
        FileInputStream fis;
        Properties property = new Properties();
        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            property.load(fis);

            DATABASE_URL = property.getProperty("DATABASE_URL");
            DATABASE_USER = property.getProperty("DATABASE_USER");
            DATABASE_PASSWORD = property.getProperty("DATABASE_PASSWORD");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager
                    .getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }

    private static void createTables(){
        String createTableUsersSQL = "CREATE TABLE IF NOT EXISTS Users("
                + "chatid text"
                + ");";

        String createTableAdminsSQL = "CREATE TABLE IF NOT EXISTS Admins("
                + "chatid text,"
                + "ismainadmin boolean,"
                + "adminname text"
                + ");";

        String createTableReservationSQL = "CREATE TABLE IF NOT EXISTS TableReservation("
                + "chatid text,"
                + "name text,"
                + "numberofplaces text,"
                + "dateofreservation text,"
                + "timeofreservation text,"
                + "telefon text,"
                + "wishes text"
                + ");";

        try {
            statement.execute(createTableUsersSQL);
            statement.execute(createTableAdminsSQL);
            statement.execute(createTableReservationSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertIntoTableUsers(String value){
        String insertIntoTableSQL = "INSERT INTO USERS"
                + "(ChatID)"
                + "VALUES"
                + " ("
                + value
                + ");";
        try {
            statement.executeUpdate(insertIntoTableSQL);
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertIntoTableAdmins(String value, boolean isMainAdmin, String adminName){
        String insertIntoTableSQL = "INSERT INTO Admins"
                + "(ChatID, IsMainAdmin, AdminName)"
                + "VALUES"
                + " ('"
                + value
                + "', "
                + isMainAdmin
                + ", '"
                + adminName
                + "');";
        try {
            statement.executeUpdate(insertIntoTableSQL);
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertIntoTableOfReservation(String chatid, String name,
                                                    String numberofplaces, String dateofreservation,
                                                    String timeOfReservation, String telefon, String wishes){
        String insertIntoTableSQL = "INSERT INTO tablereservation VALUES "
                + "('"
                + chatid + "', '"
                + name + "', '"
                + numberofplaces + "', '"
                + dateofreservation + "', '"
                + timeOfReservation + "', '"
                + telefon + "', '"
                + wishes + "');";
        try {
            statement.executeUpdate(insertIntoTableSQL);
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static ArrayList<String> selectChatIdFromTable(String tableName){
        ArrayList<String> allUsers = new ArrayList<>();
        ResultSet resultSet;
        String selectFromTableSQL = "SELECT chatid FROM " + tableName + ";";
        try {
            resultSet = statement.executeQuery(selectFromTableSQL);
            while (resultSet.next()) {
                allUsers.add(resultSet.getString("chatid"));
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return allUsers;
    }

    public static ArrayList<String> selectOneTableReservation(String chatId){
        ArrayList<String> tableReservationArray = new ArrayList<>();
        ResultSet resultSet;
        String selectFromTableSQL = "SELECT * FROM tablereservation WHERE chatid = '" + chatId + "';";
        try {
            resultSet = statement.executeQuery(selectFromTableSQL);
            resultSet.next();
            tableReservationArray.add(resultSet.getString("name"));
            tableReservationArray.add(resultSet.getString("numberofplaces"));
            tableReservationArray.add(resultSet.getString("dateofreservation"));
            tableReservationArray.add(resultSet.getString("timeofreservation"));
            tableReservationArray.add(resultSet.getString("telefon"));
            tableReservationArray.add(resultSet.getString("wishes"));
            tableReservationArray.add(resultSet.getString("chatid"));
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return tableReservationArray;
    }

    public static String selectCurrentCellFromTableReservation(String column, String chatId){
        ResultSet resultSet;
        String selectFromTableSQL = "SELECT " + column + " FROM tablereservation"
                + " WHERE chatid = '"
                + chatId
                + "';";
        try {
            resultSet = statement.executeQuery(selectFromTableSQL);
            resultSet.next();
            String result = resultSet.getString(column);
            return result;
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static ArrayList<ArrayList<String>> selectAllFromTableReservation(){
        ArrayList<ArrayList<String>> allTableReservations = new ArrayList<>();
        ResultSet resultSet;
        String selectFromTableSQL = "SELECT * FROM tablereservation;";
        try {
            resultSet = statement.executeQuery(selectFromTableSQL);
            while (resultSet.next()) {
                ArrayList<String> oneTableReservation = new ArrayList<>();
                oneTableReservation.add(resultSet.getString("name"));
                oneTableReservation.add(resultSet.getString("numberofplaces"));
                oneTableReservation.add(resultSet.getString("dateofreservation"));
                oneTableReservation.add(resultSet.getString("timeofreservation"));
                oneTableReservation.add(resultSet.getString("telefon"));
                oneTableReservation.add(resultSet.getString("wishes"));
                oneTableReservation.add(resultSet.getString("chatid"));
                allTableReservations.add(oneTableReservation);
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return allTableReservations;
    }

    public static boolean isAdminMain(String chatId){
        ResultSet resultSet;
        String selectFromTableSQL = "SELECT ismainadmin FROM admins"
                + " WHERE chatid = '"
                + chatId
                + "';";
        try {
            resultSet = statement.executeQuery(selectFromTableSQL);
            resultSet.next();
            String result = resultSet.getString("ismainadmin");
            if(result.equals("t"))
                return true;
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static String selectNameOfAdmin(String chatId){
        ResultSet resultSet;
        String selectFromTableSQL = "SELECT adminname FROM admins"
                + " WHERE chatid = '"
                + chatId
                + "';";
        try {
            resultSet = statement.executeQuery(selectFromTableSQL);
            resultSet.next();
            String result = resultSet.getString("adminname");
            return result;
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static void changeColumnIsAdminMainInAdminsTable(String chatId, boolean newAdminMode){
        String updateTableSQL = "UPDATE Admins SET isMainAdmin = "
                + newAdminMode
                + " WHERE chatId = '"
                + chatId
                + "';";
        try {
            statement.execute(updateTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteFromTable(String tableName, String chatId){
        String deleteTableSQL = "DELETE FROM "+ tableName +" WHERE chatid = '" + chatId + "';";
        try {
            statement.execute(deleteTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean existsChatIdInTable(String tableName, String chatId) {
        ResultSet resultSet;
        String selectFromTableSQL = "SELECT EXISTS (SELECT chatid FROM "
                + tableName
                + " WHERE chatid = '"
                + chatId
                + "');";
        try {
            resultSet = statement.executeQuery(selectFromTableSQL);
            resultSet.next();
            String result = resultSet.getString("exists");
            if(result.equals("t"))
                return true;
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

}
