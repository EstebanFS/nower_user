package castofo_nower.com.co.nower.models;

public class Promo {

  private int id;
  private String title;
  private String expirationDate;
  private int availableRedemptions;
  private String description;
  private String terms;
  private String pictureURL;
  private String pictureHDURL;

  public Promo(int id, String title, String expirationDate,
               int availableRedemptions, String description, String terms,
               String pictureURL, String pictureHDURL) {
    this.id = id;
    this.title = title;
    this.expirationDate = expirationDate;
    this.availableRedemptions = availableRedemptions;
    this.description = description;
    this.terms = terms;
    this.pictureURL = pictureURL;
    this.pictureHDURL = pictureHDURL;
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

  public String getDescription() {
    return description;
  }

  public String getTerms() {
    return terms;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setExpirationDate(String expirationDate) {
    this.expirationDate = expirationDate;
  }

  public void setAvailableRedemptions(int availableRedemptions) {
    this.availableRedemptions = availableRedemptions;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setTerms(String terms) {
    this.terms = terms;
  }

  public String getPictureURL() {
    return pictureURL;
  }

  public String getPictureHDURL() {
    return pictureHDURL;
  }
}
