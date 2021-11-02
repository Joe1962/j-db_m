/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.jjsoft.jdb_m.types;

/**
 *
 * @author Joe1962
 */
public class TYP_DBStructCheck {
	private String column_name;
	private String column_default;
	private String is_nullable;
	private String data_type;
	private int character_maximum_length;

	/**
	 * @return the column_name
	 */
	public String getColumn_name() {
		return column_name;
	}

	/**
	 * @param column_name the column_name to set
	 */
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}

	/**
	 * @return the column_default
	 */
	public String getColumn_default() {
		return column_default;
	}

	/**
	 * @param column_default the column_default to set
	 */
	public void setColumn_default(String column_default) {
		this.column_default = column_default;
	}

	/**
	 * @return the is_nullable
	 */
	public String getIs_nullable() {
		return is_nullable;
	}

	/**
	 * @param is_nullable the is_nullable to set
	 */
	public void setIs_nullable(String is_nullable) {
		this.is_nullable = is_nullable;
	}

	/**
	 * @return the data_type
	 */
	public String getData_type() {
		return data_type;
	}

	/**
	 * @param data_type the data_type to set
	 */
	public void setData_type(String data_type) {
		this.data_type = data_type;
	}

	/**
	 * @return the character_maximum_length
	 */
	public int getCharacter_maximum_length() {
		return character_maximum_length;
	}

	/**
	 * @param character_maximum_length the character_maximum_length to set
	 */
	public void setCharacter_maximum_length(int character_maximum_length) {
		this.character_maximum_length = character_maximum_length;
	}

}
