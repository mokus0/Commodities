package net.deepbondi.minecraft.market;

import org.bukkit.Material;

// A very simple model based on supply only.  The marginal cost of an item
// is determined by a power law function of the supply, adjusted to fix
// `basePricePerItem` as the price of a single item when the stock is at
// `referenceStockLevel' items, and `maxPricePerItem' as the price 
// when there is only one item left.
// 
// Note that `maxPricePerItem` is only a very rough approximation due to
// the use of an integral in place of a summation in `marketValue`.  This
// is an intentional compromise.
public class BondilandPriceModel implements PriceModel {
    private static final double basePricePerItem = 0.2;
    private static final int referenceStockLevel = 1000;
    private static final double maxPricePerItem = 30;

    private static final double buyerTax = 0.01;
    private static final double sellerTax = 0.01;

    // Compute the total value in the market for commodity with the given
    // stack size and stock level.
    // The proper computation would be the sum over the marginal value
    // function from stock levels 0 through qty-1.
    // This is an approximation (the integral of the same function from 0 to qty)
    private static double marketValue(final long qty) {
        final double p = basePricePerItem;
        final double y0 = maxPricePerItem;
        final double r = referenceStockLevel;

        final double alpha = StrictMath.log(y0 / p) / StrictMath.log(r) - 1.0;
        return y0 / (-alpha * StrictMath.pow((double) qty, alpha));
    }

    private static double basePrice(final Commodity item, final long qty) {
        final Material itemMaterial = Material.getMaterial(item.getItemId());

        // Cookies are always 1.0
        if (itemMaterial == Material.COOKIE) return StrictMath.abs(qty);

        final long stockLevel = item.getInStock();

        return StrictMath.abs(marketValue(stockLevel)
                - marketValue(stockLevel + qty));
    }

    @Override
    public double checkBuyPrice(final Commodity item, final long qty) {
        return (1.0 + buyerTax) * basePrice(item, -qty);
    }

    @Override
    public double checkSellPrice(final Commodity item, final long qty) {
        return basePrice(item, qty) / (1.0 + sellerTax);
    }
}

