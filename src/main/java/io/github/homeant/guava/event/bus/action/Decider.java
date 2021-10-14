package io.github.homeant.guava.event.bus.action;

import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;
import io.github.homeant.guava.event.bus.utils.PsiUtils;

import java.util.List;
import java.util.Set;



public interface Decider {

    boolean shouldShow(UsageTarget target, Usage usage);

    /** Construct with a PsiMethod from a Provider to find where this is injected. */
    public class ProvidesMethodDecider implements Decider {
        private final PsiClass returnType;
        private final Set<String> qualifierAnnotations;
        private final List<PsiType> typeParameters;

        public ProvidesMethodDecider(PsiMethod psiMethod) {
            this.returnType = PsiUtils.getReturnClassFromMethod(psiMethod, true);
            this.qualifierAnnotations = PsiUtils.getQualifierAnnotations(psiMethod);
            this.typeParameters = PsiUtils.getTypeParameters(psiMethod);
        }

        @Override public boolean shouldShow(UsageTarget target, Usage usage) {
            PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();

            PsiField field = PsiUtils.findField(element);
            if (field != null //
                    && PsiUtils.hasAnnotation(field, CLASS_INJECT) //
                    && PsiUtils.hasQuailifierAnnotations(field, qualifierAnnotations)
                    && PsiUtils.hasTypeParameters(field, typeParameters)) {
                return true;
            }

            PsiMethod method = PsiUtils.findMethod(element);
            if (method != null && (PsiUtils.hasAnnotation(method, CLASS_INJECT)
                    || PsiUtils.hasAnnotation(method, CLASS_PROVIDES))) {
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    PsiClass parameterClass = PsiUtils.checkForLazyOrProvider(parameter);
                    if (parameterClass.equals(returnType) && PsiUtils.hasQuailifierAnnotations(
                            parameter, qualifierAnnotations)
                            && PsiUtils.hasTypeParameters(parameter, typeParameters)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     * Construct with a PsiParameter from an @Inject constructor and then use this to ensure the
     * usage fits.
     */
    public class ConstructorParameterInjectDecider extends IsAProviderDecider {
        public ConstructorParameterInjectDecider(PsiParameter psiParameter) {
            super(psiParameter);
        }
    }

    public class CollectionElementParameterInjectDecider extends IsAProviderDecider {
        public CollectionElementParameterInjectDecider(PsiElement psiParameter) {
            super(psiParameter);
        }

        @Override public boolean shouldShow(UsageTarget target, Usage usage) {
            PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
            PsiMethod psimethod = PsiUtils.findMethod(element);

            PsiAnnotationMemberValue attribValue = PsiUtils
                    .findTypeAttributeOfProvidesAnnotation(psimethod);

            // Is it a @Provides method?
            return psimethod != null
                    // Ensure it has an @Provides.
                    && PsiUtils.hasAnnotation(psimethod, CLASS_PROVIDES)
                    // Check for Qualifier annotations.
                    && PsiUtils.hasQuailifierAnnotations(psimethod, qualifierAnnotations)
                    // Right return type.
                    && PsiUtils.getReturnClassFromMethod(psimethod, false)
                    .getName()
                    .equals(target.getName())
                    // Right type parameters.
                    && PsiUtils.hasTypeParameters(psimethod, typeParameters)
                    // @Provides(type=SET)
                    && attribValue != null
                    && attribValue.textMatches(SET_TYPE);
        }
    }

    /**
     * Construct with a PsiField annotated w/ @Inject and then use this to ensure the
     * usage fits.
     */
    public class FieldInjectDecider extends IsAProviderDecider {
        public FieldInjectDecider(PsiField psiField) {
            super(psiField);
        }
    }

    class IsAProviderDecider implements Decider {
        protected final Set<String> qualifierAnnotations;
        protected final List<PsiType> typeParameters;

        public IsAProviderDecider(PsiElement element) {
            this.qualifierAnnotations = PsiUtils.getQualifierAnnotations(element);
            this.typeParameters = PsiUtils.getTypeParameters(element);
        }

        @Override public boolean shouldShow(UsageTarget target, Usage usage) {
            PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();

            PsiMethod psimethod = PsiUtils.findMethod(element);

            // For constructors annotated w/ @Inject, this is searched first before committing to the usage search.

            // Is it a @Provides method?
            return psimethod != null
                    // Ensure it has an @Provides.
                    && PsiUtils.hasAnnotation(psimethod, CLASS_PROVIDES)

                    // Check for Qualifier annotations.

                    // Right return type.
                    && PsiUtils.getReturnClassFromMethod(psimethod, false)
                    .getName()
                    .equals(target.getName())

                    // Right type parameters.
                    && PsiUtils.hasTypeParameters(psimethod, typeParameters);
        }
    }
}
