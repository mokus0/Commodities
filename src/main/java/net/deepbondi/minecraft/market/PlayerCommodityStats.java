package net.deepbondi.minecraft.market;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pc_stats")
public class PlayerCommodityStats {
    public PlayerCommodityStats() {
    }

    public PlayerCommodityStats(final String playerName, final int commodityId, final long numBought, final long numSold, final double moneySpent, final double moneyGained) {
        this.playerName = playerName;
        this.commodityId = commodityId;
        this.numBought = numBought;
        this.numSold = numSold;
        this.moneySpent = moneySpent;
        this.moneyGained = moneyGained;
    }

    public void update(final long numBought, final long numSold, final double moneySpent, final double moneyGained) {
        this.numBought += numBought;
        this.numSold += numSold;
        this.moneySpent += moneySpent;
        this.moneyGained += moneyGained;
    }

    @Id
    private int id;

    @SuppressWarnings("UnusedDeclaration")
    public int getId() {
        return id;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setId(final int id) {
        this.id = id;
    }

    @NotEmpty
    private String playerName;

    @SuppressWarnings("UnusedDeclaration")
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    @NotNull
    private int commodityId;

    @SuppressWarnings("UnusedDeclaration")
    public int getCommodityId() {
        return commodityId;
    }

    public void setCommodityId(final int commodityId) {
        this.commodityId = commodityId;
    }

    @NotNull
    private long numBought;

    public long getNumBought() {
        return numBought;
    }

    public void setNumBought(final long numBought) {
        this.numBought = numBought;
    }

    @NotNull
    private long numSold;

    public long getNumSold() {
        return numSold;
    }

    public void setNumSold(final long numSold) {
        this.numSold = numSold;
    }

    @NotNull
    private double moneySpent;

    public double getMoneySpent() {
        return moneySpent;
    }

    public void setMoneySpent(final double moneySpent) {
        this.moneySpent = moneySpent;
    }

    @NotNull
    private double moneyGained;

    public double getMoneyGained() {
        return moneyGained;
    }

    public void setMoneyGained(final double moneyGained) {
        this.moneyGained = moneyGained;
    }
}

