/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.orm;

import lucee.commons.io.res.Resource;

public interface ORMConfiguration {
	public static final int DBCREATE_NONE = 0;
	public static final int DBCREATE_UPDATE = 1;
	public static final int DBCREATE_DROP_CREATE = 2;

	public String hash();

	/**
	 * @return the autogenmap
	 */
	public boolean autogenmap();

	/**
	 * @return the catalog
	 */
	public String getCatalog();

	public String getCatalog(String datasourceName);

	/**
	 * @return the cfcLocation
	 */
	public Resource[] getCfcLocations();

	public boolean isDefaultCfcLocation();

	/**
	 * @return the dbCreate
	 */
	public int getDbCreate();

	/**
	 * Get the dbcreate setting for the given datasource
	 * 
	 * @param datasourceName
	 */
	public int getDbCreate(String datasourceName);

	/**
	 * @return the dialect
	 */
	public String getDialect();

	/**
	 * Dialect for the given datasource
	 * @param datasourceName
	 */
	public String getDialect(String datasourceName);

	/**
	 * @return the eventHandling
	 */
	public boolean eventHandling();

	public String eventHandler();

	public String namingStrategy();

	/**
	 * @return the flushAtRequestEnd
	 */
	public boolean flushAtRequestEnd();

	/**
	 * @return the logSQL
	 */
	public boolean logSQL();

	/**
	 * @return the saveMapping
	 */
	public boolean saveMapping();

	/**
	 * @return the schema
	 */
	public String getSchema();

	/**
	 * Get schema for the given datasource
	 * 
	 * @param datasourceName
	 */
	public String getSchema(String datasourceName);

	/**
	 * @return the secondaryCacheEnabled
	 */
	public boolean secondaryCacheEnabled();

	/**
	 * @return the sqlScript
	 */
	public Resource getSqlScript();

	/**
	 * Get SQL script for the given datasource
	 * 
	 * @param datasourceName
	 */
	public Resource getSqlScript(String datasourceName);

	/**
	 * @return the useDBForMapping
	 */
	public boolean useDBForMapping();

	/**
	 * @return the cacheConfig
	 */
	public Resource getCacheConfig();

	/**
	 * @return the cacheProvider
	 */
	public String getCacheProvider();

	/**
	 * @return the ormConfig
	 */
	public Resource getOrmConfig();

	public boolean skipCFCWithError();

	public boolean autoManageSession();

	public Object toStruct();

}