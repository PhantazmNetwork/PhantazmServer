package org.phantazm.zombies.equipment.perk.equipment.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;

@Description("""
        Interactor that delegates to another interactor, unless its cooldown is active (in which case the interaction
        will fail, and the delegate will not be called).
                
        The cooldown may apply to left-clicking, right-clicking, selection, or some combination.
                
        Valid values to include in the `type` array parameter include:
                
        * `LEFT_CLICK`
        * `RIGHT_CLICK`
        * `CLICK`
        * `SELECTION`
        * `ATTACK`
        * `ALL`
                
        `LEFT_CLICK` causes the cooldown to only apply to left-clicking. Right-clicking and selecting will be passed
        through to the delegate. `CLICK` causes the cooldown to apply to left clicking, right clicking, and attacking,
        but not selection. `ATTACK` specifically applies to attacking entities.
                
        If the type array is empty, all types of interaction will bypass the cooldown.
        """)
@Model("zombies.perk.interactor.cooldown")
@Cache(false)
public class CooldownInteractorCreator implements PerkInteractorCreator {
    private final Data data;
    private final PerkInteractorCreator delegate;
    private final int flags;

    @FactoryMethod
    public CooldownInteractorCreator(@NotNull Data data, @NotNull @Child("delegate") PerkInteractorCreator delegate) {
        this.data = Objects.requireNonNull(data, "data");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.flags = Arrays.stream(data.types).mapToInt(d -> d.bits).reduce(0, (l, r) -> l | r);
    }

    @Override
    public @NotNull PerkInteractor forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Interactor(data, delegate.forPlayer(zombiesPlayer), flags);
    }

    private static class Interactor implements PerkInteractor {
        private final Data data;
        private final PerkInteractor delegate;
        private final int flags;

        private long lastActivated;

        private Interactor(@NotNull Data data, @NotNull @Child("delegate") PerkInteractor delegate, int flags) {
            this.data = Objects.requireNonNull(data, "data");
            this.delegate = Objects.requireNonNull(delegate, "delegate");
            this.flags = flags;

            this.lastActivated = -1;
        }

        private boolean activateIfCooldown(DelegateType type, BooleanSupplier supplier) {
            if (type.bitsHave(flags)) {
                long currentTime = System.currentTimeMillis();
                if (lastActivated == -1 || (currentTime - lastActivated) / MinecraftServer.TICK_MS >= data.cooldown) {
                    boolean success = supplier.getAsBoolean();

                    if (success) {
                        lastActivated = currentTime;
                    }
                    return success;
                }

                return false;
            }

            return supplier.getAsBoolean();
        }

        @Override
        public boolean setSelected(boolean selected) {
            return activateIfCooldown(DelegateType.SELECTION, () -> delegate.setSelected(selected));
        }

        @Override
        public boolean leftClick() {
            return activateIfCooldown(DelegateType.LEFT_CLICK, delegate::leftClick);
        }

        @Override
        public boolean rightClick() {
            return activateIfCooldown(DelegateType.RIGHT_CLICK, delegate::rightClick);
        }

        @Override
        public boolean attack(@NotNull Entity target) {
            return activateIfCooldown(DelegateType.ATTACK, () -> delegate.attack(target));
        }
    }

    public enum DelegateType {
        LEFT_CLICK(1),
        RIGHT_CLICK(2),
        CLICK(11),
        SELECTION(4),
        ATTACK(8),
        ALL(15);

        public final int bits;

        DelegateType(int bits) {
            this.bits = bits;
        }

        public boolean bitsHave(int bits) {
            return (bits & this.bits) != 0;
        }
    }

    @DataObject
    public record Data(@Description("The cooldown, in ticks") int cooldown,
                       @NotNull @Description(
                               "What interaction(s) the cooldown will be applied to") DelegateType[] types,
                       @NotNull @ChildPath("delegate") @Description("The interactor to delegate to") String delegate) {
    }
}
