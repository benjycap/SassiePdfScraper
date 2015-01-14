
import com.gargoylesoftware.htmlunit.html.*;
import javax.swing.*;

/**
 * Main entry point for application
 */
public class Main {

    static Scraper scraper = new Scraper();
    static GUI gui;

    public static void main(String[] args) {

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    gui = new GUI(scraper);
                }
            });

            // Initial scraper setup. Methods can be called directly because the GUI
            // has been invoked on a separate thread.
            scraper.setupWebClient();
            scraper.loginToMainPage();
            scraper.openShopLogFromMainPage();
            scraper.storeClientSurveySelectionNames();

            gui.populateClientSelector(scraper.clientSurveyNames);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //Debug Methods

    public static void p(Object s) {
        if (s==null) {
            System.out.println("NULL");
        } else {
            System.out.println(s.toString());
        }
    }

    public static void printElements(HtmlPage page) {
        for (HtmlElement e : page.getTabbableElements()) {
            p(e.toString());
        }
    }

    public static void printElementsWithXPaths(HtmlPage page) {
        for (HtmlElement e : page.getTabbableElements()) {
            String t = "\n" + e.toString() + "\n" + e.getCanonicalXPath();
            p(t);
        }
    }

    public static void printBody(HtmlPage page) {
        p(page.asText());
    }

}