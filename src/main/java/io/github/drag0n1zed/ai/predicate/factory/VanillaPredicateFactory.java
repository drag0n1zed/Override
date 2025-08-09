package io.github.drag0n1zed.ai.predicate.factory;

import io.github.drag0n1zed.api.ai.predicate.IPredicateRegistry;
import io.github.drag0n1zed.api.ai.predicate.PredicateDefinition;
import java.util.Objects;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public final class VanillaPredicateFactory {

    private VanillaPredicateFactory() {}

    public static void registerAll(IPredicateRegistry registry) {

        // Predicate to check for a specific entity type.
        // Params: "entity_type" (String, e.g., "minecraft:zombie")
        registry.register(new PredicateDefinition("override:is_entity_type", data -> {
            try {
                ResourceLocation typeId = ResourceLocation.parse(data.getString("entity_type"));
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(typeId);
                if (type == null) return entity -> false;
                // Explicit lambda to resolve ambiguity with EntityType::is
                return (LivingEntity entity) -> entity.getType() == type;
            } catch (ResourceLocationException e) {
                return entity -> false;
            }
        }));

        // Predicate to check for a specific player username.
        // Params: "name" (String)
        registry.register(new PredicateDefinition("override:is_player_name", data -> {
            String name = data.getString("name");
            if (name.isEmpty()) return entity -> false;
            return entity -> entity instanceof Player && entity.getName().getString().equalsIgnoreCase(name);
        }));

        // Predicate to check for armor on a specific slot.
        // Params: "slot" (String, "head"|"chest"|"legs"|"feet"), "item" (String, e.g., "minecraft:diamond_helmet")
        registry.register(new PredicateDefinition("override:has_armor", data -> {
            final ArmorItem.Type slot;
            try {
                slot = ArmorItem.Type.valueOf(data.getString("slot").toUpperCase());
            } catch (IllegalArgumentException e) {
                return entity -> false;
            }

            try {
                final ResourceLocation itemId = ResourceLocation.parse(data.getString("item"));
                return entity -> {
                    // Use getItemBySlot instead of the old getEquipment
                    ItemStack equipped = entity.getItemBySlot(slot.getSlot());
                    return !equipped.isEmpty() && Objects.equals(BuiltInRegistries.ITEM.getKey(equipped.getItem()), itemId);
                };
            } catch (ResourceLocationException e) {
                return entity -> false;
            }
        }));

        // Predicate that always returns true, useful for "attack anything".
        registry.register(new PredicateDefinition("override:is_living", data -> (entity) -> true));
    }
}