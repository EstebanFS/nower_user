package castofo_nower.com.co.nower.models;


public class Redemption {

    private String code;
    private int promoId;
    private boolean isRedeemed;

    public Redemption(String code, int promoId, boolean isRedeemed) {
        this.code = code;
        this.promoId = promoId;
        this.isRedeemed = isRedeemed;
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
}
