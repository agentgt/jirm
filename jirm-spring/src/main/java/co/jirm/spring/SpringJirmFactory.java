package co.jirm.spring;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.orm.JirmFactory;
import co.jirm.orm.OrmConfig;
import co.jirm.orm.dao.JirmDao;


public class SpringJirmFactory implements JirmFactory {
	
	private final JdbcTemplate jdbcTemplate;
	private final Supplier<SqlExecutor> sqlExecutorSupplier;
	private final Supplier<OrmConfig> ormConfigSupplier;
	
	@Autowired
	public SpringJirmFactory(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
		ormConfigSupplier = Suppliers.memoize(new Supplier<OrmConfig>() {
			@Override
			public OrmConfig get() {
				return SpringJirmFactory.this.createOrmConfig();
			}
		});
		sqlExecutorSupplier = Suppliers.memoize(new Supplier<SqlExecutor>() {
			@Override
			public SqlExecutor get() {
				return SpringJirmFactory.this.createSqlExecutor();
			}
		});
	}
	
	public <T> JirmDao<T> daoFor(Class<T> k) {
		return JirmDao.newInstance(k, ormConfigSupplier.get());
	}
	
	protected OrmConfig createOrmConfig() {
		return OrmConfig.newInstance(sqlExecutorSupplier.get(), getSqlObjectConfig());
	}
	
	protected SqlExecutor createSqlExecutor() {
		return SpringJdbcSqlExecutor.newInstance(getSqlObjectConfig(), jdbcTemplate);
	}
	
	protected SqlObjectConfig getSqlObjectConfig() {
		return SqlObjectConfig.DEFAULT;
	}
	

}
