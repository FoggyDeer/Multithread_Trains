package Cars;

import CargoClasses.LiquidBottle;

public interface LiquidMaterialsCarInterface {
    void initiateLeak(LiquidBottle liquidBottle);
    void updateLeakageVolume();
}
