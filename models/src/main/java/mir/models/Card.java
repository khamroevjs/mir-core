package mir.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

/**
 * For Fundraising
 */
@Entity
public class Card {

    @Id
    private String number;
    private LocalDate expiryDate;
    private String holderName;
    private String CVC;
    private Long money;

    //region Getters and Setters
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getCVC() {
        return CVC;
    }

    public void setCVC(String CVC) {
        this.CVC = CVC;
    }

    public Long getMoney() {
        return money;
    }

    public void setMoney(Long money) {
        this.money = money;
    }
    //endregion
}
