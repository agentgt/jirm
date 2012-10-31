[![Build Status](https://travis-ci.org/agentgt/jirm.png)](https://travis-ci.org/agentgt/jirm)

JIRM
====

A Java Immutable object Relational Mapper focused on convenience, and thread safety. 

Inspiration
-----------

**What I wanted my ORM to do is**

 1. CRUD Immutable POJOs (that is all private fields are final with a constructor that fills them)
 1. READ hiearchy of POJOs (that is `@ManyToOne` 's are loaded eagerly) but for WRITE only write the top POJO.
 1. Once the POJO is loaded there is no magic. It is not "enhanced". It is safe to deserialize or cache. 
 1. Manually do One to Many (ie collections) which IMHO is the right way to do it (because there is nothing worse than accidentally pulling 1000 items).
 1. Use JPA annotations to help map the SQL ResultSet to your POJOs
 1. Threadsafe
 1. Stateless (like Ajave EBean... ie no session factory).
 1. Fluent API
 1. Sits nicely on top of other JDBC wrappers like Spring JDBC
 1. Compile time Transaction Support through AspectJ (Through Spring JDBC).

**JIRM does all of this and more!**

There was also some one looking for one here:
http://stackoverflow.com/questions/2698665/orm-supporting-immutable-classes

JirmDaoFactory
--------------

You need a JirmFactory to use Jirm. Right now Spring JDBC is the only implementation but it is 
trivial to support other JDBC wrappers.

Spring Config:
```xml
<bean class="co.jirm.spring.SpringJirmFactory" id="jirmFactory" />
```

Now in your Spring components you can simply do:

```java
@Autowired //or however you do your wiring.
private JirmFactory jirmFactory;
```


JirmDao
-------

The `JirmDao` allows you to CRUD immutable POJO's. Immutable POJO's have all of there member fields 
`final` and the fields themselves should be immutable objects. Immutable POJO's require a constructor to instantiate
their member fields.

Because immutable objects require constructor based loading of fields we need to make a constructor with all the 
fields from table (and/or ManyToOne child tables... more on that later).

Unfortuanately the JVM has some limitations on reflection of constructor based arguments so you will have to annotate
your constructor with either JDK's `@ConstructorProperties` or Jackson's `@JsonCreator` and `@JsonProperty`

```java
public class TestBean {
    
    @Id
    private final String stringProp;
    private final long longProp;
    @Column(name="timets")
    @NotNull
    private final Calendar timeTS;
    
    @JsonCreator
    public TestBean(
            @JsonProperty("stringProp") String stringProp, 
            @JsonProperty("longProp") long longProp,
            @JsonProperty("timeTS") Calendar timeTS ) {
        super();
        this.stringProp = stringProp;
        this.longProp = longProp;
        this.timeTS = timeTS;
    }
    
    public String getStringProp() {
        return stringProp;
    }
    public long getLongProp() {
        return longProp;
    }
    public Calendar getTimeTS() {
        return timeTS;
    }
}
```
Our SQL Table for the bean might be something like (Postgres):

```sql
CREATE TABLE test_bean
(
  string_prop text NOT NULL,
  long_prop bigint,
  timets timestamp without time zone,
  CONSTRAINT string_prop_key PRIMARY KEY (string_prop)
);
```

Lets see some CRUD of our immutable `TestBean`

```java

JirmDao<TestBean> dao = daoFactory.daoFor(TestBean.class);

List<TestBean> list = 
    dao.select().where()
    .property("longProp", 1L)
    .property("stringProp").eq("blah")
    .limit(100)
    .offset(10)
    .query()
    .forList();

// You can also insert, delete, update, etc...
String id = randomId();
TestBean testBean = new TestBean(id, 1L, Calendar.getInstance());

//insert
dao.insert(testBean);
//reload
TestBean reload = dao.findById(id);
//or
reload = dao.reload(testBean);

//Explictly update one field.
dao.update()
   .set("longProp", 100L)
   .where().property("stringProp").eq(id)
   .execute();

//Of course you could update the entire object which will take advantage 
//of opportunistic locking if you have @Version

TestBean updateBean = new TestBean(testBean.getId(), 2L, Calendar.getInstance());
dao.update(updateBean);

//delete
dao.deleteById(id);

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

Notice in the above I set the parameters by position (`with` as opposed to `set`). 
In some cases that might be more convenient.
