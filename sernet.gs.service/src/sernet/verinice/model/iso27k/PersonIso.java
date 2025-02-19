/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.Collection;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.IPerson;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class PersonIso extends CnATreeElement
        implements IISO27kElement, IPerson, IAbbreviatedElement, ITaggableElement {

    public static final String TYPE_ID = "person-iso"; //$NON-NLS-1$
    public static final String PROP_ABBR = "person_abbr"; //$NON-NLS-1$
    public static final String PROP_SURNAME = "person-iso_surname"; //$NON-NLS-1$
    public static final String PROP_NAME = "person-iso_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "person-iso_tag"; //$NON-NLS-1$
    public static final String PROP_TELEFON = "person-iso_telefon"; //$NON-NLS-1$
    public static final String PROP_EMAIL = "person-iso_email"; //$NON-NLS-1$
    public static final String PROP_ANREDE = "person-iso_anrede"; //$NON-NLS-1$

    /**
     * Creates an empty asset
     */
    public PersonIso() {
        super();
        setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
    }

    public PersonIso(CnATreeElement parent) {
        super(parent);
        setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setSurname(getTypeFactory().getMessage(TYPE_ID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
     */
    @Override
    public String getTitle() {
        StringBuilder sb = new StringBuilder();
        final String surname = getEntity().getSimpleValue(PROP_SURNAME);
        if (surname != null && !surname.isEmpty()) {
            sb.append(surname);
        }
        final String name = getEntity().getSimpleValue(PROP_NAME);
        if (name != null && !name.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }

    public void setTitel(String name) {
        // empty, otherwise title get scrambled while copying, bug 264
    }

    public String getSurname() {
        return getEntity().getSimpleValue(PROP_SURNAME);
    }

    public void setSurname(String surname) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_SURNAME), surname);
    }

    public String getName() {
        return getEntity().getSimpleValue(PROP_NAME);
    }

    public void setName(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    public String getFullName() {
        if (getEntity() == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer sb = new StringBuffer();
        sb.append(getEntity().getSimpleValue(PROP_NAME));
        if (sb.length() > 0) {
            sb.append(" "); //$NON-NLS-1$
        }
        sb.append(getEntity().getSimpleValue(PROP_SURNAME));

        return sb.toString();
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
    }

    @Override
    public String getAbbreviation() {
        return getEntity().getSimpleValue(PROP_ABBR);
    }

    public void setAbbreviation(String abbreviation) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
    }

    public String getPhone() {
        return getEntity().getSimpleValue(PROP_TELEFON);
    }

    public void setPhone(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_TELEFON), value);
    }

    public String getEmail() {
        return getEntity().getSimpleValue(PROP_EMAIL);
    }

    public void setEmail(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_EMAIL), value);
    }

    public String getAnrede() {
        return getEntity().getSimpleValue(PROP_ANREDE);
    }

    @Override
    public String getFirstName() {
        return getEntity().getRawPropertyValue(PROP_NAME);
    }

    @Override
    public String getLastName() {
        return getEntity().getRawPropertyValue(PROP_SURNAME);
    }

    @Override
    public String getSalutation() {
        return getEntity().getRawPropertyValue(PROP_ANREDE);
    }

    @Override
    public String getEMailAddress() {
        return getEntity().getRawPropertyValue(PROP_EMAIL);
    }

}
