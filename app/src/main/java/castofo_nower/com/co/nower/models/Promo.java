package castofo_nower.com.co.nower.models;


public class Promo {

    private int id;
    private String title;
    private String expirationDate;
    private int availableRedemptions;


    public Promo(int id, String title, String expirationDate, int availableRedemptions) {
        this.id = id;
        this.title = title;
        this.expirationDate = expirationDate;
        this.availableRedemptions = availableRedemptions;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public int getAvailableRedemptions() {
        return availableRedemptions;
    }
}
