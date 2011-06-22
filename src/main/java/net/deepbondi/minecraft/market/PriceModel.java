package net.deepbondi.minecraft.market;

public interface PriceModel {
    double checkBuyPrice(Commodity item, long qty, CommoditiesMarket market);
    double checkSellPrice(Commodity item, long qty, CommoditiesMarket market);
}

