package com.luanma.idea.plugin.util;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.EverythingGlobalScope;
import com.luanma.idea.plugin.model.ViewPart;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 生成代码
 */
public class CodeWriter extends WriteCommandAction.Simple {

    private List<ViewPart> viewPartList;
    protected Project mProject;
    protected PsiClass mClass;
    protected PsiElementFactory mFactory;
    private PsiFile psiFile;
    private Editor mEditor;

    private boolean isRpProject;
    private boolean isAddRootView;
    private boolean isViewHolder;
    private String rootViewStr;

    public CodeWriter(PsiFile psiFile, PsiClass clazz, List<ViewPart> viewPartList, boolean isRpProject,boolean isViewHolder, boolean isAddRootView, String rootViewStr, Editor editor) {
        super(clazz.getProject(), "");
        this.psiFile = psiFile;
        mProject = clazz.getProject();
        mClass = clazz;
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        mEditor = editor;
        this.viewPartList = viewPartList;
        this.isAddRootView = isAddRootView;
        this.isViewHolder = isViewHolder;
        this.rootViewStr = rootViewStr;
        this.isRpProject = isRpProject;
    }

    /**
     * judge field exists
     *
     * @param part
     * @return
     */
    private boolean fieldExist(ViewPart part) {
        PsiField[] fields = mClass.getAllFields();
        for (PsiField field : fields) {
            if (field.getName().equals(part.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * get initView method
     *
     * @return
     */
    private PsiMethod getInitView() {
//        PsiMethod[] methods = mClass.findMethodsByName("findViews", true);
//        for (PsiMethod method : methods) {
//            if (method.getReturnType().equals(PsiType.VOID)) {
//                return method;
//            }
//        }
        return null;
    }

    /**
     * Add the findViews() after onCreate()
     *
     * @param rootViewStr
     */
    private void addInitViewAfterOnCreate(@Nullable String rootViewStr) {
        String initViewStatement = getInitViewStatementAsString(rootViewStr);
        PsiMethod createMethod = mClass.findMethodsByName("onCreate", false)[0];
        for (PsiStatement statement : createMethod.getBody().getStatements()) {
            if (statement.getText().equals(initViewStatement)) {
                return;
            }
        }
        createMethod.getBody().add(mFactory.createStatementFromText(initViewStatement, mClass));
    }

    /**
     * Add the {@code initView} method after onCreateView()
     *
     * @param rootViewStr
     */
    private void addInitViewAfterOnCreateView(@Nullable String rootViewStr) {
        String initViewStatement = getInitViewStatementAsString(rootViewStr);
        PsiMethod createMethod = mClass.findMethodsByName("onCreateView", false)[0];
        for (PsiStatement statement : createMethod.getBody().getStatements()) {
            if (statement.getText().equals(initViewStatement)) {
                return;
            }
        }
        PsiStatement inflaterStatement = findInflaterStatement(createMethod.getBody().getStatements());
        createMethod.getBody().addAfter(mFactory.createStatementFromText(initViewStatement, mClass), inflaterStatement);
    }

    /**
     * Creates a string representing the initView method.
     * <p>
     * If {@code rootViewStr} is provided then it will generate a method with
     * {@code rootViewStr} as a param. A no-params method in case it's not provided.
     *
     * @param rootViewStr the name of root view
     * @return the method to append
     */
    private String getInitViewStatementAsString(@Nullable String rootViewStr) {
        String initViewStatement = "findViews();";
        if (!TextUtils.isEmpty(rootViewStr)) {
            initViewStatement = "findViews(" + rootViewStr + ");";
        }
        return initViewStatement;
    }

    private PsiStatement findInflaterStatement(PsiStatement[] psiStatements) {
        for (PsiStatement psiStatement : psiStatements) {
            if (psiStatement.getText().contains(".inflate(")) {
                return psiStatement;
            }
        }
        return null;
    }

    @Override
    protected void run() throws Throwable {
        int fieldCount = 0;
        PsiMethod initViewMethod = getInitView();
        StringBuilder methodBuild = new StringBuilder("/**\n" +
                "     * 初始化控件，FindViewById\n" +
                "     */\n");
        if(isRpProject){
            // 如果是人品项目
            if (isAddRootView && !TextUtils.isEmpty(rootViewStr)) {
                methodBuild.append("private void findViews(View " + rootViewStr + ") {");
            } else {
                methodBuild.append("@Override\n");
                methodBuild.append("public void findViews() {");
                methodBuild.append("super.findViews();\n");
            }
        }else{
            if (isAddRootView && !TextUtils.isEmpty(rootViewStr)) {
                methodBuild.append("private void findViews(View " + rootViewStr + ") {");
            } else {
                methodBuild.append("private void findViews() {");
            }
        }
        for (ViewPart viewPart : viewPartList) {
            if (!viewPart.isSelected() || fieldExist(viewPart)) {
                continue;
            }
            mClass.add(mFactory.createFieldFromText(viewPart.getDeclareString(false, false), mClass));
            if (initViewMethod != null) {
                initViewMethod.getBody().add(mFactory.createStatementFromText(viewPart.getFindViewString(), mClass));
            } else {
                if (isViewHolder) {
                    methodBuild.append(viewPart.getFindViewStringForViewHolder("convertView"));
                } else if (isAddRootView && !TextUtils.isEmpty(rootViewStr)) {
                    methodBuild.append(viewPart.getFindViewStringWithRootView(rootViewStr));
                } else {
                    methodBuild.append(viewPart.getFindViewString());
                }
                fieldCount++;
            }
        }
        methodBuild.append("}");
        if (fieldCount > 0) {
            // 放在onCreate后面
            PsiMethod createMethod = mClass.findMethodsByName("onCreate", false)[0];
            mClass.addAfter(mFactory.createMethodFromText(methodBuild.toString(), mClass),createMethod);
        }
        addInit(rootViewStr);

        // reformat class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(psiFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
    }

    private void addInit(@Nullable String rootViewStr) {
        PsiClass activityClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.app.Activity", new EverythingGlobalScope(mProject));
        PsiClass fragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.app.Fragment", new EverythingGlobalScope(mProject));
        PsiClass supportFragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.support.v4.app.Fragment", new EverythingGlobalScope(mProject));

        // 默认不放倒到onCreate后面
        // Check for Activity class
        if(isRpProject){
            return;
        }

        if (activityClass != null && mClass.isInheritor(activityClass, true)) {
             addInitViewAfterOnCreate(rootViewStr);
        } else if ((fragmentClass != null && mClass.isInheritor(fragmentClass, true)) || (supportFragmentClass != null && mClass.isInheritor(supportFragmentClass, true))) {
             addInitViewAfterOnCreateView(rootViewStr);
        } else {
            Utils.showInfoNotification(mEditor.getProject(), "Add " + getInitViewStatementAsString(rootViewStr) + " where relevant!");
        }
    }
}
