/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.io.res.util;

import java.util.regex.Pattern;

import org.apache.oro.text.regex.MalformedPatternException;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;

/**
 * Wildcard Filter
 */
public class WildCardFilter implements ResourceAndResourceNameFilter {

	private static final String specials = "{}[]().+\\^$";
	private static final boolean IS_WIN = SystemUtil.isWindows();

	private final Pattern pattern;
	private final String wildcard;
	private boolean ignoreCase;

	public WildCardFilter(String wildcard) {
		this(wildcard, IS_WIN);
	}

	/**
	 * @param wildcard
	 * @throws MalformedPatternException
	 */
	public WildCardFilter(String wildcard, boolean ignoreCase) {
		this.wildcard = wildcard;
		StringBuilder sb = new StringBuilder(wildcard.length());
		int len = wildcard.length();

		for (int i = 0; i < len; i++) {
			char c = wildcard.charAt(i);
			if (c == '*') sb.append(".*");
			else if (c == '?') sb.append('.');
			else if (specials.indexOf(c) != -1) sb.append('\\').append(c);
			else sb.append(c);
		}

		this.ignoreCase = ignoreCase;
		pattern = Pattern.compile(ignoreCase ? StringUtil.toLowerCase(sb.toString()) : sb.toString());
	}

	@Override
	public boolean accept(Resource file) {
		return pattern.matcher(ignoreCase ? StringUtil.toLowerCase(file.getName()) : file.getName()).matches();
	}

	@Override
	public boolean accept(Resource parent, String name) {
		return pattern.matcher(ignoreCase ? StringUtil.toLowerCase(name) : name).matches();
	}

	public boolean accept(String name) {
		return pattern.matcher(ignoreCase ? StringUtil.toLowerCase(name) : name).matches();
	}

	@Override
	public String toString() {
		return "Wildcardfilter:" + wildcard;
	}

}