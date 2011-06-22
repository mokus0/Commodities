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
    
    @NotEmpty
    private String playerName;
    
    @NotNull
    private int commodityId;
    
    @NotNull
    private int numBought;
    
    @NotNull
    private int numSold;
    
    @NotNull
    private double moneySpent;
    
    @NotNull
    private double moneyGained;
}

