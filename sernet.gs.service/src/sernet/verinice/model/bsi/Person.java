/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bsi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.IPerson;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.verinice.model.common.CnATreeElement;

public class Person extends CnATreeElement implements IBSIStrukturElement, IPerson {

    private static final Logger log = Logger.getLogger(Person.class);

    public static final String PROP_TAG = "person_tag"; //$NON-NLS-1$
    public static final String P_ANREDE = "person_anrede"; //$NON-NLS-1$
    public static final String P_NAME = "nachname"; //$NON-NLS-1$
    public static final String P_VORNAME = "vorname"; //$NON-NLS-1$
    public static final String PROP_KUERZEL = "person_kuerzel"; //$NON-NLS-1$
    public static final String P_ROLLEN = "person_rollen"; //$NON-NLS-1$
    public static final String P_EMAIL = "person_email"; //$NON-NLS-1$
    public static final String P_PHONE = "person_telefon"; //$NON-NLS-1$
    public static final String P_ORGEINHEIT = "person_orgeinheit"; //$NON-NLS-1$

    // ID must correspond to entity definition in entitytype XML description:
    public static final String TYPE_ID = "person"; //$NON-NLS-1$
    public static final String PROP_ERLAEUTERUNG = "person_erlaeuterung"; //$NON-NLS-1$
    public static final String PROP_ANZAHL = "person_anzahl"; //$NON-NLS-1$

    public Person(CnATreeElement parent) {
        super(parent);
        setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
        // sets the localized name via HUITypeFactory from message bundle
        getEntity().setSimpleValue(getEntityType().getPropertyType(P_NAME),
                getTypeFactory().getMessage(TYPE_ID));
    }

    @Override
    public String getKuerzel() {
        return getEntity().getSimpleValue(PROP_KUERZEL);
    }

    @Override
    public Collection<? extends String> getTags() {
        return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
    }

    protected Person() {

    }

    @Override
    public int getSchicht() {
        return 0;
    }

    @Override
    public void setTitel(String name) {
        // empty, otherwise title get scrambled while copying, bug 264
    }

    @Override
    public String getTitle() {
        if (getEntity() == null) {
            return ""; //$NON-NLS-1$
        }
        return getTitel(getEntity());
    }

    public static String getTitel(Entity entity) {
        if (entity == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer buff = new StringBuffer();
        buff.append(entity.getSimpleValue(P_VORNAME));
        if (buff.length() > 0) {
            buff.append(" "); //$NON-NLS-1$
        }
        buff.append(entity.getSimpleValue(P_NAME));

        String rollen = getRollen(entity);
        if (rollen.length() > 0) {
            buff.append(" [" + rollen + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return buff.toString();
    }

    public String getFullName() {
        if (getEntity() == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer buff = new StringBuffer();
        buff.append(getEntity().getSimpleValue(P_VORNAME));
        if (buff.length() > 0) {
            buff.append(" "); //$NON-NLS-1$
        }
        buff.append(getEntity().getSimpleValue(P_NAME));

        return buff.toString();
    }

    public String getNachname() {
        return getEntity().getSimpleValue(P_NAME);
    }

    public String getAnrede() {
        return getEntity().getSimpleValue(P_ANREDE);
    }

    private String getRollen() {
        if (getEntity() == null) {
            return ""; //$NON-NLS-1$
        }
        return getRollen(getEntity());
    }

    private static String getRollen(Entity entity) {
        if (entity == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer buf = new StringBuffer();
        PropertyList propertyList = entity.getProperties(P_ROLLEN);
        PropertyType type = HUITypeFactory.getInstance().getPropertyType(TYPE_ID, P_ROLLEN);
        List<Property> properties = propertyList.getProperties();

        if (properties == null)
            return ""; //$NON-NLS-1$

        for (Iterator iter = properties.iterator(); iter.hasNext();) {
            Property prop = (Property) iter.next();
            String rolle = type.getOption(prop.getPropertyValue()).getName();
            buf.append(rolle);
            if (iter.hasNext()) {
                buf.append(", "); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public void addChild(CnATreeElement child) {
        // Person doesn't have children
    }

    public void setErlaeuterung(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ERLAEUTERUNG), name);
    }

    public String getErlaeuterung() {
        return getEntity().getSimpleValue(PROP_ERLAEUTERUNG);
    }

    public void setKuerzel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KUERZEL), name);
    }

    public boolean hasRole(Property role) {
        String value = role.getPropertyValue();
        if (value != null) {
            value = value.replaceAll("\u00A0", "");
            if (getRollen().indexOf(value) > -1) {
                return true;
            }
        }

        return false;
    }

    public boolean addRole(String name) {
        PropertyType propertyType = getEntityType().getPropertyType(P_ROLLEN);
        ArrayList<IMLPropertyOption> options = (ArrayList<IMLPropertyOption>) propertyType
                .getOptions();
        for (IMLPropertyOption option : options) {
            if (option.getName().equals(name)) {
                getEntity().createNewProperty(propertyType, option.getId());
                return true;
            }
        }
        return false;
    }

    public void setAnzahl(int anzahl) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ANZAHL),
                Integer.toString(anzahl));
    }

    @Override
    public String toString() {
        return "Person [getFullName()=" + getFullName() + ", getKuerzel()=" + getKuerzel()
                + ", getId()=" + getId() + "]";
    }

    @Override
    public String getFirstName() {
        return getEntity().getRawPropertyValue(P_VORNAME);
    }

    @Override
    public String getLastName() {
        return getEntity().getRawPropertyValue(P_NAME);
    }

    @Override
    public String getSalutation() {
        return getEntity().getRawPropertyValue(P_ANREDE);
    }

    @Override
    public String getEMailAddress() {
        return getEntity().getRawPropertyValue(P_EMAIL);
    }
}