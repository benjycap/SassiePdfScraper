import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * ShopFilter is a static class which is used to:
 * A) Hold a reference to the entire list of shops retrieved for the current client
 * B) Carry out operations on the above list to return a filtered list of shops according to user's input parameters
 */
public final class ShopFilter {

    private static ArrayList<Shop> allShops = new ArrayList<Shop>();

    public static void setAllShops(ArrayList<Shop> shops) {
        allShops = shops;
    }

    public static ArrayList<Shop> getAllShops() {
        return allShops;
    }

    // Publicly exposed filter method to be called by other classes. The component filtering operations are private.
    public static ArrayList<Shop> filter(String status, Date startDate, Date endDate) {
        ArrayList<Shop> filteredShops = new ArrayList<Shop>(allShops);
        filteredShops = filterByStatus(status, filteredShops);
        filteredShops = filterByDate(startDate, endDate, filteredShops);
        return filteredShops;
    }

    // Return all shops that match the status provided by the user.
    private static ArrayList<Shop> filterByStatus(String status, ArrayList<Shop> currentFilter) {
        if (status.equals("No Filter"))
            return currentFilter;

        ArrayList<Shop> filteredByStatus = new ArrayList<Shop>();
        for (Shop shop : currentFilter) {
            if (shop.getStatus().equals(status))
                filteredByStatus.add(shop);
        }
        return filteredByStatus;
    }

    // Takes an input list of shops and returns those that are dated between the start and end dates, inclusive.
    // If either the start or end date are null (i.e. empty on the input form), return all shops from/to the
    // non-null date.
    private static ArrayList<Shop> filterByDate(Date startDate, Date endDate, ArrayList<Shop> currentFilter) {
        if (startDate == null && endDate == null)
            return currentFilter;

        ArrayList<Shop> filteredByDate = new ArrayList<Shop>();
        for (Shop shop : currentFilter) {
            Date shopDate = shop.getShopDate();
            if (shopDate == null) continue;
            // Case startDate is null
            if (endDate == null) {
                if ((DateUtils.isSameDay(shopDate, startDate) || shopDate.after(startDate)))
                    filteredByDate.add(shop);
            }
            // Case endDate is null
            else if (startDate == null) {
                if (DateUtils.isSameDay(shopDate, endDate) || shopDate.before(endDate))
                    filteredByDate.add(shop);
            }
            // Case both dates are not null
            else if ((DateUtils.isSameDay(shopDate, startDate) || shopDate.after(startDate))
                        && (DateUtils.isSameDay(shopDate, endDate) || shopDate.before(endDate))) {
                filteredByDate.add(shop);
            }
        }
        return filteredByDate;
    }

}
