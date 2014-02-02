
package entity;

import java.util.List;

import javax.persistence.Id;
import play.modules.mongodb.jackson.KeyTyped;


public class Fish implements KeyTyped {
    @Id
    private String fishId; 
    private String name;
    private String latin;
    private List sizeRangeMillimeter;
    private String image;
 
    public String getFishId() {
        return fishId;
    }
    public void setFishId(String fishId) {
        this.fishId = fishId;
    }
}