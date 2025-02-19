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

import java.util.Collection;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;

public class Gebaeude extends CnATreeElement
implements IBSIStrukturElement {
    
    private static final Logger log = Logger.getLogger(Gebaeude.class);

	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "gebaeude"; //$NON-NLS-1$
	
	public static final String PROP_NAME = "gebaeude_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "gebaeude_kuerzel"; //$NON-NLS-1$
	public static final String PROP_TAG			= "gebaeude_tag"; //$NON-NLS-1$
	
	public static final String PROP_VERTRAULICHKEIT = "gebaeude_vertraulichkeit"; //$NON-NLS-1$
	public static final String PROP_VERFUEGBARKEIT = "gebaeude_verfuegbarkeit"; //$NON-NLS-1$
	public static final String PROP_INTEGRITAET = "gebaeude_integritaet"; //$NON-NLS-1$

	public static final String PROP_ERLAEUTERUNG = "gebaeude_erlaeuterung"; //$NON-NLS-1$

	private static final String PROP_ANZAHL = "gebaeude_anzahl"; //$NON-NLS-1$
	
    public static final String PROP_ESA_ENTSCHEIDUNG_DURCH = "gebaeude_ergaenzendeanalyse_entscheidung_durch"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_AM = "gebaeude_ergaenzendeanalyse_entscheidung_am"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_BIS = "gebaeude_ergaenzendeanalyse_entscheidung_bis"; //$NON-NLS-1$
	
	
	private final IReevaluator schutzbedarfProvider 
	= new ProtectionRequirementsAdapter(this);


	private final ILinkChangeListener linkChangeListener
	= new MaximumProtectionRequirementsListener(this);

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Gebaeude(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
    }
	
	@Override
    public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	
	@Override
    public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	@Override
    public int getSchicht() {
		return 2;
	}
	
	protected Gebaeude() {
		
	}
	
	@Override
	public String getTitle() {
		return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return CnaStructureHelper.canContain(obj);
	}

	@Override
    public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	@Override
	public ILinkChangeListener getLinkChangeListener() {
		return linkChangeListener;
	}

	@Override
	public IReevaluator getProtectionRequirementsProvider() {
		return schutzbedarfProvider;
	}

	public void setErlaeuterung(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ERLAEUTERUNG), name);
	}
	
	public void setKuerzel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KUERZEL), name);
	}

	public void setAnzahl(int anzahl) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ANZAHL), Integer.toString(anzahl));
	}
}
