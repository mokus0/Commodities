package net.deepbondi.minecraft.market;
import org.bukkit.Material;

// BondilandMixePriceModel by itself doesn't quite do what I want.  I want the
// big spike near zero, but I also want a fatter tail.  So, this is the sum of
// two copies of the basic model with different responses to scarcity.
public class BondilandMixedPriceModel implements PriceModel {
    BondilandPriceModel scarcity = new BondilandPriceModel();
    BondilandPriceModel abundance = new BondilandPriceModel();
    double minPricePerItem = 0.01;
    
    public BondilandMixedPriceModel() {
        scarcity.basePricePerItem = 2;
        scarcity.referenceStockLevel = 100;
        scarcity.maxPricePerItem = 90;
        
        abundance.basePricePerItem = 0.01;
        abundance.referenceStockLevel = 30000;
        abundance.maxPricePerItem = 0.015;
    }
    
    public double checkBuyPrice(Commodity item, long qty, CommoditiesMarket market) {
        // Cookies are always 1.0
        if (item.getItemId() == Material.COOKIE.getId()) return 1.0;
        return StrictMath.max(
              scarcity.checkBuyPrice(item, qty, market)
            + abundance.checkBuyPrice(item, qty, market),
            minPricePerItem * qty);
    }
    public double checkSellPrice(Commodity item, long qty, CommoditiesMarket market) {
        // Cookies are always 1.0
        if (item.getItemId() == Material.COOKIE.getId()) return 1.0;
        return StrictMath.max(
              scarcity.checkSellPrice(item, qty, market)
            + abundance.checkSellPrice(item, qty, market),
            minPricePerItem * qty);
    }}

