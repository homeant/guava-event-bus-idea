package io.github.homeant.guava.event.bus.handler;

import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.Usage;
import io.github.homeant.guava.event.bus.config.EventBusSettings;
import io.github.homeant.guava.event.bus.utils.PsiUtils;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ListenerProviderHandler implements GoItemProviderHandler {

    private static final Logger LOG = Logger.getInstance(ListenerProviderHandler.class);

    private final EventBusSettings.Setting setting;

    private final PsiElement psiElement;


    public ListenerProviderHandler(EventBusSettings.Setting setting, PsiElement psiElement) {
        this.setting = setting;
        this.psiElement = psiElement;
    }

    @Override
    public String getTitle() {
        return "Listener list";
    }

    @Override
    public PsiElement getPsiElement() {
        if(psiElement instanceof PsiMethodCallExpression) {
            PsiClass[] methodParamClass = PsiUtils.getMethodParamClass(psiElement);
            return methodParamClass[0];
        }
        return psiElement;
    }

    @Override
    public List<PsiElement> findElementList(MouseEvent e, PsiElement elt) {
        List<PsiElement> eltList = new ArrayList<>();
        Project project = elt.getProject();
        if (elt instanceof PsiMethodCallExpression) {
            for (String listener : setting.getListenerList()) {
                if (listener.startsWith("@")) {
                    PsiClass annotationClass = PsiUtils.findClass(listener.substring(1), project);
                    if (annotationClass != null) {
                        //eltList.add(annotationClass);
                    }
                } else {
                    PsiMethod[] methods = PsiUtils.findMethods(listener, project);
                    for (PsiMethod method : methods) {
                        if(!eltList.contains(method)) {
                            eltList.add(method);
                        }
                    }
                }
            }
        }
        return eltList;


//        List<GotoRelatedItem> gotoList = new ArrayList<>();
//        Project project = elt.getProject();
//        if (elt instanceof PsiMethodCallExpression) {
//            PsiMethodCallExpression expression = (PsiMethodCallExpression) elt;
//            PsiMethod sourceMethod = PsiUtils.findMethod(expression, project);
//            if (sourceMethod != null) {
//                for (String pattern : setting.getListenerList()) {
//                    // 注解
//                    if (pattern.startsWith("@")) {
//                        PsiType[] methodParamType = PsiUtils.getMethodParamType(expression);
//                        Query<PsiReference> search = ReferencesSearch.search(sourceMethod);
//                        Collection<PsiReference> refList = search.findAll();
//                        refList.forEach(ref -> {
//                            LOG.info("ref type:" + ref.toString());
//                            if (ref instanceof PsiReferenceExpression) {
//                                PsiElement parent = ((PsiReferenceExpression) ref).getParent().getParent();
//                                LOG.info("parent:" + parent.getText());
//                                PsiMethod method2 = PsiTreeUtil.getParentOfType(ref.getElement(), PsiMethod.class);
//                                PsiAnnotation annotation = method2.getAnnotation(pattern.substring(1));
//                                if (annotation != null) {
//                                    gotoList.add(new GotoRelatedItem(method2, "JAVA"));
//                                }
//                            }
//                        });
//                    } else {
//                        PsiMethod[] methods = PsiUtils.findMethods(pattern, project);
//                        for (PsiMethod method : methods) {
//                            if (PsiUtils.methodParamEquals(method, sourceMethod)) {
//                                gotoList.add(new GotoRelatedItem(method, "JAVA"));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        //return gotoList;
    }


    @Override
    public List<GotoRelatedItem> conversion(List<Usage> usageList) {
        List<GotoRelatedItem> gotoList = new ArrayList<>();
        return gotoList;
    }
}
