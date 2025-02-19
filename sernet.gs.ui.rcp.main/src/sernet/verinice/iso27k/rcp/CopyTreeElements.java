/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import sernet.verinice.iso27k.service.CopyService;
import sernet.verinice.iso27k.service.IProgressObserver;
import sernet.verinice.iso27k.service.IProgressTask;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.IProgressRunnable;
import sernet.verinice.service.commands.CopyLinksCommand;
import sernet.verinice.service.commands.CopyLinksCommand.CopyLinksMode;

/**
 * Operation with copies elements and adds them to a group.
 * 
 * Operation is executed as task in a {@link IProgressMonitor}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyTreeElements implements IProgressRunnable {

    private IProgressTask service;

    private final CnATreeElement selectedGroup;

    private final List<CnATreeElement> elements;

    private List<String> newElements;

    private final CopyLinksCommand.CopyLinksMode copyLinksMode;

    private boolean copyAttachments = false;

    public CopyTreeElements(final CnATreeElement selectedGroup, final List<CnATreeElement> elements,
            final CopyLinksCommand.CopyLinksMode copyLinksMode) {
        this.selectedGroup = selectedGroup;
        this.elements = elements;
        this.copyLinksMode = copyLinksMode;
    }

    /*
     * @see
     * org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.
     * runtime.IProgressMonitor)
     */
    @Override
    public void run(final IProgressMonitor monitor) {
        final IProgressObserver progressObserver = new RcpProgressObserver(monitor);
        service = new CopyService(progressObserver, this.selectedGroup, elements, copyLinksMode);
        ((CopyService) service).setCopyAttachments(isCopyAttachments());
        service.run();
        newElements = ((CopyService) service).getNewElements();
    }

    /**
     * @return
     */
    @Override
    public int getNumberOfElements() {
        int n = 0;
        if (service != null) {
            n = service.getNumberOfElements();
        }
        return n;
    }

    /*
     * @see sernet.verinice.rcp.IProgressRunnable#openInformation()
     */
    @Override
    public void openInformation() {
        // TODO Auto-generated method stub

    }

    public List<String> getNewElements() {
        return newElements;
    }

    /**
     * @return the copyAttachments
     */
    public boolean isCopyAttachments() {
        return copyAttachments;
    }

    /**
     * @param copyAttachments
     *            the copyAttachments to set
     */
    public void setCopyAttachments(final boolean copyAttachments) {
        this.copyAttachments = copyAttachments;
    }

}
