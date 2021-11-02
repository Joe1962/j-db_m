/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.jjsoft.jdb_m;

import static cu.jjsoft.jdb_m.DBActions.doQuery;
import static cu.jjsoft.jdb_m.DBActions.getMyConn;
import static cu.jjsoft.jutils_m.SUB_Utils.echoClassMethodComment;
import static cu.jjsoft.jutils_m.SUB_Utils.echoln;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 *
 * @author joe1962
 */
public abstract class RS {
//<editor-fold defaultstate="collapsed" desc=" My class-level variables declaration ">

	protected ResultSet RST;
	protected String SQLSelectAll;
	protected String SQLDeleteAll;
	protected String SQLSelectByDate;
	protected String SQLSelectByPK;
	protected String SQLSelectByMaster;
	protected String SQLSelectByName;
	protected String SQLSelectPrevByPK;
	protected String SQLSelectNextByPK;
	protected String SQLSelectFirstByPK;
	protected String SQLSelectLastByPK;
	protected String SQLSelectSequential;
	protected SimpleDateFormat MyDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private Boolean isInserting = false;
//</editor-fold>

	public void selectAll(String OrderByString, boolean isDebug) throws SQLException {
		String QuerySQL = SQLSelectAll + OrderByString;
		PreparedStatement pstmt;
		pstmt = DBActions.getMyConn().prepareStatement(QuerySQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		RST = DBActions.doQuery(pstmt);
		RST.first();
		echoClassMethodComment(pstmt.toString(), isDebug, false);			// DEBUG...
	}

	public void selectByDate(Date MyDate, String OrderByString, boolean isDebug) throws SQLException {
		String QuerySQL = SQLSelectByDate + OrderByString;
		PreparedStatement pstmt;
		pstmt = DBActions.getMyConn().prepareStatement(QuerySQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		pstmt.setDate(1, MyDate);
		RST = DBActions.doQuery(pstmt);
		RST.first();
		echoClassMethodComment(pstmt.toString(), isDebug, false);			// DEBUG...
	}

	public int deleteAll() throws SQLException {
		String QuerySQL = SQLDeleteAll;
		PreparedStatement pstmt;
		pstmt = DBActions.getMyConn().prepareStatement(QuerySQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		return pstmt.executeUpdate();
	}

	public abstract void selectByPK(boolean isDebug) throws SQLException;

	public abstract void selectByMaster(String OrderByString, boolean isDebug) throws SQLException;

	public void selectByName(String MyName, String MyNameField, boolean FuzzySearch, boolean CaseSensitive, String OrderByString, boolean isDebug) throws SQLException {
		if (MyName == null || MyNameField == null) {
			return;
		}

		String WhereStatement;
		if (CaseSensitive) {
			WhereStatement = "WHERE " + MyNameField + " LIKE ? ";
		} else {
			WhereStatement = "WHERE UPPER(" + MyNameField + ") LIKE ? ";
			MyName = MyName.toUpperCase();
		}
		String QuerySQL = SQLSelectAll + WhereStatement + OrderByString;
		PreparedStatement pstmt;
		pstmt = DBActions.getMyConn().prepareStatement(QuerySQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		int n = 1;
		if (FuzzySearch) {
			pstmt.setString(n++, "%" + MyName + "%");
		} else {
			pstmt.setString(n++, MyName);
		}
		RST = DBActions.doQuery(pstmt);
		RST.first();
		echoln("RS.selectByName: " + pstmt.toString(), isDebug, false);
	}

	public abstract void selectByNameByActiveState(String MyName, String OrderByString, Object ActiveState) throws SQLException;

	public abstract Object getPrev() throws SQLException;

	public abstract Object getCurrent() throws SQLException;

	public abstract Object getNext() throws SQLException;

	public boolean goFirst() throws SQLException {
		if (RST == null) {
			return false;
		}

		return RST.first();
	}

	public boolean goLast() throws SQLException {
		if (RST == null) {
			return false;
		}

		return RST.last();
	}

	public boolean goPrev() throws SQLException {
		if (RST == null) {
			return false;
		}

		return RST.previous();
	}

	public boolean goNext() throws SQLException {
		if (RST == null) {
			return false;
		}

		return RST.next();
	}

	/**
	 * Get last int value + 1 for selected DB table column. Set up the proper SQL
	 * query in the subclasses.
	 *
	 * @param ParamsMap Map key is from 1 to number of parameters in SQL query,
	 * map values are the values of the corresponding parameters.
	 * @throws java.io.SQLException
	 */
	public int getSequential(HashMap<Integer, Object> ParamsMap) throws SQLException {
		PreparedStatement pstmt;
		pstmt = getMyConn().prepareStatement(SQLSelectSequential, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

		if (ParamsMap != null) {
			for (int MapKey = 1; MapKey < ParamsMap.size() + 1.; MapKey++) {
				Object MapValue = ParamsMap.get(MapKey);
				pstmt.setObject(MapKey, MapValue);
			}
		}

		setRST(doQuery(pstmt));
		getRST().first();
		Object tmpObj = getRST().getObject("SeqNext");
		int tmpInt;
		if (tmpObj == null) {
			tmpInt = 0;
		} else {
			tmpInt = (int) getRST().getObject("SeqNext");
		}
		tmpInt++;
		return tmpInt;
	}

	public abstract int appendRow(Object MyRow) throws SQLException;

	public abstract int updateRow(Object MyRow) throws SQLException;

	public TYP_RetInsertOrUpdate InsertOrUpdateRow(Object MyRow, boolean isDEBUG) throws SQLException {
		TYP_RetInsertOrUpdate retObj = new TYP_RetInsertOrUpdate();
		int retInt;

		// Transaction to try to INSERT and, on failure, UPDATE:
		DBActions.getMyConn().setAutoCommit(false);
		Savepoint BeforeInsertOrUpdate = DBActions.getMyConn().setSavepoint("BeforeInsertOrUpdate");
		retInt = appendRow(MyRow);
		if (retInt == 0) {
			echoln("INSERT failed...", isDEBUG, false);
			retObj.setInsertSuccessful(false);
			DBActions.getMyConn().rollback(BeforeInsertOrUpdate);
		} else {
			retObj.setInsertSuccessful(true);
		}
		if (!retObj.isInsertSuccessful()) {
			retInt = updateRow(MyRow);
			if (retInt == 0) {
				DBActions.getMyConn().rollback(BeforeInsertOrUpdate);
				retObj.setUpdateSuccessful(false);
				echoln("UPDATE failed...", isDEBUG, false);
			} else {
				retObj.setUpdateSuccessful(true);
			}
		}
		DBActions.getMyConn().commit();
		DBActions.getMyConn().setAutoCommit(true);

		retObj.setRowsAffected(retInt);
		return retObj;
	}

	public TYP_RetInsertOrUpdate UpdateOrInsertRow(Object MyRow, boolean isDEBUG) throws SQLException {
		TYP_RetInsertOrUpdate retObj = new TYP_RetInsertOrUpdate();
		int retInt;

		// Transaction to try to UPDATE and, on failure, INSERT:
		DBActions.getMyConn().setAutoCommit(false);
		Savepoint BeforeUpdateOrInsert = DBActions.getMyConn().setSavepoint("BeforeUpdateOrInsert");
		retInt = updateRow(MyRow);
		if (retInt == 0) {
			DBActions.getMyConn().rollback(BeforeUpdateOrInsert);
			retObj.setUpdateSuccessful(false);
			echoln("UPDATE failed...", isDEBUG, false);
		} else {
			retObj.setUpdateSuccessful(true);
		}
		if (!retObj.isUpdateSuccessful()) {
			retInt = appendRow(MyRow);
			if (retInt == 0) {
				echoln("INSERT failed...", isDEBUG, false);
				retObj.setInsertSuccessful(false);
				DBActions.getMyConn().rollback(BeforeUpdateOrInsert);
			} else {
				retObj.setInsertSuccessful(true);
			}
		}
		DBActions.getMyConn().commit();
		DBActions.getMyConn().setAutoCommit(true);

		retObj.setRowsAffected(retInt);
		return retObj;
	}

	public abstract int deleteRow() throws SQLException;

	public abstract int deleteRowsByZeroQuant() throws SQLException;

	public abstract int deleteByMaster() throws SQLException;

	public int rsCount() {
		// Remember to use a select method first to set up the recordset...
		int NumRows;

		try {
			boolean retBool = RST.last();
			if (retBool) {
				NumRows = RST.getRow();
				RST.first();
				return NumRows;
			} else {
				return 0;
			}
		} catch (SQLException | NullPointerException ex) {
			echoln(ex.getMessage(), false, true);
			return 0;
		}
	}

	public ResultSet getRST() {
		return RST;
	}

	public void setRST(ResultSet RST) {
		this.RST = RST;
	}

	public Boolean getInserting() {
		return isInserting;
	}

	public void setInserting(Boolean isInserting) {
		this.isInserting = isInserting;
	}

	public final class TYP_RetInsertOrUpdate {

		private int RowsAffected;
		private boolean InsertSuccessful;
		private boolean UpdateSuccessful;

		/**
		 * @return the RowsAffected
		 */
		public int getRowsAffected() {
			return RowsAffected;
		}

		/**
		 * @param aRowsAffected the RowsAffected to set
		 */
		public void setRowsAffected(int aRowsAffected) {
			RowsAffected = aRowsAffected;
		}

		/**
		 * @return the InsertSuccessful
		 */
		public boolean isInsertSuccessful() {
			return InsertSuccessful;
		}

		/**
		 * @param aInsertSuccessful the InsertSuccessful to set
		 */
		public void setInsertSuccessful(boolean aInsertSuccessful) {
			InsertSuccessful = aInsertSuccessful;
		}

		/**
		 * @return the UpdateSuccessful
		 */
		public boolean isUpdateSuccessful() {
			return UpdateSuccessful;
		}

		/**
		 * @param aUpdateSuccessful the UpdateSuccessful to set
		 */
		public void setUpdateSuccessful(boolean aUpdateSuccessful) {
			UpdateSuccessful = aUpdateSuccessful;
		}
	}

}
