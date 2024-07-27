import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class CurrencyConverterUsingGUI extends JFrame {
    private JComboBox<String> fromCurrency;
    private JComboBox<String> toCurrency;
    private JTextField amountField;
    private JLabel resultLabel;
    private Map<String, Double> exchangeRates;

    public CurrencyConverterUsingGUI() {
        setTitle("Currency Converter");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        initializeExchangeRates();
        initializeComponents();

        setVisible(true);
    }

    private void initializeExchangeRates() {
        exchangeRates = new HashMap<>();
        // Example exchange rates (these would ideally come from an API)
        exchangeRates.put("USD", 1.0); // US Dollar
        exchangeRates.put("EUR", 0.85); // Euro
        exchangeRates.put("GBP", 0.75); // British Pound
        exchangeRates.put("INR", 74.0); // Indian Rupee
        exchangeRates.put("JPY", 110.0); // Japanese Yen
    }

    private void initializeComponents() {
        JLabel fromLabel = new JLabel("From:");
        fromCurrency = new JComboBox<>(exchangeRates.keySet().toArray(new String[0]));

        JLabel toLabel = new JLabel("To:");
        toCurrency = new JComboBox<>(exchangeRates.keySet().toArray(new String[0]));

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JButton convertButton = new JButton("Convert");
        convertButton.addActionListener(new ConvertButtonListener());

        resultLabel = new JLabel("Result: ");

        add(fromLabel);
        add(fromCurrency);
        add(toLabel);
        add(toCurrency);
        add(amountLabel);
        add(amountField);
        add(convertButton);
        add(resultLabel);
    }

    private class ConvertButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String from = (String) fromCurrency.getSelectedItem();
                String to = (String) toCurrency.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                double result = convertCurrency(from, to, amount);
                resultLabel.setText("Result: " + result);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid amount.");
            }
        }
    }

    private double convertCurrency(String from, String to, double amount) {
        double fromRate = exchangeRates.get(from);
        double toRate = exchangeRates.get(to);
        return amount * (toRate / fromRate);
    }

    public static void main(String[] args) {
        new CurrencyConverterUsingGUI();
    }
}
