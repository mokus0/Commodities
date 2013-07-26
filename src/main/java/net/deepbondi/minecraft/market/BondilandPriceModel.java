package net.deepbondi.minecraft.market;

import net.deepbondi.minecraft.market.exceptions.NotReadyException;
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

    private static final double buyerTax = 0.01;
    private static final double sellerTax = 0.01;
    private final CommoditiesMarket plugin;

    public BondilandPriceModel(final CommoditiesMarket plugin) {
        this.plugin = plugin;
    }

    // Compute the total value in the market for commodity with the given
    // stack size and stock level.
    // The proper computation would be the sum over the marginal value
    // function from stock levels 0 through qty-1.
    // This is an approximation (the integral of the same function from 0 to qty)
    private static double marketValue(final long qty, double marketVolume) {
        // fix items at the "cookie stock level" to 1
        final double basePricePerItem = 1;
        final double referenceStockLevel = marketVolume;

        // and items that are almost gone cost a large portion of the money that exists
        final double maxPricePerItem = 0.1 * marketVolume;

        final double alpha = StrictMath.log(maxPricePerItem / basePricePerItem) / StrictMath.log(referenceStockLevel) - 1.0;
        return maxPricePerItem / (-alpha * StrictMath.pow((double) qty, alpha));
    }

    private double basePrice(final Commodity item, final long qty) {
        final Material itemMaterial = Material.getMaterial(item.getItemId());

        // Cookies are always 1.0
        if (itemMaterial == Material.COOKIE) return StrictMath.abs(qty);

        final long stockLevel = item.getInStock();

        double marketVolume;
        try {
            marketVolume = plugin.estimateMarketVolume();
        } catch (NotReadyException e) {
            marketVolume = 1e100;
        }

        return StrictMath.abs(marketValue(stockLevel, marketVolume)
                - marketValue(stockLevel + qty, marketVolume));
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

