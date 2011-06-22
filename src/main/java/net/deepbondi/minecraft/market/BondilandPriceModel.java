package net.deepbondi.minecraft.market;
import org.bukkit.Material;

// A very simple model based on supply only.  The marginal cost of an item
// is determined by an exponential function of the supply, adjusted to fix
// `basePricePerStack` as the price of a single stack when the stock is at
// `referenceStockLevel' stacks, and `maxPricePerStack' as the price 
// of a full stack when there is only one item left.
//
// Specifically, to compute the marginal value of adding an item:
//
//  let r = referenceStockLevel
//      n = number of items on the market
//      s = stack size of the item type
//      y0 = maxPricePerStack
//      p = basePricePerStack
//   in p/s * (y0 / p) ** (1 - n / (s*r))
public class BondilandPriceModel implements PriceModel {
    private double basePricePerStack = 1;
    private int referenceStockLevel = 100;
    private double maxPricePerStack = 30;
    
    // Compute the total value in the market for commodity with the given
    // stack size and stock level.
        // The proper computation would be the sum over the marginal value
        // function from stock levels 0 through qty-1.
        // This is an approximation (the integral of the same function from 0 to qty)
    private double marketValue(int stackSize, long qty) {
        return indefiniteMarketValue(stackSize, qty)
             - indefiniteMarketValue(stackSize, 0);
    }
    private double indefiniteMarketValue(int stackSize, long qty) {
        double p = basePricePerStack;
        double y0 = maxPricePerStack;
        double r = referenceStockLevel;
        double s = stackSize;
        double alpha = p / y0;
        double lnAlpha = StrictMath.log(alpha);
        double q = s * lnAlpha;
        
        double x = (double) qty / s;
        
        return p*r / lnAlpha * StrictMath.pow(alpha, x/r - 1.0);
    }
    
    private double basePrice(Commodity item, long qty, CommoditiesMarket market) {
        Material itemMaterial = Material.getMaterial(item.getItemId());
        int stackSize = itemMaterial.getMaxStackSize();
        long stockLevel = item.getInStock();
        
        return StrictMath.abs(marketValue(stackSize, stockLevel) 
                            - marketValue(stackSize, stockLevel + qty));
    }
    
    private double buyerTax  = 0.00;
    private double sellerTax = 0.00;
    
    public double checkBuyPrice(Commodity item, long qty, CommoditiesMarket market) {
        // Cookies are always 1.0
        if (item.getItemId() == Material.COOKIE.getId()) return 1.0;
        return (1.0 + buyerTax) * basePrice(item, -qty, market);
    }
    public double checkSellPrice(Commodity item, long qty, CommoditiesMarket market) {
        // Cookies are always 1.0
        if (item.getItemId() == Material.COOKIE.getId()) return 1.0;
        return basePrice(item, qty, market) / (1.0 + sellerTax);
    }
}

