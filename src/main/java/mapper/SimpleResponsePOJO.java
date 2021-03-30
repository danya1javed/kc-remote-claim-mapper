package mapper;

public class SimpleResponsePOJO {

  private String usertype;
  private String nic;
  private boolean subscription;

  public SimpleResponsePOJO() {
  }

  public String getUsertype() {
    return usertype;
  }

  public void setUsertype(String usertype) {
    this.usertype = usertype;
  }

  public String getNic() {
    return nic;
  }

  public void setNic(String nic) {
    this.nic = nic;
  }

  public boolean isSubscription() {
    return subscription;
  }

  public void setSubscription(boolean subscription) {
    this.subscription = subscription;
  }
}
