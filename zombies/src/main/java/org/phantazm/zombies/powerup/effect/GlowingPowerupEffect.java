package org.phantazm.zombies.powerup.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Model("zombies.powerup.entity_effect.glow")
@Cache(false)
public class GlowingPowerupEffect implements PowerupEffectComponent {
    //private, to prevent early initialization if something calls Class.forName
    private static class Container {
        private static final Map<String, Team> COLOR_TEAMS;

        static {
            Set<NamedTextColor> colors = NamedTextColor.NAMES.values();

            @SuppressWarnings("unchecked")
            Map.Entry<String, Team>[] entries = new Map.Entry[colors.size()];

            Iterator<NamedTextColor> colorIterator = colors.iterator();
            for (int i = 0; i < colors.size(); i++) {
                NamedTextColor color = colorIterator.next();
                Team team = MinecraftServer.getTeamManager().createBuilder("color-team-" + color).teamColor(color)
                        .collisionRule(TeamsPacket.CollisionRule.NEVER).build();
                entries[i] = Map.entry(color.toString(), team);
            }

            COLOR_TEAMS = Map.ofEntries(entries);
        }
    }

    private final PowerupEffect effect;

    @FactoryMethod
    public GlowingPowerupEffect(@NotNull Data data) {
        this.effect = new Effect(data);
    }
    
    @Override
    public @NotNull PowerupEffect apply(@NotNull ZombiesScene scene) {
        return effect;
    }

    private record Effect(Data data) implements PowerupEffect {
        @Override
        public void apply(@NotNull LivingEntity entity) {
            entity.setTeam(Container.COLOR_TEAMS.get(data.glowColor.toString()));
            entity.setGlowing(true);
        }
    }

    @DataObject
    public record Data(@NotNull NamedTextColor glowColor) {
    }
}
