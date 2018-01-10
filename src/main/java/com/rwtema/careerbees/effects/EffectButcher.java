package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class EffectButcher extends EffectBaseThrottled {
	public static final EffectButcher INSTANCE = new EffectButcher();
	private static final Comparator<EntityAnimal> entityAnimalComparator = Comparator
			.<EntityAnimal>
					comparingInt((a) -> Math.abs(a.getGrowingAge()))
			.reversed()
			.thenComparing(EntityAnimal::isInLove)
			.thenComparingDouble(EntityAnimal::getHealth)
			.thenComparingInt(System::identityHashCode);

	public EffectButcher() {
		super("butcher", 20 * 10 / 10);
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		AxisAlignedBB aabb = getAABB(genome, housing);

		List<EntityAnimal> animals = world.getEntitiesWithinAABB(EntityAnimal.class, aabb, t -> t != null && !t.isChild());
		if (animals.size() < 2) return;

		animals.sort(entityAnimalComparator);


		for (EntityAnimal victim : animals) {
			byte b = 0;
			for (EntityAnimal animal : animals) {
				if (animal != victim && animal.getClass() == victim.getClass()) {
					b++;
					if (b >= 2) {
						victim.attackEntityFrom(DamageSource.CACTUS, 100);
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean handleEntityLiving(EntityLivingBase livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		if(livingBase instanceof EntityAnimal){
			livingBase.attackEntityFrom(DamageSource.CACTUS, 100);
			return true;
		}
		return false;
	}
}
