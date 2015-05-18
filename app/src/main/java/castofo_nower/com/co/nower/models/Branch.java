package castofo_nower.com.co.nower.models;

import java.util.ArrayList;

public class Branch {

  private int id;
  private String name;
  private double latitude;
  private double longitude;
  private int storeId;
  private String storeName;
  private String storeLogoURL;
  private ArrayList<Integer> promosIds;

  public Branch(int id, String name, double latitude, double longitude,
                int storeId, String storeName, String storeLogoURL,
                ArrayList<Integer> promosIds) {
    this.id = id;
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.storeId = storeId;
    this.storeName = storeName;
    this.storeLogoURL = storeLogoURL;
    this.promosIds = promosIds;
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

  public String getStoreLogoURL() {
    return storeLogoURL;
  }

  public ArrayList<Integer> getPromosIds() {
    return promosIds;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setStoreId(int storeId) {
    this.storeId = storeId;
  }

  public void setStoreName(String storeName) {
    this.storeName = storeName;
  }

  public void setStoreLogoURL(String storeLogoURL) {
    this.storeLogoURL = storeLogoURL;
  }

  public void setPromosIds(ArrayList<Integer> promosIds) {
    this.promosIds = promosIds;
  }
}
