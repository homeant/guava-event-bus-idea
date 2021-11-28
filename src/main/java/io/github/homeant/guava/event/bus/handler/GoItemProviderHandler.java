package io.github.homeant.guava.event.bus.handler;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.find.findUsages.FindUsagesHandlerBase;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usages.Usage;

import java.awt.event.MouseEvent;
import java.util.List;

public interface GoItemProviderHandler {

    String getTitle();

    PsiElement getPsiElement();

    PsiElement[] getPrimaryElements();

    List<PsiElement> findElementList(PsiElement elt);

    default boolean filter(Usage usage) {
        return true;
    }

    List<GotoRelatedItem> conversion(List<Usage> usageList);

    default void navigate(List<GotoRelatedItem> gotoItemList, RelativePoint relativePoint) {
        if (gotoItemList.size() == 1) {
            gotoItemList.get(0).navigate();
        } else if (gotoItemList.size() > 1) {
            NavigationUtil.getRelatedItemsPopup(gotoItemList, this.getTitle()).show(relativePoint);
        }
    }
}
