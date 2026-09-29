package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.impl.network.PacketEvent;
import me.alpha432.oyvey.event.system.Subscribe;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class VelocityModule extends Module {
    private final Setting<Boolean> knockback  = register(new Setting<>("Knockback", true));
    private final Setting<Boolean> explosions = register(new Setting<>("Explosions", true));
    private final Setting<Integer> horizontal = register(new Setting<>("Horizontal", 0, 0, 100));
    private final Setting<Integer> vertical   = register(new Setting<>("Vertical", 0, 0, 100));

    public VelocityModule() {
        super("Velocity", "Reduces or removes knockback", Category.PLAYER);
    }

    @Subscribe
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.level == null) return;

        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket packet) {
            // Only our own knockback, never other entities
            if (!knockback.getValue() || packet.getId() != mc.player.getId()) return;

            event.cancel();

            // 0/0 = no knockback; otherwise apply a scaled version ourselves
            if (horizontal.getValue() > 0 || vertical.getValue() > 0) {
                double h = horizontal.getValue() / 100.0;
                double v = vertical.getValue() / 100.0;
                mc.execute(() -> mc.player.setDeltaMovement(
                        new Vec3(packet.getXa() * h, packet.getYa() * v, packet.getZa() * h)));
            }
        } else if (event.getPacket() instanceof ClientboundExplodePacket) {
            if (explosions.getValue()) event.cancel();
        }
    }
}
