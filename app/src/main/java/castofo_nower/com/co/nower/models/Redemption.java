package castofo_nower.com.co.nower.models;

public class Redemption {

  private String code;
  private int promoId;
  private boolean isRedeemed;
  private String storeName;
  private String storeLogoURL;

  public Redemption(String code, int promoId, boolean isRedeemed,
                    String storeName, String storeLogoURL) {
    this.code = code;
    this.promoId = promoId;
    this.isRedeemed = isRedeemed;
    this.storeName = storeName;
    this.storeLogoURL = storeLogoURL;
  }

  public String getCode() {
    return code;
  }

  public int getPromoId() {
    return promoId;
  }

  public boolean isRedeemed() {
    return isRedeemed;
  }

  public String getStoreName() {
    return storeName;
  }

  public String getStoreLogoURL() {
    return storeLogoURL;
  }

}
