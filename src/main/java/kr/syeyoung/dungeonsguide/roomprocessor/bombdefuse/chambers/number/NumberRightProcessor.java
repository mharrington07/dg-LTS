package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.number;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.BlockPos;

public class NumberRightProcessor extends GeneralDefuseChamberProcessor {
    public NumberRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        d1p = chamber.getBlockPos(1,1,4);
        d2p = chamber.getBlockPos(2,1,4);
        d3p = chamber.getBlockPos(6,1,4);
        d4p = chamber.getBlockPos(7,1,4);
        center = chamber.getBlockPos(4,4,4);
    }

    @Override
    public String getName() {
        return "numberRight";
    }


    private int answer = -1, d1, d2, d3 ,d4, a1, a2, a3, a4;
    private BlockPos d1p, d2p, d3p, d4p, center;
    @Override
    public void tick() {
        super.tick();
        a4 = match(getChamber().getEntityAt(EntityArmorStand.class,d1p));
        a3 = match(getChamber().getEntityAt(EntityArmorStand.class,d2p));
        a2 = match(getChamber().getEntityAt(EntityArmorStand.class,d3p));
        a1 = match(getChamber().getEntityAt(EntityArmorStand.class,d4p));
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(a1+"", d1p.getX()+ 0.5f, d1p.getY()+ 0.6f, d1p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(a2+"", d2p.getX()+ 0.5f, d2p.getY()+ 0.6f, d2p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(a3+"", d3p.getX()+ 0.5f, d3p.getY()+ 0.6f, d3p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(a4+"", d4p.getX()+ 0.5f, d4p.getY()+ 0.6f, d4p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(answer == -1 ? "Answer not received yet. Visit left room to obtain solution" : answer + "", center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);

        RenderUtils.drawTextAtWorld(d1+"", d1p.getX()+ 0.5f, d1p.getY()+ 0.2f, d1p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(d2+"", d2p.getX()+ 0.5f, d2p.getY()+ 0.2f, d2p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(d3+"", d3p.getX()+ 0.5f, d3p.getY()+ 0.2f, d3p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(d4+"", d4p.getX()+ 0.5f, d4p.getY()+ 0.2f, d4p.getZ()+ 0.5f, 0xFFFFFFFF, 1.0F, false, false, partialTicks);
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {
        if (answer == -1) return;
        if (event.getEntity() instanceof EntityArmorStand) {
            BlockPos pos = event.getEntity().getPosition();
            if (a1 == d1 && pos.equals(d1p)) event.setCanceled(true);
            if (a2 == d2 && pos.equals(d2p)) event.setCanceled(true);
            if (a3 == d3 && pos.equals(d3p)) event.setCanceled(true);
            if (a4 == d4 && pos.equals(d4p)) event.setCanceled(true);
        }
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if ("numberMatch".equals(compound.getString("type"))) {
            d1 = compound.getInteger("d1");
            d2 = compound.getInteger("d2");
            d3 = compound.getInteger("d3");
            d4 = compound.getInteger("d4");
            answer = d1 * 1000 + d2 * 100 + d3 * 10 + d4;
        }
    }

    private int match(EntityArmorStand armorStand) {
        if (armorStand == null) return -1;
        ItemStack item = armorStand.getInventory()[4];
        NBTTagList list = item.getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 8);
        String str = ((NBTTagString)list.get(0)).getString();
        return integers.containsKey(str) ? -1 : integers.get(str);
    }

    private static final BiMap<String, Integer> integers = HashBiMap.create(10);
    {
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=", 1);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19", 2);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19", 3);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlNzhmYjIyNDI0MjMyZGMyN2I4MWZiY2I0N2ZkMjRjMWFjZjc2MDk4NzUzZjJkOWMyODU5ODI4N2RiNSJ9fX0=", 4);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ1N2UzYmM4OGE2NTczMGUzMWExNGUzZjQxZTAzOGE1ZWNmMDg5MWE2YzI0MzY0M2I4ZTU0NzZhZTIifX19", 5);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM0YjM2ZGU3ZDY3OWI4YmJjNzI1NDk5YWRhZWYyNGRjNTE4ZjVhZTIzZTcxNjk4MWUxZGNjNmIyNzIwYWIifX19", 6);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRiNmViMjVkMWZhYWJlMzBjZjQ0NGRjNjMzYjU4MzI0NzVlMzgwOTZiN2UyNDAyYTNlYzQ3NmRkN2I5In19fQ==", 7);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTkxOTQ5NzNhM2YxN2JkYTk5NzhlZDYyNzMzODM5OTcyMjI3NzRiNDU0Mzg2YzgzMTljMDRmMWY0Zjc0YzJiNSJ9fX0=", 8);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3Y2FmNzU5MWIzOGUxMjVhODAxN2Q1OGNmYzY0MzNiZmFmODRjZDQ5OWQ3OTRmNDFkMTBiZmYyZTViODQwIn19fQ==", 9);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGViZTdlNTIxNTE2OWE2OTlhY2M2Y2VmYTdiNzNmZGIxMDhkYjg3YmI2ZGFlMjg0OWZiZTI0NzE0YjI3In19fQ==", 0);
    }
}