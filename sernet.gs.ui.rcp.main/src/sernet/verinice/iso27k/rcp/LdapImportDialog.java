/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - Initial implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.IPerson;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.UsernameExistsException;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.interfaces.ldap.SizeLimitExceededException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.service.ldap.LoadLdapUser;
import sernet.verinice.service.ldap.PersonInfo;
import sernet.verinice.service.ldap.SaveLdapUser;

/**
 * Dialog that allows importing users from LDAP or Active Directory
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LdapImportDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(LdapImportDialog.class);

    private TableViewer viewer;

    private Text surname, givenName, title, department, company;

    private Set<PersonInfo> personSet;

    private Button[] radioButtonTargetPerspective = new Button[3];

    private HashMap<PersonParameter, List<PersonInfo>> ldapQueryCache;

    public LdapImportDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        personSet = new HashSet<>();
        ldapQueryCache = new HashMap<>();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        final int shellWidth = 700;
        final int shellHeight = 800;
        newShell.setText(Messages.LdapImportDialog_28);
        newShell.setSize(shellWidth, shellHeight);
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        final int gridDataHorizontalSpan = 5;

        Button buttonRemove;

        setTitle(Messages.LdapImportDialog_30);

        final int defaultMarginHeight = 10;

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layoutRoot = (GridLayout) composite.getLayout();
        layoutRoot.marginWidth = defaultMarginHeight;
        layoutRoot.marginHeight = defaultMarginHeight;
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        composite.setLayoutData(gd);

        showInformation();

        final Composite containerRoles = new Composite(composite, SWT.NONE | SWT.BORDER);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = defaultMarginHeight;
        layout.marginHeight = defaultMarginHeight;
        GridData containerGd = new GridData(GridData.GRAB_HORIZONTAL);
        containerGd.grabExcessHorizontalSpace = true;
        containerGd.grabExcessVerticalSpace = true;
        containerGd.horizontalAlignment = GridData.FILL;
        containerGd.verticalAlignment = GridData.FILL;
        containerGd.horizontalSpan = 2;
        containerRoles.setLayout(layout);
        containerRoles.setLayoutData(containerGd);

        Label givenNameLabel = new Label(containerRoles, SWT.NONE);
        givenNameLabel.setText(Messages.LdapImportDialog_0);
        givenName = new Text(containerRoles, SWT.BORDER);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        givenName.setLayoutData(gridData);

        Label nameLabel = new Label(containerRoles, SWT.NONE);
        nameLabel.setText(Messages.LdapImportDialog_31);
        surname = new Text(containerRoles, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        surname.setLayoutData(gridData);

        Label titleLabel = new Label(containerRoles, SWT.NONE);
        titleLabel.setText(Messages.LdapImportDialog_2);
        title = new Text(containerRoles, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        title.setLayoutData(gridData);

        Label departmentLabel = new Label(containerRoles, SWT.NONE);
        departmentLabel.setText(Messages.LdapImportDialog_3);
        department = new Text(containerRoles, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        department.setLayoutData(gridData);

        Label companyLabel = new Label(containerRoles, SWT.NONE);
        companyLabel.setText(Messages.LdapImportDialog_4);
        company = new Text(containerRoles, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        company.setLayoutData(gridData);

        Composite targetComposite = new Composite(containerRoles, SWT.NONE);
        GridLayout targetLayout = new GridLayout(radioButtonTargetPerspective.length + 1, false);
        targetLayout.marginWidth = defaultMarginHeight;
        targetLayout.marginHeight = defaultMarginHeight;
        targetComposite.setLayout(targetLayout);
        GridData gdTarget = new GridData(GridData.FILL_HORIZONTAL);
        gdTarget.grabExcessHorizontalSpace = true;
        gdTarget.grabExcessVerticalSpace = false;
        gdTarget.horizontalAlignment = SWT.LEFT;
        gdTarget.verticalAlignment = SWT.TOP;
        gdTarget.horizontalSpan = gridDataHorizontalSpan;
        targetComposite.setLayoutData(gdTarget);

        Label targetLabel = new Label(targetComposite, SWT.LEFT);
        targetLabel.setText(Messages.LdapImportDialog_47);
        radioButtonTargetPerspective[0] = generateButton(targetComposite, SWT.RADIO,
                Messages.LdapImportDialog_48, Boolean.FALSE);
        radioButtonTargetPerspective[1] = generateButton(targetComposite, SWT.RADIO,
                Messages.LdapImportDialog_49, Boolean.TRUE);
        radioButtonTargetPerspective[2] = generateButton(targetComposite, SWT.RADIO,
                Messages.Modernized_IT_Baseline_Protection, Boolean.FALSE);

        Button buttonAdd = new Button(containerRoles, SWT.PUSH | SWT.BORDER);
        buttonAdd.setText(Messages.LdapImportDialog_35);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.horizontalSpan = 2;
        buttonAdd.setLayoutData(gridData);
        buttonAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ldapQueryCache.containsKey(getParameter())) {
                    personSet.clear();
                    personSet.addAll(getFromCache(getParameter()));
                    refreshTable();
                } else {
                    loadLdapUser();
                }
            }

        });

        createViewer(containerRoles);

        buttonRemove = new Button(containerRoles, SWT.PUSH | SWT.BORDER);
        buttonRemove.setText(Messages.LdapImportDialog_36);
        buttonRemove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removePerson();
            }

        });
        gridData = new GridData();
        gridData.horizontalSpan = gridDataHorizontalSpan;
        gridData.horizontalAlignment = SWT.RIGHT;
        buttonRemove.setLayoutData(gridData);

        return containerRoles;
    }

    private Button generateButton(Composite composite, Integer style, String text,
            Boolean selection) {
        Button button = new Button(composite, style);
        button.setText((text != null) ? text : button.getText());
        button.setSelection((selection != null) ? selection.booleanValue() : button.getSelection());
        return button;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText(Messages.LdapImportDialog_37);
        setButtonLayoutData(ok);
    }

    /**
     * disables the assignment of a default button, so <enter> don't causes a
     * click on the ok button
     */
    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        return super.createButton(parent, id, label, false);
    }

    protected void showInformation() {
        setMessage(Messages.LdapImportDialog_38);

    }

    @SuppressWarnings("unchecked")
    protected void removePerson() {
        StructuredSelection selection = (StructuredSelection) viewer.getSelection();
        if (selection != null && !selection.isEmpty()) {
            for (Iterator<PersonInfo> iterator = selection.iterator(); iterator.hasNext();) {
                PersonInfo permission = iterator.next();
                this.personSet.remove(permission);
            }
            try {
                refreshTable();
            } catch (Exception t) {
                LOG.error("Error while setting table data", t); //$NON-NLS-1$
            }
        }
    }

    private void loadLdapUser() {
        try {
            Domain domain;
            if (radioButtonTargetPerspective[0].getSelection()) {
                domain = Domain.BASE_PROTECTION_OLD;
            } else if (radioButtonTargetPerspective[1].getSelection()) {
                domain = Domain.ISM;
            } else {
                domain = Domain.BASE_PROTECTION;
            }
            LoadLdapUser loadLdapUser = new LoadLdapUser(getParameter(), domain);
            loadLdapUser = ServiceFactory.lookupCommandService().executeCommand(loadLdapUser);
            personSet.clear();
            List<PersonInfo> personList = loadLdapUser.getPersonList();
            if (personList != null) {
                List<PersonInfo> accountList = new ArrayList<>(personList);
                personSet.addAll(accountList);
                ldapQueryCache.put(getParameter(), accountList);
            }
            // Get the content for the viewer, setInput will call getElements in
            // the
            // contentProvider
            refreshTable();
        } catch (SizeLimitExceededException sizeLimitExceededException) {
            LOG.warn("Too many results when searching for LDAP users."); //$NON-NLS-1$
            if (LOG.isDebugEnabled()) {
                LOG.debug("stacktrace: ", sizeLimitExceededException); //$NON-NLS-1$
            }
            personSet.clear();
            refreshTable();
            MessageDialog.openInformation(getShell(), Messages.LdapImportDialog_6,
                    Messages.LdapImportDialog_7);
        } catch (Exception t) {
            LOG.error("Error while setting table data", t); //$NON-NLS-1$
            personSet.clear();
            refreshTable();
            MessageDialog.openError(getShell(), Messages.LdapImportDialog_45,
                    Messages.LdapImportDialog_1);
        }
    }

    private void refreshTable() {
        PersonInfo[] personArray = personSet.toArray(new PersonInfo[personSet.size()]);
        Arrays.sort(personArray, new Comparator<PersonInfo>() {
            NumericStringComparator comparator = new NumericStringComparator();

            @Override
            public int compare(PersonInfo o1, PersonInfo o2) {
                return comparator.compare(o1.getLoginName(), o2.getLoginName());
            }
        });
        viewer.setInput(personArray);
    }

    private PersonParameter getParameter() {
        return new PersonParameter(surname.getText(), givenName.getText(), title.getText(),
                department.getText(), company.getText());
    }

    private void createViewer(Composite parent) {
        int style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
        style = style | SWT.FULL_SELECTION | SWT.BORDER;
        viewer = new TableViewer(parent, style);

        createColumns();
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());

        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
    }

    private static String getPersonSurname(CnATreeElement person) {
        if (person instanceof IPerson) {
            return ((IPerson) person).getLastName();
        }
        return "";
    }

    private static String getPersonName(CnATreeElement person) {
        if (person instanceof IPerson) {
            return ((IPerson) person).getFirstName();
        }
        return "";
    }

    private void createColumns() {
        String[] titles = { Messages.LdapImportDialog_39, Messages.LdapImportDialog_40,
                Messages.LdapImportDialog_41, Messages.LdapImportDialog_2,
                Messages.LdapImportDialog_3, Messages.LdapImportDialog_4 };
        final int[] bounds = { 80, 90, 130, 100, 100, 120 };

        // First column: login name
        TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((PersonInfo) element).getLoginName();
            }
        });

        // 2. column: name
        col = createTableViewerColumn(titles[1], bounds[1]);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return getPersonName(((PersonInfo) element).getPerson());
            }
        });

        // 3. column: surname
        col = createTableViewerColumn(titles[2], bounds[2]);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return getPersonSurname(((PersonInfo) element).getPerson());
            }
        });

        final int constant3 = 3;
        final int constant4 = 4;
        final int constant5 = 5;

        // 4. column: tile
        col = createTableViewerColumn(titles[constant3], bounds[constant3]);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((PersonInfo) element).getTitle();
            }
        });

        // 5. column: department
        col = createTableViewerColumn(titles[constant4], bounds[constant4]);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((PersonInfo) element).getDepartment();
            }
        });

        // 6. column: company
        col = createTableViewerColumn(titles[constant5], bounds[constant5]);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((PersonInfo) element).getCompany();
            }
        });
    }

    private TableViewerColumn createTableViewerColumn(String title, int bound) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;

    }

    @Override
    protected void okPressed() {
        SaveLdapUser saveLdapUser = new SaveLdapUser(personSet);
        try {
            saveLdapUser = ServiceFactory.lookupCommandService().executeCommand(saveLdapUser);
        } catch (UsernameExistsException e) {
            LOG.error(e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e); //$NON-NLS-1$
            }
            MessageDialog.openError(this.getShell(), Messages.LdapImportDialog_45,
                    NLS.bind(Messages.LdapImportDialog_46, e.getUsername()));
            return;
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        updateModel(saveLdapUser.getImportRootObject(), saveLdapUser.getChangedElements());
        super.okPressed();
        InfoDialogWithShowToggle.openInformation(Messages.LdapImportDialog_42,
                NLS.bind(Messages.LdapImportDialog_43, saveLdapUser.getChangedElements().size()),
                Messages.LdapImportDialog_44, PreferenceConstants.INFO_IMPORT_LDAP);
    }

    private static void updateModel(CnATreeElement importRootObject,
            List<CnATreeElement> changedElement) {
        final int maxNrOfElements = 9;
        if (changedElement != null && changedElement.size() > maxNrOfElements) {
            // if more than 9 elements changed or added do a complete reload
            CnAElementFactory.getInstance().reloadAllModelsFromDatabase();
        } else {
            if (importRootObject != null) {
                fireAddedEvents(importRootObject);
                if (changedElement != null) {
                    changedElement.forEach(LdapImportDialog::fireAddedEvents);
                }
            }
            if (changedElement != null) {
                changedElement.forEach(LdapImportDialog::fireChangedEvents);
            }
        }
    }

    private static void fireChangedEvents(CnATreeElement element) {
        CnAElementFactory.getModel(element).childChanged(element);
        CnAElementFactory.getModel(element).databaseChildChanged(element);
    }

    private static void fireAddedEvents(CnATreeElement element) {
        CnAElementFactory.getModel(element).childAdded(element.getParent(), element);
        CnAElementFactory.getModel(element).databaseChildAdded(element);
    }

    private List<PersonInfo> getFromCache(PersonParameter parameter) {
        if (ldapQueryCache.containsKey(parameter)) {
            return ldapQueryCache.get(parameter);
        }
        return Collections.emptyList();
    }
}
