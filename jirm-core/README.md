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

### Examples

Lets say we have a SQL file that its in the same Java package as TestBean.class called: `select-test-bean.sql`

```sql
SELECT * from test_bean
WHERE stringProp like '%Adam%' -- {name}
LIMIT 1 -- {limit}
```

**Notice how thats real SQL.** Its Not SQL with placeholders like `?` or `:name` or `#{}`.
You can copy and paste it into any SQL query tool and it will work.

*Yes those comments at the end of the lines are special*:

```java
PlainSql sql = PlainSql.fromResource(TestBean.class, "select-test-bean.sql")
		.bind("name", "Adam")
		.bind("limit", 1);
assertEquals(ImmutableList.<Object>of("Adam", 1), sql.mergedParameters());
assertEquals(
		"SELECT * from test_bean\n" + 
		"WHERE string_prop like ? \n" + 
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

The only bad news is that you will have to format the spacing of your SQL and may end 
up with more line breaks than you like but I find this generally to be benificial.

For example here is an `INSERT`:

```sql
INSERT INTO test_bean 
(string_prop, long_prop, timets)
VALUES (
'HELLO' -- {stringProp}
, 3000 -- {longProp}
, now() -- {timeTS}
)
```

Now if you add several new columns:

```sql
INSERT INTO test_bean 
(string_prop, long_prop, int_prop, float_prop, double_prop, timets)
VALUES (
'HELLO' -- {stringProp}
, 3000 -- {longProp}
, 400 -- {intProp}
, 500.0 -- {floatProp}
, 500.0 -- {doubleProp}
, now() -- {timeTS}
)
```

It scales nicely and forces your team to write consistent SQL. The number of lines is the number of placeholders.

The above in traditional JDBC:

```sql
INSERT INTO test_bean 
(string_prop, long_prop, int_prop, float_prop, double_prop, timets)
VALUES (?,?,?,?,?,?)
```

Better count those '?' carefully :)


### Parser Spec

**To formalize what the parser is doing:**

*The last literal or function with out spaces next to a comment 
at the end of line of the format `-- {}` 
or `-- {NAME}` is a placeholder. 
The literal or function will be replaced with a configurable placeholder (default is `?`).*

 * `-- {}` are positional placeholders
 * `-- {NAME}` are name based placeholders.

Currently you cannot mix and match positional and name placeholders however name 
placeholders can be programmatically used as though the were positional placeholders.


