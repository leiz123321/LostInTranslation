package translation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * A country-name translator that updates when the country or language selection changes.
 */
public class GUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Country Name Translator");
            frame.setContentPane(createMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setMinimumSize(frame.getSize());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Build the translator controls. Call this method on the Swing event dispatch thread.
     */
    static JPanel createMainPanel() {
        Translator translator = new JSONTranslator();
        CountryCodeConverter countryConverter = new CountryCodeConverter();
        LanguageCodeConverter languageConverter = new LanguageCodeConverter();

        List<String> countryNames = new ArrayList<>();
        for (String countryCode : translator.getCountryCodes()) {
            countryNames.add(countryConverter.fromCountryCode(countryCode));
        }
        Collections.sort(countryNames);

        JList<String> countryList = new JList<>(countryNames.toArray(new String[0]));
        countryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        countryList.setVisibleRowCount(10);

        List<String> languageNames = new ArrayList<>();
        for (String languageCode : translator.getLanguageCodes()) {
            languageNames.add(languageConverter.fromLanguageCode(languageCode));
        }
        Collections.sort(languageNames);

        JComboBox<String> languageComboBox = new JComboBox<>(languageNames.toArray(new String[0]));
        JLabel languageLabel = new JLabel("Language:");
        languageLabel.setLabelFor(languageComboBox);
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        languagePanel.add(languageLabel);
        languagePanel.add(languageComboBox);

        JTextArea resultText = new JTextArea(2, 24);
        resultText.setEditable(false);
        resultText.setLineWrap(true);
        resultText.setWrapStyleWord(true);
        resultText.setOpaque(false);
        resultText.setFont(languageLabel.getFont());
        JLabel translationLabel = new JLabel("Translation:");
        translationLabel.setLabelFor(resultText);
        JPanel translationPanel = new JPanel(new BorderLayout(8, 0));
        translationPanel.add(translationLabel, BorderLayout.WEST);
        translationPanel.add(resultText, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new BorderLayout(0, 8));
        controlsPanel.add(languagePanel, BorderLayout.NORTH);
        controlsPanel.add(translationPanel, BorderLayout.CENTER);

        JLabel countryLabel = new JLabel("Country:");
        countryLabel.setLabelFor(countryList);
        JScrollPane countryScrollPane = new JScrollPane(countryList);
        countryScrollPane.setPreferredSize(new Dimension(420, 220));
        JPanel countryPanel = new JPanel(new BorderLayout(0, 4));
        countryPanel.add(countryLabel, BorderLayout.NORTH);
        countryPanel.add(countryScrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(controlsPanel, BorderLayout.NORTH);
        mainPanel.add(countryPanel, BorderLayout.CENTER);

        Runnable updateTranslation = () -> {
            String country = countryList.getSelectedValue();
            String language = (String) languageComboBox.getSelectedItem();
            if (country == null || language == null) {
                resultText.setText("Select a country and language.");
                return;
            }

            // The country converter returns uppercase codes; JSONTranslator uses lowercase codes.
            String countryCode = countryConverter.fromCountry(country).toLowerCase(Locale.ROOT);
            String languageCode = languageConverter.fromLanguage(language).toLowerCase(Locale.ROOT);
            String result = translator.translate(countryCode, languageCode);
            resultText.setText(result == null ? "No translation found!" : result);
        };

        countryList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateTranslation.run();
            }
        });
        languageComboBox.addActionListener(event -> updateTranslation.run());

        countryList.setSelectedValue(countryConverter.fromCountryCode("can"), true);
        languageComboBox.setSelectedItem(languageConverter.fromLanguageCode("en"));
        updateTranslation.run();
        return mainPanel;
    }
}
