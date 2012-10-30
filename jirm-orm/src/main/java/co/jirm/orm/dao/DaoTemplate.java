package co.jirm.orm.dao;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterators.partition;
import static com.google.common.collect.Iterators.peekingIterator;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.util.JirmPrecondition;
import co.jirm.core.util.ObjectMapUtils;
import co.jirm.core.util.ObjectMapUtils.NestedKeyValue;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.mapper.definition.SqlParameterDefinition;
import co.jirm.orm.builder.query.SelectBuilderFactory;
import co.jirm.orm.builder.query.SelectRootClauseBuilder;
import co.jirm.orm.builder.query.SelectBuilderFactory.CountBuilder;
import co.jirm.orm.builder.query.SelectBuilderFactory.SelectBuilder;
import co.jirm.orm.builder.update.UpdateBuilderFactory;
import co.jirm.orm.writer.SqlWriterStrategy;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;


public class DaoTemplate<T> {

	private final SqlExecutor sqlExecutor;
	private final SqlObjectConfig config;
	private final SqlObjectDefinition<T> definition;
	private final SelectBuilderFactory<T> selectBuilderFactory;
	private final UpdateBuilderFactory<T> updateBuilderFactory;
	private final SqlWriterStrategy writerStrategy;
	
	private DaoTemplate(
			SqlExecutor sqlExecutor, 
			SqlObjectConfig config, 
			SqlObjectDefinition<T> definition,
			SqlWriterStrategy writerStrategy, 
			SelectBuilderFactory<T> queryTemplate,
			UpdateBuilderFactory<T> updateBuilderFactory) {
		super();
		this.sqlExecutor = sqlExecutor;
		this.config = config;
		this.definition = definition;
		this.writerStrategy = writerStrategy;
		this.selectBuilderFactory = queryTemplate;
		this.updateBuilderFactory = updateBuilderFactory;
	}

	private LinkedHashMap<String, Object> toLinkedHashMap(T t, boolean bulkInsert) {
		LinkedHashMap<String, Object> m = config.getObjectMapper().convertObjectToSqlMap(t);
		/*
		 * Replace the complex objects with there ids.
		 */
		for(SqlParameterDefinition pd : definition.getManyToOneParameters().values()) {
			if (pd.getObjectDefinition().isPresent() 
					&& pd.getObjectDefinition().get().getObjectDefintion().idParameter().isPresent()) {
				SqlParameterDefinition idDef = 
						pd.getObjectDefinition().get().getObjectDefintion().idParameter().get();
				NestedKeyValue<Object> nkv =  ObjectMapUtils.getNestedKeyValue(m, pd.getParameterName(), idDef.getParameterName());
				if (nkv.isPresent()) {
					/*
					 * TODO: We only set it if the object is actually present. ie do you really want to set null?
					 */
					m.put(pd.getParameterName(), nkv.object);
				}
				else if (bulkInsert) {
					//TODO default annotation perhaps here?
					//http://stackoverflow.com/questions/197045/setting-default-values-for-columns-in-jpa
					m.put(pd.getParameterName(), null);
				}
			}
		}
		if (bulkInsert) {
			LinkedHashMap<String, Object> copy = new LinkedHashMap<String, Object>(definition.getIdParameters().size());
			/*
			 * Order and the number of parameters is really important for bulk insert.
			 */
			for(SqlParameterDefinition pd : definition.getParameters().values()) {
				JirmPrecondition.check.state(m.containsKey(pd.getParameterName()), 
						"Missing parameter for bulk insert: {}", pd.getParameterName());
				Object o = m.get(pd.getParameterName());
				copy.put(pd.getParameterName(), o);
			}
			m = copy;
		}
		return m;
				
	}
	
	protected SqlParameterDefinition idParameter() {
		JirmPrecondition.check.state(definition.idParameter().isPresent(), "No id parameter for : {}", 
				definition.getObjectType());
		return this.definition.idParameter().get();
	}
	
	public SelectRootClauseBuilder<SelectBuilder<T>> select() {
		return selectBuilderFactory.select();
	}
	
	public SelectRootClauseBuilder<CountBuilder<T>> count() {
		return selectBuilderFactory.count();
	}
	
	public Optional<T> findOptionalById(Object id) {
		return select().where()
				.property(idParameter().getParameterName()).eq(id)
				.query()
				.forOptional();
	}
	
	public T findById(Object id) {
		return select().where()
				.property(idParameter().getParameterName()).eq(id)
				.query()
				.forObject();
	}
	
	public void insert(T t) {
		LinkedHashMap<String, Object> m = toLinkedHashMap(t, false);
		insert(m);
	}
	
	public int update(T t) {
		
		LinkedHashMap<String, Object> m = toLinkedHashMap(t, false);
		LinkedHashMap<String, Object> where = newLinkedHashMap();
		for (Entry<String, Object> e : m.entrySet()) {
			if (definition.getIdParameters().containsKey(e.getKey())) {
				where.put(e.getKey(), e.getValue());
			}
		}
		checkState(! where.isEmpty());
		JirmPrecondition.check.state(!where.isEmpty(), "where should not be empty");
		return update(m, where);
	}
	
	public int update(Map<String,Object> setValues, Map<String, Object> filters) {
		return updateBuilderFactory
				.update()
				.setAll(setValues)
				.where().propertyAll(filters)
				.execute();
	}
	
	public T reload(T t) {
		LinkedHashMap<String, Object> m = toLinkedHashMap(t, false);
		Optional<SqlParameterDefinition> id = definition.idParameter();
		JirmPrecondition.check.state(id.isPresent(), "No id definition");
		Optional<Object> o = id.get().valueFrom(m);
		return findById(o.get());
	}
	
	public void insert(Map<String,Object> values) {
		StringBuilder qb = new StringBuilder();
		writerStrategy.insertStatement(qb, definition, values);
		sqlExecutor.update(qb.toString(), values.values().toArray());
	}
	
	public void insert(Iterator<T> values, int batchSize) {
		Iterator<Map<String,Object>> t = Iterators.transform(values, new Function<T, Map<String,Object>>() {
			@Override
			public Map<String, Object> apply(T input) {
				return toLinkedHashMap(input, true);
			}
		});
		insertMaps(t, batchSize);
	}
	
	public void insertMaps(Iterator<Map<String,Object>> values, int batchSize) {
		if (! values.hasNext() ) return;
		PeekingIterator<Map<String,Object>> vs = peekingIterator(values);
		Map<String,Object> first = vs.peek();
		final String sql = writerStrategy.insertStatement(new StringBuilder(), definition, first).toString();
		ImmutableList<String> keys = ImmutableList.copyOf(vs.peek().keySet());
		Iterator<List<Map<String,Object>>> it = partition(vs, batchSize);

		while (it.hasNext()) {
			List<Map<String,Object>> batch = it.next();
			final List<Object[]> batchValues = Lists.newArrayListWithExpectedSize(batch.size());
			for (Map<String,Object> b : batch) {
				ImmutableList<String> actualKeys = ImmutableList.copyOf(b.keySet());
				JirmPrecondition.check.state(actualKeys.equals(keys), "Keys don't match up to {} for {}", keys, actualKeys);
				batchValues.add(writerStrategy.fillValues(definition, b).toArray());
			}
			/*
			 * TODO this will keep making a prepared statementS.
			 * Hopefully the JDBC driver has some caching for this.
			 */
			sqlExecutor.batchUpdate(sql, batchValues);
		}
		
	}
}
