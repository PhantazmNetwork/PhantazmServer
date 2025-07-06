package org.phantazm.mob2;

import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Default("""
    {
      customName=null,
      customNameVisible=false,
      isInvisible=false,
      isGlowing=false,
      angerTime=-1,
      isBaby=false,
      isSmall=false,
      hasGravity=true,
      hasPhysics=true,
      size=0,
      itemStack=null
    }
    """)
public record MobMeta(@Nullable Component customName,
    boolean customNameVisible,
    boolean isInvisible,
    boolean isGlowing,
    int angerTime,
    boolean isBaby,
    boolean isSmall,
    boolean hasGravity,
    boolean hasPhysics,
    int size,
    ItemStack itemStack) {

}
