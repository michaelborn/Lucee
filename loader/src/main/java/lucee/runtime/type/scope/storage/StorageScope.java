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
package lucee.runtime.type.scope.storage;

import lucee.runtime.config.Config;
import lucee.runtime.type.scope.SharedScope;

/**
 * scope that can be stored, in a storage
 */
public interface StorageScope extends SharedScope {

	/**
	 * @return time when the Scope last time was visited
	 */
	public abstract long lastVisit();

	public abstract String getStorageType();

	public long getLastAccess();

	public void touch();

	public boolean isExpired();

	public long getTimeSpan();

	/**
	 * store content on persistent layer
	 * 
	 * @param config config
	 */
	public void store(Config config);

	/**
	 * remove stored data from persistent layer
	 * 
	 * @param config config
	 */
	public void unstore(Config config);

	/**
	 * sets the name of the storage used, this is not the storage type!
	 * 
	 * @param storage storage name
	 */
	public void setStorage(String storage);

	/**
	 * return the name of the storage used, this is not the storage type!
	 * 
	 * @return Returns the storage name.
	 */
	public String getStorage();

	/**
	 * Specifies the time, in seconds, between client requests before the servlet container will
	 * invalidate this session. A negative time indicates the session should never timeout.
	 * 
	 * @param interval - An integer specifying the number of seconds
	 */
	public void setMaxInactiveInterval(int interval);

	/**
	 * Returns the maximum time interval, in seconds, that the servlet container will keep this session
	 * open between client accesses. After this interval, the servlet container will invalidate the
	 * session. The maximum time interval can be set with the setMaxInactiveInterval method. A negative
	 * time indicates the session should never timeout.
	 * 
	 * @return an integer specifying the number of seconds this session remains open between client
	 *         requests
	 */
	public int getMaxInactiveInterval();

	public long getCreated();

	public String generateToken(String key, boolean forceNew);

	public boolean verifyToken(String token, String key);
}