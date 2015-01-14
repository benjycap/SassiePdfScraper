import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model object class representing a shop visit
 */
public class Shop {

    public static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

    private int id;
    private Date shopDate;
    private String location;
    private String status;
    private String pdfUrl;
    private boolean isForDownload;

    public boolean isForDownload() {
        return isForDownload;
    }

    public void setForDownload(boolean isForDownload) {
        this.isForDownload = isForDownload;
    }

    public Shop(HtmlTableRow inputRow) {
        initId(inputRow);
        initDate(inputRow);
        initLocation(inputRow);
        initStatus(inputRow);
        initPdfUrl(inputRow);
        isForDownload = true;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public int getId() {
        return id;
    }

    public Date getShopDate() {
        return shopDate;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    private void initId(HtmlTableRow inputRow) {
        HtmlTableCell parseCell = inputRow.getCell(0);
        id = Integer.parseInt(parseCell.asText().trim());
    }

    private void initDate(HtmlTableRow inputRow) {
        HtmlTableCell parseCell = inputRow.getCell(2);
        try {
            shopDate = formatter.parse(parseCell.asText());
        } catch (ParseException pe) {
            shopDate = null;
        }
    }

    private void initLocation(HtmlTableRow inputRow) {
        HtmlTableCell parseCell = inputRow.getCell(4);
        String cellContents = parseCell.asText();
        // Trim everything before the first new line
        int secondLineIndex = cellContents.indexOf('\n') + 1;
        String locationWithShopId = cellContents.substring(secondLineIndex);
        // Trim everything before the first space character
        location = locationWithShopId.substring(locationWithShopId.indexOf(' ') + 1).trim();
    }

    private void initStatus(HtmlTableRow inputRow) {
        HtmlTableCell parseCell = inputRow.getCell(6);
        status = parseCell.asText();
    }

    private void initPdfUrl(HtmlTableRow inputRow) {
        HtmlAnchor viewLink = inputRow.getFirstByXPath("td[8]/a[3]");
        pdfUrl = SassieUrls.PDF_DOWNLOAD_PREFIX
                    + viewLink.getHrefAttribute().substring(3)
                    + SassieUrls.PDF_DOWNLOAD_SUFFIX;
    }

    @Override
    public String toString() {
        return ""+id+'\n'+shopDate+'\n'+location+'\n'+status+'\n'+pdfUrl+'\n';
    }

    public static String dateToString(Date date) {
        return (date == null) ? "---" : formatter.format(date);
    }
}
