import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject; // Add a JSON library like org.json

public class CurrencyConverterUsingGUI extends JFrame {
    private JComboBox<String> fromCurrency;
    private JComboBox<String> toCurrency;
    private JTextField amountField;
    private JLabel resultLabel;
    private JLabel exchangeRateLabel;
    private JTextArea historyArea;
    private Map<String, Double> exchangeRates;
    private Map<String, String> currencySymbols;
    private JCheckBox conversionFeeCheckbox;
    private JSlider conversionFeeSlider;
    private double conversionFeePercentage;

    // Your API key is set here
    private static final String API_KEY = "76494c1a4ef317b69ce4f49e";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    public CurrencyConverterUsingGUI() {
        setTitle("Currency Converter");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeCurrencySymbols();
        initializeComponents();

        setVisible(true);
    }

    private void initializeCurrencySymbols() {
        currencySymbols = new HashMap<>();
        currencySymbols.put("USD", "$");
        currencySymbols.put("EUR", "€");
        currencySymbols.put("GBP", "£");
        currencySymbols.put("INR", "₹");
        currencySymbols.put("JPY", "¥");
    }

    private void initializeComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel fromLabel = new JLabel("From:");
        fromCurrency = new JComboBox<>(new String[]{"USD", "EUR", "GBP", "INR", "JPY"});
        fromCurrency.addActionListener(e -> updateExchangeRate());

        JLabel toLabel = new JLabel("To:");
        toCurrency = new JComboBox<>(new String[]{"USD", "EUR", "GBP", "INR", "JPY"});
        toCurrency.addActionListener(e -> updateExchangeRate());

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JButton convertButton = new JButton("Convert");
        convertButton.addActionListener(new ConvertButtonListener());

        JButton swapButton = new JButton("Swap");
        swapButton.addActionListener(e -> swapCurrencies());

        resultLabel = new JLabel("Result: ");
        exchangeRateLabel = new JLabel("Exchange Rate: ");

        // Conversion Fee Components
        conversionFeeCheckbox = new JCheckBox("Apply Conversion Fee");
        conversionFeeSlider = new JSlider(0, 10, 2); // Fee percentage (0-10%)
        conversionFeeSlider.setEnabled(false); // Disabled until checkbox is selected
        conversionFeeSlider.setMajorTickSpacing(2);
        conversionFeeSlider.setPaintTicks(true);
        conversionFeeSlider.setPaintLabels(true);
        conversionFeeCheckbox.addActionListener(e -> conversionFeeSlider.setEnabled(conversionFeeCheckbox.isSelected()));

        // History Section
        historyArea = new JTextArea(5, 30);
        historyArea.setEditable(false);
        JScrollPane historyScrollPane = new JScrollPane(historyArea);
        JLabel historyLabel = new JLabel("History:");

        panel.add(fromLabel);
        panel.add(fromCurrency);
        panel.add(toLabel);
        panel.add(toCurrency);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(conversionFeeCheckbox);
        panel.add(conversionFeeSlider);
        panel.add(swapButton);
        panel.add(convertButton);
        panel.add(exchangeRateLabel);
        panel.add(resultLabel);
        panel.add(historyLabel);
        panel.add(historyScrollPane);

        add(panel, BorderLayout.CENTER);
        updateExchangeRate();
    }

    private class ConvertButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String from = (String) fromCurrency.getSelectedItem();
                String to = (String) toCurrency.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                conversionFeePercentage = conversionFeeCheckbox.isSelected() ? conversionFeeSlider.getValue() : 0;

                if (amount < 0) {
                    throw new NumberFormatException();
                }

                double result = convertCurrency(from, to, amount);
                String resultText = currencySymbols.get(to) + String.format("%.2f", result);
                resultLabel.setText("Result: " + resultText);

                // Add to history
                String historyEntry = String.format("Converted %.2f %s to %s. Fee: %.2f%%. Result: %s\n",
                        amount, from, to, conversionFeePercentage, resultText);
                historyArea.append(historyEntry);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid positive amount.");
            }
        }
    }

    private double convertCurrency(String from, String to, double amount) {
        double fromRate = exchangeRates.get(from);
        double toRate = exchangeRates.get(to);
        double result = amount * (toRate / fromRate);

        // Apply conversion fee
        if (conversionFeePercentage > 0) {
            double fee = (conversionFeePercentage / 100) * result;
            result -= fee;
        }

        return result;
    }

    private void updateExchangeRate() {
        String from = (String) fromCurrency.getSelectedItem();
        String to = (String) toCurrency.getSelectedItem();
        fetchExchangeRates(from);
        double fromRate = exchangeRates.get(from);
        double toRate = exchangeRates.get(to);
        double rate = toRate / fromRate;
        exchangeRateLabel.setText("Exchange Rate: 1 " + from + " = " + String.format("%.4f", rate) + " " + to);
    }

    private void fetchExchangeRates(String baseCurrency) {
        try {
            String urlStr = API_URL + baseCurrency;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject rates = json.getJSONObject("conversion_rates");

            // Update the exchangeRates map with the latest values
            exchangeRates = new HashMap<>();
            exchangeRates.put("USD", rates.getDouble("USD"));
            exchangeRates.put("EUR", rates.getDouble("EUR"));
            exchangeRates.put("GBP", rates.getDouble("GBP"));
            exchangeRates.put("INR", rates.getDouble("INR"));
            exchangeRates.put("JPY", rates.getDouble("JPY"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void swapCurrencies() {
        String from = (String) fromCurrency.getSelectedItem();
        String to = (String) toCurrency.getSelectedItem();
        fromCurrency.setSelectedItem(to);
        toCurrency.setSelectedItem(from);
        updateExchangeRate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CurrencyConverterUsingGUI::new);
    }
}
