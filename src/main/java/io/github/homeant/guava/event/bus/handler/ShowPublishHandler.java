package io.github.homeant.guava.event.bus.handler;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.ide.util.gotoByName.ModelDiff;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.table.JBTable;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageToPsiElementProvider;
import com.intellij.usages.UsageViewManager;
import com.intellij.usages.impl.NullUsage;
import com.intellij.usages.impl.UsageNode;
import com.intellij.usages.impl.UsageViewImpl;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import io.github.homeant.guava.event.bus.config.EventBusSettings;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowPublishHandler implements GutterIconNavigationHandler {

    private static final Logger LOG = Logger.getInstance(ShowPublishHandler.class);

    private final EventBusSettings.Setting setting;

    public ShowPublishHandler(EventBusSettings.Setting setting) {
        this.setting = setting;
    }

    @Override
    public void navigate(MouseEvent event, PsiElement elt) {
        Project project = elt.getProject();
        if (elt instanceof PsiIdentifier) {
            elt = PsiTreeUtil.getParentOfType(elt, PsiMethod.class);
        }
        if (elt instanceof PsiMethod) {
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // com.google.common.eventbus.EventBus
            PsiMethod method = (PsiMethod) elt;
            PsiClass[] paramClass = PsiUtils.getMethodParamClass(method);
            if (paramClass.length > 0) {
                PsiClass eventBusClass = javaPsiFacade.findClass(paramClass[0].getQualifiedName(), GlobalSearchScope.allScope(project));
                if (eventBusClass == null) return;
                List<PsiMethod> psiMethodList = new ArrayList<>();
                for (String pattern : setting.getPublisherList()) {
                    if (!pattern.contains("(")) {
                        PsiMethod[] methods = PsiUtils.findMethods(pattern, project);
                        if (methods.length > 0) {
                            for (PsiMethod psiMethod : methods) {
                                if (PsiUtils.methodParamEquals(method, psiMethod)) {
                                    PsiClass publishClazz = (PsiClass) method.getParent();
                                    LOG.info("find usages " + publishClazz.getQualifiedName() + "." + psiMethod.getName());
                                    psiMethodList.add(psiMethod);
                                }
                            }
                        }
                    }
                }
                PsiElement[] psiElements = new PsiElement[psiMethodList.size()];
                for (int i = 0; i < psiMethodList.size(); i++) {
                    psiElements[i] = psiMethodList.get(i);
                }
                ApplicationManager.getApplication().assertIsDispatchThread();

                UsageViewManager manager = UsageViewManager.getInstance(project);
                FindUsagesManager findUsagesManager = ((FindManagerImpl) FindManager.getInstance(project)).getFindUsagesManager();
                FindUsagesHandler handler = findUsagesManager.getNewFindUsagesHandler(elt, false);
//                UsageView usageView = findUsagesManager.findUsagesInEditor(new PsiElement[]{elt},psiElements, handler, new FindUsagesOptions(project), true);
//                if (usageView != null) {
//                    for (Usage usage : usageView.getUsages()) {
//                        LOG.debug("usage:", usage);
//                    }
//                }
//                final UsageViewPresentation presentation = findUsagesManager.createPresentation(handler, new FindUsagesOptions(project));
//                presentation.setDetachedMode(true);
//                UsageTarget[] usageTargets = psiMethodList.stream()
//                        .map(psiMethod -> new PsiElement2UsageTargetAdapter(psiMethod, true)).collect(Collectors.toList()).toArray(new UsageTarget[0]);
//                com.intellij.usages.Usage[] usages = psiMethodList.stream()
//                        .map(psiMethod -> new UsageInfo2UsageAdapter(new UsageInfo(psiMethod,psiMethod.getTextRange(),true))).collect(Collectors.toList()).toArray(new Usage[0]);
//                if(usages.length==1){
//                    usages[0].navigate(true);
//                }else{
//
//                    UsageViewImpl usageView = (UsageViewImpl)manager.createUsageView(usageTargets, Usage.EMPTY_ARRAY, presentation, null);
//                    JBTable table = new UsageTable();
//                    PopupChooserBuilder builder = new PopupChooserBuilder(table);
//                    List<UsageNode> usageNodes = new ArrayList<>();
//                    for (Usage usage : usages) {
//                        UsageNode usageNode = usageView.doAppendUsage(usage);
//                        if(usageNode!=null){
//                            usageNodes.add(usageNode);
//                        }
//                    }
//                    setTableModel(table,null,usageNodes);
//                    JBPopup popup = builder.createPopup();
//                    popup.show(new RelativePoint(event));
//                }
            }
        }
    }

    @NotNull
    private static UsageModel setTableModel(@NotNull JTable table,
                                            @NotNull UsageViewImpl usageView,
                                            @NotNull final List<UsageNode> data) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        final int columnCount = calcColumnCount(data);
        UsageModel model = table.getModel() instanceof UsageModel ? (UsageModel) table.getModel() : null;
        if (model == null || model.getColumnCount() != columnCount) {
            model = new UsageModel(data, columnCount);
            table.setModel(model);
        }
        return model;
    }

    private static int calcColumnCount(@NotNull List<UsageNode> data) {
        return data.isEmpty() || data.get(0) instanceof StringNode ? 1 : 3;
    }

    public class UsageTable extends JBTable implements DataProvider {

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public Object getData(@NotNull @NonNls String dataId) {
            if (CommonDataKeys.PSI_ELEMENT.is(dataId)) {
                final int[] selected = getSelectedRows();
                if (selected.length == 1) {
                    return getPsiElementForHint(getValueAt(selected[0], 0));
                }
            }
            return null;
        }

        @Nullable
        PsiElement getPsiElementForHint(Object selectedValue) {
            if (selectedValue instanceof UsageNode) {
                final Usage usage = ((UsageNode) selectedValue).getUsage();
                if (usage instanceof UsageInfo2UsageAdapter) {
                    final PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
                    if (element != null) {
                        final PsiElement view = UsageToPsiElementProvider.findAppropriateParentFrom(element);
                        return view == null ? element : view;
                    }
                }
            }
            return null;
        }
    }

    private static class UsageModel extends ListTableModel<UsageNode> implements ModelDiff.Model<Object> {
        private UsageModel(@NotNull List<UsageNode> data, int cols) {
            super(cols(cols), data, 0);
        }

        @NotNull
        private static ColumnInfo<UsageNode, UsageNode>[] cols(int cols) {
            ColumnInfo<UsageNode, UsageNode> o = new ColumnInfo<>("") {
                @Nullable
                @Override
                public UsageNode valueOf(UsageNode node) {
                    return node;
                }
            };
            List<ColumnInfo<UsageNode, UsageNode>> list = Collections.nCopies(cols, o);
            return list.toArray(new ColumnInfo[list.size()]);
        }

        @Override
        public void addToModel(int idx, Object element) {
            UsageNode node = element instanceof UsageNode ? (UsageNode) element : new StringNode(element);

            if (idx < getRowCount()) {
                insertRow(idx, node);
            } else {
                addRow(node);
            }
        }

        @Override
        public void removeRangeFromModel(int start, int end) {
            for (int i = end; i >= start; i--) {
                removeRow(i);
            }
        }
    }

    static class StringNode extends UsageNode {
        private final Object myString;

        StringNode(Object string) {
            super(null, NullUsage.INSTANCE);
            myString = string;
        }

        @Override
        public String toString() {
            return myString.toString();
        }
    }


}
