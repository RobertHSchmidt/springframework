/**
 * 
 */
package org.springframework.batch.io.driving.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.springframework.batch.item.StreamContext;
import org.springframework.batch.item.stream.GenericStreamContext;
import org.springframework.core.CollectionFactory;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * </p>Extension of the ColumnMapRowMapper that converts a column map to {@link StreamContext} and allows
 * {@link StreamContext} to be converted back as a PreparedStatementSetter.  This is useful in a restart 
 * scenario, as it allows for the standard functionality of the ColumnMapRowMapper to be used to 
 * create a map representing the columns returned by a query.  It should be noted that this column ordering
 * is preserved in the map using a link list version of Map. 
 * 
 * 
 * @author Lucas Ward
 * @author Dave Syer
 * @see RestartDataRowMapper
 */
public class ColumnMapStreamContextRowMapper extends ColumnMapRowMapper implements RestartDataRowMapper{
	
	static final String KEY = ClassUtils.getQualifiedName(ColumnMapStreamContextRowMapper.class) + ".KEY.";
	
	public PreparedStatementSetter createSetter(StreamContext streamContext) {
		
		ColumnMapRestartData columnData = new ColumnMapRestartData(streamContext.getProperties());
		
		List columns = new ArrayList();
		for (Iterator iterator = columnData.entrySet().iterator(); iterator.hasNext();) {
			Entry entry = (Entry)iterator.next();
			Object column = entry.getValue();
			columns.add(column);
		}
		
		return new ArgPreparedStatementSetter(columns.toArray());
	}

	public StreamContext createRestartData(Object key) {
		
		Assert.isInstanceOf(Map.class, key, "Key must be of type Map.");
		Map keys = (Map)key;
		
		return new ColumnMapRestartData(keys);
	}
	
	
	private static class ColumnMapRestartData extends GenericStreamContext{
		
		public ColumnMapRestartData(Map keys) {
			super();
			for(Iterator it = keys.entrySet().iterator();it.hasNext();){
				Entry entry = (Entry)it.next();
				putString(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		
		public ColumnMapRestartData(Properties props) {
			super(props);
		}
		
	}
	
	/*
	 * Exact duplicate of Spring class of the same name, copied because it is
	 * package private.
	 */
	private static class ArgPreparedStatementSetter implements PreparedStatementSetter{

		private final Object[] args;


		/**
		 * Create a new ArgPreparedStatementSetter for the given arguments.
		 * @param args the arguments to set
		 */
		public ArgPreparedStatementSetter(Object[] args) {
			this.args = args;
		}


		public void setValues(PreparedStatement ps) throws SQLException {
			if (this.args != null) {
				for (int i = 0; i < this.args.length; i++) {
					Object arg = this.args[i];
					if (arg instanceof SqlParameterValue) {
						SqlParameterValue paramValue = (SqlParameterValue) arg;
						StatementCreatorUtils.setParameterValue(ps, i + 1, paramValue, paramValue.getValue());
					}
					else {
						StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, arg);
					}
				}
			}
		}
	}
}
