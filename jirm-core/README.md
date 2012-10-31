JIRM Core
=========

Jirm core has some useful utilities with minimal dependencies.
Most notable is the SQL Placeholder Parser.

**You do not need all of JIRM to use the Parser**

```xml
<dependency>
    <groupId>co.jirm</groupId>
    <artifactId>jirm-core</artifactId>
    <version>${jirm.version}</version>
</dependency>
```

SQL Placeholder parser
----------------------

JIRM has a simple but powerful placeholder parser that allows you to use **REAL** SQL.
Its best to understand how the parser works by example.

Lets say we have a SQL file that its in the same Java package as TestBean.class called: `select-test-bean.sql`

```sql
SELECT * from test_bean
WHERE stringProp like '%Adam%' -- {name}
LIMIT 1 -- {limit}
```

**Notice how thats real SQL.** Its Not SQL with placeholders like `?` or `:name` or `#{}`.
You can copy and paste it into any SQL query tool and it will work.

Yes those comments at the end of the lines are special:

```java
PlainSql sql = PlainSql.fromResource(TestBean.class, "select-test-bean.sql")
		.bind("name", "Adam")
		.bind("limit", 1);
assertEquals(ImmutableList.<Object>of("Adam", 1), sql.mergedParameters());
assertEquals(
		"SELECT * from test_bean\n" + 
		"WHERE stringProp like ? \n" + 
		"LIMIT ? ", sql.getSql());
```

The generated SQL and parameters can be used with any JDBC library/driver.

So in spring you could do something like:

```java
JdbcTemplate template = new JdbcTemplate(dataSource)
PlainSql sql = PlainSql.fromResource(TestBean.class, "select-test-bean.sql")
    .with("Adam")
    .with(1);

template.queryForList(sql.getSql(), sql.mergedParameters());
```

Notice in the above I set the parameters by position (`with` as opposed to `bind`). 
In some cases that might be more convenient.
