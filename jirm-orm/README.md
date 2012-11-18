JIRM ORM
=========

Jirm uses a combination of JPA annotations and Jackson annotations.
We use Jackson annotations because Jackson is not only one of the best JSON libs it is 
also very powerful object mapper.

Supported JPA annotations
-------------------------

 * `@Id` - Only single column id.
 * `@GeneratedValue` - This can be used on `@Id` columns and non `@Id` (Jirm specific impl).
 * `@Column` - Allows you to specify the database column name.
 * `@ManyToOne` - Jirm supports many to one 
 * `@Table` - Allows you to specify the database table name.
 * `@Version` - For optimistic locking. Only numbers (`Long` and `Integer`) are supported.




