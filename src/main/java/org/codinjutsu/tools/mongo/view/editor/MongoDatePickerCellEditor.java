package org.codinjutsu.tools.mongo.view.editor;

import org.codinjutsu.tools.mongo.view.style.DarculaStyleAttributesProvider;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.event.DateSelectionListener;
import org.jdesktop.swingx.table.DatePickerCellEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.DateFormat;

public class MongoDatePickerCellEditor extends DatePickerCellEditor {

    private static Color backgroundColor = new JPanel().getBackground();
    private static Color foregroundColor = new JPanel().getForeground();
    private static Color selectionBackgroundColor = Color.LIGHT_GRAY;
    private static Color selectionForegroundColor = Color.BLACK;
    private static Color monthForegroundColor = DarculaStyleAttributesProvider.BLUE;
    private static Color dayOfTheWeekForegroundColor = DarculaStyleAttributesProvider.ORANGE;
    private static Color todayBackgroundColor = Color.WHITE;


    public MongoDatePickerCellEditor() {
        this.datePicker.getEditor().setEditable(false);
        applyUIStyle();
    }

    public void addActionListener(ActionListener actionListener) {
        datePicker.addActionListener(actionListener);
    }

    private void applyUIStyle() {
        JXMonthView monthView = this.datePicker.getMonthView();
        monthView.setMonthStringBackground(backgroundColor);
        monthView.setMonthStringForeground(monthForegroundColor);
        monthView.setSelectionBackground(selectionBackgroundColor);
        monthView.setSelectionForeground(selectionForegroundColor);
        monthView.setDaysOfTheWeekForeground(dayOfTheWeekForegroundColor);
        monthView.setBackground(backgroundColor);
        monthView.setForeground(foregroundColor);
        monthView.setTodayBackground(todayBackgroundColor);
    }
}
