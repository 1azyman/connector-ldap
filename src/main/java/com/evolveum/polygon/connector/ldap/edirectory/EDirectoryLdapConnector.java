/*
 * Copyright (c) 2015-2016 Evolveum
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.ConnectorClass;

import com.evolveum.polygon.connector.ldap.AbstractLdapConfiguration;
import com.evolveum.polygon.connector.ldap.AbstractLdapConnector;
import com.evolveum.polygon.connector.ldap.LdapUtil;
import com.evolveum.polygon.connector.ldap.schema.LdapFilterTranslator;
import com.evolveum.polygon.connector.ldap.schema.SchemaTranslator;

@ConnectorClass(displayNameKey = "connector.ldap.edir.display", configurationClass = EDirectoryLdapConfiguration.class)
public class EDirectoryLdapConnector extends AbstractLdapConnector<EDirectoryLdapConfiguration> {

    private static final Log LOG = Log.getLog(EDirectoryLdapConnector.class);

	@Override
	protected SchemaTranslator<EDirectoryLdapConfiguration> createSchemaTranslator() {
		return new EDirectorySchemaTranslator(getSchemaManager(), getConfiguration());
	}

	@Override
	protected LdapFilterTranslator<EDirectoryLdapConfiguration> createLdapFilterTranslator(ObjectClass ldapObjectClass) {
		return new EDirectoryLdapFilterTranslator(getSchemaTranslator(), ldapObjectClass);
	}

	@Override
	protected EDirectorySchemaTranslator getSchemaTranslator() {
		return (EDirectorySchemaTranslator)super.getSchemaTranslator();
	}
	
	@Override
	protected void addAttributeModification(Dn dn, List<Modification> modifications,
			ObjectClass ldapStructuralObjectClass,
			org.identityconnectors.framework.common.objects.ObjectClass icfObjectClass, Attribute icfAttr,
			ModificationOperation modOp) {
		if (icfAttr.is(OperationalAttributes.ENABLE_NAME)) {
			List<Object> values = icfAttr.getValue();
			if (values.size() != 1) {
				throw new InvalidAttributeValueException("Unexpected number of values in attribute "+icfAttr);
			}
			Boolean value = (Boolean)values.get(0);
			if (value) {
				modifications.add(
						new DefaultModification(modOp, EDirectoryConstants.ATTRIBUTE_LOGIN_DISABLED_NAME, 
								AbstractLdapConfiguration.BOOLEAN_FALSE));
			} else {
				modifications.add(
						new DefaultModification(modOp, EDirectoryConstants.ATTRIBUTE_LOGIN_DISABLED_NAME, 
								AbstractLdapConfiguration.BOOLEAN_TRUE));
			}
		} else if (icfAttr.is(OperationalAttributes.LOCK_OUT_NAME)) {
			List<Object> values = icfAttr.getValue();
			if (values.size() != 1) {
				throw new InvalidAttributeValueException("Unexpected number of values in attribute "+icfAttr);
			}
			Boolean value = (Boolean)values.get(0);
			if (value) {
				throw new UnsupportedOperationException("Locking object is not supported (only unlocking is)");
			}
			modifications.add(
					new DefaultModification(modOp, EDirectoryConstants.ATTRIBUTE_LOCKOUT_LOCKED_NAME, 
							AbstractLdapConfiguration.BOOLEAN_FALSE));
			modifications.add(
					new DefaultModification(modOp, EDirectoryConstants.ATTRIBUTE_LOCKOUT_RESET_TIME_NAME)); // no value

		} else if (getSchemaTranslator().isGroupObjectClass(ldapStructuralObjectClass.getName())) {
			// modification handles modification of ordinary attributes - and also modification of "member" itself
			super.addAttributeModification(dn, modifications, ldapStructuralObjectClass, icfObjectClass, icfAttr, modOp);
			if (icfAttr.is(getConfiguration().getGroupObjectMemberAttribute())) {
				if (getConfiguration().isManageEquivalenceAttributes()) {
					// do the same operation with a equivalentToMe attribute
					super.addAttributeModification(dn, modifications, ldapStructuralObjectClass, icfObjectClass,
							AttributeBuilder.build(EDirectoryConstants.ATTRIBUTE_EQUIVALENT_TO_ME_NAME, icfAttr.getValue()),
							modOp);
				}
			}
		} else {
			super.addAttributeModification(dn, modifications, ldapStructuralObjectClass, icfObjectClass, icfAttr, modOp);
		}
	}

	@Override
	protected void postUpdate(org.identityconnectors.framework.common.objects.ObjectClass icfObjectClass,
			Uid uid, Set<Attribute> values, OperationOptions options, ModificationOperation modOp, 
			Dn dn, org.apache.directory.api.ldap.model.schema.ObjectClass ldapStructuralObjectClass,
			List<Modification> modifications) {
		super.postUpdate(icfObjectClass, uid, values, options, modOp, dn, ldapStructuralObjectClass, modifications);
		if (!getConfiguration().isManageReciprocalGroupAttributes()) {
			return;
		}
		if (getSchemaTranslator().isGroupObjectClass(ldapStructuralObjectClass.getName())) {
			for (Attribute icfAttr: values) {
				if (icfAttr.is(getConfiguration().getGroupObjectMemberAttribute())) {
					for (Object val: icfAttr.getValue()) {
						Dn memberDn = getSchemaTranslator().toDn((String)val);
						List<Modification> rModifications = new ArrayList<Modification>(2);
						rModifications.add(
								new DefaultModification(modOp, EDirectoryConstants.ATTRIBUTE_GROUP_MEMBERSHIP_NAME, 
										dn.toString()));
						// No need to update securityEquals. eDirectory is doing that by itself
						// (the question is why it cannot do also to the groupMemberhip?)
//						if (getConfiguration().isManageEquivalenceAttributes()) {
//							rModifications.add(
//									new DefaultModification(modOp, EDirectoryConstants.ATTRIBUTE_SECURITY_EQUALS_NAME, 
//											dn));
//						}
						modify(memberDn, rModifications);
					}
				}
			}
		}
	}
    
}
