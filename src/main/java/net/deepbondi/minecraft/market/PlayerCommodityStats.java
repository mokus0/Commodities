package net.deepbondi.minecraft.market;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.avaje.ebean.validation.Past;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity()
@Table(name="pc_stats")
public class PlayerCommodityStats {
    @Id
    private int id;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    @NotEmpty
    private String playerName;
    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    @NotNull
    private int commodityId;
    public int getCommodityId() {
        return commodityId;
    }
    public void setCommodityId(int commodityId) {
        this.commodityId = commodityId;
    }
    
    @NotNull
    private long numBought;
    public long getNumBought() {
        return numBought;
    }
    public void setNumBought(long numBought) {
        this.numBought = numBought;
    }
    
    @NotNull
    private long numSold;
    public long getNumSold() {
        return numSold;
    }
    public void setNumSold(long numSold) {
        this.numSold = numSold;
    }
    
    @NotNull
    private double moneySpent;
    public double getMoneySpent() {
        return moneySpent;
    }
    public void setMoneySpent(double moneySpent) {
        this.moneySpent = moneySpent;
    }
    
    @NotNull
    private double moneyGained;
    public double getMoneyGained() {
        return moneyGained;
    }
    public void setMoneyGained(double moneyGained) {
        this.moneyGained = moneyGained;
    }
}

