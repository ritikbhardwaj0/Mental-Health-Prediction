package com.example.mentalhealth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.CardLayout;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MentalHealthApp extends JFrame {
    private static final Color INK = new Color(24, 43, 57);
    private static final Color MUTED = new Color(102, 122, 135);
    private static final Color TEAL = new Color(75, 145, 120);
    private static final Color BACKGROUND = new Color(244, 247, 243);
    private static final Color BORDER = new Color(226, 233, 226);

    private final Map<String, JComponent> fields = new LinkedHashMap<>();
    private final JLabel status = new JLabel("Your responses stay in this local project.");
    private final JLabel resultCopy = new JLabel("Add your daily habits to get a score estimate.");
    private final JLabel resultTag = new JLabel("READY FOR A CHECK-IN");
    private final ScoreGauge gauge = new ScoreGauge();
    private final JButton predict = new JButton("Check my score");
    private final Map<String, JLabel> analyticsValues = new LinkedHashMap<>();
    private final Map<String, String> metricTitles = new LinkedHashMap<>();
    private final List<JPanel> probabilityRows = new ArrayList<>();
    private final JLabel analyticsStatus = new JLabel("Loading dataset summary…");
    private final ScoreDistribution distribution = new ScoreDistribution();
    private final StressMeanChart stressMeanChart = new StressMeanChart();
    private final ScoreBoxPlot scoreBoxPlot = new ScoreBoxPlot();
    private boolean analyticsLoaded;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();

    public MentalHealthApp() {
        super("Mental Stress Meter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 720));
        setSize(1140, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        pages.setOpaque(false);
        pages.add(createMainContent(), "meter");
        pages.add(createAnalyticsPanel(), "analytics");
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BACKGROUND);
        center.add(createNavigation(), BorderLayout.NORTH);
        center.add(pages, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0)) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setPaint(new GradientPaint(0, 0, new Color(235, 246, 240), getWidth(), getHeight(), new Color(246, 247, 239)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        header.setBorder(new EmptyBorder(20, 34, 20, 34));
        JLabel eyebrow = new JLabel("STUDENT WELLBEING  ·  PERSONAL CHECK-IN");
        eyebrow.setForeground(new Color(57, 130, 111));
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        JLabel title = new JLabel("Mental Stress Meter");
        title.setForeground(new Color(35, 67, 59));
        title.setFont(new Font("SansSerif", Font.BOLD, 29));
        JLabel subtitle = new JLabel("A quick look at the daily habits connected to your wellbeing.");
        subtitle.setForeground(new Color(93, 119, 111));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(eyebrow); text.add(Box.createVerticalStrut(8)); text.add(title);
        text.add(Box.createVerticalStrut(5)); text.add(subtitle);
        JLabel badge = new JLabel("  PRIVATE LOCAL APP  ");
        badge.setOpaque(true);
        badge.setBackground(new Color(224, 241, 232));
        badge.setForeground(new Color(57, 112, 96));
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(198, 223, 210)), new EmptyBorder(8, 8, 8, 8)));
        header.add(text, BorderLayout.CENTER);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private JComponent createNavigation() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        nav.setBackground(BACKGROUND);
        nav.setBorder(new EmptyBorder(4, 28, 0, 28));
        addNavigationButton(nav, "Stress Meter", "meter");
        addNavigationButton(nav, "Student Wellbeing Insights", "analytics");
        selectPage("meter");
        return nav;
    }

    private void addNavigationButton(JPanel nav, String title, String page) {
        JButton button = new JButton(title);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            selectPage(page);
            if (page.equals("analytics") && !analyticsLoaded) loadAnalytics();
        });
        navigationButtons.put(page, button);
        nav.add(button);
    }

    private void selectPage(String selected) {
        if (pages.getComponentCount() > 0) pageLayout.show(pages, selected);
        for (var entry : navigationButtons.entrySet()) {
            boolean active = entry.getKey().equals(selected);
            entry.getValue().setBackground(active ? Color.WHITE : new Color(235, 241, 237));
            entry.getValue().setForeground(active ? new Color(47, 121, 101) : MUTED);
        }
    }

    private JComponent createMainContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(22, 28, 18, 28));

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; c.fill = GridBagConstraints.BOTH; c.weighty = 1;

        c.gridx = 0; c.weightx = 0.65; c.insets = new Insets(0, 0, 0, 12);
        columns.add(createFormCard(), c);
        c.gridx = 1; c.weightx = 0.35; c.insets = new Insets(0, 12, 0, 0);
        columns.add(createMeterCard(), c);
        wrapper.add(columns, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFormCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(22, 22, 20, 22));
        card.add(sectionHeading("Your daily snapshot", "A few details help the model make an estimate."), BorderLayout.NORTH);

        JPanel sections = new JPanel();
        sections.setOpaque(false);
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));

        JPanel about = new JPanel(new GridLayout(0, 2, 14, 10));
        about.setOpaque(false);
        about.add(field("Age", "age", text("20")));
        about.add(field("Gender", "gender", combo("Male", "Female")));
        about.add(field("Country", "country", text("India")));
        about.add(field("Academic level", "academic_level", combo("Undergraduate", "Graduate", "High School")));
        sections.add(sectionLabel("ABOUT YOU"));
        sections.add(about);
        sections.add(Box.createVerticalStrut(16));

        JPanel habits = new JPanel(new GridLayout(0, 2, 14, 10));
        habits.setOpaque(false);
        habits.add(field("Social platform", "most_used_platform", combo("Instagram", "Facebook", "LinkedIn", "Snapchat", "Twitter", "YouTube", "TikTok", "LINE", "KakaoTalk", "VKontakte", "WhatsApp", "WeChat")));
        habits.add(field("Main reason for use", "purpose_of_use", combo("Education", "Networking", "Entertainment", "News")));
        habits.add(field("Social media hours / day", "avg_daily_usage_hours", text("3")));
        habits.add(field("Phone unlocks / day", "daily_unlocks", text("50")));
        habits.add(field("Study hours / day", "study_hours", text("4")));
        habits.add(field("Activity hours / day", "physical_activity_hours", text("1")));
        habits.add(field("Sleep hours / night", "sleep_hours_per_night", text("7")));
        habits.add(field("Self-reported stress", "stress_level", combo("Low", "Medium", "High", "Very High")));
        sections.add(sectionLabel("HABITS & STRESS"));
        sections.add(habits);
        JScrollPane formScroll = new JScrollPane(sections,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(formScroll, BorderLayout.CENTER);

        JPanel action = new JPanel(new BorderLayout(12, 0));
        action.setOpaque(false);
        action.setBorder(new EmptyBorder(14, 0, 0, 0));
        JLabel saveNote = new JLabel("Your check-in is saved in local history.");
        saveNote.setForeground(MUTED);
        saveNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
        predict.setFont(new Font("SansSerif", Font.BOLD, 14));
        predict.setForeground(Color.WHITE);
        predict.setBackground(TEAL.darker());
        predict.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        predict.setOpaque(true);
        predict.setContentAreaFilled(true);
        predict.setBorderPainted(false);
        predict.setFocusPainted(false);
        predict.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        predict.setBorder(new EmptyBorder(13, 22, 13, 22));
        predict.addActionListener(e -> submit());
        action.add(saveNote, BorderLayout.CENTER);
        action.add(predict, BorderLayout.EAST);
        card.add(action, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createMeterCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(22, 20, 20, 20));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(sectionHeading("Your meter", "A model estimate based on your answers."));
        top.add(Box.createVerticalStrut(6));
        gauge.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(gauge);

        JPanel feedback = new JPanel();
        feedback.setOpaque(false);
        feedback.setLayout(new BoxLayout(feedback, BoxLayout.Y_AXIS));
        resultTag.setFont(new Font("SansSerif", Font.BOLD, 10));
        resultTag.setForeground(TEAL.darker());
        resultCopy.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultCopy.setForeground(MUTED);
        resultCopy.setVerticalAlignment(SwingConstants.TOP);
        feedback.add(resultTag);
        feedback.add(Box.createVerticalStrut(8));
        feedback.add(resultCopy);

        JPanel note = new JPanel(new BorderLayout(8, 0));
        note.setBackground(new Color(235, 247, 245));
        note.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel dot = new JLabel("i");
        dot.setOpaque(true); dot.setBackground(TEAL); dot.setForeground(Color.WHITE);
        dot.setHorizontalAlignment(SwingConstants.CENTER);
        dot.setFont(new Font("SansSerif", Font.BOLD, 12));
        dot.setPreferredSize(new Dimension(21, 21));
        JLabel noteText = new JLabel("This is a project estimate, not a diagnosis.");
        noteText.setForeground(new Color(48, 97, 94));
        noteText.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.add(dot, BorderLayout.WEST); note.add(noteText, BorderLayout.CENTER);

        JPanel lower = new JPanel(new BorderLayout(0, 16));
        lower.setOpaque(false);
        lower.add(feedback, BorderLayout.NORTH);
        lower.add(note, BorderLayout.SOUTH);
        card.add(top, BorderLayout.CENTER);
        card.add(lower, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(14, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 28, 16, 28));
        status.setForeground(MUTED);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.add(status, BorderLayout.CENTER);
        return footer;
    }

    private JComponent createAnalyticsPanel() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(20, 28, 16, 28));
        JPanel intro = new JPanel(new BorderLayout(16, 0));
        intro.setOpaque(false);
        intro.add(sectionHeading("Student Wellbeing Insights", "Score patterns and probability estimates from the supplied student dataset."), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh figures");
        refresh.addActionListener(e -> loadAnalytics());
        intro.add(refresh, BorderLayout.EAST);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; c.weightx = 1; c.weighty = 0; c.insets = new Insets(0, 0, 12, 0);
        JPanel stats = cardPanel();
        stats.setLayout(new BorderLayout(0, 10));
        stats.setBorder(new EmptyBorder(16, 18, 16, 18));
        stats.add(sectionHeading("Score summary", "Descriptive statistics for Mental_Health_Score, on a 0–10 scale."), BorderLayout.NORTH);
        JPanel metrics = new JPanel(new GridLayout(2, 5, 10, 10));
        metrics.setOpaque(false);
        addMetric(metrics, "Students", "sample_size", "whole");
        addMetric(metrics, "Mean", "mean", "decimal");
        addMetric(metrics, "Median", "median", "decimal");
        addMetric(metrics, "Mode", "mode", "decimal");
        addMetric(metrics, "Std. deviation", "std_dev", "decimal");
        addMetric(metrics, "Variance", "variance", "decimal");
        addMetric(metrics, "Minimum", "min", "decimal");
        addMetric(metrics, "1st quartile", "q1", "decimal");
        addMetric(metrics, "3rd quartile", "q3", "decimal");
        addMetric(metrics, "Maximum", "max", "decimal");
        stats.add(metrics, BorderLayout.CENTER);
        c.gridx = 0; c.gridy = 0; c.weighty = 0.45;
        body.add(stats, c);

        JPanel lower = new JPanel(new GridLayout(2, 2, 14, 14));
        lower.setOpaque(false);
        JPanel probabilities = cardPanel();
        probabilities.setLayout(new BoxLayout(probabilities, BoxLayout.Y_AXIS));
        probabilities.setBorder(new EmptyBorder(16, 18, 16, 18));
        probabilities.add(sectionHeading("Observed probabilities", "Relative frequencies in this dataset."));
        probabilities.add(Box.createVerticalStrut(12));
        addProbability(probabilities, "P(score < 4)", "p_below_4", "below_4_count", "sample_size");
        addProbability(probabilities, "P(4 ≤ score < 7)", "p_from_4_to_7", "from_4_to_7_count", "sample_size");
        addProbability(probabilities, "P(score ≥ 7)", "p_at_least_7", "at_least_7_count", "sample_size");
        addProbability(probabilities, "P(score < 4 | very high stress)", "p_below_4_given_very_high_stress", "below_4_given_very_high_count", "very_high_stress_count");
        addProbability(probabilities, "P(score ≥ 7 | low stress)", "p_at_least_7_given_low_stress", "at_least_7_given_low_count", "low_stress_count");
        lower.add(probabilities);

        JPanel distributionCard = cardPanel();
        distributionCard.setLayout(new BorderLayout(0, 12));
        distributionCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        distributionCard.add(sectionHeading("Score bands", "Counts grouped by the meter's score ranges."), BorderLayout.NORTH);
        distribution.setPreferredSize(new Dimension(360, 235));
        distributionCard.add(distribution, BorderLayout.CENTER);
        lower.add(distributionCard);

        JPanel stressCard = cardPanel();
        stressCard.setLayout(new BorderLayout(0, 10));
        stressCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        stressCard.add(sectionHeading("Average score by stress", "Dataset mean for each self-reported stress group."), BorderLayout.NORTH);
        stressMeanChart.setPreferredSize(new Dimension(360, 200));
        stressCard.add(stressMeanChart, BorderLayout.CENTER);
        lower.add(stressCard);

        JPanel spreadCard = cardPanel();
        spreadCard.setLayout(new BorderLayout(0, 10));
        spreadCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        spreadCard.add(sectionHeading("Score spread", "Five-number summary: minimum, quartiles, median, and maximum."), BorderLayout.NORTH);
        scoreBoxPlot.setPreferredSize(new Dimension(360, 200));
        spreadCard.add(scoreBoxPlot, BorderLayout.CENTER);
        lower.add(spreadCard);

        c.gridy = 1; c.weighty = 0.55;
        body.add(lower, c);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        analyticsStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        analyticsStatus.setForeground(MUTED);
        bottom.add(analyticsStatus, BorderLayout.WEST);

        JScrollPane analysisScroll = new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        analysisScroll.setBorder(BorderFactory.createEmptyBorder());
        analysisScroll.setOpaque(false);
        analysisScroll.getViewport().setOpaque(false);
        analysisScroll.getVerticalScrollBar().setUnitIncrement(16);
        page.add(intro, BorderLayout.NORTH);
        page.add(analysisScroll, BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private void addMetric(JPanel parent, String title, String key, String format) {
        JPanel box = new JPanel();
        box.setBackground(new Color(246, 250, 250));
        box.setBorder(new EmptyBorder(10, 10, 8, 10));
        box.setLayout(new BorderLayout());
        JLabel value = new JLabel(metricHtml(title, "—"));
        value.setForeground(INK);
        box.add(value, BorderLayout.CENTER);
        analyticsValues.put(key, value);
        metricTitles.put(key, title);
        parent.add(box);
    }

    private String metricHtml(String title, String value) {
        return "<html><span style='font-size:10pt;color:#667A87'>" + title
                + "</span><br><span style='font-size:17pt;font-weight:bold;color:#182B39'>" + value + "</span></html>";
    }

    private void addProbability(JPanel parent, String title, String probabilityKey, String countKey, String denominatorKey) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));
        String safeTitle = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        JLabel label = new JLabel("<html><b>" + safeTitle + "</b><br><font color='#667A87'>— / — observations</font></html>");
        label.setForeground(INK); label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel value = new JLabel("—");
        value.setForeground(TEAL.darker()); value.setFont(new Font("SansSerif", Font.BOLD, 17));
        row.putClientProperty("countKey", countKey);
        row.putClientProperty("denominatorKey", denominatorKey);
        row.putClientProperty("probabilityKey", probabilityKey);
        row.add(label, BorderLayout.CENTER); row.add(value, BorderLayout.EAST);
        parent.add(row);
        probabilityRows.add(row);
        analyticsValues.put(probabilityKey, value);
        analyticsValues.put(probabilityKey + "_label", label);
    }

    private void loadAnalytics() {
        analyticsStatus.setText("Loading dataset summary…");
        new SwingWorker<Map<String, Double>, Void>() {
            @Override protected Map<String, Double> doInBackground() throws Exception { return AnalyticsClient.fetch(); }
            @Override protected void done() {
                try {
                    Map<String, Double> values = get();
                    for (var entry : analyticsValues.entrySet()) {
                        String key = entry.getKey();
                        JLabel label = entry.getValue();
                        if (key.endsWith("_label")) continue;
                        if (metricTitles.containsKey(key)) {
                            double value = values.getOrDefault(key, Double.NaN);
                            String formatted = key.equals("sample_size") ? String.format("%,.0f", value) : String.format("%.2f", value);
                            label.setText(metricHtml(metricTitles.get(key), formatted));
                        }
                        else if (key.startsWith("p_")) label.setText(String.format("%.2f%%", values.getOrDefault(key, 0.0) * 100));
                        else {
                            double value = values.getOrDefault(key, Double.NaN);
                            label.setText(key.equals("sample_size") ? String.format("%,.0f", value) : String.format("%.2f", value));
                        }
                    }
                    for (JPanel row : probabilityRows) {
                        String countKey = (String) row.getClientProperty("countKey");
                        String denominatorKey = (String) row.getClientProperty("denominatorKey");
                        double numerator = values.getOrDefault(countKey, 0.0);
                        double denominator = values.getOrDefault(denominatorKey, 0.0);
                        JLabel line = (JLabel) row.getComponent(0);
                        String html = line.getText().replaceFirst("— / — observations", String.format("%.0f / %.0f observations", numerator, denominator));
                        line.setText(html);
                    }
                    distribution.setCounts(values.getOrDefault("below_4_count", 0.0), values.getOrDefault("from_4_to_7_count", 0.0), values.getOrDefault("at_least_7_count", 0.0), values.getOrDefault("sample_size", 0.0));
                    stressMeanChart.setMeans(values.getOrDefault("mean_low_stress", 0.0), values.getOrDefault("mean_medium_stress", 0.0), values.getOrDefault("mean_high_stress", 0.0), values.getOrDefault("mean_very_high_stress", 0.0));
                    scoreBoxPlot.setSummary(values.getOrDefault("min", 0.0), values.getOrDefault("q1", 0.0), values.getOrDefault("median", 0.0), values.getOrDefault("q3", 0.0), values.getOrDefault("max", 0.0));
                    analyticsLoaded = true;
                    analyticsStatus.setText("Figures calculated from python/data/students.csv.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    analyticsStatus.setText("Could not load figures: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private JPanel sectionHeading(String titleText, String subtitleText) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel(titleText);
        title.setForeground(INK); title.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(MUTED); subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        heading.add(title); heading.add(Box.createVerticalStrut(4)); heading.add(subtitle);
        return heading;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(65, 103, 112));
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setBorder(new EmptyBorder(0, 0, 7, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel field(String label, String key, JComponent input) {
        JPanel field = new JPanel(new BorderLayout(0, 5));
        field.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setForeground(new Color(62, 82, 94));
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        styleInput(input);
        field.add(title, BorderLayout.NORTH);
        field.add(input, BorderLayout.CENTER);
        fields.put(key, input);
        return field;
    }

    private void styleInput(JComponent input) {
        input.setFont(new Font("SansSerif", Font.PLAIN, 13));
        input.setForeground(INK);
        input.setBackground(new Color(250, 252, 252));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 9, 7, 9)));
        input.setPreferredSize(new Dimension(160, 36));
    }

    private JPanel cardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(1, 1, 1, 1)));
        return card;
    }

    private static JTextField text(String initial) { return new JTextField(initial); }
    private static JComboBox<String> combo(String... choices) { return new JComboBox<>(choices); }

    private String value(String key) {
        JComponent component = fields.get(key);
        return component instanceof JComboBox<?> box ? String.valueOf(box.getSelectedItem()) : ((JTextField) component).getText().trim();
    }

    private void submit() {
        try {
            StudentRecord record = new StudentRecord(
                    Integer.parseInt(value("age")), value("gender"), value("country"), value("academic_level"),
                    value("most_used_platform"), value("purpose_of_use"), Double.parseDouble(value("avg_daily_usage_hours")),
                    Integer.parseInt(value("daily_unlocks")), Double.parseDouble(value("study_hours")),
                    Double.parseDouble(value("physical_activity_hours")), Double.parseDouble(value("sleep_hours_per_night")), value("stress_level"));
            predict.setEnabled(false);
            predict.setText("Calculating…");
            status.setText("Sending your check-in to the model…");
            new SwingWorker<Double, Void>() {
                @Override protected Double doInBackground() throws Exception {
                    return PredictionClient.predict(record);
                }
                @Override protected void done() {
                    predict.setEnabled(true);
                    predict.setText("Check my score");
                    try {
                        double score = get();
                        gauge.setScore(score);
                        resultTag.setText(bandFor(score));
                        resultCopy.setText("<html>Estimated wellbeing score: <b>" + String.format("%.2f", score)
                                + " / 10</b><br><br>Saved to your local prediction history.</html>");
                        status.setText("Check-in complete and saved.");
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        resultTag.setText("COULD NOT CREATE THE METER");
                        resultCopy.setText("<html>" + escapeHtml(cause.getMessage())
                                + "<br><br>Check that the local backend is running.</html>");
                        status.setText("Prediction was not completed.");
                    }
                }
            }.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers in the numeric fields.", "Check your answers", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String bandFor(double score) {
        if (score < 4.0) return "GIVE YOURSELF A LITTLE EXTRA CARE";
        if (score < 7.0) return "A MIXED BUT STEADY SIGNAL";
        return "A STRONGER WELLBEING SIGNAL";
    }

    private String escapeHtml(String text) {
        return text == null ? "Unexpected error" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class ScoreGauge extends JPanel {
        private double score = -1;
        private ScoreGauge() { setOpaque(false); setPreferredSize(new Dimension(280, 225)); }
        private void setScore(double value) { score = Math.max(0, Math.min(10, value)); repaint(); }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double cx = getWidth() / 2.0;
            double arcW = Math.min(getWidth() - 30, 232);
            double arcH = arcW * 0.60;
            double x = cx - arcW / 2.0;
            double y = 18;
            g.setStroke(new BasicStroke(17, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(235, 241, 241));
            g.draw(new Arc2D.Double(x, y, arcW, arcW, 180, -180, Arc2D.OPEN));

            Color[] colors = {new Color(233, 117, 101), new Color(243, 188, 91), new Color(67, 184, 145)};
            for (int i = 0; i < colors.length; i++) {
                double segmentStart = 180 - i * 60;
                double progress = score < 0 ? 0 : Math.max(0, Math.min(60, (score / 10.0) * 180 - i * 60));
                if (progress > 0) {
                    g.setColor(colors[i]);
                    g.draw(new Arc2D.Double(x, y, arcW, arcW, segmentStart, -progress, Arc2D.OPEN));
                }
            }

            double cy = y + arcW / 2.0;
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(151, 171, 177));
            for (int i = 0; i <= 10; i += 2) {
                double angle = Math.toRadians(180 - i * 18);
                double rx = arcW / 2.0;
                double ry = arcW / 2.0;
                int x1 = (int) (cx + (rx - 16) * Math.cos(angle));
                int y1 = (int) (cy - (ry - 16) * Math.sin(angle));
                int x2 = (int) (cx + (rx - 6) * Math.cos(angle));
                int y2 = (int) (cy - (ry - 6) * Math.sin(angle));
                g.drawLine(x1, y1, x2, y2);
            }

            String scoreText = score < 0 ? "—" : String.format("%.2f", score);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            g.setColor(INK);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(scoreText, (float) (cx - fm.stringWidth(scoreText) / 2.0), (float) (cy + 57));
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(MUTED);
            String label = "OUT OF 10";
            fm = g.getFontMetrics();
            g.drawString(label, (float) (cx - fm.stringWidth(label) / 2.0), (float) (cy + 79));

            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(new Color(211, 120, 105));
            g.drawString("ATTENTION", (float) (x - 2), (float) (cy + 27));
            g.setColor(new Color(58, 151, 125));
            String high = "DOING WELL";
            g.drawString(high, (float) (x + arcW - g.getFontMetrics().stringWidth(high) + 2), (float) (cy + 27));
            if (score >= 0) {
                double angle = Math.toRadians(180 - score * 18);
                double dotX = cx + (arcW / 2.0) * Math.cos(angle);
                double dotY = cy - (arcW / 2.0) * Math.sin(angle);
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(dotX - 6, dotY - 6, 12, 12));
                g.setColor(INK);
                g.setStroke(new BasicStroke(2));
                g.draw(new Ellipse2D.Double(dotX - 6, dotY - 6, 12, 12));
            }
            g.dispose();
        }
    }

    private static final class ScoreDistribution extends JPanel {
        private final String[] names = {"Below 4", "4 to <7", "7 and above"};
        private final Color[] colors = {new Color(233, 117, 101), new Color(243, 188, 91), new Color(67, 184, 145)};
        private double[] counts = {0, 0, 0};
        private double total = 1;

        private ScoreDistribution() { setOpaque(false); }

        private void setCounts(double below, double middle, double high, double sampleSize) {
            counts = new double[]{below, middle, high};
            total = Math.max(1, sampleSize);
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int top = 24, bottom = getHeight() - 42;
            int availableH = Math.max(70, bottom - top);
            int barW = Math.min(54, (getWidth() - 90) / 5);
            int groupW = getWidth() / 3;
            for (int i = 0; i < counts.length; i++) {
                int barH = (int) Math.round(availableH * counts[i] / total);
                int x = groupW * i + (groupW - barW) / 2;
                int y = bottom - barH;
                g.setColor(new Color(234, 240, 241));
                g.fillRoundRect(x, top, barW, availableH, 10, 10);
                g.setColor(colors[i]);
                g.fillRoundRect(x, y, barW, Math.max(4, barH), 10, 10);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                String count = String.format("%,.0f", counts[i]);
                FontMetrics fm = g.getFontMetrics();
                g.setColor(INK);
                g.drawString(count, x + (barW - fm.stringWidth(count)) / 2, Math.max(14, y - 7));
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                fm = g.getFontMetrics();
                g.setColor(MUTED);
                g.drawString(names[i], groupW * i + (groupW - fm.stringWidth(names[i])) / 2, bottom + 20);
            }
            g.dispose();
        }
    }

    private static final class StressMeanChart extends JPanel {
        private final String[] groups = {"Low", "Medium", "High", "Very high"};
        private final Color[] colors = {new Color(116, 181, 151), new Color(133, 177, 180), new Color(222, 184, 128), new Color(207, 145, 135)};
        private double[] means = {0, 0, 0, 0};

        private StressMeanChart() { setOpaque(false); }
        private void setMeans(double low, double medium, double high, double veryHigh) {
            means = new double[]{low, medium, high, veryHigh}; repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 34, right = getWidth() - 12, top = 22, bottom = getHeight() - 38;
            int plotH = Math.max(70, bottom - top);
            for (int tick = 0; tick <= 10; tick += 5) {
                int y = bottom - tick * plotH / 10;
                g.setColor(new Color(231, 237, 233)); g.drawLine(left, y, right, y);
                g.setFont(new Font("SansSerif", Font.PLAIN, 9)); g.setColor(MUTED);
                g.drawString(String.valueOf(tick), 8, y + 4);
            }
            int groupW = Math.max(30, (right - left) / 4);
            int barW = Math.min(36, groupW / 2);
            for (int i = 0; i < means.length; i++) {
                int height = (int) Math.round(plotH * means[i] / 10.0);
                int x = left + groupW * i + (groupW - barW) / 2;
                int y = bottom - height;
                g.setColor(colors[i]); g.fillRoundRect(x, y, barW, Math.max(3, height), 9, 9);
                String number = String.format("%.2f", means[i]);
                g.setFont(new Font("SansSerif", Font.BOLD, 10)); g.setColor(INK);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(number, x + (barW - fm.stringWidth(number)) / 2, Math.max(13, y - 5));
                g.setFont(new Font("SansSerif", Font.PLAIN, 9)); g.setColor(MUTED);
                fm = g.getFontMetrics();
                g.drawString(groups[i], left + groupW * i + (groupW - fm.stringWidth(groups[i])) / 2, bottom + 16);
            }
            g.dispose();
        }
    }

    private static final class ScoreBoxPlot extends JPanel {
        private double min, q1, median, q3, max;
        private ScoreBoxPlot() { setOpaque(false); }
        private void setSummary(double min, double q1, double median, double q3, double max) {
            this.min = min; this.q1 = q1; this.median = median; this.q3 = q3; this.max = max; repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 26, right = getWidth() - 26, axisY = getHeight() - 42;
            int centerY = Math.max(54, getHeight() / 2 - 4);
            double scale = (right - left) / 10.0;
            int xMin = (int) (left + min * scale), xQ1 = (int) (left + q1 * scale);
            int xMedian = (int) (left + median * scale), xQ3 = (int) (left + q3 * scale);
            int xMax = (int) (left + max * scale);
            g.setStroke(new BasicStroke(2)); g.setColor(new Color(106, 150, 136));
            g.drawLine(xMin, centerY, xMax, centerY);
            g.drawLine(xMin, centerY - 9, xMin, centerY + 9);
            g.drawLine(xMax, centerY - 9, xMax, centerY + 9);
            g.setColor(new Color(220, 237, 227));
            g.fillRoundRect(xQ1, centerY - 20, Math.max(3, xQ3 - xQ1), 40, 8, 8);
            g.setColor(new Color(77, 143, 117)); g.drawRoundRect(xQ1, centerY - 20, Math.max(3, xQ3 - xQ1), 40, 8, 8);
            g.setStroke(new BasicStroke(3)); g.drawLine(xMedian, centerY - 20, xMedian, centerY + 20);
            int[] xs = {xMin, xQ1, xMedian, xQ3, xMax};
            String[] labels = {"Min", "Q1", "Median", "Q3", "Max"};
            double[] vals = {min, q1, median, q3, max};
            g.setFont(new Font("SansSerif", Font.BOLD, 9)); g.setColor(INK);
            for (int i = 0; i < xs.length; i++) {
                String text = String.format("%s %.1f", labels[i], vals[i]);
                int textW = g.getFontMetrics().stringWidth(text);
                int tx = Math.max(left - 12, Math.min(right - textW + 12, xs[i] - textW / 2));
                g.drawString(text, tx, centerY - 29);
            }
            g.setStroke(new BasicStroke(1)); g.setColor(new Color(170, 185, 178));
            g.drawLine(left, axisY, right, axisY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 9)); g.setColor(MUTED);
            for (int tick = 0; tick <= 10; tick += 2) {
                int x = (int) (left + tick * scale);
                g.drawLine(x, axisY - 3, x, axisY + 3);
                String text = String.valueOf(tick); int textW = g.getFontMetrics().stringWidth(text);
                g.drawString(text, x - textW / 2, axisY + 16);
            }
            g.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MentalHealthApp().setVisible(true));
    }
}
