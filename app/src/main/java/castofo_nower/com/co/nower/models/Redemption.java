package castofo_nower.com.co.nower.models;

public class Redemption {

  private String code;
  private int promoId;
  private boolean isRedeemed;
  private String storeName;

  public Redemption(String code, int promoId, boolean isRedeemed,
                    String storeName) {
    this.code = code;
    this.promoId = promoId;
    this.isRedeemed = isRedeemed;
    this.storeName = storeName;
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

  public String getStoreName() { return storeName; }

}
