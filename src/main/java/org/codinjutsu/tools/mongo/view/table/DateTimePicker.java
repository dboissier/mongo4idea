/**
 * This is licensed under LGPL.  License can be found here:  http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * This is provided as is.  If you have questions please direct them to charlie.hubbard at gmail dot you know what.
 */
package org.codinjutsu.tools.mongo.view.table;

import com.intellij.ui.JBColor;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;


public class DateTimePicker extends JXDatePicker {

    private static final Locale LOCALE = Locale.getDefault();
    private static Color backgroundColor = new JPanel().getBackground();
    private static Color foregroundColor = new JPanel().getForeground();
    private static Color selectionBackgroundColor = JBColor.LIGHT_GRAY;
    private static Color selectionForegroundColor = JBColor.BLACK;
    private static Color monthForegroundColor = StyleAttributesProvider.NUMBER_COLOR;
    private static Color dayOfTheWeekForegroundColor = StyleAttributesProvider.KEY_COLOR;
    private static Color todayBackgroundColor = JBColor.WHITE;


    private JSpinner timeSpinner;
    private JPanel timePanel;
    private DateFormat timeFormat;

    public static DateTimePicker create() {
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setFormats(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, LOCALE));
        dateTimePicker.setTimeFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM, LOCALE));
        dateTimePicker.applyUIStyle();

        return dateTimePicker;
    }

    public DateTimePicker() {
        super();
        getMonthView().setSelectionModel(new SingleDaySelectionModel());
    }

    public void commitEdit() throws ParseException {
        commitTime();
        super.commitEdit();
    }

    public void cancelEdit() {
        super.cancelEdit();
        setTimeSpinners();
    }

    @Override
    public JPanel getLinkPanel() {
        super.getLinkPanel();
        if (timePanel == null) {
            timePanel = createTimePanel();
        }
        setTimeSpinners();
        return timePanel;
    }

    @Override
    public Date getDate() {
        return super.getDate();
    }

    private JPanel createTimePanel() {
        JPanel newPanel = new JPanel();
        newPanel.setLayout(new FlowLayout());

        SpinnerDateModel dateModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(dateModel);
        if (timeFormat == null) timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        updateTextFieldFormat();
        newPanel.add(timeSpinner);
        return newPanel;
    }

    private void updateTextFieldFormat() {
        if (timeSpinner == null) return;
        JFormattedTextField tf = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
        DefaultFormatterFactory factory = (DefaultFormatterFactory) tf.getFormatterFactory();
        DateFormatter formatter = (DateFormatter) factory.getDefaultFormatter();
        // Change the date format to only show the hours
        formatter.setFormat(timeFormat);
    }

    private void commitTime() {
        Date date = getDate();
        if (date != null) {
            Date time = (Date) timeSpinner.getValue();
            GregorianCalendar timeCalendar = new GregorianCalendar();
            timeCalendar.setTime(time);

            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, 0);

            Date newDate = calendar.getTime();
            setDate(newDate);
        }
    }

    private void setTimeSpinners() {
        Date date = getDate();
        if (date != null) {
            timeSpinner.setValue(date);
        }
    }

    public void setTimeFormat(DateFormat timeFormat) {
        this.timeFormat = timeFormat;
        updateTextFieldFormat();
    }

    private void applyUIStyle() {
        JXMonthView monthView = getMonthView();
        monthView.setMonthStringBackground(backgroundColor);
        monthView.setMonthStringForeground(monthForegroundColor);
        monthView.setSelectionBackground(selectionBackgroundColor);
        monthView.setSelectionForeground(selectionForegroundColor);
        monthView.setDaysOfTheWeekForeground(dayOfTheWeekForegroundColor);
        monthView.setBackground(backgroundColor);
        monthView.setForeground(foregroundColor);
        monthView.setTodayBackground(todayBackgroundColor);

        getLinkPanel().setBackground(backgroundColor);
        getLinkPanel().setForeground(foregroundColor);
    }


    public static void main(String[] args) {
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            if (!id.startsWith("Etc")) {
                TimeZone zone = TimeZone.getTimeZone(id);
                int offset = zone.getRawOffset() / 1000;
                int hour = offset / 3600;
                int minutes = (offset % 3600) / 60;
                System.out.println(String.format("(GMT%+d:%02d) %s", hour, minutes, id));
            }
        }
    }
}