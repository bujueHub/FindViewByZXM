package com.luanma.idea.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.luanma.idea.plugin.model.ViewPart;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ViewSaxHandler extends DefaultHandler {
    private List<ViewPart> viewPartList;
    private String layoutPath = "";
    private Project project;

    public static void main(String[] args) {
        String str = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
                "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:orientation=\"vertical\"\n" +
                "    tools:context=\"com.luanma.app.MainActivity\"\n" +
                "    tools:layout_editor_absoluteX=\"0dp\"\n" +
                "    tools:layout_editor_absoluteY=\"81dp\">\n" +
                "\n" +
                "    <TextView\n" +
                "        android:id=\"@+id/textView\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Hello World!\"\n" +
                "        tools:layout_editor_absoluteX=\"176dp\"\n" +
                "        tools:layout_editor_absoluteY=\"217dp\" />\n" +
                "\n" +
                "\n" +
                "    <TextView\n" +
                "        android:id=\"@+id/test_1\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Hello World!\"\n" +
                "        tools:layout_editor_absoluteX=\"176dp\"\n" +
                "        tools:layout_editor_absoluteY=\"217dp\" />\n" +
                "\n" +
                "    <LinearLayout\n" +
                "        android:id=\"@+id/layout\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:orientation=\"vertical\">\n" +
                "\n" +
                "        <Button\n" +
                "            android:id=\"@+id/btn_1\"\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"Hello World!\"\n" +
                "            tools:layout_editor_absoluteX=\"176dp\"\n" +
                "            tools:layout_editor_absoluteY=\"217dp\" />\n" +
                "\n" +
                "        <Button\n" +
                "            android:id=\"@+id/btn_2\"\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"Hello World!\"\n" +
                "            tools:layout_editor_absoluteX=\"176dp\"\n" +
                "            tools:layout_editor_absoluteY=\"217dp\" />\n" +
                "    </LinearLayout>\n" +
                "\n" +
                "    <ListView\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"match_parent\">\n" +
                "\n" +
                "    </ListView>\n" +
                "\n" +
                "\n" +
                "</LinearLayout>\n";

        ViewSaxHandler handler = new ViewSaxHandler();
        try {
            handler.createViewList(str);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<ViewPart> viewParts = handler.getViewPartList();
        for (ViewPart viewPart : viewParts) {
            System.out.println(viewPart.toString());
        }
    }


    public void createViewList(String string) throws ParserConfigurationException, SAXException, IOException {
        InputStream xmlStream = new ByteArrayInputStream(string.getBytes("UTF-8"));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(xmlStream, this);
    }

    public void createViewList(InputStream xmlStream) throws Exception {

    }


    @Override
    public void startDocument() throws SAXException {
        if (viewPartList == null) {
            viewPartList = new ArrayList<ViewPart>();
        }
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("include")) {
            String includeLayout = attributes.getValue("layout");
            if (includeLayout != null) {
                File file = new File(getLayoutPath(), includeLayout.replace("@layout", "") + ".xml");
                if (file.exists()) {
                    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
                    if (virtualFile == null) {
                        return;
                    }
                    PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);
                    try {
                        if (psiFile != null) {
                            this.createViewList(psiFile.getText());
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            String id = attributes.getValue("android:id");
            if (id != null) {
                ViewPart viewPart = new ViewPart();
                viewPart.setType(qName);
                viewPart.setId(id.replace("@+id/", "").replace("@id/", "").replace("@android:id/", ""));
                viewPartList.add(viewPart);
            }
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    public List<ViewPart> getViewPartList() {
        return viewPartList;
    }

    public String getLayoutPath() {
        return layoutPath;
    }

    public void setLayoutPath(String layoutPath) {
        this.layoutPath = layoutPath;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
