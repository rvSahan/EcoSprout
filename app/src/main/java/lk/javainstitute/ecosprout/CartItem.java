package lk.javainstitute.ecosprout;

public class CartItem {
    private String productId;
    private String name;
    private String price;
    private String image;
    private String qty;
    public CartItem() {
    }

    public CartItem(String productId, String name, String price, String image, String qty) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.image = image;
        this.qty = qty;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }
}
