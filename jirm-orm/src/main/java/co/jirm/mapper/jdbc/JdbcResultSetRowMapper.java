package co.jirm.mapper.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;


public interface JdbcResultSetRowMapper<T> {
	
	public T mapRow(ResultSet rs, int rowNum) throws SQLException;

}
