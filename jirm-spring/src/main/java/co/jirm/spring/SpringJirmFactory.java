/**
 * Copyright (C) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
