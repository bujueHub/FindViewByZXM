package com.luanma.idea.plugin.model;

import com.luanma.idea.plugin.util.Definitions;
import com.luanma.idea.plugin.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jaeger
 * 15/11/25
 */
public class ViewPart {

    private static final String OUTPUT_DECLARE_COMMENT = "// %s\n";
    private static final String OUTPUT_DECLARE_STRING = "private %s %s;\n";
    private static final String OUTPUT_DECLARE_STRING_NOT_PRIVATE = "%s %s;\n";
    private static final String OUTPUT_FIND_VIEW_STRING = "%s = (%s) findViewById(R.id.%s);\n";
    private static final String OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW = "%s = (%s) %s.findViewById(R.id.%s);\n";
    private static final String OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER = "viewHolder.%s = (%s) %s.findViewById(R.id.%s);\n";
    private String type;
    private String typeFull;
    private String id;
    private String name;
    private boolean selected;
    // 描述
    private String comment;

    public ViewPart() {
        selected = true;
    }

    private void generateName(String id) {
        Pattern pattern = Pattern.compile("_([a-zA-Z])");
        Matcher matcher = pattern.matcher(id);

        char[] chars = id.toCharArray();
        while (matcher.find()) {
            int index = matcher.start(1);
            chars[index] = Character.toUpperCase(chars[index]);
        }
        String name = String.copyValueOf(chars);
        name = name.replaceAll("_", "");
        setName(name);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        String[] packages = type.split("\\.");
        if (packages.length > 1) {
            this.typeFull = type;
            this.type = packages[packages.length - 1];
        } else {
            this.typeFull = null;
            this.type = type;
        }
    }

    public String getTypeFull() {
        return typeFull;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        generateName(id);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDeclareString(boolean isViewHolder, boolean isShow) {
        StringBuilder stringBuilder = new StringBuilder();
        if(comment != null && comment.length() > 0){
            stringBuilder.append(String.format(OUTPUT_DECLARE_COMMENT,comment));
        }
        if (isViewHolder) {
            String content = String.format(OUTPUT_DECLARE_STRING_NOT_PRIVATE, type, name);
            stringBuilder.append(content);
            return stringBuilder.toString();
        } else {
            if (isShow) {
                String content = String.format(OUTPUT_DECLARE_STRING, type, name);
                stringBuilder.append(content);
                return stringBuilder.toString();
            }

            String realType;
            if (!Utils.isEmptyString(getTypeFull())) {
                realType = getTypeFull();
            } else if (Definitions.paths.containsKey(getType())) {
                realType = Definitions.paths.get(getType());
            } else {
                realType = "android.widget." + getType();
            }
            String content = String.format(OUTPUT_DECLARE_STRING, realType, name);
            stringBuilder.append(content);
            return stringBuilder.toString();
        }
    }


    public String getFindViewStringWithRootView(String rootView) {
        return String.format(OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW, name, type, rootView, id);
    }

    public String getFindViewString() {
        return String.format(OUTPUT_FIND_VIEW_STRING, name, type, id);
    }

    public void resetName() {
        generateName(id);
    }

    public void addM2Name() {
        generateName("m_" + id);
    }

    public String getFindViewStringForViewHolder(String rootView) {
        return String.format(OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER, name, type, rootView, id);
    }

    @Override
    public String toString() {
        return "ViewPart{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", selected=" + selected +
                ", comment=" + comment +
                '}';
    }
}

