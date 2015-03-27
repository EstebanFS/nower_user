package castofo_nower.com.co.nower.models;


public class Branch {

    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private int storeId;
    private String storeName;

    public Branch(int id, String name, double latitude, double longitude, int storeId,
                  String storeName) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.storeId = storeId;
        this.storeName = storeName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }
}
