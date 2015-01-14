import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.*;

/**
 * Class containing GUI and associated background tasks
 */
public class GUI extends JFrame {

    private static final Color COLOR_BACKGROUND_PURPLE = new Color(0x56217a);
    private static final Color COLOR_TEXT_PURPLE = new Color(0xd93a96);
    private static final String STRING_LOADING = "Loading...";
    private static final String STRING_WELCOME = "<html><div style=\"text-align: center;\"><br>Welcome to the Sassie PDF Downloader!<br>Choose a client to begin.<html>";

    private final Scraper scraper;
    private ShopTableModel shopTableModel = new ShopTableModel(null);

    int selectedClientIndex = 0;
    // ClientChangeWorker carries out web scraping away from the GUI thread in order to
    // populate the shop table with relevant information according to a newly selected client.
    private class ClientChangerWorker extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            if (clientSelector.getItemCount() <= 1)
                return null;
            setClientSelectionEnabled(false);
            setFiltersEnabled(false);
            String selectedClientName = (String)clientSelector.getSelectedItem();
            selectedClientIndex = clientSelector.getSelectedIndex();
            // Empty shop table
            shopLogSummaryLabel.setText("Loading data from " + selectedClientName);
            scraper.selectClient(selectedClientIndex);
            scraper.shopLogPageClickGoButton();
            ArrayList<Shop> shopList = (selectedClientIndex == 0) ? null : scraper.getAllShopsForClient();
            ShopFilter.setAllShops(shopList);
            // Populate table with new data
            populateShopTable(shopList);
            populateStatusSelector(shopList);
            shopLogSummaryLabel.setText(getShopLogSummaryLabelText());
            setClientSelectionEnabled(true);
            setFiltersEnabled(true);
            return null;
        }
    }
    ClientChangerWorker clientChangerWorker;

    private class PdfDownloadWorker extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            for (Shop shop : shopTableModel.getShops()) {
                if (shop.isForDownload()) {
                    PdfDownloader.downloadPdf(shop);
                }

                if (isCancelled()) {
                    cleanup();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void done() {
            cleanup();
            super.done();
        }

        private void cleanup() {
            downloadButton.setText("Download Selected PDFs");
            downloadButton.setEnabled(true);
        }
    }
    PdfDownloadWorker pdfDownloadWorker;

    // Custom date picker
    private class DateLabelFormatter extends javax.swing.JFormattedTextField.AbstractFormatter {
        @Override
        public Object stringToValue(String text) throws ParseException {
            return Shop.formatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return Shop.formatter.format(cal.getTime());
            }
            return "";
        }
    }

    private class MyJLabel extends JLabel {
        public MyJLabel(String text) {
            super(text, SwingConstants.CENTER);
            setForeground(COLOR_TEXT_PURPLE);
        }
    }

    // Panels
    private JPanel mainPanel = new JPanel(new BorderLayout(50, 50));
    private JPanel panel1 = new JPanel(new GridLayout(4, 1));
    // Labels
    private MyJLabel welcomeLabel = new MyJLabel(STRING_WELCOME);
    private MyJLabel shopLogSummaryLabel = new MyJLabel("");
    // Client selector
    private JComboBox clientSelector = new JComboBox(new String[] {STRING_LOADING});
    private JPanel clientSelectorContainer = new JPanel();
    private JButton clientChangeButton = new JButton("Select Client");
    // Status Filter
    private JComboBox statusSelector = new JComboBox(new String[] {""});
    private JPanel statusSelectorContainer = new JPanel();
    private JButton filterButton = new JButton("Filter");
    // Date Filter
    private UtilDateModel model1 = new UtilDateModel();
    private UtilDateModel model2 = new UtilDateModel();
    private Properties JDatePanelProperties = new Properties();
    private JDatePanelImpl datePanel1, datePanel2;
    private JDatePickerImpl datePickerFrom, datePickerTo;
    // Shop Table
    private JTable shopTable = new JTable(shopTableModel);
    private JScrollPane shopTableScrollPane = new JScrollPane(shopTable);
    // Downloading
    private JPanel downloadPanel = new JPanel();
    private JButton downloadButton = new JButton("Download Selected PDFs");

    public GUI(Scraper inScraper) {
        // Window Name
        super("Sassie PDF Downloader");

        scraper = inScraper;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 400);
        //setResizable(false);

        panel1.add(welcomeLabel);

        // Selector Container and Selector
        clientSelectorContainer.add(clientSelector);
        clientSelectorContainer.add(clientChangeButton);
        clientChangeButton.setEnabled(false);
        clientSelectorContainer.setBackground(COLOR_BACKGROUND_PURPLE);
        clientChangeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((clientChangerWorker==null || clientChangerWorker.isDone())
                        // No need to get shops again if option is already selected
                        && selectedClientIndex != clientSelector.getSelectedIndex()) {
                    clientChangerWorker = new ClientChangerWorker();
                    clientChangerWorker.execute();
                }
            }
        });
        panel1.add(clientSelectorContainer);

        // Date Filter
        JDatePanelProperties.put("text.today", "Today");
        JDatePanelProperties.put("text.month", "Month");
        JDatePanelProperties.put("text.year", "Year");
        datePanel1 = new JDatePanelImpl(model1, JDatePanelProperties);
        datePanel2 = new JDatePanelImpl(model2, JDatePanelProperties);
        datePickerFrom = new JDatePickerImpl(datePanel1, new DateLabelFormatter());
        datePickerTo = new JDatePickerImpl(datePanel2, new DateLabelFormatter());
        statusSelectorContainer.add(datePickerFrom);
        statusSelectorContainer.add(datePickerTo);

        // Status Filter
        statusSelectorContainer.add(statusSelector);
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Populate a new shop table according to the results returned by filter
                String selectedStatus = (String) statusSelector.getSelectedItem();
                Date fromDate = (Date) datePickerFrom.getModel().getValue();
                Date toDate = (Date) datePickerTo.getModel().getValue();
                populateShopTable(ShopFilter.filter(selectedStatus, fromDate, toDate));
                // Once filter has populated results, append information to the shop log summary to inform user
                String newLogSummary = getShopLogSummaryLabelText();
                if (!(selectedStatus == "No Filter" && fromDate == null && toDate == null))
                        newLogSummary += "<br>"+shopTableModel.getRowCount()+" shops filtered";
                shopLogSummaryLabel.setText(newLogSummary);
            }
        });
        statusSelectorContainer.add(filterButton);
        statusSelectorContainer.setBackground(COLOR_BACKGROUND_PURPLE);
        panel1.add(statusSelectorContainer);

        panel1.add(shopLogSummaryLabel);

        // Download Panel
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pdfDownloadWorker == null || pdfDownloadWorker.isDone()) {
                    pdfDownloadWorker = new PdfDownloadWorker();
                    pdfDownloadWorker.execute();
                    downloadButton.setText("Cancel");
                }
                if (pdfDownloadWorker.getState() == SwingWorker.StateValue.STARTED) {
                    pdfDownloadWorker.cancel(false);
                    downloadButton.setText("Cancelling...");
                    downloadButton.setEnabled(false);
                }
            }
        });
        downloadPanel.add(downloadButton);

        // Set filter selection tools as invisible initially
        setFiltersVisible(false);

        // Panels
        mainPanel.setBackground(COLOR_BACKGROUND_PURPLE);
        panel1.setBackground(COLOR_BACKGROUND_PURPLE);
        // TableFrame and Table
        mainPanel.add(shopTableScrollPane, BorderLayout.CENTER);
        // Info, Selectors & Filters
        mainPanel.add(panel1, BorderLayout.NORTH);
        // Downloading
        mainPanel.add(downloadPanel, BorderLayout.SOUTH);
        add(mainPanel);

        setVisible(true);
    }

    public void populateClientSelector(ArrayList<String> clients) {
        clientSelector.removeAllItems();
        for (String client : clients) {
            clientSelector.addItem(client);
        }
        // Once clients are populated we can enable the selection button
        clientChangeButton.setEnabled(true);
    }

    public void populateShopTable(ArrayList<Shop> shops) {
        shopTableModel = new ShopTableModel(shops);
        shopTable.setModel(shopTableModel);
    }

    private void populateStatusSelector(ArrayList<Shop> shops) {
        HashSet<String> statuses = new HashSet<String>();
        if (shops != null) {
            for (Shop shop : shops) {
                statuses.add(shop.getStatus());
            }
        }
        statusSelector.removeAllItems();
        statusSelector.addItem("No Filter");
        for (String status : statuses) {
            statusSelector.addItem(status);
        }
        // Set filter selection tools as visible
        setFiltersVisible(true);
    }

    private void setClientSelectionEnabled(boolean bool) {
        clientSelector.setEnabled(bool);
        clientChangeButton.setEnabled(bool);
    }

    private void setFiltersEnabled(boolean bool) {
        datePickerFrom.setEnabled(bool);
        datePickerTo.setEnabled(bool);
        statusSelector.setEnabled(bool);
        filterButton.setEnabled(bool);
    }

    private void setFiltersVisible(boolean bool) {
        datePickerFrom.setVisible(bool);
        datePickerTo.setVisible(bool);
        statusSelector.setVisible(bool);
        filterButton.setVisible(bool);
    }

    private String getShopLogSummaryLabelText() {
        return "<html><div style=\"text-align: center;\">"+clientSelector.getItemAt(selectedClientIndex)+"<br>Shop Count: "+ShopFilter.getAllShops().size();
    }

}

