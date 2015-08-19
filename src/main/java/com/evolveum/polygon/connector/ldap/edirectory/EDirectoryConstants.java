/**
 * Copyright (c) 2015 Evolveum
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
package com.evolveum.polygon.connector.ldap.edirectory;

/**
 * @author semancik
 *
 */
public class EDirectoryConstants {
	
	public static final String ATTRIBUTE_LOGIN_DISABLED_NAME = "loginDisabled";
	public static final String ATTRIBUTE_LOCKOUT_LOCKED_NAME = "lockedByIntruder";
	public static final String ATTRIBUTE_LOCKOUT_RESET_TIME_NAME = "loginIntruderResetTime";
	public static final String ATTRIBUTE_GROUP_MEMBERSHIP_NAME = "groupMembership";
	public static final String ATTRIBUTE_EQUIVALENT_TO_ME_NAME = "equivalentToMe";
	public static final String ATTRIBUTE_SECURITY_EQUALS_NAME = "securityEquals";
	
	public static final String OID_NOVELL_PREFIX = "2.16.840.1.113719";
	public static final String OID_NOVELL_SYNTAX_PREFIX = OID_NOVELL_PREFIX+".1.1.5.1";
	public static final String OID_NOVELL_SYNTAX_CASE_IGNORE_LIST = OID_NOVELL_SYNTAX_PREFIX+".6";
	public static final String OID_NOVELL_SYNTAX_NETADDRESS = OID_NOVELL_SYNTAX_PREFIX+".12";
	public static final String OID_NOVELL_SYNTAX_TAGGED_STRING = OID_NOVELL_SYNTAX_PREFIX+".14";
	public static final String OID_NOVELL_SYNTAX_TAGGED_NAME_AND_STRING = OID_NOVELL_SYNTAX_PREFIX+".15";
	public static final String OID_NOVELL_SYNTAX_NDS_ACL = OID_NOVELL_SYNTAX_PREFIX+".17";
	public static final String OID_NOVELL_SYNTAX_NDS_TIMESTAMP = OID_NOVELL_SYNTAX_PREFIX+".19";
	public static final String OID_NOVELL_SYNTAX_COUNTER = OID_NOVELL_SYNTAX_PREFIX+".22";
	public static final String OID_NOVELL_SYNTAX_TAGGED_NAME = OID_NOVELL_SYNTAX_PREFIX+".23";
	public static final String OID_NOVELL_SYNTAX_TYPED_NAME = OID_NOVELL_SYNTAX_PREFIX+".25";

}
