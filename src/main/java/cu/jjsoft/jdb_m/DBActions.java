/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.jjsoft.jdb_m;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import static cu.jjsoft.jutils_m.subs.SUB_UtilsNotifications.echoln;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joe1962
 */
public class DBActions {

	private static Connection MyConn;
	//static final SimpleDateFormat MyDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 *
	 * @param MyDBDriverType
	 * @param MyDBDriver
	 * @param MyDBServer
	 * @param MyDBPort
	 * @param MyDBName
	 * @param MyDBUser
	 * @param MyDBPass
	 * @throws SQLException
	 */
	public static void dbConnectServer(String MyDBDriverType, String MyDBDriver, String MyDBServer, int MyDBPort, String MyDBName, String MyDBUser, String MyDBPass) throws SQLException {
		// Connect to the database server:
		String sMyConn = MyDBDriverType + ":" + MyDBDriver + "://" + MyDBServer + ":" + String.valueOf(MyDBPort) + "/" + MyDBName;
		MyConn = DriverManager.getConnection(sMyConn, MyDBUser, MyDBPass);
	}

	/**
	 *
	 * @param MyDBDriverType
	 * @param MyDBDriver
	 * @param MyDBPath
	 * @param MyDBUser
	 * @param MyDBPass
	 * @throws SQLException
	 */
	public static void dbConnectFileSystem(String MyDBDriverType, String MyDBDriver, String MyDBPath, String MyDBUser, String MyDBPass) throws SQLException {
		// Connect to the database file:
		String sMyConn = MyDBDriverType + ":" + MyDBDriver + ":directory:/" + MyDBPath + "/";
		//echoln(sMyConn, false, false);			// DEBUG...
		MyConn = DriverManager.getConnection(sMyConn, MyDBUser, MyDBPass);
	}

	/**
	 *
	 * @throws SQLException
	 */
	public static void dbDisconnect() throws SQLException {
		// Disconnect from the database:
		// Close Connection:
		if (getMyConn() != null) {
			getMyConn().close();
		}
	}

	/**
	 *
	 * @param pstmt
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet doQuery(PreparedStatement pstmt) throws SQLException {
		// Execute SQL query by prepared statement...
		return pstmt.executeQuery();
	}

	/**
	 *
	 * @param pstmt
	 * @return
	 * @throws SQLException
	 */
	public static int doUpdate(PreparedStatement pstmt) throws SQLException {
		// Execute SQL update query by prepared statement...
		return pstmt.executeUpdate();
	}

	/**
	 *
	 * @param DBName
	 * @throws SQLException
	 */
	public static void doTruncate(String DBName) throws SQLException {
		// Truncate DB table, probably postgreSQL-specific...
		String QuerySQL = "TRUNCATE " + DBName + " RESTART IDENTITY CASCADE";
		PreparedStatement pstmt = getMyConn().prepareStatement(QuerySQL);
		doUpdate(pstmt);
	}

	/**
	 *
	 * @param Type
	 * @param Description
	 * @param MyOperator
	 * @param isDebug
	 * @throws SQLException
	 */
	public static void WriteSysLog(int Type, String Description, String MyOperator, boolean isDebug) throws SQLException {
		// Requires existence of syslog table in current DB connection...
		String SQLSelectSequential = "SELECT max(consec) AS SeqNext FROM syslog ";
		PreparedStatement pstmt;
		pstmt = getMyConn().prepareStatement(SQLSelectSequential, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet RST;
		RST = doQuery(pstmt);
		RST.first();
		Object tmpObj = RST.getObject("SeqNext");
		long tmpLong;
		if (tmpObj == null) {
			tmpLong = 0;
		} else {
			tmpLong = (long) RST.getObject("SeqNext");
		}

		String QuerySQL = "INSERT INTO syslog (consec, fecha_hora, tipo, descrip, operator) VALUES (?, ?, ?, ?, ?)";
		//PreparedStatement pstmt;
		pstmt = getMyConn().prepareStatement(QuerySQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		int n = 1;
		Date tmpDate = new java.util.Date();
		pstmt.setLong(n++, ++tmpLong);
		pstmt.setTimestamp(n++, new java.sql.Timestamp(tmpDate.getTime()));
		pstmt.setInt(n++, Type);
		pstmt.setString(n++, Description);
		pstmt.setString(n++, MyOperator);
		int retBool = doUpdate(pstmt);
		echoln(retBool, isDebug, false);
	}

	/**
	 *
	 * @param TimeoutSecs
	 * @return
	 * @throws SQLException
	 */
	public static boolean IsValid(int TimeoutSecs) throws SQLException {
		boolean DBConnValid;
		DBConnValid = DBActions.getMyConn().isValid(5);

		return DBConnValid;
	}

	public static void doCopyBackup(String DBTable, String FileDest) throws SQLException, IOException {
		String QuerySQL = "SELECT * FROM " + DBTable;
		PreparedStatement pstmt = getMyConn().prepareStatement(QuerySQL);
		ResultSet MyRS = doQuery(pstmt);

		try (FileWriter MyWriter = new FileWriter(FileDest, false); CSVWriter MyCSVWriter = new CSVWriter(MyWriter)) {
			MyCSVWriter.writeAll(MyRS, true);
		}
	}

	public static long doCopyRestore(String DBTable, String FileSource) throws FileNotFoundException, IOException, SQLException {
		List<String[]> MyList;
		try (FileReader MyReader = new FileReader(FileSource); CSVReader MyCSVReader = new CSVReader(MyReader)) {
			MyList = MyCSVReader.readAll();
		} catch (CsvException ex) {
			Logger.getLogger(DBActions.class.getName()).log(Level.SEVERE, null, ex);
		}

		// TODO: truncate table and import CSV list...
		return 0;			// Return number of records...
	}

	/**
	 *
	 * @return
	 */
	public static Connection getMyConn() {
		return MyConn;
	}

	/**
	 *
	 * @param aMyConn
	 */
	public static void setMyConn(Connection aMyConn) {
		MyConn = aMyConn;
	}

}
