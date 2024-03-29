package kr.syeyoung.dungeonsguide.mod.commands;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.NestedCategory;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.MapProcessor;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.DungeonEventHolder;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.*;
import kr.syeyoung.dungeonsguide.mod.whosonline.WhosOnlineManager;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class CommandDgDebug extends CommandBase {

    @Override
    public String getCommandName() {
        return "dgdebug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "dgdebug";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) return;
        String arg = args[0].toLowerCase();


        if ("scoreboard".equals(arg)) {
            ScoreBoardUtils.forEachLine(l -> {
                ChatTransmitter.addToQueue("LINE: " + l, false);
            });
        } else if ("scoreboardclean".equals(arg)) {
            ScoreBoardUtils.forEachLineClean(l -> {
                ChatTransmitter.addToQueue("LINE: " + l, false);
            });
        } else if ("title".equals(arg)) {
            if (args.length == 2) {
                System.out.println("Displayuing title:" + args[1]);
                TitleRender.displayTitle(args[1], "", 10, 40, 20);
            }
        } else if ("mockdungeonstart".equals(arg)) {
            if (!Minecraft.getMinecraft().isSingleplayer()) {
                ChatTransmitter.addToQueue("This only works in singlepauer", false);
                return;
            }

            if (args.length == 2) {
                int time = Integer.parseInt(args[1]);
                ChatTransmitter.addToQueue("§r§aDungeon starts in " + time + " seconds.§r", false);
                return;
            }


            (new Thread(() -> {
                try {
                    ChatTransmitter.addToQueue("§r§aDungeon starts in 15 seconds.§r", false);
                    Thread.sleep(6000);
                    ChatTransmitter.addToQueue("§r§aDungeon starts in 10 seconds.§r", false);
                    Thread.sleep(700);
                    ChatTransmitter.addToQueue("§r§aDungeon starts in 5 seconds.§r", false);
                    Thread.sleep(1000);
                    ChatTransmitter.addToQueue("§r§aDungeon starts in 4 seconds.§r", false);
                    Thread.sleep(1000);
                    ChatTransmitter.addToQueue("§r§aDungeon starts in 3 seconds.§r", false);
                    Thread.sleep(1000);
                    ChatTransmitter.addToQueue("§r§aDungeon starts in 2 seconds.§r", false);
                    Thread.sleep(1000);
                    ChatTransmitter.addToQueue("§r§aDungeon starts in 1 seconds.§r", false);
                } catch (InterruptedException ignored) {
                }
            })).start();
        } else if ("saverooms".equals(arg)) {
            DungeonRoomInfoRegistry.saveAll(Main.getConfigDir());
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved user generated roomdata"));
        } else if ("loadrooms".equals(arg)) {
            try {
                DungeonRoomInfoRegistry.loadAll(Main.getConfigDir());
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully loaded roomdatas"));
                return;
            } catch (BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
                     NoSuchAlgorithmException | IOException | IllegalBlockSizeException |
                     NoSuchPaddingException e) {
                e.printStackTrace();
            }
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cAn error has occurred while loading roomdata"));
        } else if ("reloadah".equals(arg)) {
            try {
                AhUtils.loadAuctions();
            } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException |
                     InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException |
                     KeyStoreException | IllegalBlockSizeException | KeyManagementException e) {
                e.printStackTrace();
            }
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fReloaded Ah data"));
        } else if ("brand".equals(arg)) {
            String serverBrand = Minecraft.getMinecraft().thePlayer.getClientBrand();
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e" + serverBrand));
        } else if ("pathfind".equals(arg)) {
            try {
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                if (thePlayer == null) return;
                if (context.getBossfightProcessor() != null) context.getBossfightProcessor().tick();
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
                grp.pathfind("COMMAND", args[1], args[2], FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.getRouteProperties());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if ("process".equals(arg)) {
            File fileRoot = Main.getConfigDir();
            File dir = new File(fileRoot, "processorinput");
            File outsecret = new File(fileRoot, "processoroutsecret");
            for (File f : dir.listFiles()) {
                if (!f.getName().endsWith(".roomdata")) continue;
                try {
                    InputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                    ois.close();
                    fis.close();
                    dri.setUserMade(false);

                    FileOutputStream fos = new FileOutputStream(new File(outsecret, dri.getUuid().toString() + ".roomdata"));
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(dri);
                    oos.flush();
                    oos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if ("check".equals(arg)) {
            File fileroot = Main.getConfigDir();
            for (File f : fileroot.listFiles()) {
                if (!f.getName().endsWith(".roomdata")) continue;
                try {
                    InputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                    ois.close();
                    fis.close();
                    System.out.println("Starting at " + dri.getName() + " - " + dri.getUuid());
                    for (Map.Entry<String, DungeonMechanic> value2 : dri.getMechanics().entrySet()) {
                        DungeonMechanic value = value2.getValue();
                        if (value instanceof DungeonSecret &&
                                (((DungeonSecret) value).getSecretType() == DungeonSecret.SecretType.BAT
                                        || ((DungeonSecret) value).getSecretType() == DungeonSecret.SecretType.CHEST)
                                && ((DungeonSecret) value).getSecretPoint().getY() == 0) {
                            OffsetPoint offsetPoint = ((DungeonSecret) value).getSecretPoint();
                            if (dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                System.out.println("Fixing " + value2.getKey() + " - as secret " + ((DungeonSecret) value).getSecretType() + " - at " + ((DungeonSecret) value).getSecretPoint());
                            }
                        } else if (value instanceof DungeonOnewayDoor) {
                            for (OffsetPoint offsetPoint : ((DungeonOnewayDoor) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing " + value2.getKey() + " - o-door - at " + offsetPoint);
                                }
                            }
                        } else if (value instanceof DungeonDoor) {
                            for (OffsetPoint offsetPoint : ((DungeonDoor) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing " + value2.getKey() + " - door - at " + offsetPoint);
                                }
                            }
                        } else if (value instanceof DungeonBreakableWall) {
                            for (OffsetPoint offsetPoint : ((DungeonBreakableWall) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing " + value2.getKey() + " - wall - at " + offsetPoint);
                                }
                            }
                        } else if (value instanceof DungeonTomb) {
                            for (OffsetPoint offsetPoint : ((DungeonTomb) value).getSecretPoint().getOffsetPointList()) {
                                if (offsetPoint.getY() == 0 && dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] != -1) {
                                    dri.getBlocks()[offsetPoint.getZ()][offsetPoint.getX()] = -1;
                                    System.out.println("Fixing " + value2.getKey() + " - crypt - at " + offsetPoint);
                                }
                            }
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(f);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(dri);
                    oos.flush();
                    oos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if ("reloaddungeon".equals(arg)) {
            try {
                MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());

                DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
                MapUtils.clearMap();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if ("partyid".equals(arg)) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fInternal Party id: " + Optional.ofNullable(PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyID).orElse(null)));
        } else if ("loc".equals(arg)) {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fYou're in " + DungeonContext.getDungeonName()));
        } else if ("saverun".equals(arg)) {
            try {
                File f = Main.getConfigDir();
                File runDir = new File(f, "dungeonruns");
                runDir.mkdirs();

                File runFile = new File(runDir, UUID.randomUUID() + ".dgrun");

                DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                if (dungeonContext == null) {
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cCouldn't find dungeon to save!"));
                    return;
                }
                DungeonEventHolder dungeonEventHolder = new DungeonEventHolder();
                dungeonEventHolder.setDate(dungeonContext.getInit());
                dungeonEventHolder.setPlayers(dungeonContext.getPlayers());
                dungeonEventHolder.setEventDataList(dungeonContext.getEvents());


                ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(runFile.toPath()));
                oos.writeObject(dungeonEventHolder);
                oos.flush();
                oos.close();
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved dungeon run to " + runFile.getAbsolutePath()));
            } catch (Exception e) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cAn error occured while writing rundata " + e.getMessage()));
                e.printStackTrace();
            }
        } else if ("requeststaticresource".equals(arg)) {
            UUID uid = UUID.fromString(args[1]);
            StaticResourceCache.INSTANCE.getResource(uid).thenAccept(a -> {
                sender.addChatMessage(new ChatComponentText(a.getResourceID() + ": " + a.getValue() + ": " + a.isExists()));
            });
        } else if ("createfakeroom".equals(arg)) {// load schematic
            File f = new File(Main.getConfigDir(), "schematics/new roonm-b2df250c-4af2-4201-963c-0ee1cb6bd3de-5efb1f0c-c05f-4064-bde7-cad0874fdf39.schematic");
            NBTTagCompound compound;
            try {
                compound = CompressedStreamTools.readCompressed(new FileInputStream(f));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            byte[] blocks = compound.getByteArray("Blocks");
            byte[] meta = compound.getByteArray("Data");
            for (int x = 0; x < compound.getShort("Width"); x++) {
                for (int y = 0; y < compound.getShort("Height"); y++) {
                    for (int z = 0; z < compound.getShort("Length"); z++) {
                        int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                        BlockPos pos = new BlockPos(x, y, z);
                        World w = MinecraftServer.getServer().getEntityWorld();
                        w.setBlockState(pos, Block.getBlockById(blocks[index] & 0xFF).getStateFromMeta(meta[index] & 0xFF), 2);
                    }
                }
            }


            DungeonSpecificDataProviderRegistry.doorFinders.put(Pattern.compile("TEST DG"), new DungeonSpecificDataProvider() {
                @Override
                public BlockPos findDoor(World w, String dungeonName) {
                    return new BlockPos(0, 0, 0);
                }

                @Override
                public Vector2d findDoorOffset(World w, String dungeonName) {
                    return null;
                }

                @Override
                public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
                    return null;
                }

                @Override
                public boolean isTrapSpawn(String dungeonName) {
                    return false;
                }

                @Override
                public double secretPercentage(String dungeonName) {
                    return 0;
                }

                @Override
                public int speedSecond(String dungeonName) {
                    return 0;
                }
            });
            DungeonContext.setDungeonName("TEST DG");
            DungeonContext fakeContext = new DungeonContext(Minecraft.getMinecraft().theWorld);
            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(fakeContext);
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(true);
            MapProcessor mapProcessor = fakeContext.getMapProcessor();
            mapProcessor.setUnitRoomDimension(new Dimension(16, 16));
            mapProcessor.setBugged(false);
            mapProcessor.setDoorDimensions(new Dimension(4, 4));
            mapProcessor.setTopLeftMapPoint(new Point(0, 0));
            fakeContext.setDungeonMin(new BlockPos(0, 70, 0));

            DungeonRoom dungeonRoom = new DungeonRoom(Arrays.asList(new Point(0, 0)), ShortUtils.topLeftifyInt((short) 1), (byte) 63, new BlockPos(0, 70, 0), new BlockPos(31, 70, 31), fakeContext, Collections.emptySet());

            fakeContext.getDungeonRoomList().add(dungeonRoom);
            for (Point p : Arrays.asList(new Point(0, 0))) {
                fakeContext.getRoomMapper().put(p, dungeonRoom);
            }

            EditingContext.createEditingContext(dungeonRoom);
            EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
        } else if ("closecontext".equals(arg)) {
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(false);

            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
        } else if ("dumpsettings".equals(arg)) {
            NestedCategory nestedCategory = new NestedCategory("ROOT");
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                String category = abstractFeature.getCategory();
                NestedCategory currentRoot = nestedCategory;
                for (String s : category.split("\\.")) {
                    NestedCategory finalCurrentRoot = currentRoot;
                    if (currentRoot.children().containsKey(s))
                        currentRoot = currentRoot.children().get(s);
                    else {
                        currentRoot.child(currentRoot = new NestedCategory(finalCurrentRoot.categoryFull() + "." + s));
                    }
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();

            Stack<Tuple<NestedCategory, Integer>> stak = new Stack<>();
            stak.push(new Tuple<>(nestedCategory, 0));
            Set<NestedCategory> discovered = new HashSet<>();
            while (!stak.isEmpty()) {
                Tuple<NestedCategory, Integer> n = stak.pop();
                if (discovered.contains(n.getFirst())) continue;
                discovered.add(n.getFirst());
                for (Map.Entry<String, NestedCategory> stringNestedCategoryEntry : n.getFirst().children().entrySet()) {
                    stak.push(new Tuple<>(stringNestedCategoryEntry.getValue(), n.getSecond() + 1));
                }

                if (n.getFirst().categoryFull().equals("ROOT")) continue;

                String prefix = "";
                for (int i = 0; i < n.getSecond() - 1; i++) {
                    prefix += "    ";
                }

                List<AbstractFeature> abstractFeatureList = FeatureRegistry.getFeaturesByCategory().getOrDefault(n.getFirst().categoryFull().substring(5), Collections.emptyList());
                stringBuilder.append(prefix).append("- C ").append(n.getFirst().categoryFull()).append("\n");
                stringBuilder2.append(n.getFirst().categoryFull()).append("\n");
                for (AbstractFeature abstractFeature : abstractFeatureList) {
                    stringBuilder.append(prefix).append("    - F ").append(abstractFeature.getName()).append(" / ").append(abstractFeature.getDescription().replace("\n", "$NEW_LINE$")).append("\n");
                }
            }
            System.out.println(stringBuilder.toString());
            System.out.println(stringBuilder2.toString());
        } else if ("connectwhois".equals(arg)) {
            DungeonsGuide.getDungeonsGuide().getWhosOnlineManager().close();
            DungeonsGuide.getDungeonsGuide().setWhosOnlineManager(new WhosOnlineManager("wss://virginity.kokoniara.software/ws"));
            DungeonsGuide.getDungeonsGuide().getWhosOnlineManager().init();
        } else if ("isonline".equals(arg)) {
            if(args.length > 2){
                sender.addChatMessage(new ChatComponentText("TOO LITTLE ARGS"));
            }

            if(DungeonsGuide.getDungeonsGuide().getWhosOnlineManager() == null){
                sender.addChatMessage(new ChatComponentText("didnt init manager"));
            }


            val tocheck = args[1];

            (new Thread(() -> {
                Future<Boolean> online = DungeonsGuide.getDungeonsGuide().getWhosOnlineManager().getWebsocketClient().isOnline(tocheck);
                if(online != null){
                    boolean aBoolean = false;
                    try {
                        aBoolean = online.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    ChatTransmitter.addToQueue(tocheck + " is " + (aBoolean ? "online" : "offline"));
                }else {
                    ChatTransmitter.addToQueue("NULL");
                }


            })).start();


        } else if ("readmap".equals(arg)) {
            try {
                int fromX = Integer.parseInt(args[1]);
                int fromY = Integer.parseInt(args[2]);
                sender.addChatMessage(new ChatComponentText(MapUtils.readDigit(MapUtils.getColors(), fromX, fromY) + "-"));
//                int cntY = Integer.parseInt(args[3]);
//                int target = Integer.parseInt(args[4]);
//                StringBuilder sb = new StringBuilder("{");
//                for (int y = fromY; y < fromY + cntY; y++) {
//                    int curr = 0;
//                    for (int x = fromX; x < fromX+8; x++) {
//                        byte clr = MapUtils.getMapColorAt(MapUtils.getColors(), x,y);
//                        if (clr == target) curr = (curr << 1) | 1;
//                        else curr <<= 1;
//                    }
//                    sb.append("0x").append(Integer.toHexString(curr).toUpperCase());
//                    if (y != fromY + cntY - 1) sb.append(", ");
//                }
//                sb.append("}");
//                System.out.println("\n"+sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("listcosmetics".equals(arg)){
            DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticMap().forEach((uuid, activeCosmetic) ->{
                System.out.println("OWNER UUID: " + uuid);
                System.out.println("   " + activeCosmetic);
            });
        } else if ("mockcosmetics".equals(arg)){
            toOpen = new GuiScreen() {


                private GuiTextField activityUID;
                private GuiTextField playerUID;
                private GuiTextField cosmeticUID;
                private GuiTextField username;

                @Override
                public void initGui() {
                    System.out.println("Init");
                    this.buttonList.add(
                            new GuiButton(
                                    106,
                                    this.width / 2 - 155,
                                    this.height / 6 + 72 - 6,
                                    this.fontRendererObj.getStringWidth("add") + 5,
                                    this.fontRendererObj.FONT_HEIGHT + 2,
                                    "add"));

                    this.buttonList.add(
                            new GuiButton(
                                    105,
                                    this.width / 2 - 155,
                                    this.height / 6 + 80 - 6,
                                    this.fontRendererObj.getStringWidth("an p") + 5,
                                    this.fontRendererObj.FONT_HEIGHT + 2,
                                    "an p"));

                    this.activityUID = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 68, this.height / 2 - 10, 137, 20);
                    activityUID.setMaxStringLength(500);
                    activityUID.setCanLoseFocus(true);
                    activityUID.setText("activityUID");
                    activityUID.setEnabled(false);

                    this.playerUID = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 68, this.height / 2 - 30, 137, 20);
                    playerUID.setMaxStringLength(500);
                    playerUID.setCanLoseFocus(true);
                    playerUID.setText("playerUID");
                    playerUID.setEnabled(false);


                    this.cosmeticUID = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 68, this.height / 2 - 50, 137, 20);
                    cosmeticUID.setMaxStringLength(500);
                    cosmeticUID.setCanLoseFocus(true);
                    cosmeticUID.setText("cosmeticUID");
                    cosmeticUID.setEnabled(false);

                    this.username = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 68, this.height / 2 - 70, 137, 20);
                    username.setMaxStringLength(500);
                    username.setCanLoseFocus(true);
                    username.setText("username");
                    username.setEnabled(false);



                    super.initGui();
                }

                @Override
                protected void actionPerformed(GuiButton button) throws IOException {
                    if(button.id == 105){
                        activityUID.setText("98e445dc-650f-49e1-ba6f-b12086533cf7");

                        cosmeticUID.setText("dd99fea2-1f18-49ee-92ae-6e00a7a5b75f");
                    }
                    if (button.id == 106) {
                        DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticMap().entrySet()
                                .removeIf(uuid -> uuid.getKey().equals(UUID.fromString(playerUID.getText())));


                        ActiveCosmetic cosmeticData = new ActiveCosmetic();
                        cosmeticData.setActivityUID(UUID.fromString(activityUID.getText()));
                        cosmeticData.setPlayerUID(UUID.fromString(playerUID.getText()));
                        cosmeticData.setCosmeticData(UUID.fromString(cosmeticUID.getText()));
                        cosmeticData.setUsername(username.getText());

                        DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticMap().put(cosmeticData.getActivityUID(), cosmeticData);

                        try {
                            Method method = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getClass().getDeclaredMethod("rebuildCaches");
                            method.setAccessible(true);
                            method.invoke(DungeonsGuide.getDungeonsGuide().getCosmeticsManager());
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    super.actionPerformed(button);
                }


                @Override
                protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

                    this.cosmeticUID.mouseClicked(mouseX, mouseY, mouseButton);
                    this.playerUID.mouseClicked(mouseX, mouseY, mouseButton);
                    this.activityUID.mouseClicked(mouseX, mouseY, mouseButton);
                    this.username.mouseClicked(mouseX, mouseY, mouseButton);

                    this.cosmeticUID.setEnabled(cosmeticUID.isFocused());
                    this.playerUID.setEnabled(playerUID.isFocused());
                    this.activityUID.setEnabled(activityUID.isFocused());
                    this.username.setEnabled(username.isFocused());


                    super.mouseClicked(mouseX, mouseY, mouseButton);
                }

                @Override
                protected void keyTyped(char typedChar, int keyCode) throws IOException {

                    this.cosmeticUID.textboxKeyTyped(typedChar, keyCode);
                    this.playerUID.textboxKeyTyped(typedChar, keyCode);
                    this.activityUID.textboxKeyTyped(typedChar, keyCode);
                    this.username.textboxKeyTyped(typedChar, keyCode);

                    this.cosmeticUID.setEnabled(cosmeticUID.isFocused());
                    this.playerUID.setEnabled(playerUID.isFocused());
                    this.activityUID.setEnabled(activityUID.isFocused());
                    this.username.setEnabled(username.isFocused());


                    super.keyTyped(typedChar, keyCode);
                }

                @Override
                public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                    this.drawDefaultBackground();

                    this.cosmeticUID.drawTextBox();
                    this.playerUID.drawTextBox();
                    this.activityUID.drawTextBox();
                    this.username.drawTextBox();




                    super.drawScreen(mouseX, mouseY, partialTicks);
                }

            };





        } else {
            sender.addChatMessage(new ChatComponentText("ain't gonna find much anything here"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg loadrooms §7-§f Reloads dungeon roomdata."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg brand §7-§f View server brand."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg info §7-§f View Current DG User info."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverun §7-§f Save run to be sent to developer."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverooms §7-§f Saves usergenerated dungeon roomdata."));
        }
    }

    GuiScreen toOpen;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        try {
            if (toOpen != null && e.phase == TickEvent.Phase.START) {
                Minecraft.getMinecraft().displayGuiScreen(toOpen);
                toOpen = null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
