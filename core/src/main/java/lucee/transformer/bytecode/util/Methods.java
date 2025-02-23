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
package lucee.transformer.bytecode.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public final class Methods {

	// Caster String
	final public static Method METHOD_TO_STRING = new Method("toString", Types.STRING, new Type[] { Types.OBJECT });
	// final public static Method METHOD_TO_STRING_FROM_STRING = new Method("toString",Types.STRING,new
	// Type[]{Types.STRING});

	final public static Method METHOD_TO_STRING_FROM_DOUBLE_VALUE = new Method("toString", Types.STRING, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_STRING_FROM_NUMBER = new Method("toString", Types.STRING, new Type[] { Types.NUMBER });
	final public static Method METHOD_TO_STRING_FROM_BOOLEAN = new Method("toString", Types.STRING, new Type[] { Types.BOOLEAN_VALUE });

	// Caster Boolean
	// Boolean toBoolean (Object)
	final public static Method METHOD_TO_BOOLEAN = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.OBJECT });
	// boolean toBooleanValue (Object)
	final public static Method METHOD_TO_BOOLEAN_VALUE = new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.OBJECT });

	// Boolean toBoolean (double)
	final public static Method METHOD_TO_BOOLEAN_FROM_DOUBLE_VALUE = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_BOOLEAN_FROM_NUMBER = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.NUMBER });

	// boolean toBooleanValue (double)
	final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_DOUBLE_VALUE = new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_NUMBER = new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.NUMBER });

	// Boolean toBoolean (boolean)
	final public static Method METHOD_TO_BOOLEAN_FROM_BOOLEAN = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.BOOLEAN_VALUE });
	// boolean toBooleanValue (boolean)
	// final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_BOOLEAN = new
	// Method("toBooleanValue",Types.BOOLEAN_VALUE,new Type[]{Types.BOOLEAN_VALUE});

	// Boolean toBoolean (String)
	final public static Method METHOD_TO_BOOLEAN_FROM_STRING = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.STRING });
	// boolean toBooleanValue (String)
	final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_STRING = new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.STRING });

	// Caster Double
	final public static Method METHOD_TO_DOUBLE = new Method("toDouble", Types.DOUBLE, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_NUMBER = new Method("toNumber", Types.NUMBER, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_FLOAT = new Method("toFloat", Types.FLOAT, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_INTEGER = new Method("toInteger", Types.INTEGER, new Type[] { Types.OBJECT });

	final public static Method METHOD_TO_DOUBLE_VALUE = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_FLOAT_VALUE = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_INT_VALUE = new Method("toIntValue", Types.FLOAT_VALUE, new Type[] { Types.OBJECT });

	final public static Method METHOD_TO_INTEGER_FROM_INT = new Method("toInteger", Types.INTEGER, new Type[] { Types.INT_VALUE });
	final public static Method METHOD_TO_LONG_FROM_LONG_VALUE = new Method("toLong", Types.LONG, new Type[] { Types.LONG_VALUE });

	final public static Method METHOD_TO_DOUBLE_FROM_DOUBLE_VALUE = new Method("toDouble", Types.DOUBLE, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_DOUBLE_FROM_FLOAT_VALUE = new Method("toDouble", Types.DOUBLE, new Type[] { Types.FLOAT_VALUE });
	final public static Method METHOD_TO_FLOAT_FROM_DOUBLE = new Method("toFloat", Types.FLOAT, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_FLOAT_FROM_NUMBER = new Method("toFloat", Types.FLOAT, new Type[] { Types.NUMBER });

	final public static Method METHOD_TO_FLOAT_FROM_FLOAT = new Method("toFloat", Types.FLOAT, new Type[] { Types.FLOAT_VALUE });
	// final public static Method METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE = new
	// Method("toDoubleValue",Types.DOUBLE_VALUE,new Type[]{Types.DOUBLE_VALUE});
	final public static Method METHOD_TO_FLOAT_VALUE_FROM_DOUBLE = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_FLOAT_VALUE_FROM_NUMBER = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.NUMBER });

	final public static Method METHOD_TO_INT_VALUE_FROM_DOUBLE_VALUE = new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_INTEGER_FROM_DOUBLE_VALUE = new Method("toInteger", Types.INTEGER, new Type[] { Types.DOUBLE_VALUE });

	final public static Method METHOD_TO_NUMBER_FROM_BOOLEAN_VALUE = new Method("toNumber", Types.NUMBER, new Type[] { Types.BOOLEAN_VALUE });
	final public static Method METHOD_TO_NUMBER_FROM_DOUBLE_VALUE = new Method("toNumber", Types.NUMBER, new Type[] { Types.DOUBLE_VALUE });

	final public static Method METHOD_TO_DOUBLE_FROM_BOOLEAN_VALUE = new Method("toDouble", Types.DOUBLE, new Type[] { Types.BOOLEAN_VALUE });
	final public static Method METHOD_TO_FLOAT_FROM_BOOLEAN_VALUE = new Method("toFloat", Types.FLOAT, new Type[] { Types.BOOLEAN_VALUE });

	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_BOOLEAN_VALUE = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.BOOLEAN_VALUE });
	final public static Method METHOD_TO_FLOAT_VALUE_FROM_BOOLEAN_VALUE = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.BOOLEAN_VALUE });

	final public static Method METHOD_TO_INT_VALUE_FROM_BOOLEAN_VALUE = new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.BOOLEAN_VALUE });
	final public static Method METHOD_TO_INTEGER_FROM_BOOLEAN_VALUE = new Method("toInteger", Types.INTEGER, new Type[] { Types.BOOLEAN_VALUE });

	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.DOUBLE });
	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_NUMBER = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.NUMBER });
	final public static Method METHOD_TO_DOUBLE_FROM_NUMBER = new Method("toDouble", Types.DOUBLE, new Type[] { Types.NUMBER });

	final public static Method METHOD_TO_DOUBLE_FROM_STRING = new Method("toDouble", Types.DOUBLE, new Type[] { Types.STRING });
	final public static Method METHOD_TO_BIG_DECIMAL_FROM_STRING = new Method("toBigDecimal", Types.BIG_DECIMAL, new Type[] { Types.STRING });
	final public static Method METHOD_TO_NUMBER_FROM_PC_STRING = new Method("toNumber", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.STRING });
	final public static Method METHOD_TO_FLOAT_FROM_STRING = new Method("toFloat", Types.FLOAT, new Type[] { Types.STRING });
	final public static Method METHOD_TO_INTEGER_FROM_STRING = new Method("toInteger", Types.INTEGER, new Type[] { Types.STRING });

	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_PC_STRING = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.STRING });
	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_STRING = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.STRING });
	final public static Method METHOD_TO_FLOAT_VALUE_FROM_STRING = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.STRING });
	final public static Method METHOD_TO_INT_VALUE_FROM_STRING = new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.STRING });

	final public static Method METHOD_TO_BIG_DECIMAL_STR = new Method("toBigDecimal", Types.BIG_DECIMAL, new Type[] { Types.STRING });
	final public static Method METHOD_TO_BIG_DECIMAL_OBJ = new Method("toBigDecimal", Types.BIG_DECIMAL, new Type[] { Types.OBJECT });
	final public static Method METHOD_NEGATE_NUMBER = new Method("negate", Types.NUMBER, new Type[] { Types.NUMBER });

}