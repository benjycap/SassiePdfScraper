import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Scraper navigates through web pages on Sassie in order to acquire relevant information for the user
 */
public class Scraper {

    private WebClient webClient;
    private HtmlPage currentPage;

    public ArrayList<String> clientSurveyNames = new ArrayList<String>();

    // Set up the WebClient to be used throughout the PDF scraping operation
    public void setupWebClient() {
        webClient = new WebClient();
        webClient.getOptions().setActiveXNative(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }

    // Login to Sassie and return the main admin page
    public void loginToMainPage() throws IOException {
        HtmlPage page = webClient.getPage(SassieUrls.LOGIN_PAGE);
        HtmlTextInput usernameInput = page.getElementByName("login");
        HtmlPasswordInput passwordInput = page.getElementByName("password");
        HtmlAnchor submitAnchor = page.getAnchorByHref("javascript: document.forms[0].submit();");

        usernameInput.setText("***");
        passwordInput.setText("***");

        currentPage = submitAnchor.click();
    }

    // Open specific shop log page from Main Page
    public void openShopLogFromMainPage() throws IOException {
        HtmlAnchor shopLogAnchor = currentPage.getAnchorByHref("javascript: window.document.forms[0].mode.value='shoptools'; window.document.forms[0].submit();");
        // Click go button
        currentPage = shopLogAnchor.click();
    }

    public void storeClientSurveySelectionNames() throws Exception {
        assertShopLog();

        HtmlSelect clientSurveySelector = currentPage.getElementByName("ClientSurveyID");

        for (HtmlOption option : clientSurveySelector.getOptions()) {
            Main.p(clientSurveyNames.size() + ": " + option.asText());
            clientSurveyNames.add(option.asText());
        }
    }

    public void selectClient(int clientIndex) throws Exception {
        assertShopLog();
        // Throw an error if trying to access a nonexistent shop log
        if (clientIndex >= clientSurveyNames.size())
            throw new ArrayIndexOutOfBoundsException();

        HtmlSelect clientSurveySelector = currentPage.getElementByName("ClientSurveyID");
        currentPage = (HtmlPage)clientSurveySelector.getOption(clientIndex).setSelected(true);
    }

    // Click go button on shop log page
    public void shopLogPageClickGoButton() throws Exception {
        assertShopLog();
        HtmlAnchor goButton = currentPage.getFirstByXPath("/html/body/form/div/table/tbody/tr[3]/td[6]/a");
        currentPage = goButton.click();
    }

    private static final int NONE = 0;
    private static final int ANCHORS = 1;
    private static final int SELECTOR = 2;

    public int getPageChangerType() throws Exception {
        assertShopLog();
        HtmlDivision outerDiv = currentPage.getFirstByXPath("/html/body/form/div");
        DomNode child = outerDiv.getFirstChild();

        if (child instanceof HtmlParagraph) {
            child = child.getFirstChild();
            if (child instanceof HtmlSpan) {
                return NONE;
            } else if (child instanceof HtmlHiddenInput) {
                return ANCHORS;
            }
        } else if (child instanceof HtmlHiddenInput) {
            return SELECTOR;
        }

        // Something went wrong
        return -1;
    }



    public ArrayList<Shop> getAllShopsForClient() throws Exception {
        assertShopLog();

        ArrayList<Shop> allClientShops;
        int numPages;

        int pageChangerType = getPageChangerType();
        switch (pageChangerType) {
            case NONE:
                return getShopsOnCurrentPage();
            case ANCHORS:
                allClientShops = new ArrayList<Shop>();
                HtmlParagraph containerParagraph = currentPage.getFirstByXPath("/html/body/form/div/p[1]");
                numPages = containerParagraph.getChildElementCount() - 2;
                for (int p = 0; p < numPages; p++) {
                    if (p > 0) {
                        currentPage = webClient.getPage(SassieUrls.SHOP_LOG_PAGE_PREFIX + p);
                    }
                    allClientShops.addAll(getShopsOnCurrentPage());
                }
                return allClientShops;
            case SELECTOR:
                allClientShops = new ArrayList<Shop>();
                HtmlSelect selector = currentPage.getFirstByXPath("/html/body/form/div/div[1]/p/select");
                numPages = selector.getOptionSize();
                for (int p = 0; p < numPages; p++) {
                    if (p > 0) {
                        currentPage = webClient.getPage(SassieUrls.SHOP_LOG_PAGE_PREFIX + p);
                    }
                    allClientShops.addAll(getShopsOnCurrentPage());
                }
                return allClientShops;
            default:
                throw new Exception("Page Changer Type could not be identified");
        }
    }

    public ArrayList<Shop> getShopsOnCurrentPage() throws Exception {
        assertShopLog();

        ArrayList<Shop> shops = new ArrayList<Shop>();
        HtmlTableBody logTable = currentPage.getFirstByXPath("/html/body/form/div/table/tbody");
        List<HtmlTableRow> rows = logTable.getRows();
        HtmlTableRow row;
        // Only rows from 4 to penultimate row are relevant
        for (int i = 4; i < rows.size() - 1; i++) {
            row = rows.get(i);
            Shop shop = new Shop(row);
            shops.add(shop);
        }
        return shops;
    }

    private void assertShopLog() throws Exception {
        if (!currentPage.getTitleText().equals("Admin Shop Log"))
            throw new Exception("Current page is not Admin Shop Log: "+currentPage.getTitleText());
    }

    public void closeAllWindows() {
        webClient.closeAllWindows();
    }


    // debug

    static void p(Object s) {
        System.out.println(s.toString());
    }

    static void pPageChangerType(int t) {
        String out;
        switch (t) {
            case 0:
                out = "NONE";
                break;
            case 1:
                out = "ANCHORS";
                break;
            case 2:
                out = "SELECTOR";
                break;
            default:
                out = "ERROR";
                break;
        }
        p(out);
    }
}
