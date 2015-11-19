[![Build Status](https://travis-ci.org/agentgt/jirm.png)](https://travis-ci.org/agentgt/jirm)
JIRM
====

**J**ava **I**mmutable object **R**elational **M**apper is a unique Java SQL ORM that allows you 
to CRUD [**immutable objects**](http://docs.oracle.com/javase/tutorial/essential/concurrency/immutable.html).
It is a good match for highly concurrent message driven architectures.

Inspiration
-----------

**How JIRM is different**

 1. CRUD truly **Immutable** POJOs. That is all fields are final with a constructor that fills them.
 1. Uses [JPA annotations](https://github.com/agentgt/jirm/tree/master/jirm-orm#supported-jpa-annotations) to help map the SQL ResultSet to your POJOs.
 1. READs hierarchy of Immutable POJOs. That is `@ManyToOne`s are loaded eagerly, but for WRITE we only write the top POJO.
 1. Once the POJO is loaded there is no magic. It is not "enhanced". It is safe to deserialize or cache especially because they are immutable.
 1. Manually do one-to-many (i.e. collections) which IMHO is the right way to do it (because there is nothing worse than accidentally pulling 1000 items).
 1. No hidden lazy loading.
 1. Threadsafe - Most of the library is threadsafe.
 1. Stateless (like Ajave EBean... i.e. no session factory).
 1. Fluent API.
 1. Or you can use SQL with IMHO the best [**SQL Placeholder templates**](https://github.com/agentgt/jirm/tree/master/jirm-core#sql-placeholder-parser).
 1. Sits nicely on top of other JDBC wrappers like [**Spring JDBC**](http://static.springsource.org/spring/docs/3.0.x/reference/jdbc.html).
 1. Let your JDBC wrapper handle transactions - e.g. compile time Transaction Support through AspectJ (Through Spring JDBC).
 
 
**JIRM does all of this and more!**

There was also someone looking for one here:
http://stackoverflow.com/questions/2698665/orm-supporting-immutable-classes

Install
-------

The current version of jirm in the maven central repository is: `0.0.8`

If you would like full usage of the ORM, Spring is *currently* the only JDBC Wrapper supported.

```xml
<dependency>
    <groupId>co.jirm</groupId>
    <artifactId>jirm-spring</artifactId>
    <version>${jirm.version}</version>
</dependency>
```

Alternatively if you want to use only the [SQL Placeholder templates](https://github.com/agentgt/jirm/tree/master/jirm-core/README.md)
you only need `jirm-core`.

JirmFactory
-----------

You need a JirmFactory to use Jirm. Right now Spring JDBC is the only implementation but it is 
trivial to support other JDBC wrappers by implementing `SqlExecutor` interface.

*Why choose Spring JDBC?* - Because it's an extremely mature JDBC wrapper that does most things correctly 
(exception handling and transactions).

Spring Config:
```xml
<bean class="co.jirm.spring.SpringJirmFactory" id="jirmFactory" />
```

Now in your Spring components you can simply do:

```java
@Autowired //or however you do your wiring.
private JirmFactory jirmFactory;
```

Now you can create a *threadsafe* `JirmDao` for your Immutable POJO like:

```java
   JirmDao<MyBean> dao = jirmFactory.daoFor(MyBean.class);
```

JirmDao
-------

The `JirmDao` allows you to CRUD immutable POJOs. Immutable POJOs have all of their member fields 
`final` and the fields themselves should be immutable objects. Immutable POJOs require a constructor to instantiate
their member fields.

Because immutable objects require constructor based loading of fields we need to make a constructor with all the 
fields from table (and/or many-to-one child tables... more on that later).

Unfortunately the JVM has some limitations on reflection of constructor based arguments so you will have to annotate
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

Let's see some CRUD of our immutable `TestBean`

```java

JirmDao<TestBean> dao = daoFactory.daoFor(TestBean.class);

// You can insert, delete, update, etc...
String id = randomId();
TestBean testBean = new TestBean(id, 1L, Calendar.getInstance());

//insert
dao.insert(testBean);

//Or batch insert 200 beans at a time
Iterator<TestBean> testBeanIterator = other.iterator();
dao.insert(testBeanIterator, 200);

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
dao.update(updateBean).execute();
//Or exclude a field from update
dao.update(updateBean).exclude("longProp").execute();

//delete
dao.deleteById(id);
//or
dao.delete()
   .where().property("stringProp").eq(id)
   .execute();

//Now let's select some TestBeans.
List<TestBean> list = 
    dao.select().where()
    .property("longProp", 1L)
    .property("stringProp").eq("blah")
	.orderBy("longProp").desc()
    .limit(100)
    .offset(10)
    .query()
    .forList();
```

JIRM embraces SQL
-----------------

**When the going gets tough JIRM says write SQL.** 

JIRM's select, update, and delete builders (fluent api) are generally for convenience of simply tasks.
That is because update, delete and insert are pretty simple one table operations.
Also most selecting only requires inner joining `@ManyToOne` children.

As soon as it gets complicated we recommend you write SQL. Luckily JIRM provides awesome support for that.

Besides `TestBean` mentioned earlier lets say we have another table/object called `ParentBean`.
```java
public class ParentBean {
    @Id
    private final String id;
    @ManyToOne(targetEntity=TestBean.class, fetch=FetchType.EAGER)
    private final TestBean test;
    //... snip ...
}
```

Now we can select `ParentBean` using plain SQL by making a SQL file in the class path we'll call it `select-parent-bean.sql`.

```sql
SELECT parent_bean.id    AS "id", 
       test.string_prop  AS "test.stringProp", 
       test.long_prop    AS "test.longProp",
       test.timets       AS "test.timeTS"
FROM parent_bean 
INNER JOIN test_bean test ON test.string_prop = parent_bean.test 
WHERE test.string_prop = 'test' -- {testName}
AND test.long_prop = 100 -- {testAmount}
```

Yes the above is real SQL without any placeholders that would break normal SQL parsing.
We use comments on the end of the line to indicate a place holder. You can read more about it
[here](https://github.com/agentgt/jirm/tree/master/jirm-core#sql-placeholder-parser).

Besides the comment placeholders the other thing to notice is the use of result column labels for property paths.
By using `AS "dottedPropertyPath"` gives JIRM clues on how to map the *flat* `ResultSet` back to a *hierarchical* object. 

Now here is the Java:

```java
JirmDao<ParentBean> dao = jirmFactory.daoFor(ParentBean.class);

List<ParentBean> results = 
   dao.getSelectBuilderFactory().sqlFromResource("select-parent-bean.sql")
      .bind("testName", "test")
      .bind("testAmount", 100)
      .query()
      .forList();
```
 
JIRM's [SQL Placeholder Parser](https://github.com/agentgt/jirm/tree/master/jirm-core#sql-placeholder-parser) 
can also be used independently of JIRM's ORM functionality.


