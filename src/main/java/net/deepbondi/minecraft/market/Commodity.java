package net.deepbondi.minecraft.market;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "commodities")
public class Commodity {
    @Id
    private int id;

    public void setId(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @NotEmpty
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @NotNull
    private int itemId;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(final int itemId) {
        this.itemId = itemId;
    }

    @NotNull
    private byte byteData;

    public byte getByteData() {
        return byteData;
    }

    public void setByteData(final byte byteData) {
        this.byteData = byteData;
    }

    @NotNull
    private long inStock;

    public long getInStock() {
        return inStock;
    }

    public void setInStock(final long inStock) {
        this.inStock = inStock;
    }
}

