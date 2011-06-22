package net.deepbondi.minecraft.market;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.avaje.ebean.validation.Past;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity()
@Table(name="commodities")
public class Commodity {
    @Id
    private int id;
    public void setId(int id) {
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
    public void setName(String name) {
        this.name = name;
    }
    
    @NotNull
    private int itemId;
    public int getItemId() {
        return itemId;
    }
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    @NotNull
    private byte byteData;
    public byte getByteData() {
        return byteData;
    }
    public void setByteData(byte byteData) {
        this.byteData = byteData;
    }
    
    @NotNull
    private long inStock;
    public long getInStock() {
        return inStock;
    }
    public void setInStock(long inStock) {
        this.inStock = inStock;
    }
}

