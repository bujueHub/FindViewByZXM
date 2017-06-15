package com.luanma.idea.plugin.util;

import com.luanma.idea.plugin.model.ViewPart;
import org.xml.sax.SAXException;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * 对话框中内容Tab
 */
public class ActionUtil {
    static String[] headers = {"selected", "type", "id", "name","comment"};

    public static List<ViewPart> getViewPartList(ViewSaxHandler viewSaxHandler, String oriContact) {
        try {
            viewSaxHandler.createViewList(oriContact);
            return viewSaxHandler.getViewPartList();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void switchAddM(List<ViewPart> viewParts, boolean isAddM) {
        if (isAddM) {
            for (ViewPart viewPart : viewParts) {
                viewPart.addM2Name();
            }
        } else {
            for (ViewPart viewPart : viewParts) {
                viewPart.resetName();
            }
        }
    }

    /**
     * 生成代码
     * @param viewParts
     * @param isViewHolder
     * @param isAddRootView
     * @param rootView
     * @return
     */
    public static String generateCode(List<ViewPart> viewParts, boolean isViewHolder, boolean isAddRootView, String rootView) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ViewPart viewPart : viewParts) {
            if (viewPart.isSelected()) {
                stringBuilder.append(viewPart.getDeclareString(isViewHolder, true));
            }
        }
        stringBuilder.append("\n");
        for (ViewPart viewPart : viewParts) {
            if (viewPart.isSelected()) {
                if (isViewHolder) {
                    stringBuilder.append(viewPart.getFindViewStringForViewHolder("convertView"));
                } else if (isAddRootView && !TextUtils.isEmpty(rootView)) {
                    stringBuilder.append(viewPart.getFindViewStringWithRootView(rootView));
                } else {
                    stringBuilder.append(viewPart.getFindViewString());
                }
            }
        }
        return stringBuilder.toString();
    }

    public static DefaultTableModel getTableModel(List<ViewPart> viewParts, TableModelListener tableModelListener) {
        DefaultTableModel tableModel;
        int size = viewParts.size();
        Object[][] cellData = new Object[size][5];
        for (int i = 0; i < size; i++) {
            ViewPart viewPart = viewParts.get(i);
            for (int j = 0; j < 4; j++) {
                switch (j) {
                    case 0:
                        cellData[i][j] = viewPart.isSelected();
                        break;
                    case 1:
                        cellData[i][j] = viewPart.getType();
                        break;
                    case 2:
                        cellData[i][j] = viewPart.getId();
                        break;
                    case 3:
                        cellData[i][j] = viewPart.getName();
                        break;
                }
            }
        }


        tableModel = new DefaultTableModel(cellData, headers) {
            final Class[] typeArray = {Boolean.class, Object.class, Object.class, Object.class,Object.class};

            @Override
            public boolean isCellEditable(int row, int column) {
                switch (column){
                    case 0:
                    case 3:
                    case 4:
                        return true;
                    case 1:
                    default:
                        return false;
                }
            }

            @SuppressWarnings("rawtypes")
            public Class getColumnClass(int column) {
                return typeArray[column];
            }
        };
        tableModel.addTableModelListener(tableModelListener);
        return tableModel;
    }
}
