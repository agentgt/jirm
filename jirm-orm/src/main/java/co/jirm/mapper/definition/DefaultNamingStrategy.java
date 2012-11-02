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
//$Id: ImprovedNamingStrategy.java 9941 2006-05-22 18:28:53Z epbernard $
package co.jirm.mapper.definition;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.Serializable;


public class DefaultNamingStrategy implements NamingStrategy, Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * A convenient singleton instance
	 */
	public static final NamingStrategy INSTANCE = new DefaultNamingStrategy();

	/**
	 * Return the unqualified class name, mixed case converted to
	 * underscores
	 */
	public String classToTableName(String className) {
		return addUnderscores( unqualify(className) );
	}
	/**
	 * Return the full property path with underscore seperators, mixed
	 * case converted to underscores
	 */
	public String propertyToColumnName(String propertyName) {
		return addUnderscores( unqualify(propertyName) );
	}
	/**
	 * Convert mixed case to underscores
	 */
	public String tableName(String tableName) {
		return addUnderscores(tableName);
	}
	/**
	 * Convert mixed case to underscores
	 */
	public String columnName(String columnName) {
		return addUnderscores(columnName);
	}

	protected static String addUnderscores(String name) {
		StringBuilder buf = new StringBuilder( name.replace('.', '_') );
		for (int i=1; i<buf.length()-1; i++) {
			if (
				Character.isLowerCase( buf.charAt(i-1) ) &&
				Character.isUpperCase( buf.charAt(i) ) &&
				Character.isLowerCase( buf.charAt(i+1) )
			) {
				buf.insert(i++, '_');
			}
		}
		return buf.toString().toLowerCase();
	}

	public String collectionTableName(
			String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable,
			String propertyName
	) {
		return tableName( ownerEntityTable + '_' + propertyToColumnName(propertyName) );
	}

	/**
	 * Return the argument
	 */
	public String joinKeyColumnName(String joinedColumn, String joinedTable) {
		return columnName( joinedColumn );
	}

	/**
	 * Return the property name or propertyTableName
	 */
	public String foreignKeyColumnName(
			String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName
	) {
		String header = propertyName != null ? unqualify( propertyName ) : propertyTableName;
		
		checkState(header != null, "NamingStrategy not properly filled");
		
		return columnName( header ); //+ "_" + referencedColumnName not used for backward compatibility
	}

	/**
	 * Return the column name or the unqualified property name
	 */
	public String logicalColumnName(String columnName, String propertyName) {
		
		return ! isNullOrEmpty( columnName ) ? columnName : unqualify( propertyName );
	}

	/**
	 * Returns either the table name if explicit or
	 * if there is an associated table, the concatenation of owner entity table and associated table
	 * otherwise the concatenation of owner entity table and the unqualified property name
	 */
	public String logicalCollectionTableName(String tableName,
											 String ownerEntityTable, String associatedEntityTable, String propertyName
	) {
		if ( tableName != null ) {
			return tableName;
		}
		else {
			//use of a StringBuilder to workaround a JDK bug
			return new StringBuffer(ownerEntityTable).append("_")
					.append(
						associatedEntityTable != null ?
						associatedEntityTable :
						unqualify( propertyName )
					).toString();
		}
	}
	/**
	 * Return the column name if explicit or the concatenation of the property name and the referenced column
	 */
	public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
		return isNullOrEmpty( columnName ) ?
				columnName :
				unqualify( propertyName ) + "_" + referencedColumn;
	}
	
	public static String unqualify(String qualifiedName) {
		int loc = qualifiedName.lastIndexOf(".");
		return ( loc < 0 ) ? qualifiedName : qualifiedName.substring( qualifiedName.lastIndexOf(".") + 1 );
	}

	public static String qualifier(String qualifiedName) {
		int loc = qualifiedName.lastIndexOf(".");
		return ( loc < 0 ) ? "" : qualifiedName.substring( 0, loc );
	}

}