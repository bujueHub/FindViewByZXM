package com.luanma.idea.plugin.action;

import com.intellij.ide.util.PropertiesComponent;
import com.luanma.idea.plugin.model.PropertiesKey;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;

public class FindViewDialog extends JDialog {
    private JPanel contentPane;
    public JButton btnOk;
    public JButton btnClose;
    public JCheckBox chbAddRootView;
    public JTextField textRootView;
    public JTextArea textCode;
    public JCheckBox chbAddM;
    public JTable tableViews;
    public JButton btnSelectAll;
    public JButton btnSelectNone;
    private JCheckBox chbIsViewHolder;
    private JCheckBox chbRpProject;
    private onClickListener onClickListener;

    public FindViewDialog() {
        dispose();
        setContentPane(contentPane);
        setModal(true);
        textRootView.setEnabled(false);

        initStatus();

        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onClickListener != null) {
                    onClickListener.onOK();
                }
                onCancel();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindViewDialog.this.onCancel();
            }
        });

        textRootView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (onClickListener != null) {
                    onClickListener.onUpdateRootView();
                }

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (onClickListener != null) {
                    onClickListener.onUpdateRootView();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (onClickListener != null) {
                    onClickListener.onUpdateRootView();
                }
            }
        });

        chbAddM.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (onClickListener != null) {
                    onClickListener.onSwitchAddM(chbAddM.isSelected());
                    PropertiesComponent.getInstance().setValue(PropertiesKey.SAVE_ADD_M_ACTION, chbAddM.isSelected());
                }
            }
        });

        chbIsViewHolder.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (onClickListener != null) {
                    onClickListener.onSwitchIsViewHolder(chbIsViewHolder.isSelected());
                }
            }
        });

        chbAddRootView.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean isAdd = chbAddRootView.isSelected();
                if (onClickListener != null) {
                    onClickListener.onSwitchAddRootView(isAdd);
                }
                textRootView.setEnabled(isAdd);
            }
        });

        // 人品项目
        chbRpProject.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean isAdd = chbRpProject.isSelected();
                if (onClickListener != null) {
                    onClickListener.onSwitchUseRpProject(isAdd);
                    PropertiesComponent.getInstance().setValue(PropertiesKey.SAVE_USE_RPPROJECT_ACTION, isAdd);
                }
            }
        });


        btnSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onClickListener != null) {
                    onClickListener.onSelectAll();
                }
            }
        });

        btnSelectNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onClickListener != null) {
                    onClickListener.onSelectNone();
                }
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
                                               @Override
                                               public void actionPerformed(ActionEvent e) {
                                                   FindViewDialog.this.onCancel();
                                               }
                                           },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void initStatus() {
        chbAddM.setSelected(PropertiesComponent.getInstance().getBoolean(PropertiesKey.SAVE_ADD_M_ACTION, false));
        chbRpProject.setSelected(PropertiesComponent.getInstance().getBoolean(PropertiesKey.SAVE_USE_RPPROJECT_ACTION, true));
    }

    private void onCancel() {
        dispose();
        if (onClickListener != null) {
            onClickListener.onFinish();
        }
    }

    public void setTextCode(String codeStr) {
        textCode.setText(codeStr);
    }

    public interface onClickListener {
        void onUpdateRootView();

        void onOK();

        void onSelectAll();

        void onSelectNone();

        void onSwitchAddRootView(boolean isAddRootView);

        void onSwitchUseRpProject(boolean isAddUseRp);

        void onSwitchAddM(boolean addM);

        void onSwitchIsViewHolder(boolean isViewHolder);

        void onFinish();
    }

    public void setOnClickListener(FindViewDialog.onClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setModel(DefaultTableModel model) {
        tableViews.setModel(model);
        tableViews.getColumnModel().getColumn(0).setPreferredWidth(20);
    }

    public String getRootView() {
        return textRootView.getText().trim();
    }
}
