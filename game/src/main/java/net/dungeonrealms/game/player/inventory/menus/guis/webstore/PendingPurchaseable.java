package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Rar349 on 5/13/2017.
 */
@Getter
@AllArgsConstructor
public class PendingPurchaseable {

    private static final String delimiter = "@#:#&@";

    @NonNull private Purchaseables purchaseables;
    @NonNull private String whoPurchased;
    @NonNull private String whoPurchaseEnjinID;
    @NonNull private String timePurchased;
    @Setter
    @NonNull private int numberPurchased;
    @Getter
    private String transactionId;

    @Override
    public String toString() {
        return purchaseables.name() + delimiter + whoPurchased + delimiter + whoPurchaseEnjinID + delimiter + timePurchased + delimiter + numberPurchased + delimiter + transactionId;
    }

    //Didnt use Json because this class is so small. Json is very hard on the DBMS.
    public static PendingPurchaseable fromString(String serialized) {
        try {
            String[] parts = serialized.split(delimiter);
            Purchaseables item = Purchaseables.valueOf(parts[0]);
            String purchased = parts[1];
            String purchasedEnjinID = parts[2];
            String time = parts[3];
            int numPurchased = Integer.valueOf(parts[4]);
            String transactionId = parts[5];
            return new PendingPurchaseable(item,purchased,purchasedEnjinID,time,numPurchased,transactionId);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
