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
package sernet.verinice.iso27k.rcp;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.bsi.actions.NaturalizeAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropListener;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinUmsetzungTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IBSIStrukturElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kGroupTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ItemTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.SearchViewElementTransfer;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditor;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.DefaultModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.rcp.action.AddGroup;
import sernet.verinice.iso27k.rcp.action.BSIModelDropPerformer;
import sernet.verinice.iso27k.rcp.action.CollapseAction;
import sernet.verinice.iso27k.rcp.action.ExpandAction;
import sernet.verinice.iso27k.rcp.action.FileDropPerformer;
import sernet.verinice.iso27k.rcp.action.HideEmptyFilter;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TagParameter;
import sernet.verinice.model.common.TypeParameter;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.iso27k.VulnerabilityGroup;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.ViewFilterAction;
import sernet.verinice.rcp.tree.TreeContentProvider;
import sernet.verinice.rcp.tree.TreeLabelProvider;
import sernet.verinice.rcp.tree.TreeUpdateListener;
import sernet.verinice.service.tree.ElementManager;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ISMView extends RightsEnabledView implements ILinkedWithEditorView {

    private static final Logger LOG = Logger.getLogger(ISMView.class);

    public static final String ID = "sernet.verinice.iso27k.rcp.ISMView"; //$NON-NLS-1$

    private static int operations = DND.DROP_COPY | DND.DROP_MOVE;

    protected TreeViewer viewer;

    private TreeContentProvider contentProvider;
    private ElementManager elementManager;

    private DrillDownAdapter drillDownAdapter;

    private ShowBulkEditAction bulkEditAction;

    private ExpandAction expandAction;

    private CollapseAction collapseAction;

    private Action expandAllAction;

    private Action collapseAllAction;

    private Action linkWithEditorAction;

    private IPartListener2 linkWithEditorPartListener = new LinkWithEditorPartListener(this);

    private ViewFilterAction filterAction;

    private MetaDropAdapter metaDropAdapter;

    private ShowAccessControlEditAction accessControlEditAction;

    private NaturalizeAction naturalizeAction;

    private IModelLoadListener modelLoadListener;

    private Object mutex = new Object();

    private IISO27KModelListener modelUpdateListener;

    private boolean linkingActive = false;

    private ICommandService commandService;

    public ISMView() {
        super();
        elementManager = new ElementManager();
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.ISMVIEW;
    }

    /*
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.
     * widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
        super.createPartControl(parent);
        try {
            initView(parent);
            startInitDataJob();
        } catch (Exception e) {
            LOG.error("Error while creating organization view", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ISMView_2);
        }
    }

    protected void initView(Composite parent) {
        IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
        if (CnAElementFactory.isIsoModelLoaded()) {
            CnAElementFactory.getInstance().reloadAllModelsFromDatabase();
        }

        contentProvider = new TreeContentProvider(elementManager);
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new DecoratingLabelProvider(new TreeLabelProvider(),
                workbench.getDecoratorManager()));
        toggleLinking(Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.LINK_TO_EDITOR));

        getSite().setSelectionProvider(viewer);
        hookContextMenu();
        makeActions();
        addActions();
        fillToolBar();
        hookDNDListeners();

        getSite().getPage().addPartListener(linkWithEditorPartListener);
    }

    protected void startInitDataJob() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ISMview: startInitDataJob"); //$NON-NLS-1$
        }
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.ISMView_4, //$NON-NLS-1$
                            e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    protected void initData() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ISMVIEW: initData"); //$NON-NLS-1$
        }
        synchronized (mutex) {
            if (CnAElementFactory.isIsoModelLoaded()) {
                if (modelUpdateListener == null) {
                    // modellistener should only be created once!
                    if (LOG.isDebugEnabled()) {
                        Logger.getLogger(this.getClass())
                                .debug("Creating modelUpdateListener for ISMView."); //$NON-NLS-1$
                    }
                    modelUpdateListener = new TreeUpdateListener(viewer, elementManager);
                    CnAElementFactory.getInstance().getISO27kModel()
                            .addISO27KModelListener(modelUpdateListener);
                    Display.getDefault().syncExec(
                            () -> setInput(CnAElementFactory.getInstance().getISO27kModel()));
                }
            } else if (modelLoadListener == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ISMView No model loaded, adding model load listener."); //$NON-NLS-1$
                }
                // model is not loaded yet: add a listener to load data when
                // it's loaded
                modelLoadListener = new DefaultModelLoadListener() {
                    @Override
                    public void loaded(ISO27KModel model) {
                        startInitDataJob();
                    }
                };
                CnAElementFactory.getInstance().addLoadListener(modelLoadListener);

            }
        }
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        elementManager.clearCache();
        if (CnAElementFactory.isIsoModelLoaded()) {
            CnAElementFactory.getInstance().getISO27kModel()
                    .removeISO27KModelListener(modelUpdateListener);
        }
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
        getSite().getPage().removePartListener(linkWithEditorPartListener);
        super.dispose();
    }

    public void setInput(ISO27KModel model) {
        viewer.setInput(model);
    }

    public void setInput(List<Organization> organizationList) {
        viewer.setInput(organizationList);
    }

    public void setInput(Organization organization) {
        viewer.setInput(organization);
    }

    private void makeActions() {
        BSIModelViewDropListener bsiDropAdapter;

        bulkEditAction = new ShowBulkEditAction(getViewSite().getWorkbenchWindow(),
                Messages.ISMView_6);

        expandAction = new ExpandAction(viewer, contentProvider);
        expandAction.setText(Messages.ISMView_7);
        expandAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

        collapseAction = new CollapseAction(viewer);
        collapseAction.setText(Messages.ISMView_8);
        collapseAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));

        expandAllAction = new Action() {
            @Override
            public void run() {
                expandAll();
            }
        };
        expandAllAction.setText(Messages.ISMView_9);
        expandAllAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

        collapseAllAction = new Action() {
            @Override
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText(Messages.ISMView_10);
        collapseAllAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));

        HideEmptyFilter hideEmptyFilter = createHideEmptyFilter();
        TypeParameter typeParameter = createTypeParameter();
        TagParameter tagParameter = new TagParameter();
        filterAction = new ViewFilterAction(Messages.ISMView_12, tagParameter, hideEmptyFilter,
                typeParameter);

        elementManager.addParameter(tagParameter);
        if (typeParameter != null) {
            elementManager.addParameter(typeParameter);
        }

        metaDropAdapter = new MetaDropAdapter(viewer);
        bsiDropAdapter = new BSIModelViewDropListener(viewer);
        BSIModelDropPerformer bsi2IsmDropAdapter = new BSIModelDropPerformer(viewer);
        FileDropPerformer fileDropPerformer = new FileDropPerformer(viewer);
        metaDropAdapter.addAdapter(bsiDropAdapter);

        metaDropAdapter.addAdapter(bsi2IsmDropAdapter);
        metaDropAdapter.addAdapter(fileDropPerformer);

        accessControlEditAction = new ShowAccessControlEditAction(
                getViewSite().getWorkbenchWindow(), Messages.ISMView_11);

        naturalizeAction = new NaturalizeAction(getViewSite().getWorkbenchWindow());

        linkWithEditorAction = new Action(Messages.ISMView_5, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                toggleLinking(isChecked());
            }
        };
        linkWithEditorAction.setChecked(isLinkingActive());
        linkWithEditorAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));

    }

    /**
     * Override this in subclasses to hide empty groups on startup.
     *
     * @return a HideEmptyFilter
     */
    protected HideEmptyFilter createHideEmptyFilter() {
        return new HideEmptyFilter(viewer);
    }

    /**
     * Override this in subclasses to hide empty groups on startup.
     *
     * @return a {@link TypeParameter}
     */
    protected TypeParameter createTypeParameter() {
        return new TypeParameter();
    }

    protected void fillToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        drillDownAdapter.addNavigationActions(manager);
        manager.add(filterAction);
        manager.add(linkWithEditorAction);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(this::fillContextMenu);
        Menu menu = menuMgr.createContextMenu(viewer.getControl());

        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void hookDNDListeners() {
        Transfer[] dragTypes = new Transfer[] { ISO27kElementTransfer.getInstance(),
                ISO27kGroupTransfer.getInstance() };
        Transfer[] dropTypes = new Transfer[] { IGSModelElementTransfer.getInstance(),
                BausteinUmsetzungTransfer.getInstance(), ItemTransfer.getInstance(),
                ISO27kElementTransfer.getInstance(), ISO27kGroupTransfer.getInstance(),
                IBSIStrukturElementTransfer.getInstance(), FileTransfer.getInstance(),
                SearchViewElementTransfer.getInstance() };

        viewer.addDragSupport(operations, dragTypes, new BSIModelViewDragListener(viewer));
        viewer.addDropSupport(operations, dropTypes, metaDropAdapter);

    }

    protected void expandAll() {
        viewer.expandAll();
    }

    private void addActions() {
        viewer.addDoubleClickListener(event -> {
            ISelection selection = event.getViewer().getSelection();
            if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
                Object sel = ((IStructuredSelection) selection).getFirstElement();
                EditorFactory.getInstance().updateAndOpenObject(sel);
            }
        });

        viewer.addSelectionChangedListener(expandAction);
        viewer.addSelectionChangedListener(collapseAction);
    }

    protected void fillContextMenu(IMenuManager manager) {
        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection
                && ((IStructuredSelection) selection).size() == 1) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if (sel instanceof Organization) {
                Organization element = (Organization) sel;
                if (CnAElementHome.getInstance().isNewChildAllowed(element)) {
                    MenuManager submenuNew = new MenuManager(Messages.NewObjectMenu, "content/new"); //$NON-NLS-1$ //$NON-NLS-2$
                    submenuNew.add(new AddGroup(element, AssetGroup.TYPE_ID, Asset.TYPE_ID));
                    submenuNew.add(new AddGroup(element, AuditGroup.TYPE_ID, Audit.TYPE_ID));
                    submenuNew.add(new AddGroup(element, ControlGroup.TYPE_ID, Control.TYPE_ID));
                    submenuNew.add(new AddGroup(element, DocumentGroup.TYPE_ID, Document.TYPE_ID));
                    submenuNew.add(new AddGroup(element, EvidenceGroup.TYPE_ID, Evidence.TYPE_ID));
                    submenuNew.add(new AddGroup(element, ExceptionGroup.TYPE_ID,
                            sernet.verinice.model.iso27k.Exception.TYPE_ID));
                    submenuNew.add(new AddGroup(element, FindingGroup.TYPE_ID, Finding.TYPE_ID));
                    submenuNew.add(new AddGroup(element, IncidentGroup.TYPE_ID, Incident.TYPE_ID));
                    submenuNew.add(new AddGroup(element, IncidentScenarioGroup.TYPE_ID,
                            IncidentScenario.TYPE_ID));
                    submenuNew
                            .add(new AddGroup(element, InterviewGroup.TYPE_ID, Interview.TYPE_ID));
                    submenuNew.add(new AddGroup(element, PersonGroup.TYPE_ID, PersonIso.TYPE_ID));
                    submenuNew.add(new AddGroup(element, ProcessGroup.TYPE_ID,
                            sernet.verinice.model.iso27k.Process.TYPE_ID));
                    submenuNew.add(new AddGroup(element, RecordGroup.TYPE_ID, Record.TYPE_ID));
                    submenuNew.add(
                            new AddGroup(element, RequirementGroup.TYPE_ID, Requirement.TYPE_ID));
                    submenuNew.add(new AddGroup(element, ResponseGroup.TYPE_ID, Response.TYPE_ID));
                    submenuNew.add(new AddGroup(element, ThreatGroup.TYPE_ID, Threat.TYPE_ID));
                    submenuNew.add(new AddGroup(element, VulnerabilityGroup.TYPE_ID,
                            Vulnerability.TYPE_ID));
                    manager.add(submenuNew);
                }
            }
        }

        manager.add(new GroupMarker("content")); //$NON-NLS-1$
        manager.add(new Separator());
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new Separator());
        manager.add(new GroupMarker("special")); //$NON-NLS-1$
        manager.add(bulkEditAction);
        manager.add(accessControlEditAction);
        manager.add(naturalizeAction);
        manager.add(new Separator());
        manager.add(expandAction);
        manager.add(collapseAction);
        drillDownAdapter.addNavigationActions(manager);
    }

    /*
     * @see
     * sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.
     * eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart editor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this)) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(editor);
        if (element == null && editor.getEditorInput() instanceof AttachmentEditorInput) {
            element = getElementFromAttachment(editor);

        }
        if (!(element instanceof IISO27kElement)) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Element in editor: " + element.getUuid()); //$NON-NLS-1$
            LOG.debug("Expanding tree now to show element..."); //$NON-NLS-1$
        }
        if (element != null) {
            viewer.setSelection(new StructuredSelection(element), true);
        } else {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Tree is expanded."); //$NON-NLS-1$
        }
    }

    /**
     * gets Element that is referenced by attachment shown by editor
     * 
     * @param editor
     *            - ({@link AttachmentEditor}) Editor of {@link Attachment}
     * @return {@link CnATreeElement}
     */
    private CnATreeElement getElementFromAttachment(IEditorPart editor) {
        return AttachmentEditorInput.extractCnaTreeElement(editor);
    }

    protected void toggleLinking(boolean checked) {
        this.linkingActive = checked;
        if (checked) {
            Optional.ofNullable(getSite().getPage().getActiveEditor())
                    .ifPresent(this::editorActivated);
        }
    }

    protected boolean isLinkingActive() {
        return linkingActive;
    }

    /**
     * Passing the focus request to the viewer's control.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandService();
        }
        return commandService;
    }

    private ICommandService createCommandService() {
        return ServiceFactory.lookupCommandService();
    }
}
